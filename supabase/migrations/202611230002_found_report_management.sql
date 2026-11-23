begin;

alter table public.found_items add column archived boolean not null default false;

alter policy found_read_members on public.found_items
using (not archived or reporter_id = (select auth.uid()));

-- Pelapor tetap dapat membaca riwayat klaim dari laporan yang diarsipkan.
create or replace function public.get_found_reporter(p_found_item_id uuid)
returns table(display_name text, campus text, contact_email text, phone text)
language sql stable security definer set search_path = '' as $$
    select p.display_name, p.campus,
           case when p.share_contact or p.id = auth.uid() then nullif(p.contact_email, '') end,
           case when p.share_contact or p.id = auth.uid() then nullif(p.phone, '') end
    from public.profiles p
    join public.found_items f on f.reporter_id = p.id
    where f.id = p_found_item_id and auth.uid() is not null
      and (not f.archived or f.reporter_id = auth.uid());
$$;

create index found_active_claims_idx on public.claims (found_item_id)
where status in ('pending', 'approved');

-- UPDATE/DELETE memegang kunci baris yang juga dipakai validate_new_claim.
create function public.guard_found_management()
returns trigger language plpgsql security definer set search_path = '' as $$
declare
    active_claim boolean;
    approved_claim boolean;
begin
    if auth.uid() is null then
        if tg_op = 'DELETE' then return old; end if;
        return new;
    end if;

    if tg_op = 'INSERT' then
        if new.status <> 'open' or new.archived then
            raise exception 'Laporan baru harus berstatus aktif.' using errcode = '23514';
        end if;
        return new;
    end if;

    if old.reporter_id is distinct from auth.uid() then
        raise exception 'Hanya pelapor yang dapat mengelola laporan.' using errcode = '42501';
    end if;

    if old.status <> 'open' or old.archived then
        raise exception 'Laporan ini sudah selesai atau diarsipkan.' using errcode = '23514';
    end if;

    select exists (select 1 from public.claims where found_item_id = old.id and status in ('pending', 'approved')),
           exists (select 1 from public.claims where found_item_id = old.id and status = 'approved')
      into active_claim, approved_claim;

    if tg_op = 'UPDATE' and new.status = 'returned' then
        if not approved_claim or (to_jsonb(new) - 'status') <> (to_jsonb(old) - 'status') then
            raise exception 'Konfirmasi pengembalian memerlukan klaim yang disetujui dan hanya boleh mengubah status.' using errcode = '23514';
        end if;
        return new;
    end if;

    if active_claim then
        raise exception 'Laporan terkunci karena terdapat klaim aktif.' using errcode = '23514';
    end if;

    if tg_op = 'DELETE' then return old; end if;

    if new.archived and (new.status <> 'closed'
        or (to_jsonb(new) - 'archived' - 'status') <> (to_jsonb(old) - 'archived' - 'status')) then
        raise exception 'Arsipkan laporan tanpa mengubah informasi barang.' using errcode = '23514';
    end if;
    return new;
end;
$$;

revoke all on function public.guard_found_management() from public, anon, authenticated;
-- Urutan nama memastikan pemeriksaan berjalan sebelum trigger yang mengubah updated_at.
create trigger temubarang_manage_found
before insert or update or delete on public.found_items
for each row execute function public.guard_found_management();

create or replace function public.validate_found_write() returns trigger language plpgsql set search_path = '' as $$
begin
    if tg_op = 'UPDATE' and (new.id <> old.id or new.reporter_id <> old.reporter_id or new.created_at <> old.created_at) then
        raise exception 'Identitas laporan tidak dapat diubah' using errcode = '23514';
    end if;
    if tg_op = 'UPDATE' and (new.archived or new.status = 'returned')
        and (to_jsonb(new) - 'archived' - 'status') = (to_jsonb(old) - 'archived' - 'status') then
        new.updated_at := now();
        return new;
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

create unique index found_single_approved_claim_idx on public.claims (found_item_id)
where status = 'approved';

create or replace function public.validate_new_claim() returns trigger language plpgsql security definer set search_path = '' as $$
declare f public.found_items;
begin
    select * into f from public.found_items where id = new.found_item_id for update;
    if not found or f.status <> 'open' or f.reporter_id = new.claimant_id or new.status <> 'pending' or new.decided_by is not null or new.decided_at is not null then
        raise exception 'Klaim tidak valid atau barang tidak tersedia' using errcode = '23514';
    end if;
    if exists (select 1 from public.claims where found_item_id = f.id and status = 'approved') then
        raise exception 'Klaim untuk barang ini sudah disetujui.' using errcode = '23514';
    end if;
    return new;
end;
$$;

create or replace function public.review_found_claim(p_claim_id uuid, p_approve boolean)
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
        update public.claims set status = 'rejected', decided_by = auth.uid(), decided_at = now()
            where found_item_id = f.id and status = 'pending';
    end if;
end;
$$;
revoke all on function public.review_found_claim(uuid, boolean) from public, anon;
grant execute on function public.review_found_claim(uuid, boolean) to authenticated;

create policy temubarang_keep_found_report_photo
on storage.objects as restrictive for delete to authenticated
using (
    bucket_id <> 'temubarang-images'
    or not exists (
        select 1 from public.found_items
        where image_path = storage.objects.name and reporter_id = (select auth.uid())
    )
);

commit;
