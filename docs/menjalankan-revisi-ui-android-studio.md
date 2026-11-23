# Menjalankan revisi UI TemuBarang di Android Studio

Revisi berada langsung pada branch `feature/found-items`. Jangan mengambil `main` apabila ingin melihat revisi ini.

## 1. Perbarui source code

Jika belum pernah clone:

```powershell
git clone https://github.com/rrrafaa/TemuBarang.git
cd TemuBarang
git fetch origin
git switch --track origin/feature/found-items
```

Jika repo sudah tersedia pada komputer:

```powershell
cd TemuBarang
git status
git fetch origin
git switch feature/found-items
git pull --ff-only origin feature/found-items
```

Jika `git switch feature/found-items` tidak menemukan branch lokal, gunakan:
`git switch --track origin/feature/found-items`.

**Penting:** Sebelum switch/pull, simpan commit atau stash perubahan lokal yang belum disimpan. Jangan memakai `git reset --hard` untuk memaksa proses.

## 2. Konfigurasi Android Studio

1. Buka folder root `TemuBarang` via Android Studio → Open (berisi `settings.gradle.kts`).
2. Atur Gradle JDK ke **JDK 17** di Settings → Build, Execution, Deployment → Build Tools → Gradle.
3. Dari Tools → SDK Manager instal **Android SDK Platform API 36**. `minSdk` proyek adalah 26.
4. Isi `local.properties` di root proyek dengan `sdk.dir` dan dua properti backend yang diberikan tim: `SUPABASE_URL` dan `SUPABASE_PUBLISHABLE_KEY`. Jangan commit file ini; jangan gunakan service-role key. Jika nilainya belum tersedia, minta akses kepada pengelola proyek.
5. Klik **Sync Project with Gradle Files** lalu tunggu proses selesai.

## 3. Periksa build dan unit test

Pada Terminal Android Studio (Windows PowerShell):

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

Output APK debug ada di `app/build/outputs/apk/debug/app-debug.apk` apabila build berhasil.

## 4. Jalankan aplikasi

1. Buka Tools → Device Manager, pilih/emulasikan perangkat Android API 26 atau lebih baru, atau aktifkan USB debugging pada ponsel fisik.
2. Pilih konfigurasi run **app**, pilih perangkat, lalu tekan **Run ▶**.
3. Login dengan akun pengujian yang memiliki akses ke Supabase proyek tim.
4. Periksa langkah uji di bawah ini.

## 5. Migrasi Supabase yang diperlukan untuk foto Barang Hilang

Proyek target: `hjgfcsdyifeqbgwiqbtx`. Pastikan URL di `local.properties` mengarah ke proyek TemuBarang tersebut dan jangan gunakan service-role key pada aplikasi.

**Penting:** GitHub hanya menyimpan file SQL; commit **tidak otomatis menjalankan migrasi** ke Supabase.

1. Buka dashboard Supabase proyek TemuBarang → SQL Editor.
2. Periksa apakah bucket `temubarang-images`, tabel `lost_items` / `found_items`, dan kebijakan Storage sudah ada. Contoh kueri tanpa perubahan data tersedia di `supabase/verification/temubarang_read_only_checks.sql`.
3. Jika migrasi pendahulu `202611160002_found_storage.sql` sudah diterapkan dan kebijakan `temubarang_lost_insert_self` belum ada, jalankan `supabase/migrations/202611230001_lost_storage_permissions.sql` sekali pada proyek TemuBarang. Jangan menjalankan migrasi pendahulu ulang tanpa memeriksa skema karena banyak perintah di dalamnya bersifat non-idempoten.
4. Verifikasi kembali hasilnya dengan kueri baca-saja dan uji pembuatan laporan dengan foto dari akun pengujian.

Jika SQL Editor menolak pembuatan policy, jangan nonaktifkan RLS dan jangan memasukkan kunci rahasia ke Android. Pastikan pengguna database berwenang dan cek kebijakan yang sudah ada. Jika proyek belum tersambung ke integrasi Supabase, sambungkan proyek yang benar melalui koneksi Supabase sebelum menjalankan perubahan dari ChatGPT.

## 6. Checklist regresi UI/UX

- **Navigasi:** Dari Beranda ketuk **Saya Menemukan Barang**. Pastikan membuka daftar **Barang Ditemukan**, tab Ditemukan aktif, lalu pindah ke Beranda/Hilang/Klaim/Profil dan kembali tanpa tab mati.
- **Navbar:** Pastikan desain navbar asli (bidang solid, lekukan, ikon aktif terangkat) kembali tampil dan seluruh tab dapat diakses.
- **Beranda:** Urutan Hi nama pengguna → salam waktu → search → tombol temuan → filter → feed. Tautan **Lihat Hilang** tidak ada.
- **Cari dan filter:** Uji query cocok/tidak cocok pada Beranda, Hilang, dan Ditemukan. Uji filter **Semua/Hilang/Ditemukan**, **Tersedia**, dan **Laporan saya**.
- **Form:** Tambah/Edit Hilang dan Ditemukan, back pada header, pilih tanggal melalui kalender, gunakan kamera/galeri, simpan, dan periksa pesan validasi.
- **Detail:** Buka detail kedua jenis laporan, lihat card dan badge status, gunakan tombol kembali, edit/status/hapus sesuai hak akses.
- **Responsif:** Coba emulator 320 dp dan 360 dp, font scale 1.3 atau lebih besar, keyboard muncul, rotasi layar, serta navigasi gestur.

Pengujian manual ini belum dijalankan dalam lingkungan pengembangan GitHub. Build, emulator, dan koneksi ke backend harus diperiksa di Android Studio milik pengembang sebelum merge ke main.
