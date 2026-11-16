package com.example.temubarang.features.profile.model

import org.junit.Assert.*
import org.junit.Test

class ProfileValidatorTest {
    @Test fun privateProfileNeedsNoContact() {
        assertNull(ProfileValidator.validate("Rafa", "Kampus A", "", "", false))
    }
    @Test fun sharedProfileNeedsContact() {
        assertNotNull(ProfileValidator.validate("Rafa", "Kampus A", "", "", true))
    }
    @Test fun acceptsEmailAndInternationalPhone() {
        assertNull(ProfileValidator.validate("Rafa", "Kampus A", "rafa@example.com", "+628123456789", true))
    }
    @Test fun rejectsMalformedEmailAndPhone() {
        assertNotNull(ProfileValidator.validate("Rafa", "Kampus A", "rafa@", "", true))
        assertNotNull(ProfileValidator.validate("Rafa", "Kampus A", "", "tel:123", true))
    }
    @Test fun rejectsBlankNameAndCampus() {
        assertNotNull(ProfileValidator.validate("  ", "Kampus A", "", "", false))
        assertNotNull(ProfileValidator.validate("Rafa", " ", "", "", false))
    }
}
