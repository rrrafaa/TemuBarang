# 🔎 TemuBarang — Aplikasi Laporan Barang Hilang & Ditemukan

**TemuBarang** adalah aplikasi Android yang membantu masyarakat, khususnya mahasiswa dan warga sekitar, untuk melaporkan barang hilang, menginformasikan barang yang ditemukan, dan mengajukan klaim kepemilikan. Aplikasi ini dikembangkan sebagai proyek mata kuliah **Pengembangan Aplikasi Mobile (PAM)** menggunakan **Kotlin, Jetpack Compose, dan Supabase**.

> **Dokumentasi kolaborasi:** README ini tidak memuat URL proyek backend, API key, password, token, data akun, maupun informasi rahasia lain. Konfigurasi akses Supabase dibagikan melalui komunikasi pribadi kepada anggota kelompok.

## 📱 1. Fitur Aplikasi

| Fitur | Deskripsi | Status pada baseline proyek |
| --- | --- | --- |
| **Login & Register** | Autentikasi email/password, validasi form, penanganan error pada UI, sesi pengguna, dan logout | Sudah diimplementasikan |
| **Beranda** | Menampilkan daftar laporan barang hilang terbaru dari database | Sudah diimplementasikan sebagai tampilan **Read** |
| **Laporan Barang Ditemukan** | Menambah laporan, melihat daftar dan detail, mengedit laporan, memilih foto dari galeri atau kamera, serta mengunggah foto ke Cloud Storage | Sudah diimplementasikan; perlu pengujian pada perangkat masing-masing |
| **Laporan Barang Hilang** | Mengelola laporan barang hilang milik pengguna | Modul anggota; CRUD masih dikembangkan |
| **Klaim Barang** | Mengajukan klaim terhadap laporan barang ditemukan dan melihat statusnya | Modul anggota; masih dikembangkan |
| **Bottom Navigation** | Navigasi Beranda, Hilang, Ditemukan, dan Klaim | Sudah diimplementasikan |

**Catatan:** Beranda adalah modul bersama yang **membaca** data `lost_items`. Pembuatan dan pengelolaan laporan barang hilang tetap dikerjakan di modul **Barang Hilang**; keduanya tidak perlu dibuat sebagai fitur yang saling menduplikasi.

## 🛠️ 2. Teknologi yang Digunakan

- **Bahasa pemrograman:** Kotlin
- **IDE:** Android Studio
- **Antarmuka:** Jetpack Compose + Material 3
- **Arsitektur:** MVVM (*Model–View–ViewModel*) + Repository
- **State management:** ViewModel, `StateFlow`, dan Compose State
- **Navigasi:** Navigation Compose
- **Backend:** Supabase Authentication, PostgreSQL, dan Storage
- **Komunikasi data:** Supabase Kotlin SDK dan Kotlin Coroutines
- **Gambar:** Coil, Android Photo Picker/galeri, serta kamera perangkat melalui `ActivityResultContracts.TakePicture` dan `FileProvider`
- **Build system:** Gradle Kotlin DSL (`.kts`)

Konfigurasi yang digunakan pada baseline proyek: `compileSdk = 36`, `targetSdk = 35`, dan `minSdk = 26`. Versi plugin dan dependency selengkapnya mengikuti **`gradle/libs.versions.toml`** di repository; jangan memperbarui versi dependency secara sepihak tanpa pengujian bersama.

## 💻 3. Prasyarat Sebelum Clone

Pastikan perangkat pengembangan memiliki:

1. **Git**, untuk mengambil dan mengirim perubahan ke repository.
2. **Android Studio** dengan dukungan Kotlin dan Jetpack Compose.
3. **JDK 17** sebagai Gradle JDK.
4. **Android SDK Platform API 36** dan komponen SDK yang diperlukan proyek (melalui **Tools → SDK Manager**).
5. **Android Emulator** atau perangkat Android fisik dengan versi Android minimal **8.0 (API 26)**.
6. **Akses Internet**, untuk Gradle Sync dan komunikasi aplikasi dengan Supabase.
7. **Akses project backend** yang dibagikan secara pribadi kepada anggota kelompok.

> Gradle Wrapper telah disertakan di repository. Anggota tidak perlu menginstal Gradle secara manual selama `gradlew` / `gradlew.bat` tersedia.

## 🚀 4. Panduan Clone dan Menjalankan Project

### Langkah 1 — Clone repository

Buka **PowerShell**, Terminal Android Studio, atau terminal lain, lalu jalankan:

```powershell
git clone https://github.com/rrrafaa/TemuBarang.git
cd TemuBarang
```

