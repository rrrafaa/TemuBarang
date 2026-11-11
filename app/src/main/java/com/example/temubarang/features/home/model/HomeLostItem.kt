package com.example.temubarang.features.home.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Snapshot read-only tabel lost_items untuk feed Beranda. Fitur CRUD tetap milik anggota 1. */
@Serializable
data class HomeLostItem(
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

data class HomeUiState(
    val lostItems: List<HomeLostItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
