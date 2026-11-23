package com.example.temubarang.ui.validation

import com.example.temubarang.features.found.model.FoundHandover
import com.example.temubarang.features.found.model.custodyTypes
import com.example.temubarang.features.found.model.foundCategories
import java.time.LocalDate

/** Error per field dengan urutan sesuai form. Aturan server tetap dijalankan ViewModel. */
object ReportValidation {
    private fun common(
        title: String, description: String, location: String, date: String
    ): LinkedHashMap<String, String> {
        val errors = linkedMapOf<String, String>()
        when {
            title.isBlank() -> errors["title"] = "Nama barang wajib diisi."
            title.trim().length !in 3..120 -> errors["title"] = "Nama barang harus 3–120 karakter."
        }
        when {
            description.isBlank() -> errors["description"] = "Deskripsi barang wajib diisi."
            description.trim().length < 10 -> errors["description"] = "Deskripsi minimal 10 karakter."
        }
        when {
            location.isBlank() -> errors["location"] = "Lokasi barang wajib diisi."
            location.trim().length > 200 -> errors["location"] = "Lokasi maksimal 200 karakter."
        }
        val parsed = runCatching { LocalDate.parse(date.trim()) }.getOrNull()
        when {
            date.isBlank() -> errors["date"] = "Tanggal wajib dipilih."
            parsed == null -> errors["date"] = "Tanggal tidak valid. Pilih melalui kalender."
            parsed.isAfter(LocalDate.now()) -> errors["date"] = "Tanggal tidak boleh di masa depan."
        }
        return errors
    }

    fun lost(
        title: String, description: String, location: String, date: String,
        hasPhoto: Boolean
    ): Map<String, String> = common(title, description, location, date).apply {
        if (!hasPhoto) this["photo"] = "Silakan tambahkan foto barang."
    }

    fun found(
        title: String, description: String, location: String, date: String,
        handover: FoundHandover, status: String, hasPhoto: Boolean = true
    ): Map<String, String> {
        val errors = common(title, description, location, date).apply {
        if (handover.category !in foundCategories) this["category"] = "Silakan pilih kategori barang."
        if (handover.campus.isBlank()) this["campus"] = "Kampus wajib diisi."
        else if (handover.campus.trim().length !in 2..120) {
            this["campus"] = "Nama kampus harus 2–120 karakter."
        }
        if (handover.custodyType !in custodyTypes) this["custody"] = "Pilih tempat penyimpanan barang."
        if (handover.pickupLocation.isBlank()) this["pickup"] = "Lokasi pengambilan wajib diisi."
        else if (handover.pickupLocation.trim().length !in 3..200) {
            this["pickup"] = "Lokasi pengambilan harus 3–200 karakter."
        }
        if (handover.pickupNotes.trim().length > 1000) this["notes"] = "Petunjuk maksimal 1.000 karakter."
        if (status !in setOf("open", "returned", "closed")) this["status"] = "Status laporan tidak valid."
        if (!hasPhoto) this["photo"] = "Silakan tambahkan foto barang."
        }
        // Urutan error mengikuti posisi field pada form, bukan urutan penyusunan aturan.
        val order = listOf("title", "description", "category", "location", "date",
            "campus", "custody", "pickup", "notes", "status", "photo")
        return errors.entries.sortedBy { order.indexOf(it.key) }
            .associateTo(linkedMapOf()) { it.key to it.value }
    }
}
