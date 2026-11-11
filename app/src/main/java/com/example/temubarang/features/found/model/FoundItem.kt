package com.example.temubarang.features.found.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Sesuai kolom tabel public.found_items, tidak perlu mengubah schema.sql. */
@Serializable
data class FoundItem(
    val id: String,
    @SerialName("reporter_id") val reporterId: String,
    val title: String,
    val description: String,
    @SerialName("found_location") val foundLocation: String,
    @SerialName("found_at") val foundAt: String,
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
    @SerialName("image_path") val imagePath: String? = null
)

@Serializable
data class FoundItemChanges(
    val title: String,
    val description: String,
    @SerialName("found_location") val foundLocation: String,
    @SerialName("found_at") val foundAt: String,
    val status: String
)

fun String.foundStatusLabel(): String = when (this) {
    "open" -> "Masih tersedia"
    "returned" -> "Sudah dikembalikan"
    "closed" -> "Laporan ditutup"
    else -> this
}
