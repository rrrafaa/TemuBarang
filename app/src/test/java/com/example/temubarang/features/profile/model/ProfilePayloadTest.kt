package com.example.temubarang.features.profile.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class ProfilePayloadTest {
    @Test fun disablingSharingIsExplicitInPayload() {
        val payload = Json.parseToJsonElement(Json.encodeToString(Profile("id", "Rafa", "Kampus A", "", "", false))).toString()
        assertTrue(payload.contains("\"share_contact\":false"))
        assertTrue(payload.contains("\"contact_email\":\"\""))
        assertTrue(payload.contains("\"phone\":\"\""))
    }
}
