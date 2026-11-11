package com.example.temubarang.features.found.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.found.model.FoundErrorMapper
import com.example.temubarang.features.found.model.FoundFormValidator
import com.example.temubarang.features.found.model.FoundUiState
import com.example.temubarang.features.found.repository.FoundItemRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoundItemsViewModel(private val repository: FoundItemRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FoundUiState())
    val uiState: StateFlow<FoundUiState> = _uiState.asStateFlow()

    val userId: String? get() = repository.signedInUserId()
    fun photoUrl(path: String?): String? = repository.publicPhotoUrl(path)
    fun clearMessage() = _uiState.update { it.copy(error = null, notice = null) }

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repository.list()
                _uiState.update { it.copy(items = result) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun load(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, detail = null, error = null) }
            try {
                val item = repository.get(id)
                _uiState.update { it.copy(detail = item) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                _uiState.update { it.copy(isLoadingDetail = false) }
            }
        }
    }

    fun create(
        title: String, description: String, location: String, date: String, photo: Uri?,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        val validation = FoundFormValidator.validate(title, description, location, date)
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
            try {
                repository.create(title, description, location, date, photo)
                _uiState.update { it.copy(notice = "Laporan berhasil ditambahkan.") }
                onSuccess()
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun edit(
        id: String, title: String, description: String, location: String,
        date: String, status: String, onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        val validation = FoundFormValidator.validate(title, description, location, date)
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }
        if (status !in listOf("open", "returned", "closed")) {
            _uiState.update { it.copy(error = "Status laporan tidak valid.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
            try {
                repository.update(id, title, description, location, date, status)
                _uiState.update { state ->
                    state.copy(
                        notice = "Laporan berhasil diperbarui.",
                        detail = state.detail?.takeIf { it.id == id }?.copy(
                            title = title.trim(), description = description.trim(),
                            foundLocation = location.trim(), foundAt = date.trim(), status = status
                        ) ?: state.detail
                    )
                }
                onSuccess()
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun showError(e: Exception) {
        Log.e("TemuBarangFound", "Operasi laporan barang ditemukan gagal", e)
        // UI mendapat pesan singkat; stack trace tetap tersedia pada Logcat.
        val message = if (e is IllegalArgumentException || e is IllegalStateException) {
            e.message ?: "Data belum valid."
        } else FoundErrorMapper.describe(e)
        _uiState.update { it.copy(error = message) }
    }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(FoundItemsViewModel::class.java))
                return FoundItemsViewModel(
                    FoundItemRepository(SupabaseProvider.client, app.contentResolver)
                ) as T
            }
        }
    }
}
