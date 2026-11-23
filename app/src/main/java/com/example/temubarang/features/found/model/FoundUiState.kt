package com.example.temubarang.features.found.model

data class FoundUiState(
    val revision: Long = 0,
    val items: List<FoundItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSaving: Boolean = false,
    val detail: FoundItem? = null,
    val claimStatus: String? = null,
    val reporter: com.example.temubarang.features.profile.model.ReporterProfile? = null,
    val reporterLoading: Boolean = false,
    val reporterError: String? = null,
    val error: String? = null,
    val notice: String? = null
)

fun FoundUiState.canManage(itemId: String, userId: String?): Boolean =
    detail?.let { it.id == itemId && it.reporterId == userId && it.status == "open" } == true &&
        claimStatus == "none"

fun FoundUiState.canConfirmReturn(itemId: String, userId: String?): Boolean =
    detail?.let { it.id == itemId && it.reporterId == userId && it.status == "open" } == true &&
        claimStatus == "approved"
