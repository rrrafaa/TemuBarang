package com.example.temubarang.features.found.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.found.model.FoundHandover
import com.example.temubarang.features.profile.repository.ProfileRepository
import com.example.temubarang.features.found.model.FoundErrorMapper
import com.example.temubarang.features.found.model.FoundFormValidator
import com.example.temubarang.features.found.model.FoundUiState
import com.example.temubarang.features.found.repository.FoundItemRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FoundItemsViewModel(private val repository: FoundItemRepository, private val profiles: ProfileRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(FoundUiState())
    val uiState: StateFlow<FoundUiState> = _uiState.asStateFlow()

    val userId: String? get() = repository.signedInUserId()
    fun photoUrl(path: String?): String? = repository.publicPhotoUrl(path)
    fun clearMessage() = _uiState.update { it.copy(error = null, notice = null) }

    private var refreshJob: Job? = null
    private var refreshVersion = 0L

    fun refresh() {
        val version = ++refreshVersion
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = repository.list()
                _uiState.update { it.copy(items = result) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                if (version == refreshVersion) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private var detailJob: Job? = null
    private var detailVersion = 0L
    fun load(id: String) {
        val version = ++detailVersion
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true, detail = null, error = null, reporter = null, reporterError = null, reporterLoading = true) }
            try {
                val item = repository.get(id)
                _uiState.update { it.copy(detail = item, isLoadingDetail = false) }
                try {
                    val reporter = profiles.reporter(id)
                    _uiState.update { it.copy(reporter = reporter) }
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) {
                    _uiState.update { it.copy(reporterError = "Profil pelapor belum dapat dimuat. Coba muat ulang detail.") }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showError(e)
            } finally {
                if (version == detailVersion) _uiState.update { it.copy(isLoadingDetail = false, reporterLoading = false) }
            }
        }
    }

    fun create(
        title: String, description: String, location: String, date: String, photo: Uri?, handover: FoundHandover,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        val validation = FoundFormValidator.validate(title, description, location, date)
            ?: handover.validate()
            ?: FoundFormValidator.validatePhoto(photo != null)
        if (validation != null) {
            _uiState.update { it.copy(error = validation) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
            try {
                val created = repository.create(title, description, location, date, photo, handover)
                _uiState.update { state -> state.copy(
                    items = (listOf(created) + state.items).distinctBy { it.id },
                    notice = "Laporan berhasil ditambahkan.", revision = state.revision + 1
                ) }
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
        date: String, status: String, handover: FoundHandover, onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        val validation = FoundFormValidator.validate(title, description, location, date) ?: handover.validate()
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
                val updated = repository.update(id, title, description, location, date, status, handover)
                _uiState.update { state -> state.copy(
                    items = state.items.map { if (it.id == id) updated else it },
                    detail = updated,
                    notice = "Laporan berhasil diperbarui.", revision = state.revision + 1
                ) }
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

    fun delete(id: String, onSuccess: () -> Unit) {
        if (_uiState.value.isSaving) return
        val item = _uiState.value.detail?.takeIf { it.id == id }
        if (item == null || item.reporterId != userId) {
            _uiState.update { it.copy(error = "Hanya pelapor yang dapat menghapus laporan ini.") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
        viewModelScope.launch {
            try {
                val warning = repository.delete(id)
                _uiState.update { state -> state.copy(
                    revision = state.revision + 1,
                    items = state.items.filterNot { it.id == id }, detail = null,
                    notice = warning ?: "Laporan berhasil dihapus."
                ) }
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
                    FoundItemRepository(SupabaseProvider.client, app.contentResolver), ProfileRepository(SupabaseProvider.client)
                ) as T
            }
        }
    }
}
