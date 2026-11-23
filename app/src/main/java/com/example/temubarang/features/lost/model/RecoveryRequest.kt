package com.example.temubarang.features.lost.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecoverySubmission(
    @SerialName("p_id") val id: String,
    @SerialName("p_lost_item_id") val lostItemId: String,
    @SerialName("p_found_location") val location: String,
    @SerialName("p_found_at") val foundAt: String,
    @SerialName("p_item_condition") val condition: String,
    @SerialName("p_custodian") val custodian: String,
    @SerialName("p_pickup_location") val pickup: String,
    @SerialName("p_notes") val notes: String,
    @SerialName("p_has_photo") val hasPhoto: Boolean
)

@Serializable
data class RecoveryRequest(
    val id: String,
    @SerialName("item_title") val title: String,
    @SerialName("finder_id") val finderId: String,
    @SerialName("reporter_id") val reporterId: String,
    @SerialName("found_location") val location: String,
    @SerialName("found_at") val foundAt: String,
    @SerialName("item_condition") val condition: String,
    val custodian: String,
    @SerialName("pickup_location") val pickup: String,
    val notes: String,
    val status: String
) {
    val statusLabel: String get() = when (status) {
        "pending" -> "Menunggu pemeriksaan"
        "confirmed" -> "Menunggu serah terima"
        "rejected" -> "Ditolak atau dibatalkan"
        "completed" -> "Dikembalikan"
        else -> status
    }
}
