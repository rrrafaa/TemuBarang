package com.example.temubarang.features.found.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kolom tambahan mengikuti migrasi found + profil; data lama memiliki default. */
@Serializable
data class FoundItem(
    val id: String,
    @SerialName("reporter_id") val reporterId: String,
    val title: String,
    val description: String,
    @SerialName("found_location") val foundLocation: String,
    @SerialName("found_at") val foundAt: String,
    val campus: String = "",
    val category: String = "other",
    @SerialName("custody_type") val custodyType: String = "reporter",
    @SerialName("pickup_location") val pickupLocation: String = "",
    @SerialName("pickup_notes") val pickupNotes: String = "",
    @SerialName("image_path") val imagePath: String? = null,
    val status: String = "open",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class NewFoundItem(
    @SerialName("reporter_id") val reporterId: String,
    val title: String,
    val description: String,
    @SerialName("found_location") val foundLocation: String,
    @SerialName("found_at") val foundAt: String,
    val campus: String,
    val category: String,
    @SerialName("custody_type") val custodyType: String,
    @SerialName("pickup_location") val pickupLocation: String,
    @SerialName("pickup_notes") val pickupNotes: String,
    @SerialName("image_path") val imagePath: String? = null
)

@Serializable
data class FoundItemChanges(
    val title: String,
    val description: String,
    @SerialName("found_location") val foundLocation: String,
    @SerialName("found_at") val foundAt: String,
    val campus: String,
    val category: String,
    @SerialName("custody_type") val custodyType: String,
    @SerialName("pickup_location") val pickupLocation: String,
    @SerialName("pickup_notes") val pickupNotes: String,
    val status: String
)

fun String.foundStatusLabel(): String = when (this) {
    "open" -> "Masih tersedia"
    "returned" -> "Sudah dikembalikan"
    "closed" -> "Laporan ditutup"
    else -> this
}

/** Nilai kode sama dengan CHECK constraint PostgreSQL. */
val foundCategories = linkedMapOf("electronics" to "Elektronik", "documents" to "Dokumen", "keys" to "Kunci", "bags" to "Tas", "clothing" to "Pakaian", "other" to "Lainnya")
val custodyTypes = linkedMapOf("reporter" to "Disimpan pelapor", "security" to "Dititipkan ke keamanan", "lost_and_found" to "Pos barang temuan")

data class FoundHandover(
    val campus: String = "", val category: String = "other", val custodyType: String = "reporter",
    val pickupLocation: String = "", val pickupNotes: String = ""
) {
    fun validate(): String? = when {
        campus.trim().length !in 2..120 -> "Nama kampus harus 2–120 karakter."
        category !in foundCategories -> "Kategori barang tidak valid."
        custodyType !in custodyTypes -> "Pihak penyimpan tidak valid."
        pickupLocation.trim().length !in 3..200 -> "Lokasi pengambilan harus 3–200 karakter."
        pickupNotes.trim().length > 1000 -> "Petunjuk pengambilan maksimal 1.000 karakter."
        else -> null
    }
}

fun filterFoundItems(items: List<FoundItem>, query: String, onlyOpen: Boolean, onlyMine: Boolean, userId: String?): List<FoundItem> {
    val term = query.trim()
    return items.filter { item ->
        (!onlyOpen || item.status == "open") && (!onlyMine || (userId != null && item.reporterId == userId)) &&
            (term.isEmpty() || listOf(item.title, item.description, item.campus, item.foundLocation, item.pickupLocation)
                .any { it.contains(term, ignoreCase = true) })
    }
}
