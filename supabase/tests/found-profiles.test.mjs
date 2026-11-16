import { PGlite } from '@electric-sql/pglite';
import { readFile } from 'node:fs/promises';
import assert from 'node:assert/strict';
const db = new PGlite();
const read = path => readFile(new URL(path, import.meta.url), 'utf8');
const owner = '00000000-0000-0000-0000-000000000001';
const member = '00000000-0000-0000-0000-000000000002';
const stranger = '00000000-0000-0000-0000-000000000003';
const item = '10000000-0000-0000-0000-000000000001';
const claim = '20000000-0000-0000-0000-000000000001';
let passed = 0;
async function test(name, body) { await body(); passed++; console.log(`PASS ${name}`); }
async function login(user, role = 'authenticated') {
  await db.exec(`reset role; set role ${role};`);
  await db.query("select set_config('request.jwt.claim.sub', $1, false)", [user]);
}
async function rejects(sql, code) { await assert.rejects(db.exec(sql), e => e.code === code); }
try {
  await db.exec(await read('./base-schema.sql'));
  await db.exec(`insert into auth.users(id, raw_user_meta_data) values ('${owner}', '{"full_name":"Rafa"}'), ('${member}', '{}');
    insert into public.found_items(id,reporter_id,title,description,found_location,found_at)
    values ('${item}','${owner}','Kunci motor','Kunci motor merah','Kantin','2026-01-01');`);
  await db.exec(await read('../migrations/202611160001_found_profiles.sql'));
  await db.exec(await read('../migrations/202611160002_found_storage.sql'));
  await test('migration preserves legacy report and backfills profiles', async () => {
    assert.equal((await db.query('select count(*)::int as n from public.found_items')).rows[0].n, 1);
    assert.equal((await db.query('select count(*)::int as n from public.profiles')).rows[0].n, 2);
    assert.equal((await db.query(`select pickup_location from public.found_items`)).rows[0].pickup_location, '');
  });
  await test('new signup creates a private profile', async () => {
    await db.exec(`insert into auth.users(id) values ('${stranger}')`);
    const p = (await db.query(`select * from public.profiles where id='${stranger}'`)).rows[0];
    assert.equal(p.display_name, 'Anggota kampus'); assert.equal(p.share_contact, false);
  });
  await login(owner);
  await db.exec(`update public.profiles set campus='Kampus A', contact_email='rafa@example.com', phone='+628123456789' where id='${owner}';
    update public.found_items set campus='Kampus A', pickup_location='Pos keamanan A', custody_type='security' where id='${item}';`);
  await test('profile owner can read their unshared contact', async () => {
    assert.equal((await db.query(`select * from public.get_found_reporter('${item}')`)).rows[0].contact_email, 'rafa@example.com');
  });
  await test('invalid and future found reports are rejected', async () => {
    await rejects(`insert into public.found_items(reporter_id,title,description,found_location,found_at) values ('${owner}','Kunci','Kunci motor merah','Kantin',current_date)`, '23514');
    await rejects(`update public.found_items set found_at=current_date+2 where id='${item}'`, '23514');
  });
  await test('report identity cannot be reassigned', async () => {
    await rejects(`update public.found_items set reporter_id='${member}' where id='${item}'`, '23514');
  });
  await test('reporter cannot claim their own item', async () => {
    await rejects(`insert into public.claims(found_item_id,claimant_id,proof_description) values ('${item}','${owner}','Bukti yang lengkap')`, '23514');
  });
  await login(member);
  await test('other members cannot read private profiles or contacts', async () => {
    assert.equal((await db.query(`select * from public.profiles where id='${owner}'`)).rows.length, 0);
    const p = (await db.query(`select * from public.get_found_reporter('${item}')`)).rows[0];
    assert.equal(p.display_name, 'Rafa'); assert.equal(p.contact_email, null); assert.equal(p.phone, null);
  });
  await test('nonowners cannot edit/delete report or profile', async () => {
    assert.equal((await db.query(`update public.found_items set title='Dicuri' where id='${item}' returning id`)).rows.length, 0);
    assert.equal((await db.query(`delete from public.found_items where id='${item}' returning id`)).rows.length, 0);
    assert.equal((await db.query(`update public.profiles set share_contact=true where id='${owner}' returning id`)).rows.length, 0);
  });
  await test('identity spoofing is rejected by RLS', async () => {
    await rejects(`insert into public.found_items(reporter_id,title,description,found_location,found_at,campus,pickup_location) values ('${owner}','Kunci','Kunci motor merah','Kantin',current_date,'Kampus A','Pos keamanan')`, '42501');
  });
  await test('member can claim once, cannot self-approve', async () => {
    await db.exec(`insert into public.claims(id,found_item_id,claimant_id,proof_description) values ('${claim}','${item}','${member}','Bukti yang lengkap')`);
    await rejects(`insert into public.claims(found_item_id,claimant_id,proof_description) values ('${item}','${member}','Bukti yang lengkap')`, '23505');
    await rejects(`insert into public.claims(found_item_id,claimant_id,proof_description,decided_by) values ('${item}','${member}','Bukti yang lengkap','${owner}')`, '23514');
    await rejects(`update public.claims set status='approved' where id='${claim}'`, '42501');
    await rejects(`select public.review_found_claim('${claim}',true)`, '42501');
  });
  await login(stranger);
  await test('claim evidence is visible only to participants', async () => {
    assert.equal((await db.query(`select * from public.claims`)).rows.length, 0);
    await db.exec(`insert into public.claims(found_item_id,claimant_id,proof_description) values ('${item}','${stranger}','Bukti klaim lainnya')`);
  });
  await login(owner);
  await test('reporter sees claims, but cannot delete a report with claims', async () => {
    assert.equal((await db.query(`select * from public.claims`)).rows.length, 2);
    await rejects(`delete from public.found_items where id='${item}'`, '23503');
  });
  await test('sharing contact is opt-in and revocable', async () => {
    await db.exec(`update public.profiles set share_contact=true where id='${owner}'`);
    await login(member);
    assert.equal((await db.query(`select * from public.get_found_reporter('${item}')`)).rows[0].phone, '+628123456789');
    await login(owner);
    await db.exec(`update public.profiles set share_contact=false where id='${owner}'`);
    await login(member);
    assert.equal((await db.query(`select * from public.get_found_reporter('${item}')`)).rows[0].phone, null);
    await login(owner);
  });
  await test('approval atomically returns item and rejects competing claims', async () => {
    await db.exec(`select public.review_found_claim('${claim}',true)`);
    const claims = (await db.query(`select status from public.claims order by id`)).rows.map(r => r.status);
    assert.equal(claims.filter(s => s === 'approved').length, 1);
    assert.equal(claims.filter(s => s === 'rejected').length, 1);
    assert.equal((await db.query(`select status from public.found_items where id='${item}'`)).rows[0].status, 'returned');
    assert.equal((await db.query(`select decided_by from public.claims where id='${claim}'`)).rows[0].decided_by, owner);
    assert.ok((await db.query(`select decided_at from public.claims where id='${claim}'`)).rows[0].decided_at);
    await rejects(`select public.review_found_claim('${claim}',true)`, '23514');
  });
  await login('', 'anon');
  await test('anonymous access to reports, profiles and contact RPC is denied', async () => {
    await rejects('select * from public.found_items', '42501');
    await rejects('select * from public.profiles', '42501');
    await rejects(`select * from public.get_found_reporter('${item}')`, '42501');
  });
  await login(owner);
  await test('Storage enforces owner folder and preserves sibling module access', async () => {
    await db.exec(`insert into storage.objects(bucket_id,name) values ('temubarang-images','${owner}/found/photo.jpg')`);
    await rejects(`insert into storage.objects(bucket_id,name) values ('temubarang-images','${member}/found/spoof.jpg')`, '42501');
    await db.exec(`insert into storage.objects(bucket_id,name) values ('temubarang-images','${owner}/lost/photo.jpg')`);
    assert.equal((await db.query(`select count(*)::int as n from storage.objects where name='${owner}/lost/photo.jpg'`)).rows[0].n, 1);
    await db.exec(`delete from storage.objects where name='${owner}/lost/photo.jpg'`);
  });
  await login(member);
  await test('Storage guards block cross-user deletion despite broad legacy policy', async () => {
    assert.equal((await db.query(`delete from storage.objects where name='${owner}/found/photo.jpg' returning id`)).rows.length, 0);
    assert.equal((await db.query(`update storage.objects set name='${member}/found/stolen.jpg' where name='${owner}/found/photo.jpg' returning id`)).rows.length, 0);
  });
  await login('', 'anon');
  await test('anonymous photo mutation is denied despite broad legacy policy', async () => {
    await rejects(`insert into storage.objects(bucket_id,name) values ('temubarang-images','${owner}/found/anon.jpg')`, '42501');
    assert.equal((await db.query(`delete from storage.objects returning id`)).rows.length, 0);
  });
  await login(owner);
  await test('owner can clean up their own uploaded photo', async () => {
    assert.equal((await db.query(`delete from storage.objects where name='${owner}/found/photo.jpg' returning id`)).rows.length, 1);
  });
  console.log(`${passed} database checks passed`);
} finally { await db.close(); }