Repository: **https://github.com/rrrafaa/TemuBarang**

Jika repository masih **Private**, pastikan undangan kolaborator telah diterima dan akun GitHub yang dipakai memiliki izin mengaksesnya.

### Langkah 2 — Buka project di Android Studio

1. Buka **Android Studio → Open**.
2. Pilih folder `TemuBarang` hasil clone (folder yang berisi `settings.gradle.kts`, `gradlew.bat`, dan `app/`).
3. Tunggu Android Studio membaca project Gradle.
4. Pastikan **Gradle JDK** menggunakan JDK 17 pada pengaturan Gradle Android Studio.
5. Instal Android SDK Platform yang diminta jika muncul notifikasi *missing SDK*.

### Langkah 3 — Setup konfigurasi lokal (dibagikan secara pribadi)

Aplikasi membutuhkan konfigurasi Supabase yang **tidak disimpan di GitHub**. Setiap anggota wajib menyiapkan file **`local.properties`** di root project, sesuai petunjuk konfigurasi yang dibagikan secara pribadi.

- Android Studio biasanya membuat file ini otomatis untuk lokasi Android SDK.
- Parameter dan nilai konfigurasi backend dibagikan melalui **chat pribadi/kanal privat**.
- Jangan mengubah konfigurasi backend secara sembarangan atau menggunakan project Supabase yang berbeda.
- **Jangan mengunggah, memotret, atau membagikan isi `local.properties` di Issue, Pull Request, screenshot, maupun README.**
- Setelah konfigurasi disimpan, lakukan **Gradle Sync** dan **Build ulang**, karena pengaturan tersebut dibaca saat proses build.

> **Penting:** File `local.properties` adalah file lokal setiap komputer. Jika file tersebut tidak muncul setelah clone, itu **normal** karena sengaja dikecualikan dari Git.

### Langkah 4 — Sync Gradle

