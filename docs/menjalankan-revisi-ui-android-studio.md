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

## 5. Checklist regresi UI/UX

- **Navigasi:** Dari Beranda ketuk **Saya Menemukan Barang**. Pastikan membuka daftar **Barang Ditemukan**, tab Ditemukan aktif, lalu pindah ke Beranda/Hilang/Klaim/Profil dan kembali tanpa tab mati.
- **Navbar:** Pastikan latar navbar transparan, ikon/label jelas, dan seluruh area sentuh tab bekerja.
- **Beranda:** Urutan Hi nama pengguna → salam waktu → search → tombol temuan → filter → feed. Tautan **Lihat Hilang** tidak ada.
- **Cari dan filter:** Uji query cocok/tidak cocok pada Beranda, Hilang, dan Ditemukan. Uji filter **Semua/Hilang/Ditemukan**, **Tersedia**, dan **Laporan saya**.
- **Form:** Tambah/Edit Hilang dan Ditemukan, back pada header, pilih tanggal melalui kalender, gunakan kamera/galeri, simpan, dan periksa pesan validasi.
- **Detail:** Buka detail kedua jenis laporan, lihat card dan badge status, gunakan tombol kembali, edit/status/hapus sesuai hak akses.
- **Responsif:** Coba emulator 320 dp dan 360 dp, font scale 1.3 atau lebih besar, keyboard muncul, rotasi layar, serta navigasi gestur.

Pengujian manual ini belum dijalankan dalam lingkungan pengembangan GitHub. Build, emulator, dan koneksi ke backend harus diperiksa di Android Studio milik pengembang sebelum merge ke main.
