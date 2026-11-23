-- Storage uses the existing public image URL workflow. Photos must contain no personal
-- documents/contact details. Profile contacts are not stored in this bucket.
begin;
insert into storage.buckets(id, name, public, file_size_limit, allowed_mime_types)
values ('temubarang-images', 'temubarang-images', true, 5242880, array['image/jpeg','image/png','image/webp'])
on conflict (id) do nothing;
do $$ begin
    if not exists (select 1 from storage.buckets where id='temubarang-images' and public) then
        raise exception 'Bucket temubarang-images is private; review signed URL support before applying';
    end if;
end $$;
-- Restrictive guards also apply when an older broad permissive policy exists.
-- Unrelated buckets retain their existing access behavior. No overwrites are needed.
create policy temubarang_found_insert_guard on storage.objects as restrictive
for insert to public with check (
    bucket_id <> 'temubarang-images' or
    (auth.uid() is not null and (storage.foldername(name))[1] = auth.uid()::text)
);
create policy temubarang_found_delete_guard on storage.objects as restrictive
for delete to public using (
    bucket_id <> 'temubarang-images' or
    (auth.uid() is not null and (storage.foldername(name))[1] = auth.uid()::text)
);
create policy temubarang_found_update_guard on storage.objects as restrictive
for update to public using (
    bucket_id <> 'temubarang-images' or
    (auth.uid() is not null and (storage.foldername(name))[1] = auth.uid()::text
        and (storage.foldername(name))[2] <> 'found')
) with check (
    bucket_id <> 'temubarang-images' or
    (auth.uid() is not null and (storage.foldername(name))[1] = auth.uid()::text
        and (storage.foldername(name))[2] <> 'found')
);
create policy temubarang_found_insert_self on storage.objects for insert to authenticated
with check (bucket_id='temubarang-images' and (storage.foldername(name))[1]=auth.uid()::text
    and (storage.foldername(name))[2]='found');
create policy temubarang_found_delete_self on storage.objects for delete to authenticated
using (bucket_id='temubarang-images' and (storage.foldername(name))[1]=auth.uid()::text
    and (storage.foldername(name))[2]='found');
create policy temubarang_images_read_members on storage.objects for select to authenticated
using (bucket_id='temubarang-images');
commit;
