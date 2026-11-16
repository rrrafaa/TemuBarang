package com.example.temubarang.features.found.model

import org.junit.Assert.*
import org.junit.Test

class FoundHandoverTest {
    private val valid = FoundHandover("Kampus A", "keys", "security", "Pos keamanan A", "Bawa bukti kepemilikan")
    @Test fun validatesCompleteHandover() { assertNull(valid.validate()) }
    @Test fun requiresConfirmedPickupLocation() { assertNotNull(valid.copy(pickupLocation = "  ").validate()) }
    @Test fun rejectsInvalidCategoryAndCustody() {
        assertNotNull(valid.copy(category = "unknown").validate())
        assertNotNull(valid.copy(custodyType = "unknown").validate())
    }
    @Test fun enforcesCampusAndNotesLimits() {
        assertNotNull(valid.copy(campus = "a").validate())
        assertNotNull(valid.copy(pickupNotes = "x".repeat(1001)).validate())
    }
    @Test fun searchCombinesOwnerAvailabilityAndCampus() {
        val mine = FoundItem("1", "me", "Kunci", "Kunci motor merah", "Kantin", "2026-11-10", campus = "Kampus A", pickupLocation = "Pos keamanan")
        val closed = mine.copy(id = "2", status = "returned")
        val other = mine.copy(id = "3", reporterId = "other")
        assertEquals(listOf(mine), filterFoundItems(listOf(mine, closed, other), " KAMPUS A ", true, true, "me"))
        assertTrue(filterFoundItems(listOf(mine), "", false, true, null).isEmpty())
        assertEquals(listOf(mine), filterFoundItems(listOf(mine), "keamanan", false, false, null))
    }
}
