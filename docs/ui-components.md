# Komponen tampilan bersama

- `ui/component/AppHeader.kt`: judul, tombol kembali opsional, tombol muat ulang opsional, dan indikator proses. Pemanggil menentukan callback, keadaan loading, dan inset. Dipakai pada daftar/form/detail Found dan Beranda.
- `ui/component/AppBottomNavigation.kt`: menerima daftar `AppNavigationItem`, route aktif, dan callback pemilihan. Aturan `popUpTo`/pemulihan state tetap di `AppNavigation.kt`.
- `AppChromePreview.kt`: contoh Preview Compose header/navbar pada 360 dp dan 320 dp dengan skala teks 1,3. Buka mode Split/Design Android Studio; Preview tidak memerlukan Supabase.
- `features/found/ui/component/FoundFormComponents.kt`: label tetap di atas input, placeholder contoh, baris pilihan horizontal yang dapat digeser, dan kartu section detail. Label input tetap tersedia untuk pembaca layar; ikon dekoratif tidak mengulang nama tombol.

Daftar Found memakai satu `LazyColumn` dengan area aksi terpisah di bawahnya. Dengan demikian tombol Tambah Laporan tetap terlihat di atas navbar, termasuk ketika daftar kosong atau sedang dimuat. Form tambah/edit memakai pilihan kategori, penyimpan, dan status dalam satu baris horizontal yang bisa digeser. Detail terdiri atas kartu Informasi Barang dan Pelapor; kontrol edit/hapus berada setelah kedua kartu.

## Sumber ikon

Ikon disimpan sebagai Android VectorDrawable 24 × 24, dikonversi dari data SVG resmi Iconify, tanpa library atau permintaan jaringan saat aplikasi berjalan.

