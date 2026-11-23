# Revisi terpadu TemuBarang

## Audit produksi

Pemeriksaan baca pada proyek TemuBarang menemukan `profiles` dengan primary key `id`, foreign key ke Auth, pemicu registrasi `temubarang_create_profile`, serta tiga policy yang membatasi baca, tambah, dan ubah profil ke akun sendiri. Kolom identitas tambahan, `recovery_requests`, dan `notifications` belum tersedia. Bucket `temubarang-images` bersifat publik dan hanya sesuai untuk gambar laporan publik. Trigger `temubarang_manage_found` terverifikasi aktif. CRUD dan aturan klaim Found yang sudah ada dipakai kembali.

## Perubahan klien

| Bagian | Kode dan perilaku |
|---|---|
| Profil | `ProfileScreen` memiliki mode Utama, Detail, Kontak dan Privasi, serta Edit. `ProfileViewModel.consumeSaveNotice` mengonsumsi hasil simpan setelah sukses. `ProfileRepository.accountEmail` membaca email autentikasi secara terpisah. Afiliasi baru masih tampilan. |
| Beranda dan Lost | `HomeScreen` meneruskan ID laporan ke `AppNavigation`. `LostItemRepository.get` dan `LostItemsViewModel.loadDetail` memuat detail langsung. `LostItemsScreen` memakai filter Semua Laporan dan Laporan Saya. Kedua jenis kartu menampilkan Milik Saya berdasarkan ID akun. |
| Found dan Klaim | `FoundItemDetailScreen` membuka route sementara `claims/{foundItemId}` bagi akun lain pada laporan terbuka. Validasi kelayakan global masih menunggu kontrak backend yang aman. Tidak ada pengiriman klaim atau notifikasi keberhasilan. |
| Temuan dan notifikasi | `RecoveryDraftScreen` menampilkan laporan asal, isian temuan dan pilihan foto galeri tanpa upload. Ditemukan memiliki bagian Konfirmasi Temuan yang terpisah dari daftar publik. Beranda membuka halaman awal pusat notifikasi. |

## Rancangan wajib untuk ditinjau

- **Profil.** Tambahkan `affiliation_type`, `faculty_or_unit` dan `entry_year` secara nullable. Batasi jenis ke mahasiswa, dosen, tenaga kependidikan, atau petugas kampus. Pada penyelesaian profil, wajibkan kampus, unit/fakultas dan tahun 4 digit bagi mahasiswa. Validasi batas tahun yang masuk akal pada server dan aplikasi. Tahun masuk diabaikan bagi jenis lain. Perlakukan afiliasi sebagai informasi privat yang dilaporkan pengguna. Ubah trigger registrasi agar menginisialisasi email kontak dari email Auth untuk akun baru, dengan berbagi kontak nonaktif. Pertahankan email kontak lama termasuk nilai kosong yang sengaja dipilih dan sediakan pelengkapan bertahap. Batasi aksi menulis setelah backend dapat membedakan profil tidak lengkap dari kegagalan pemuatan.
- **Temuan privat.** Buat `recovery_requests` dengan ID, FK `lost_item_id` dengan penghapusan RESTRICT, FK pengaju, snapshot judul untuk riwayat, lokasi/waktu temuan, kondisi, pengelola, lokasi pengambilan, catatan, kunci objek foto, status, serta waktu pembuatan/keputusan/penyelesaian. Gunakan indeks parsial unik per laporan dan pengaju untuk status pending atau confirmed, dan per laporan untuk confirmed. RLS baca hanya untuk kedua peserta. Tolak mutasi langsung. RPC tervalidasi memakai identitas sesi dan mengunci baris laporan sebelum membuat atau memutuskan pengajuan. Pengajuan hanya pada Lost open milik orang lain. Pemilik dapat menolak pending, mengonfirmasi satu pending sambil menolak pesaing, membatalkan serah terima gagal dengan alasan dan hasil rejected, serta menandai completed setelah penerimaan. Completion sekaligus mengubah Lost menjadi found. Gunakan trigger untuk mengunci mutasi biasa selama pending/confirmed dan arsip yang mempertahankan histori.
- **Media.** Buat bucket privat tersendiri dengan tipe/ukuran dibatasi, jalur per pengaju dan ID pengajuan, serta policy yang membatasi baca ke peserta. Unggah ke jalur sementara milik pengaju, validasi bukti di RPC sebelum ditautkan, batasi penggantian/penghapusan setelah ditautkan dan bersihkan unggahan yatim. Signed URL berumur pendek diterbitkan setelah otorisasi.
- **Notifikasi.** Gunakan tabel `notifications` berisi ID, penerima, jenis, referensi objek, pesan, waktu pembuatan dan waktu dibaca. Beri hak baca hanya ke penerima dan RPC untuk set waktu dibaca. Buat notifikasi dalam transaksi perubahan status dan gunakan kunci unik berdasarkan penerima dan ID kejadian agar retry idempoten. Pending memberi tahu pelapor, confirmed/rejected memberi tahu pengaju, dan completed memberi tahu kedua pihak. deep link memeriksa kembali akses, pesan tidak membawa bukti atau kontak. Modul Claims dapat memakai mekanisme yang sama setelah kontrak disetujui. Gunakan RPC kelayakan klaim berbentuk boolean untuk menyembunyikan tombol ketika laporan telah punya klaim approved atau akun sudah pernah mengajukan tanpa membuka bukti klaim ke pihak lain.

Katalog fakultas lintas kampus, Realtime dan push notification bersifat opsional. Tahap ini hanya menggunakan kueri baca di produksi. Migrasi dan integrasi permanen menunggu persetujuan rancangan.

## Verifikasi

Jalankan skenario akun A sebagai pelapor, B sebagai pengaju, dan C sebagai pesaing serta pihak luar. Uji akses REST dan Storage, penolakan akses pihak luar, retry, benturan konfirmasi, efek transaksi notifikasi, keputusan gagal, status selesai, dan bahwa pengajuan tidak menambah laporan Found publik. Uji navigasi, pengembalian dari form profil dan filter kepemilikan di perangkat. Dua puluh cek regresi database lama lulus. Build Android terhenti saat Gradle 8.11.1 gagal diunduh.
