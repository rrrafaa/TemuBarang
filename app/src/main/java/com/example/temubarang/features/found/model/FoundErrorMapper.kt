package com.example.temubarang.features.found.model

import java.io.IOException

/** Error server ditampilkan di UI secara ringkas tanpa mengekspos request/token. */
object FoundErrorMapper {
    fun describe(error: Exception): String {
        val all = generateSequence<Throwable>(error) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" ")
            .lowercase()
        if (generateSequence<Throwable>(error) { it.cause }.any { it is IOException }) {
            return "Tidak bisa menghubungi server. Periksa koneksi internet emulator/HP, lalu coba lagi."
        }
        return when {
            "pgrst202" in all || "pgrst205" in all || "42703" in all || "42p01" in all ->
                "Struktur database belum sesuai. Minta pengelola menerapkan migrasi Found dan Profil, lalu coba lagi."
            "jwt" in all || "401" in all || "unauthorized" in all ->
                "Sesi login tidak berlaku. Silakan login kembali."
            "row level security" in all || "42501" in all || "permission denied" in all || "403" in all ->
                "Akses ditolak Supabase (RLS). Pastikan kamu login dan mengubah laporan milik sendiri."
            "23503" in all || "foreign key" in all ->
                "Laporan masih terkait klaim. Selesaikan klaim atau tutup laporan melalui Edit sebelum menghapus."
            "klaim aktif" in all -> "Laporan terkunci karena terdapat klaim aktif. Muat ulang detail laporan."
            "laporan ini sudah selesai" in all -> "Laporan yang sudah selesai atau diarsipkan tidak dapat diubah."
            "23514" in all || "check constraint" in all ->
                "Data tidak memenuhi aturan database. Periksa nama, deskripsi, tanggal, dan status."
            "413" in all || "payload too large" in all || "file size" in all ->
                "Ukuran foto melebihi batas Storage (maksimal 5 MB)."
            "bucket" in all && ("not found" in all || "404" in all) ->
                "Bucket temubarang-images belum ditemukan. Periksa Supabase Storage."
            "timeout" in all -> "Server tidak merespons tepat waktu. Coba lagi."
            else -> "Permintaan gagal. Periksa Auth/Database/Storage Logs di Supabase atau Logcat."
        }
    }
}
