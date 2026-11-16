package com.example.temubarang.features.lost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sesuai kolom tabel public.lost_items. Tidak mengubah schema database. */
@Serializable
data class LostItem(
    val id: String,
    @SerialName("reporter_id") val reporterId: String,
    val title: String,
    val description: String,
    @SerialName("last_seen_location") val lastSeenLocation: String,
    @SerialName("lost_at") val lostAt: String,
    @SerialName("image_path") val imagePath: String? = null,
    val status: String = "open",
    @SerialName("created_at") val createdAt: String? = null
)

/** Data yang dikirim saat Create. id, status, dan created_at diisi otomatis oleh database. */
@Serializable
data class NewLostItem(
    @SerialName("reporter_id") val reporterId: String,
    val title: String,
    val description: String,
    @SerialName("last_seen_location") val lastSeenLocation: String,
    @SerialName("lost_at") val lostAt: String,
    @SerialName("image_path") val imagePath: String? = null
)

/** Perubahan teks laporan tanpa mengganti foto. */
@Serializable
data class LostItemChanges(
    val title: String,
    val description: String,
    @SerialName("last_seen_location") val lastSeenLocation: String,
    @SerialName("lost_at") val lostAt: String
)

/** Perubahan teks laporan sekaligus mengganti foto. */
@Serializable
data class LostItemChangesWithPhoto(
    val title: String,
    val description: String,
    @SerialName("last_seen_location") val lastSeenLocation: String,
    @SerialName("lost_at") val lostAt: String,
    @SerialName("image_path") val imagePath: String
)

@Serializable
data class LostStatusChange(val status: String)

fun String.lostStatusLabel(): String = when (this) {
    "open" -> "Masih dicari"
    "found" -> "Sudah ditemukan"
    "closed" -> "Laporan ditutup"
    else -> this
}