Di Android Studio pilih **File → Sync Project with Gradle Files**. Pastikan proses selesai tanpa error. Apabila muncul masalah dependency, baca bagian [Troubleshooting](#-10-troubleshooting).

### Langkah 5 — Build project

Di terminal **root project**, jalankan salah satu perintah sesuai sistem operasi:

**Windows (PowerShell):**

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

**macOS/Linux:**

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Kedua perintah digunakan untuk memeriksa build debug dan menjalankan unit test. Keberhasilan build **tidak otomatis** memastikan semua fitur backend berfungsi; tetap lakukan pengujian pada emulator atau perangkat.

### Langkah 6 — Jalankan aplikasi

1. Aktifkan emulator melalui **Tools → Device Manager**, atau hubungkan HP Android dengan USB debugging.
2. Pilih perangkat pada toolbar Android Studio.
3. Klik **Run ▶** pada modul `app`.
4. Uji **Register / Login**, lalu navigasi ke **Beranda**, **Hilang**, **Ditemukan**, dan **Klaim**.

Untuk pengujian autentikasi, gunakan akun pengujian yang telah disepakati kelompok. Jika konfirmasi email aktif pada Supabase, selesaikan verifikasi sebelum Login.

## 🏗️ 5. Struktur Folder (MVVM)

```text
TemuBarang/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/temubarang/
│   │   │   ├── activity/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── navigation/        # Rute dan bottom navigation
│   │   │   │   └── supabase/          # Supabase Client bersama
│   │   │   ├── features/
│   │   │   │   ├── auth/              # Login, Register, session
│   │   │   │   ├── home/              # Feed barang hilang (Read)
│   │   │   │   ├── lost/              # Fitur anggota 1
│   │   │   │   ├── found/             # Fitur anggota 2
│   │   │   │   └── claims/            # Fitur anggota 3
│   │   │   └── ui/
│   │   │       ├── theme/             # Warna, tipografi, tema
│   │   │       └── component/         # Komponen UI global
│   │   ├── res/                      # Logo, ikon, XML FileProvider
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradlew
├── gradlew.bat
├── build.gradle.kts
├── settings.gradle.kts
├── .gitignore
└── README.md
```

Struktur standar setiap fitur yang dikembangkan:

```text
features/nama_fitur/
├── model/          # Data class, validator, UI state
├── repository/     # Query ke Supabase, upload/download
├── viewmodel/      # StateFlow, validasi, coroutine, aksi UI
└── ui/
    ├── screen/     # Halaman Compose: list, form, detail
    └── component/  # Card, input, dialog dan komponen khusus fitur
```

**Alur data:** `Compose Screen → ViewModel → Repository → Supabase`. Data yang diterima dari repository disimpan sebagai state di ViewModel dan diobservasi oleh UI. **Jangan menjalankan query Supabase langsung dari Composable Screen.**

## 👥 6. Pembagian Tugas Kelompok

| Anggota | Fitur | Tabel utama | Target minimum |
| --- | --- | --- | --- |
| **Anggota 1** | Laporan Barang Hilang (`features/lost`) | `lost_items` | Create, Read, daftar dengan LazyList, ViewModel |
| **Anggota 2** | Laporan Barang Ditemukan (`features/found`) | `found_items` | Create, Read, LazyList, ViewModel; ditambah Update dan foto kamera/galeri |
| **Anggota 3** | Klaim Barang (`features/claims`) | `claims` | Create, Read, daftar dengan LazyList, ViewModel |
| **Bersama** | Login, Register, Beranda, bottom navigation, integrasi dan pengujian | Supabase Auth dan tabel terkait | Integrasi aplikasi secara keseluruhan |

**Ketentuan per anggota:** minimal satu tabel Supabase yang dapat di-*Insert* dan di-*Select*, data ditampilkan menggunakan `LazyColumn`, `LazyRow`, atau `LazyVerticalGrid`, serta pengelolaan state menggunakan `ViewModel`. Operasi Update dan Delete boleh ditambahkan sesuai kebutuhan dan aturan akses.

## 🗄️ 7. Backend dan Pengelolaan Data

Aplikasi menggunakan satu project Supabase bersama untuk:

- **Authentication:** pendaftaran, login, sesi pengguna, dan logout.
- **PostgreSQL:** tabel `lost_items`, `found_items`, dan `claims`.
- **Storage:** penyimpanan foto laporan yang diunggah dari galeri atau kamera.

**Skema database telah disiapkan pada project Supabase bersama.** Anggota yang melakukan clone **tidak perlu menjalankan SQL pembuatan tabel ulang**. Jika memerlukan perubahan tabel, diskusikan lebih dahulu agar relasi dan fitur anggota lain tidak rusak.

Pengaturan keamanan data menggunakan **Row Level Security (RLS)**. Hindari menonaktifkan RLS hanya untuk mengatasi error; cek policy, status autentikasi, dan hak akses pengguna terlebih dahulu. Jangan mengunggah dump database, data pengguna, token sesi, maupun kredensial backend ke repository.

### Alur foto laporan Barang Ditemukan

1. Buka tab **Ditemukan → Tambah Laporan**.
2. Pilih **Ambil Foto** atau **Galeri**.
3. Jika menggunakan kamera, ambil dan konfirmasi foto; aplikasi kembali ke form sambil mempertahankan input sebelumnya.
4. Foto ditampilkan sebagai **preview lokal** terlebih dahulu.
5. Lengkapi form dan tekan **Kirim Laporan**.
6. Aplikasi mengunggah foto ke Storage dan menyimpan data laporan ke tabel `found_items`.
7. Laporan yang tersimpan dapat dilihat kembali melalui daftar dan detail.

**Menyimpan foto di aplikasi kamera belum sama dengan mengirim laporan.** Upload ke Supabase dilakukan ketika pengguna mengirim form laporan.

## 🌿 8. Git Workflow untuk Kolaborasi

**Jangan melakukan pekerjaan fitur langsung pada branch `main`.** Gunakan branch terpisah dan Pull Request.

### Ambil pembaruan sebelum mengerjakan fitur

```powershell
git switch main
git pull --ff-only origin main
```

### Buat branch fitur

Buat **satu** branch sesuai tugas:

```powershell
# Anggota 1
git switch -c feature/lost-items

# Anggota 2 (jalankan dari main, bukan dari branch anggota 1)
# git switch -c feature/found-items

# Anggota 3 (jalankan dari main, bukan dari branch anggota lain)
# git switch -c feature/claims
```

Jika branch sudah tersedia di remote, **jangan membuat branch baru dengan nama yang sama**. Gunakan `git fetch origin` dan checkout/switch branch yang sesuai.

### Simpan pekerjaan dan push

```powershell
git status
git add .
git diff --cached --name-only
git diff --cached
```

**Tinjau terlebih dahulu** agar tidak ada file lokal atau data sensitif yang masuk commit. Jika aman:

```powershell
git commit -m "feat(found): implementasi laporan barang ditemukan"
git push -u origin feature/found-items
```

Ganti pesan commit dan nama branch sesuai fitur yang benar. Setelah push, buat **Pull Request ke `main`**, minta review, lalu merge setelah aplikasi berhasil dibangun dan diuji. Seluruh anggota mengerjakan perubahan pada **file bersama** (misalnya `MainActivity.kt`, `AppNavigation.kt`, `Theme.kt`, atau Gradle) melalui koordinasi agar tidak saling menimpa.

**Contoh format commit:**

- `feat(auth): tambah validasi form login`
- `feat(found): tambah upload foto kamera`
- `feat(lost): tambah daftar barang hilang`
- `fix(camera): pertahankan state form setelah foto`
- `docs(readme): perbarui panduan setup`

## 🔒 9. Keamanan Repository

File lokal/rahasia **tidak boleh di-commit**: `local.properties`, file environment berisi konfigurasi privat, keystore/private key, dump/backup database, token, dan data pribadi pengguna. Hasil build serta cache (`build/`, `.gradle/`) juga tidak perlu diunggah.

Periksa aturan `.gitignore` dan status file sebelum commit:

```powershell
git check-ignore -v local.properties
git ls-files -- local.properties
git status --short
git diff --cached --name-only
```

- `git check-ignore` menampilkan aturan ignore yang berlaku.
- `git ls-files` **seharusnya tidak** menampilkan `local.properties`.
- Jika file sensitif pernah terlanjur masuk **commit**, `.gitignore` saja tidak menghapusnya dari riwayat Git. Segera koordinasikan penanganan dan rotasi kredensial dengan anggota kelompok bila diperlukan.
- Jangan unggah screenshot dashboard Supabase yang memperlihatkan informasi privat.

## 🧪 10. Pengujian Fungsional

Gunakan checklist berikut sebelum membuat Pull Request:

- [ ] Gradle Sync dan `assembleDebug` berhasil.
- [ ] Login dengan akun pengujian yang valid berhasil masuk ke Beranda.
- [ ] Register menampilkan validasi dan pesan kesalahan secara langsung di UI.
- [ ] Logout berhasil mengembalikan pengguna ke halaman autentikasi.
- [ ] Beranda menampilkan laporan dari `lost_items` atau *empty state* jika datanya kosong.
- [ ] Bottom navigation menuju tab yang sesuai.
- [ ] Fitur masing-masing anggota bisa **Create** dan **Read** data dari tabelnya.
- [ ] Data hasil Select tampil pada **LazyList**, bukan data hardcoded.
- [ ] Kamera bisa dibuka dari form Barang Ditemukan, lalu kembali ke form tanpa menghapus isian.
- [ ] Preview foto tampil, upload berhasil, dan foto dapat dimuat kembali dari Storage.
- [ ] Pesan error jaringan, autentikasi, dan validasi tampil tanpa membuat aplikasi crash.
- [ ] Data pengguna lain tidak bisa diedit tanpa izin.

## 🩺 11. Troubleshooting

| Gejala | Pemeriksaan awal |
| --- | --- |
| **Gradle Sync gagal** | Cek koneksi, Gradle JDK 17, Android SDK 36, dan dependency pada `libs.versions.toml`. Baca **Build Output** untuk akar error. |
| **Konfigurasi Supabase belum tersedia** | Pastikan file lokal telah disiapkan sesuai instruksi yang diberikan secara pribadi, kemudian Sync dan Build ulang. |
| **Login gagal / email belum terverifikasi** | Periksa pesan di UI dan status pengguna di Supabase Authentication bersama pengelola backend. |
| **Terlalu banyak percobaan Register** | Jangan mengulang request terus-menerus; laporkan waktu kejadian dan error ke pengelola untuk dicek pada Auth Logs. |
| **Tidak dapat terhubung ke server** | Cek internet emulator/HP, jaringan, pengaturan backend lokal, dan Logcat tanpa membagikan token. |
| **Logo tidak ditemukan** | Pastikan resource gambar ada pada `res/drawable-nodpi/` dan nama file sesuai referensi kode. |
| **Kamera tidak kembali ke form** | Uji pada perangkat, periksa FileProvider, state draft foto, serta Logcat. |
| **Data tidak tampil** | Periksa status login, tabel Supabase, policy RLS, dan pesan error repository/ViewModel. |

## 📚 12. Dokumentasi dan Pengumpulan

Dokumentasi akhir kelompok meliputi penjelasan fitur, arsitektur ViewModel, screen flow, navigasi, event handling, LazyList, komunikasi data dengan Supabase, screenshot implementasi, serta video demo penggunaan aplikasi.

**Tenggat laporan akhir: 13 Desember 2026**, sesuai ketentuan tugas. Tautan video presentasi dan dokumen laporan dapat ditambahkan setelah benar-benar tersedia. Jangan memasukkan tautan contoh seolah-olah merupakan hasil final.

---

**TemuBarang — Membantu mempertemukan kembali barang dengan pemiliknya.**
