-- LOCAL TEST FIXTURE ONLY: reproduces the supplied schema and a minimal Supabase Auth.
create role anon nologin;
create role authenticated nologin;
create schema auth;
create table auth.users(id uuid primary key, raw_user_meta_data jsonb default '{}');
create function auth.uid() returns uuid language sql stable as $$ select nullif(current_setting('request.jwt.claim.sub', true), '')::uuid $$;
grant usage on schema auth, public to authenticated, anon;
grant execute on function auth.uid() to authenticated, anon;
create table public.lost_items (
 id uuid primary key default gen_random_uuid(), reporter_id uuid not null references auth.users(id),
 title text not null check (char_length(trim(title)) between 3 and 120),
 description text not null check (char_length(trim(description)) >= 10),
 last_seen_location text not null, lost_at date not null, image_path text,
 status text not null default 'open' check (status in ('open','found','closed')), created_at timestamptz not null default now()
);
create table public.found_items (
 id uuid primary key default gen_random_uuid(), reporter_id uuid not null references auth.users(id),
 title text not null check (char_length(trim(title)) between 3 and 120),
 description text not null check (char_length(trim(description)) >= 10),
 found_location text not null, found_at date not null, image_path text,
 status text not null default 'open' check (status in ('open','returned','closed')), created_at timestamptz not null default now()
);
create table public.claims (
 id uuid primary key default gen_random_uuid(), found_item_id uuid not null references public.found_items(id),
 claimant_id uuid not null references auth.users(id), proof_description text not null check (char_length(trim(proof_description)) >= 10),
 status text not null default 'pending' check (status in ('pending','approved','rejected')), created_at timestamptz not null default now()
);
-- Minimal Storage metadata/RLS fixture. Does not emulate the Storage HTTP service.
create schema storage;
create table storage.buckets(id text primary key, name text not null, public boolean default false, file_size_limit bigint, allowed_mime_types text[]);
create table storage.objects(id uuid primary key default gen_random_uuid(), bucket_id text references storage.buckets(id), name text not null);
create function storage.foldername(name text) returns text[] language sql immutable as $$
    select (string_to_array(name, '/'))[1:array_length(string_to_array(name, '/'), 1)-1]
$$;
alter table storage.objects enable row level security;
grant usage on schema storage to anon, authenticated;
grant select, insert, update, delete on storage.objects to anon, authenticated;
-- Deliberately broad legacy policy: guards must still stop cross-user mutations.
create policy legacy_broad_access on storage.objects for all to public using (true) with check (true);