- [Lucide Extended](https://icon-sets.iconify.design/lucide-extended/) dari Applifted: varian `duotone` untuk house, search, package-check, clipboard-check, user-round, circle-arrow-left, camera, image, circle-plus, package, mail, phone, dan trash.
- `refresh-cw` memakai [Lucide](https://icon-sets.iconify.design/lucide/refresh-cw/), karena Lucide Extended tidak menyediakan ikon refresh. Ukuran dan ketebalan garis mengikuti keluarga Lucide yang sama.
- Data sumber: `iconify/icon-sets`, berkas `json/lucide-extended.json` dan `json/lucide.json`. Lisensi dan atribusi disertakan di `docs/licenses/` dan dibundel dalam APK melalui `assets/third_party_licenses/` (ISC).

Pemeriksaan perangkat: pastikan tombol bawah tidak menutupi baris terakhir; geser seluruh pilihan pada layar sempit; periksa keyboard pada form dan skala teks besar; pastikan ikon refresh/kembali terbaca oleh TalkBack. Build/lint dan Preview Compose tidak menggantikan pengujian perangkat.


## Layout referensi: header tetap, navbar aktif, dan profil

`AppPage` menjadi kerangka semua halaman utama setelah login, termasuk subhalaman Found. Header berada di luar konten yang digulir sehingga judul dan aksi tetap di atas. `AppPage` menangani inset status bar; halaman tidak menambahkan `statusBarsPadding` lagi. Navbar induk menangani inset bawah pada tab utama, sementara form/detail tanpa navbar memakai `navigationBarsPadding`.

```kotlin
AppPage("Nama Halaman", onBack = onBack, onRefresh = viewModel::refresh,
    refreshing = state.isLoading) {
    LazyColumn(Modifier.fillMaxSize()) {
        // Isi halaman saja; jangan letakkan header di dalam daftar.
    }
}
```

Navbar memakai ikon aktif dalam lingkaran terangkat dengan lekukan di permukaan bar, mengikuti referensi. Lima tab tetap Beranda, Hilang, Ditemukan, Klaim, Profil. Label dapat membungkus saat teks diperbesar; seluruh tab menggunakan semantics `Role.Tab` dan state pilihan. Inset gestur mengikuti sistem Android; tidak menggambar home indicator palsu.

Profil menampilkan kartu identitas dengan inisial (belum ada fitur avatar), lalu kelompok menu Akun dan Bantuan. Kelola Profil serta Kontak dan Privasi membuka editor tersimpan yang sama. Panduan dan Tentang membuka dialog informasi yang tersedia. Pengaturan sandi, notifikasi, dan bahasa dari referensi tidak ditambahkan karena belum didukung aplikasi. Keluar Akun berada di Profil.

Beranda menampilkan sapaan menurut waktu perangkat dan nama profil; jika nama belum dimuat, digunakan “Teman Kampus”. ViewModel profil dibagikan di scope akun sehingga perubahan nama yang tersimpan juga muncul di beranda. Seluruh isi beranda dapat digulir sementara header tetap di atas.

Pemeriksaan perangkat tambahan: pilih kelima tab untuk memeriksa lekukan pada posisi tepi, gulir setiap halaman dan pastikan header diam, uji profil dengan email panjang dan skala teks besar, simpan nama lalu kembali ke Beranda, serta buka keyboard pada editor profil. Pemeriksaan ini belum menggantikan pengujian di emulator/perangkat.


## Design System terpusat (revisi November 2026)

- `ui/theme/Color.kt`, `Theme.kt`, dan `Type.kt`: warna identitas, tema Material 3, dan hierarki tipografi. Warna status tambahan ada di `DesignTokens.kt`.
- `ui/theme/DesignTokens.kt`: `AppSpacing`, `AppRadius`, `AppSizes`, dan `AppStatusColors`. Gunakan token saat menambah komponen; jangan mengulangi angka tanpa alasan.
- `ui/component/AppFields.kt`: `AppSearchBar`, `AppFormField`, `AppSectionCard`, `AppFilterChip`, `AppStatusBadge`, `AppDetailRow`, dan `AppDropdownField`. Field menyediakan label, placeholder, supporting text, status error, jenis keyboard, dan multiline; wrapper per fitur tetap dapat menjaga parameter API lama.
- `ui/component/AppHeader.kt` + `AppPage.kt`: header yang sama untuk beranda, daftar, tambah/edit, dan detail. Tombol kembali dan refresh dikendalikan callback dari navigasi.
- `ui/component/AppBottomNavigation.kt`: latar transparan; tanpa bidang solid dan tanpa Canvas lekukan. Setiap tab mempunyai area sentuh dan indikator aktif. Navigasi utama dikelola terpusat oleh `AuthenticatedNavigation`.
- `ui/component/AppPhotoPicker.kt` menyatukan tampilan picker foto untuk Lost/Found; callback kamera, galeri, dan penyimpanan URI tetap dimiliki masing-masing fitur.
- `ui/component/AppDatePicker.kt` menyediakan `AppDatePickerField` bersama dengan kalender tanggal; wrapper lama `DatePickerField` tetap tersedia pada modul Lost untuk kompatibilitas.

Contoh:
```kotlin
AppSearchBar(value = query, onValueChange = { query = it })
AppSectionCard("Detail barang") {
    AppFormField(value = title, onValueChange = { title = it },
        label = "Nama barang *", placeholder = "Contoh: Kunci motor")
}
AppStatusBadge("Masih tersedia")
AppDropdownField("Kategori barang", options, selected, onSelect = { selected = it })
```

Urutan Beranda: sapaan nama, sapaan waktu, search, aksi temuan, judul feed, filter, daftar.
Urutan Ditemukan: search, filter, daftar, dan tombol tambah tetap tersedia. Halaman Hilang menggunakan search yang sama. Semua filter bekerja di state lokal / ViewModel dan tidak mengubah query Supabase.
Detail Hilang dan Ditemukan memakai section card, foto, label-nilai, serta header yang sama. Data kontak pelapor pada Found tetap mengikuti izin profil; laporan Hilang tidak memuat data kontak pelapor sehingga UI tidak mengada-adakan informasi.

### Checklist uji manual

1. Beranda → Saya Menemukan Barang → tab Ditemukan aktif → pindah Hilang/Klaim/Profil/Beranda → Ditemukan; periksa tombol selalu dapat diklik.
2. Uji search dan filter dengan query kosong, cocok, tidak cocok, dan pergantian orientasi.
3. Tambah/edit Lost dan Found, pilih tanggal lewat kalender, unggah foto, buka/tutup keyboard, dan periksa tombol aksi tidak menutup input.
4. Buka detail Lost/Found, cek tampilan status, back, aksi edit/status/hapus dan pembatasan hak akses.
5. Uji emulator 320dp dan 360dp, font scale 1.3+, keyboard IME, dan navigasi gestur.

Pemeriksaan ini harus dilakukan melalui Android Studio karena perubahan kode di GitHub tidak dapat menggantikan build/emulator.
