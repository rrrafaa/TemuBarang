-- Pemeriksaan baca-saja untuk proyek TemuBarang. Tidak mengubah data atau RLS.
-- Gunakan pada SQL Editor proyek hjgfcsdyifeqbgwiqbtx.

-- Tabel dan skema public yang dibutuhkan:
select schemaname, tablename, rowsecurity
from pg_tables
where schemaname = 'public'
  and tablename in ('lost_items', 'found_items', 'claims', 'profiles')
order by tablename;

-- Kolom wajib. Cocokkan dengan model Android dan migrasi fitur Found.
select table_name, column_name, data_type, is_nullable, column_default
from information_schema.columns
where table_schema = 'public'
  and table_name in ('lost_items', 'found_items')
order by table_name, ordinal_position;

-- Policy INSERT/SELECT/UPDATE/DELETE untuk akun authenticated.
select schemaname, tablename, policyname, permissive, roles, cmd, qual, with_check
from pg_policies
where (schemaname = 'public' and tablename in ('lost_items','found_items'))
   or (schemaname = 'storage' and tablename = 'objects')
order by schemaname, tablename, policyname;

-- Bucket foto beserta batas ukuran dan MIME.
select id, name, public, file_size_limit, allowed_mime_types
from storage.buckets
where id = 'temubarang-images';

-- Tidak perlu menjalankan INSERT/DELETE percobaan di SQL Editor.
-- Uji CRUD menggunakan akun dummy dari aplikasi dengan aturan RLS normal.
