begin;

alter table public.profiles
    add column affiliation_type text,
    add column faculty_or_unit text,
    add column entry_year integer,
    add constraint profiles_affiliation_check check (
        (affiliation_type is null and faculty_or_unit is null and entry_year is null)
        or (affiliation_type is not null and affiliation_type in ('student','lecturer','staff','campus_officer')
            and faculty_or_unit is not null and char_length(btrim(faculty_or_unit)) between 2 and 120
            and ((affiliation_type = 'student' and entry_year is not null
                  and entry_year between 1900 and extract(year from current_date)::integer)
                 or (affiliation_type <> 'student' and entry_year is null)))
    );

create or replace function public.create_member_profile() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    insert into public.profiles(id, display_name, contact_email)
    values (new.id,
        case when char_length(btrim(coalesce(new.raw_user_meta_data->>'full_name',''))) >= 2
             then left(btrim(new.raw_user_meta_data->>'full_name'),80) else 'Anggota kampus' end,
        case when char_length(new.email) <= 254 and new.email ~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
             then new.email else '' end);
    return new;
end;
$$;

create table public.recovery_requests (
    id uuid primary key,
    lost_item_id uuid not null references public.lost_items(id) on delete restrict,
    finder_id uuid not null references auth.users(id) on delete restrict,
    reporter_id uuid not null references auth.users(id) on delete restrict,
    item_title text not null,
    found_location text not null check (char_length(btrim(found_location)) between 2 and 200),
    found_at timestamptz not null,
    item_condition text not null check (char_length(btrim(item_condition)) between 2 and 1000),
    custodian text not null check (char_length(btrim(custodian)) between 2 and 120),
    pickup_location text not null check (char_length(btrim(pickup_location)) between 2 and 200),
    notes text not null default '' check (char_length(notes) <= 1000),
    evidence_path text,
    status text not null default 'pending' check (status in ('pending','confirmed','rejected','completed')),
    created_at timestamptz not null default now(),
    decided_at timestamptz,
    completed_at timestamptz
);
create unique index recovery_active_finder_idx on public.recovery_requests (lost_item_id,finder_id)
where status in ('pending','confirmed');
create unique index recovery_confirmed_idx on public.recovery_requests (lost_item_id) where status = 'confirmed';
create index recovery_reporter_idx on public.recovery_requests (reporter_id);
create index recovery_finder_idx on public.recovery_requests (finder_id);

alter table public.recovery_requests enable row level security;
revoke all on public.recovery_requests from public, anon, authenticated;
grant select on public.recovery_requests to authenticated;
create policy recovery_participant_read on public.recovery_requests for select to authenticated
using ((select auth.uid()) in (finder_id, reporter_id));

insert into storage.buckets (id,name,public,file_size_limit,allowed_mime_types)
values ('recovery-evidence','recovery-evidence',false,5242880,array['image/jpeg']);

create policy recovery_evidence_upload on storage.objects for insert to authenticated
with check (bucket_id = 'recovery-evidence' and name ~ (
    '^' || (select auth.uid())::text || '/[0-9a-f-]{36}\.jpg$'
));
create policy recovery_evidence_read on storage.objects for select to authenticated
using (bucket_id = 'recovery-evidence' and exists (
    select 1 from public.recovery_requests r where r.evidence_path = storage.objects.name
));
create policy recovery_evidence_select_guard on storage.objects as restrictive for select to public
using (bucket_id <> 'recovery-evidence' or exists (
    select 1 from public.recovery_requests r where r.evidence_path = storage.objects.name
      and (select auth.uid()) in (r.finder_id,r.reporter_id)
));
create policy recovery_evidence_insert_guard on storage.objects as restrictive for insert to public
with check (bucket_id <> 'recovery-evidence' or name ~ (
    '^' || (select auth.uid())::text || '/[0-9a-f-]{36}\.jpg$'
));
create policy recovery_evidence_update_guard on storage.objects as restrictive for update to public
using (bucket_id <> 'recovery-evidence') with check (bucket_id <> 'recovery-evidence');
create policy recovery_evidence_delete_guard on storage.objects as restrictive for delete to public
using (bucket_id <> 'recovery-evidence');

