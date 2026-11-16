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
