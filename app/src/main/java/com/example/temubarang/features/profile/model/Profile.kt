package com.example.temubarang.features.profile.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val campus: String,
    @SerialName("contact_email") val contactEmail: String,
    val phone: String,
    @SerialName("share_contact") val shareContact: Boolean
)

/** RPC menyaring kontak di server, bukan hanya menyembunyikannya di UI. */
@Serializable
data class ReporterProfile(
    @SerialName("display_name") val displayName: String,
    val campus: String,
    @SerialName("contact_email") val contactEmail: String?,
    val phone: String?
)

object ProfileValidator {
    fun validateFields(
        name: String, campus: String, email: String, phone: String, share: Boolean
    ): Map<String, String> = linkedMapOf<String, String>().apply {
        if (name.isBlank()) this["name"] = "Nama tampilan wajib diisi."
        else if (name.trim().length !in 2..80) this["name"] = "Nama harus 2–80 karakter."
        if (campus.isBlank()) this["campus"] = "Kampus wajib diisi."
        else if (campus.trim().length !in 2..120) this["campus"] = "Kampus harus 2–120 karakter."
        if (email.isNotBlank() && (email.trim().length > 254 ||
                !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()))) {
            this["email"] = "Alamat email kontak tidak valid."
        }
        if (phone.isNotBlank() && !Regex("^\\+?[0-9]{8,15}$").matches(phone.trim())) {
            this["phone"] = "Nomor telepon 8–15 digit, boleh diawali +."
        }
        if (share && email.isBlank() && phone.isBlank()) {
            this["share"] = "Isi minimal satu kontak sebelum membagikan kontak."
        }
    }

    fun validate(name: String, campus: String, email: String, phone: String, share: Boolean): String? {
        if (name.trim().length !in 2..80) return "Nama tampilan harus 2–80 karakter."
        if (campus.trim().length !in 2..120) return "Nama kampus harus 2–120 karakter."
        if (email.isNotBlank() && (email.trim().length > 254 ||
            !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())))
            return "Alamat email kontak tidak valid."
        if (phone.isNotBlank() && !Regex("^\\+?[0-9]{8,15}$").matches(phone.trim()))
            return "Nomor telepon harus 8–15 digit, boleh diawali + (contoh +628123456789)."
        if (share && email.isBlank() && phone.isBlank()) return "Isi minimal satu kontak sebelum mengaktifkan berbagi kontak."
        return null
    }
}
