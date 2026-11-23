package com.example.temubarang.features.home.model

import com.example.temubarang.features.found.model.FoundItem
import com.example.temubarang.features.found.model.foundStatusLabel
import java.time.Instant

enum class ReportType(val label: String) { LOST("Hilang"), FOUND("Ditemukan") }

data class HomeFeedItem(
    val id: String, val type: ReportType, val title: String, val location: String,
    val date: String, val statusLabel: String, val imagePath: String?, val createdAt: String?
) {
    val key: String get() = "${type.name}:$id"
}

fun buildHomeFeed(lost: List<HomeLostItem>, found: List<FoundItem>): List<HomeFeedItem> =
    (lost.map {
        HomeFeedItem(it.id, ReportType.LOST, it.title, it.lastSeenLocation, it.lostAt,
            when (it.status) {
                "open" -> "Masih dicari"
                "found" -> "Sudah ditemukan"
                "closed" -> "Laporan ditutup"
                else -> it.status
            }, it.imagePath, it.createdAt)
    } + found.map {
        HomeFeedItem(it.id, ReportType.FOUND, it.title, it.foundLocation, it.foundAt,
            it.status.foundStatusLabel(), it.imagePath, it.createdAt)
    }).sortedWith(compareByDescending<HomeFeedItem> {
        // Timestamp dibandingkan sebagai waktu, termasuk perbedaan offset zona waktu.
        runCatching { Instant.parse(it.createdAt) }.getOrDefault(Instant.MIN)
    }.thenBy { it.key })