create function public.submit_recovery_request(
    p_id uuid, p_lost_item_id uuid, p_found_location text, p_found_at timestamptz,
    p_item_condition text, p_custodian text, p_pickup_location text, p_notes text, p_has_photo boolean
) returns uuid language plpgsql security definer set search_path = '' as $$
declare
    actor uuid := auth.uid();
    report public.lost_items;
    photo_path text;
begin
    if actor is null then raise exception 'Login diperlukan' using errcode = '42501'; end if;
    select * into report from public.lost_items where id = p_lost_item_id for update;
    if not found or report.reporter_id = actor then
        raise exception 'Laporan tidak tersedia untuk pengajuan' using errcode = '42501';
    end if;
    if exists (select 1 from public.recovery_requests where id=p_id and finder_id=actor and lost_item_id=report.id) then
        return p_id;
    end if;
    if report.status <> 'open' or p_found_at > now() or exists (
        select 1 from public.recovery_requests where lost_item_id=report.id and status='confirmed'
    ) then
        raise exception 'Laporan atau waktu penemuan tidak memenuhi syarat' using errcode = '23514';
    end if;
    if p_has_photo then
        photo_path := actor::text || '/' || p_id::text || '.jpg';
        if not exists (select 1 from storage.objects where bucket_id='recovery-evidence' and name=photo_path) then
            raise exception 'Foto pendukung belum tersedia' using errcode = '23514';
        end if;
    end if;
    insert into public.recovery_requests
        (id,lost_item_id,finder_id,reporter_id,item_title,found_location,found_at,item_condition,custodian,pickup_location,notes,evidence_path)
    values (p_id,report.id,actor,report.reporter_id,report.title,btrim(p_found_location),p_found_at,
        btrim(p_item_condition),btrim(p_custodian),btrim(p_pickup_location),btrim(p_notes),photo_path);
    return p_id;
end;
$$;
revoke all on function public.submit_recovery_request(uuid,uuid,text,timestamptz,text,text,text,text,boolean) from public,anon;
grant execute on function public.submit_recovery_request(uuid,uuid,text,timestamptz,text,text,text,text,boolean) to authenticated;

create function public.guard_lost_recovery() returns trigger
language plpgsql security definer set search_path = '' as $$
begin
    if auth.uid() is not null and exists (
        select 1 from public.recovery_requests where lost_item_id=old.id and status in ('pending','confirmed')
    ) then
        raise exception 'Laporan terkunci selama ada konfirmasi temuan aktif' using errcode = '23514';
    end if;
    if tg_op = 'DELETE' then return old; end if;
    if new.reporter_id is distinct from old.reporter_id or new.id is distinct from old.id then
        raise exception 'Identitas laporan tidak dapat diubah' using errcode = '23514';
    end if;
    return new;
end;
$$;
revoke all on function public.guard_lost_recovery() from public,anon,authenticated;
create trigger temubarang_guard_lost_recovery before update or delete on public.lost_items
for each row execute function public.guard_lost_recovery();

create function public.review_recovery_request(p_id uuid, p_status text)
returns void language plpgsql security definer set search_path = '' as $$
declare
    actor uuid := auth.uid();
    request public.recovery_requests;
    report public.lost_items;
begin
    select * into request from public.recovery_requests where id=p_id;
    if actor is null or not found or actor <> request.reporter_id then
        raise exception 'Akses ditolak' using errcode = '42501';
    end if;
    select * into report from public.lost_items where id=request.lost_item_id for update;
    select * into request from public.recovery_requests where id=p_id;
    if report.status <> 'open' or report.reporter_id <> actor or p_status is null
        or not ((request.status='pending' and p_status in ('confirmed','rejected'))
            or (request.status='confirmed' and p_status in ('completed','rejected'))) then
        raise exception 'Perubahan status tidak diizinkan' using errcode = '23514';
    end if;
    update public.recovery_requests set status=p_status,
        decided_at=case when p_status='completed' then decided_at else now() end,
        completed_at=case when p_status='completed' then now() end where id=p_id;
    if p_status='confirmed' then
        update public.recovery_requests set status='rejected', decided_at=now()
        where lost_item_id=report.id and id<>p_id and status='pending';
    elsif p_status='completed' then
        update public.lost_items set status='found' where id=report.id;
    end if;
end;
$$;
revoke all on function public.review_recovery_request(uuid,text) from public,anon;
grant execute on function public.review_recovery_request(uuid,text) to authenticated;

commit;
