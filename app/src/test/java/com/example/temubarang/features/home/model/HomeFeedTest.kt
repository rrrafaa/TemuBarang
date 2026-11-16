package com.example.temubarang.features.home.model

import com.example.temubarang.features.found.model.FoundItem
import org.junit.Assert.*
import org.junit.Test

class HomeFeedTest {
    private fun lost(id: String = "same", time: String? = null) = HomeLostItem(
        id, "owner", "Dompet hilang", "Hitam", "Kampus", "2026-11-15", createdAt = time
    )
    private fun found(id: String = "same", time: String? = null, status: String = "open") = FoundItem(
        id, "owner", "Kunci ditemukan", "Biru", "Perpustakaan", "2026-11-16",
        imagePath = "owner/found/photo.jpg", status = status, createdAt = time
    )

    @Test fun combinesBothTablesWithoutCollidingKeys() {
        val feed = buildHomeFeed(listOf(lost()), listOf(found()))
        assertEquals(2, feed.size)
        assertEquals(2, feed.map { it.key }.toSet().size)
        val item = feed.single { it.type == ReportType.FOUND }
        assertEquals("same", item.id)
        assertEquals("Perpustakaan", item.location)
        assertEquals("owner/found/photo.jpg", item.imagePath)
        assertEquals("Masih tersedia", item.statusLabel)
    }

    @Test fun sortsByActualTimeAcrossOffsetsAndPutsMissingTimeLast() {
        val feed = buildHomeFeed(
            listOf(lost(time = "2026-11-16T09:00:00Z"), lost("missing")),
            listOf(found(time = "2026-11-16T15:00:00+07:00"))
        )
        assertEquals(listOf(ReportType.LOST, ReportType.FOUND, ReportType.LOST), feed.map { it.type })
        assertEquals("missing", feed.last().id)
    }

    @Test fun filterKeepsFoundRowsAfterRefreshAndHandlesEmptyCategory() {
        val feed = buildHomeFeed(listOf(lost()), listOf(found()))
        val state = HomeUiState(items = feed, filter = ReportType.FOUND)
        assertEquals(1, state.visibleItems.size)
        assertEquals(ReportType.FOUND, state.visibleItems.single().type)
        assertTrue(state.copy(items = buildHomeFeed(listOf(lost()), emptyList())).visibleItems.isEmpty())
        assertEquals(2, state.copy(filter = null).visibleItems.size)
    }

    @Test fun handlesEmptyAndInvalidDatesAndReturnedStatus() {
        assertTrue(buildHomeFeed(emptyList(), emptyList()).isEmpty())
        val item = buildHomeFeed(emptyList(), listOf(found(time = "invalid", status = "returned"))).single()
        assertEquals("Sudah dikembalikan", item.statusLabel)
    }
}
