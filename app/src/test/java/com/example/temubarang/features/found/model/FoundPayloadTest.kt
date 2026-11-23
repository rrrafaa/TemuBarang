package com.example.temubarang.features.found.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class FoundPayloadTest {
    @Test fun clearingPickupNotesAndResettingCategoryAreExplicit() {
        val changes = FoundItemChanges("Kunci", "Kunci motor merah", "Kantin", "2026-11-10", "Kampus A", "other", "reporter", "Pos keamanan", "", "open")
        val payload = Json.encodeToString(changes)
        assertTrue(payload.contains("\"pickup_notes\":\"\""))
        assertTrue(payload.contains("\"category\":\"other\""))
        assertTrue(payload.contains("\"custody_type\":\"reporter\""))
    }
}
