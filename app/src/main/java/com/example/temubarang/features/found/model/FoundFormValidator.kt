package com.example.temubarang.features.found.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Validator tanpa dependensi Android, dapat diuji dengan local unit test. */
object FoundFormValidator {
    fun validate(title: String, description: String, location: String, date: String): String? {
        if (title.trim().length !in 3..120) return "Nama barang harus 3–120 karakter."
        if (description.trim().length < 10) return "Deskripsi minimal 10 karakter."
        if (location.isBlank()) return "Lokasi penemuan wajib diisi."
        if (location.trim().length > 200) return "Lokasi terlalu panjang (maksimal 200 karakter)."
        val parsedDate = try {
            LocalDate.parse(date.trim())
        } catch (_: DateTimeParseException) {
            return "Tanggal harus menggunakan format YYYY-MM-DD, contoh 2026-11-11."
        }
        if (parsedDate.isAfter(LocalDate.now())) return "Tanggal ditemukan tidak boleh di masa depan."
        return null
    }
}
