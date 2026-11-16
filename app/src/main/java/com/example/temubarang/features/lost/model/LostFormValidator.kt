package com.example.temubarang.features.lost.model

import java.time.LocalDate
import java.time.format.DateTimeParseException

/** Validator tanpa dependensi Android, sehingga bisa diuji dengan local unit test. */
object LostFormValidator {
    fun validate(title: String, description: String, location: String, date: String): String? {
        if (title.trim().length !in 3..120) return "Nama barang harus 3 sampai 120 karakter."
        if (description.trim().length < 10) return "Deskripsi minimal 10 karakter."
        if (location.isBlank()) return "Lokasi terakhir terlihat wajib diisi."
        if (location.trim().length > 200) return "Lokasi terlalu panjang (maksimal 200 karakter)."
        val parsedDate = try {
            LocalDate.parse(date.trim())
        } catch (_: DateTimeParseException) {
            return "Tanggal harus berformat YYYY-MM-DD, contoh 2026-11-11."
        }
        if (parsedDate.isAfter(LocalDate.now())) return "Tanggal kehilangan tidak boleh di masa depan."
        return null
    }

    fun validatePhoto(hasPhoto: Boolean): String? =
        if (hasPhoto) null else "Foto barang wajib diunggah."
}