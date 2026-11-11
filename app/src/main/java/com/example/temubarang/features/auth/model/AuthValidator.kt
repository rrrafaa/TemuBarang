package com.example.temubarang.features.auth.model

/** Validasi murni Kotlin yang bisa diuji tanpa jaringan dan tanpa perangkat Android. */
object AuthValidator {
    // Sederhana namun lebih tepat daripada hanya memeriksa keberadaan @.
    private val emailPattern = Regex("""^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$""")

    fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() -> "Email wajib diisi."
        !emailPattern.matches(email.trim()) -> "Format email tidak valid."
        password.isBlank() -> "Password wajib diisi."
        else -> null
    }

    fun validateRegister(name: String, email: String, password: String, confirmation: String): String? = when {
        name.trim().length < 2 -> "Nama lengkap minimal 2 karakter."
        email.isBlank() -> "Email wajib diisi."
        !emailPattern.matches(email.trim()) -> "Format email tidak valid."
        password.length < 8 -> "Password minimal 8 karakter."
        password != confirmation -> "Konfirmasi password tidak sama."
        else -> null
    }
}
