package com.example.temubarang.features.found.model

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class FoundFormValidatorTest {
    private val validDate = LocalDate.now().minusDays(1).toString()
    @Test fun validInput() {
        assertNull(FoundFormValidator.validate("Dompet hitam", "Dompet kulit warna hitam", "Depan perpustakaan", validDate))
    }
    @Test fun titleTooShort() {
        assertTrue(FoundFormValidator.validate("AB", "Deskripsi cukup panjang", "Parkiran", validDate)!!.contains("Nama"))
    }
    @Test fun descriptionTooShort() {
        assertTrue(FoundFormValidator.validate("Dompet", "Pendek", "Parkiran", validDate)!!.contains("Deskripsi"))
    }
    @Test fun emptyLocation() {
        assertTrue(FoundFormValidator.validate("Dompet", "Deskripsi cukup panjang", " ", validDate)!!.contains("Lokasi"))
    }
    @Test fun invalidDate() {
        assertTrue(FoundFormValidator.validate("Dompet", "Deskripsi cukup panjang", "Parkiran", "31-10-2026")!!.contains("YYYY"))
    }
    @Test fun futureDate() {
        assertTrue(FoundFormValidator.validate("Dompet", "Deskripsi cukup panjang", "Parkiran", LocalDate.now().plusDays(1).toString())!!.contains("masa depan"))
    }
}
