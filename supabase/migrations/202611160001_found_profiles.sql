-- Apply once to the existing schema. All operations roll back if a prerequisite fails.
begin;
create table public.profiles (
    id uuid primary key references auth.users(id) on delete cascade,
    display_name text not null check (char_length(btrim(display_name)) between 2 and 80),
    campus text not null default '' check (char_length(btrim(campus)) <= 120),
    contact_email text not null default '' check (contact_email = '' or
        (char_length(contact_email) <= 254 and contact_email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$')),
    phone text not null default '' check (phone = '' or phone ~ '^\+?[0-9]{8,15}$'),
    share_contact boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    check (not share_contact or contact_email <> '' or phone <> '')
);
-- Do not copy login email/phone to a publicly shared contact.
insert into public.profiles(id, display_name)
select id, case when char_length(btrim(coalesce(raw_user_meta_data->>'full_name', ''))) >= 2
    then left(btrim(raw_user_meta_data->>'full_name'), 80) else 'Anggota kampus' end from auth.users;
create function public.create_member_profile() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    insert into public.profiles(id, display_name)
    values (new.id, case when char_length(btrim(coalesce(new.raw_user_meta_data->>'full_name', ''))) >= 2
        then left(btrim(new.raw_user_meta_data->>'full_name'), 80) else 'Anggota kampus' end);
    return new;
end;
$$;
revoke all on function public.create_member_profile() from public;
create trigger temubarang_create_profile after insert on auth.users
for each row execute function public.create_member_profile();

alter table public.found_items
    add column campus text not null default '',
    add column category text not null default 'other',
    add column custody_type text not null default 'reporter',
    add column pickup_location text not null default '',
    add column pickup_notes text not null default '',
    add column updated_at timestamptz not null default now(),
    add constraint found_campus_length check (char_length(btrim(campus)) <= 120),
    add constraint found_category_check check (category in ('electronics','documents','keys','bags','clothing','other')),
    add constraint found_custody_check check (custody_type in ('reporter','security','lost_and_found')),
    add constraint found_pickup_length check (char_length(btrim(pickup_location)) <= 200),
    add constraint found_notes_length check (char_length(btrim(pickup_notes)) <= 1000);
-- Legacy rows remain readable. Do not assume the found location is the pickup location.
create function public.validate_found_write() returns trigger language plpgsql set search_path = '' as $$
begin
    if tg_op = 'UPDATE' and (new.id <> old.id or new.reporter_id <> old.reporter_id or new.created_at <> old.created_at) then
        raise exception 'Identitas laporan tidak dapat diubah' using errcode = '23514';
    end if;
    if char_length(btrim(new.campus)) < 2 or char_length(btrim(new.pickup_location)) < 3
        or char_length(btrim(new.found_location)) not between 1 and 200
        or new.found_at > (now() at time zone 'Asia/Jakarta')::date then
        raise exception 'Lengkapi kampus, lokasi, dan tanggal ditemukan yang valid' using errcode = '23514';
    end if;
    new.updated_at := now();
    return new;
end;
$$;
create trigger temubarang_validate_found before insert or update on public.found_items
for each row execute function public.validate_found_write();
create function public.touch_profile() returns trigger language plpgsql set search_path = '' as $$
begin
    if new.id <> old.id or new.created_at <> old.created_at then
        raise exception 'Identitas profil tidak dapat diubah' using errcode = '23514';
    end if;
    new.updated_at := now(); return new;
end;
$$;
create trigger temubarang_touch_profile before update on public.profiles
for each row execute function public.touch_profile();

create index found_items_reporter_created_idx on public.found_items(reporter_id, created_at desc);
create index found_items_status_created_idx on public.found_items(status, created_at desc);
create index lost_items_reporter_idx on public.lost_items(reporter_id);
alter table public.claims
    add column decided_by uuid references auth.users(id),
    add column decided_at timestamptz;
create index claims_claimant_idx on public.claims(claimant_id);
-- Existing duplicates abort migration. Review them manually; never discard claims.
create unique index claims_one_per_claimant_idx on public.claims(found_item_id, claimant_id);
create unique index claims_one_approved_idx on public.claims(found_item_id) where status = 'approved';

alter table public.profiles enable row level security;
alter table public.found_items enable row level security;
alter table public.claims enable row level security;
alter table public.lost_items enable row level security;
-- Permissive policies combine with OR. Intentionally replace policies on these four
-- tables; review with the other module owners before deployment.
do $$
declare p record;
begin
    for p in select schemaname, tablename, policyname from pg_policies
        where schemaname = 'public' and tablename in ('profiles','found_items','claims','lost_items')
    loop execute format('drop policy %I on %I.%I', p.policyname, p.schemaname, p.tablename); end loop;
end;
$$;
revoke all on public.profiles, public.found_items, public.claims, public.lost_items from anon;
-- PUBLIC must not retain table grants either.
revoke all on public.profiles, public.found_items, public.claims, public.lost_items from public;
grant select, insert, update on public.profiles to authenticated;
grant select, insert, update, delete on public.found_items, public.lost_items to authenticated;
grant select, insert on public.claims to authenticated;
revoke update, delete on public.claims from authenticated;
create policy profiles_read_self on public.profiles for select to authenticated using (id = (select auth.uid()));
create policy profiles_insert_self on public.profiles for insert to authenticated with check (id = (select auth.uid()));
create policy profiles_update_self on public.profiles for update to authenticated using (id = (select auth.uid())) with check (id = (select auth.uid()));
create policy found_read_members on public.found_items for select to authenticated using (true);
create policy found_insert_self on public.found_items for insert to authenticated with check (reporter_id = (select auth.uid()));
create policy found_update_self on public.found_items for update to authenticated using (reporter_id = (select auth.uid())) with check (reporter_id = (select auth.uid()));
create policy found_delete_self on public.found_items for delete to authenticated using (reporter_id = (select auth.uid()));
create policy lost_read_members on public.lost_items for select to authenticated using (true);
create policy lost_insert_self on public.lost_items for insert to authenticated with check (reporter_id = (select auth.uid()));
create policy lost_update_self on public.lost_items for update to authenticated using (reporter_id = (select auth.uid())) with check (reporter_id = (select auth.uid()));
create policy lost_delete_self on public.lost_items for delete to authenticated using (reporter_id = (select auth.uid()));
create policy claims_read_participants on public.claims for select to authenticated using (
    claimant_id = (select auth.uid()) or exists (
        select 1 from public.found_items f where f.id = found_item_id and f.reporter_id = (select auth.uid())
    )
);
create policy claims_insert_self on public.claims for insert to authenticated with check (
    claimant_id = (select auth.uid()) and status = 'pending' and exists (
        select 1 from public.found_items f where f.id = found_item_id
            and f.reporter_id <> (select auth.uid()) and f.status = 'open'
    )
);
-- Keep the supplied FK: a report with claims cannot be deleted, only closed.
create function public.validate_new_claim() returns trigger language plpgsql security definer set search_path = '' as $$
declare f public.found_items;
begin
    select * into f from public.found_items where id = new.found_item_id for update;
    if not found or f.status <> 'open' or f.reporter_id = new.claimant_id or new.status <> 'pending' or new.decided_by is not null or new.decided_at is not null then
        raise exception 'Klaim tidak valid atau barang tidak tersedia' using errcode = '23514';
    end if;
    return new;
end;
$$;
revoke all on function public.validate_new_claim() from public;
create trigger temubarang_validate_claim before insert on public.claims
for each row execute function public.validate_new_claim();

-- Contact masking happens in PostgreSQL, not just in the UI.
create function public.get_found_reporter(p_found_item_id uuid)
returns table(display_name text, campus text, contact_email text, phone text)
language sql stable security definer set search_path = '' as $$
    select p.display_name, p.campus,
        case when p.share_contact or p.id = auth.uid() then nullif(p.contact_email, '') end,
        case when p.share_contact or p.id = auth.uid() then nullif(p.phone, '') end
    from public.profiles p join public.found_items f on f.reporter_id = p.id
    where f.id = p_found_item_id and auth.uid() is not null;
$$;
revoke all on function public.get_found_reporter(uuid) from public, anon;
grant execute on function public.get_found_reporter(uuid) to authenticated;

-- Prepared API for the claims module: only reporter can decide, atomically.
create function public.review_found_claim(p_claim_id uuid, p_approve boolean)
returns void language plpgsql security definer set search_path = '' as $$
declare c public.claims; f public.found_items; parent_id uuid;
begin
    if auth.uid() is null then raise exception 'Login diperlukan' using errcode = '42501'; end if;
    if p_approve is null then raise exception 'Keputusan diperlukan' using errcode = '23514'; end if;
    select found_item_id into parent_id from public.claims where id = p_claim_id;
    select * into f from public.found_items where id = parent_id for update;
    if not found or f.reporter_id <> auth.uid() then
        raise exception 'Hanya pelapor dapat memutuskan klaim' using errcode = '42501';
    end if;
    select * into c from public.claims where id = p_claim_id for update;
    if c.status <> 'pending' or f.status <> 'open' then
        raise exception 'Klaim atau laporan sudah diproses' using errcode = '23514';
    end if;
    update public.claims set status = case when p_approve then 'approved' else 'rejected' end,
        decided_by = auth.uid(), decided_at = now() where id = c.id;
    if p_approve then
        update public.found_items set status = 'returned' where id = f.id;
        update public.claims set status = 'rejected', decided_by = auth.uid(), decided_at = now()
            where found_item_id = f.id and status = 'pending';
    end if;
end;
$$;
revoke all on function public.review_found_claim(uuid, boolean) from public, anon;
grant execute on function public.review_found_claim(uuid, boolean) to authenticated;
commit;
