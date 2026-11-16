package com.example.temubarang.features.lost.model

data class LostUiState(
    val items: List<LostItem> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val notice: String? = null
)