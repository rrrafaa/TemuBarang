package com.example.temubarang.features.auth.model

/** State aplikasi, termasuk pesan error/sukses yang ditampilkan langsung di UI. */
data class AuthUiState(
    val isInitializing: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isSubmitting: Boolean = false,
    val registrationComplete: Boolean = false,
    val notice: String? = null,
    val error: String? = null
)
