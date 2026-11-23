package com.example.temubarang.ui.validation

import com.example.temubarang.features.auth.model.AuthValidator
import com.example.temubarang.features.found.model.FoundHandover
import com.example.temubarang.features.profile.model.ProfileValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ReportValidationTest {
    private val yesterday = LocalDate.now().minusDays(1).toString()
    private val validHandover = FoundHandover(
        campus = "Kampus A", category = "keys", custodyType = "reporter",
        pickupLocation = "Pos keamanan", pickupNotes = ""
    )

    @Test fun lostRejectsBlankFieldsAndMissingPhoto() {
        val errors = ReportValidation.lost("", "", "", yesterday, hasPhoto = false)
        assertEquals("Nama barang wajib diisi.", errors["title"])
        assertEquals("Deskripsi barang wajib diisi.", errors["description"])
        assertEquals("Lokasi barang wajib diisi.", errors["location"])
        assertEquals("Silakan tambahkan foto barang.", errors["photo"])
        assertEquals("title", errors.keys.first())
    }

    @Test fun lostAcceptsValidReportAndPhoto() {
        val errors = ReportValidation.lost(
            "Kunci motor", "Gantungan kecil warna biru", "Perpustakaan",
            yesterday, hasPhoto = true
        )
        assertTrue(errors.isEmpty())
    }

    @Test fun foundRequiresCategoryAndPickup() {
        val errors = ReportValidation.found(
            "Kunci motor", "Gantungan kecil warna biru", "Gedung A", yesterday,
            validHandover.copy(category = "", pickupLocation = ""), "open"
        )
        assertEquals("Silakan pilih kategori barang.", errors["category"])
        assertEquals("Lokasi pengambilan wajib diisi.", errors["pickup"])
        assertEquals("category", errors.keys.first())
    }

    @Test fun foundKeepsOrderedErrorsAndAcceptsCompleteReport() {
        val errors = ReportValidation.found(
            "", "Gantungan kecil warna biru", "", yesterday,
            validHandover.copy(category = ""), "open"
        )
        assertEquals(listOf("title", "category", "location"), errors.keys.toList())
        assertTrue(ReportValidation.found(
            "Kunci motor", "Gantungan kecil warna biru", "Gedung A",
            yesterday, validHandover, "open"
        ).isEmpty())
    }

    @Test fun foundCreateRequiresPhotoAndKeepsOtherValidFields() {
        val errors = ReportValidation.found(
            "Kunci motor", "Gantungan kecil warna biru", "Gedung A",
            yesterday, validHandover, "open", hasPhoto = false
        )
        assertEquals(mapOf("photo" to "Silakan tambahkan foto barang."), errors)
        assertTrue(ReportValidation.found(
            "Kunci motor", "Gantungan kecil warna biru", "Gedung A",
            yesterday, validHandover, "open", hasPhoto = true
        ).isEmpty())
    }

    @Test fun authFieldsRejectInvalidInputBeforeNetwork() {
        val errors = AuthValidator.validateRegisterFields("A", "salah", "abc", "abc")
        assertEquals(listOf("name", "email", "password"), errors.keys.toList())
        assertFalse(AuthValidator.validateLoginFields("salah", "").isEmpty())
        assertTrue(AuthValidator.validateLoginFields("test@example.com", "validPassword").isEmpty())
    }

    @Test fun profileFieldsRequireContactWhenSharing() {
        val errors = ProfileValidator.validateFields("Rafa", "Kampus A", "", "", true)
        assertEquals("Isi minimal satu kontak sebelum membagikan kontak.", errors["share"])
        assertTrue(ProfileValidator.validateFields("Rafa", "Kampus A",
            "rafa@example.com", "", true).isEmpty())
    }
}
