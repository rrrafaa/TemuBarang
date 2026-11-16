package com.example.temubarang.features.lost.model

import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LostFormValidatorTest {
    private val validDate = LocalDate.now().minusDays(1).toString()

    @Test fun validInput() {
        assertNull(LostFormValidator.validate("Dompet hitam", "Dompet kulit warna hitam", "Kantin kampus", validDate))
    }
    @Test fun titleTooShort() {
        assertTrue(LostFormValidator.validate("AB", "Deskripsi cukup panjang", "Kantin", validDate)!!.contains("Nama"))
    }
    @Test fun descriptionTooShort() {
        assertTrue(LostFormValidator.validate("Dompet", "Pendek", "Kantin", validDate)!!.contains("Deskripsi"))
    }
    @Test fun emptyLocation() {
        assertTrue(LostFormValidator.validate("Dompet", "Deskripsi cukup panjang", " ", validDate)!!.contains("Lokasi"))
    }
    @Test fun invalidDate() {
        assertTrue(LostFormValidator.validate("Dompet", "Deskripsi cukup panjang", "Kantin", "31-10-2026")!!.contains("YYYY"))
    }
    @Test fun futureDate() {
        val future = LocalDate.now().plusDays(1).toString()
        assertTrue(LostFormValidator.validate("Dompet", "Deskripsi cukup panjang", "Kantin", future)!!.contains("masa depan"))
    }
}