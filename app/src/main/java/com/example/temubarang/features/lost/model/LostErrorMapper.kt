package com.example.temubarang.features.lost.model

import java.io.IOException

object LostErrorMapper {
    fun describe(error: Exception): String {
        val chain = generateSequence<Throwable>(error) { it.cause }
        if (chain.any { it is IOException }) {
            return "Tidak bisa menghubungi server. Periksa koneksi internet, lalu coba lagi."
        }
        val all = chain.mapNotNull { it.message }.joinToString(" ").lowercase()
        return when {
            "jwt" in all || "401" in all || "unauthorized" in all ->
                "Sesi login tidak berlaku. Silakan login kembali."
            "row level security" in all || "42501" in all || "permission denied" in all || "403" in all ->
                "Akses ditolak oleh database. Pastikan kamu sudah login dan hanya mengubah laporan milik sendiri."
            "23514" in all || "check constraint" in all ->
                "Data tidak memenuhi aturan database. Periksa nama, deskripsi, tanggal, dan status."
            "23502" in all || "not-null" in all || "null value" in all ->
                "Ada data wajib yang belum terisi. Periksa kembali form laporan."
            "413" in all || "payload too large" in all || "file size" in all ->
                "Ukuran foto melebihi batas Storage (maksimal 5 MB)."
            "bucket" in all && ("not found" in all || "404" in all) ->
                "Bucket temubarang-images belum ditemukan. Periksa Supabase Storage."
            "timeout" in all -> "Server tidak merespons tepat waktu. Coba lagi."
            else -> "Permintaan gagal. Periksa Logcat atau Logs di Supabase."
        }
    }
}