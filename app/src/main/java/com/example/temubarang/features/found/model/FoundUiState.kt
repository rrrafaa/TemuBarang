package com.example.temubarang.features.found.model

data class FoundUiState(
    val items: List<FoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSaving: Boolean = false,
    val detail: FoundItem? = null,
    val error: String? = null,
    val notice: String? = null
)
