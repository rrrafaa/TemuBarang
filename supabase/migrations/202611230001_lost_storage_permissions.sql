-- Jalankan setelah 202611160002_found_storage.sql pada proyek Supabase TemuBarang.
-- Izin hanya untuk objek milik pengguna di folder <uid>/lost/.
begin;

create policy temubarang_lost_insert_self on storage.objects
for insert to authenticated
with check (
    bucket_id = 'temubarang-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and (storage.foldername(name))[2] = 'lost'
);

create policy temubarang_lost_delete_self on storage.objects
for delete to authenticated
using (
    bucket_id = 'temubarang-images'
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and (storage.foldername(name))[2] = 'lost'
);

commit;
