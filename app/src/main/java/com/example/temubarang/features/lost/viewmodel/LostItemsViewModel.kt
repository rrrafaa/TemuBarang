package com.example.temubarang.features.lost.viewmodel

import android.net.Uri
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.lost.model.LostErrorMapper
import com.example.temubarang.features.lost.model.LostFormValidator
import com.example.temubarang.features.lost.model.LostUiState
import com.example.temubarang.features.lost.repository.LostItemRepository
import com.example.temubarang.features.lost.model.RecoveryRequest
import com.example.temubarang.features.lost.model.RecoverySubmission
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LostItemsViewModel(private val repository: LostItemRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LostUiState())
    val uiState: StateFlow<LostUiState> = _uiState.asStateFlow()

    data class RecoveryState(
        val items: List<RecoveryRequest> = emptyList(), val busy: Boolean = false, val error: String? = null, val notice: String? = null
    )

    private val _recovery = MutableStateFlow(RecoveryState())
    val recovery = _recovery.asStateFlow()
    val userId: String? get() = repository.signedInUserId()

    fun clearRecoveryMessage() = _recovery.update { it.copy(error = null, notice = null) }

    fun loadRecovery() = runRecovery {
        val requests = repository.listRecovery()
        _recovery.update { it.copy(items = requests) }
    }

    fun submitRecovery(submission: RecoverySubmission, photo: Uri?, onSuccess: () -> Unit) = runRecovery {
        repository.submitRecovery(submission, photo)
        _recovery.update { it.copy(notice = "Pengajuan temuan berhasil dikirim.") }
        onSuccess()
    }

    fun reviewRecovery(id: String, status: String) = runRecovery {
        repository.reviewRecovery(id, status)
        val requests = repository.listRecovery()
        _recovery.update { it.copy(items = requests) }
        refresh()
    }

    private fun runRecovery(action: suspend () -> Unit) {
        if (_recovery.value.busy) return
        _recovery.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = e.message.orEmpty()
                val error = when {
                    "23505" in message -> "Kamu sudah memiliki pengajuan aktif untuk laporan ini."
                    "23514" in message || "42501" in message -> "Laporan, waktu, atau tindakan ini tidak memenuhi syarat."
                    else -> "Konfirmasi temuan belum dapat diproses. Periksa koneksi dan kesiapan Supabase."
                }
                _recovery.update { it.copy(error = error) }
            } finally {
                _recovery.update { it.copy(busy = false) }
            }
        }
    }

    fun loadDetail(id: String) {
        viewModelScope.launch {
            try {
                val item = repository.get(id)
                _uiState.update { it.copy(items = it.items.filterNot { report -> report.id == id } + item) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = LostErrorMapper.describe(e)) }
            }
        }
    }

    fun ownsReport(id: String): Boolean {
        val userId = repository.signedInUserId() ?: return false
        return _uiState.value.items.any { it.id == id && it.reporterId == userId }
    }

    fun photoUrl(path: String?): String? = repository.publicPhotoUrl(path)

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = repository.list()
                _uiState.update { it.copy(items = items) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memuat laporan barang hilang", e)
                _uiState.update { it.copy(error = LostErrorMapper.describe(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /** Validasi dulu di ViewModel. Hanya bila lolos, data dikirim ke Repository. */
    fun create(
        title: String,
        description: String,
        location: String,
        date: String,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        val validationError = LostFormValidator.validate(title, description, location, date)
            ?: LostFormValidator.validatePhoto(imageUri != null)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError, notice = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
            try {
                val created = repository.create(title, description, location, date, imageUri)
                _uiState.update { s -> s.copy(
                    items = (listOf(created) + s.items).distinctBy { it.id },
                    notice = "Laporan barang hilang berhasil dikirim."
                ) }
                onSuccess()
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menyimpan laporan barang hilang", e)
                _uiState.update { it.copy(error = LostErrorMapper.describe(e)) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun update(
        id: String,
        title: String,
        description: String,
        location: String,
        date: String,
        newImageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isSaving) return
        if (!ownsReport(id)) return
        val current = _uiState.value.items.firstOrNull { it.id == id }
        val hasPhoto = newImageUri != null || !current?.imagePath.isNullOrBlank()
        val validationError = LostFormValidator.validate(title, description, location, date)
            ?: LostFormValidator.validatePhoto(hasPhoto)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError, notice = null) }
            return
        }
        runSaving(notice = "Laporan berhasil diperbarui.", onSuccess = onSuccess) {
            val saved = repository.update(id, title, description, location, date, newImageUri, current?.imagePath)
            _uiState.update { s -> s.copy(items = s.items.map { if (it.id == id) saved else it }) }
            refresh()
        }
    }

    fun updateStatus(id: String, status: String, onSuccess: () -> Unit = {}) {
        if (!ownsReport(id)) return
        runSaving(notice = "Status laporan diperbarui.", onSuccess = onSuccess) {
            val saved = repository.updateStatus(id, status)
            _uiState.update { s -> s.copy(items = s.items.map { if (it.id == id) saved else it }) }
            refresh()
        }
    }

    fun delete(id: String, onSuccess: () -> Unit) {
        if (!ownsReport(id)) return
        val imagePath = _uiState.value.items.firstOrNull { it.id == id }?.imagePath
        runSaving(notice = "Laporan berhasil dihapus.", onSuccess = onSuccess) {
            repository.delete(id, imagePath)
            _uiState.update { s -> s.copy(items = s.items.filterNot { it.id == id }) }
        }
    }

    private fun runSaving(notice: String, onSuccess: () -> Unit = {}, action: suspend () -> Unit) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, notice = null) }
            try {
                action()
                _uiState.update { it.copy(notice = notice) }
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Aksi laporan gagal", e)
                _uiState.update { it.copy(error = LostErrorMapper.describe(e)) }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, notice = null) }
    }

    companion object {
        private const val TAG = "TemuBarangLost"

        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass.isAssignableFrom(LostItemsViewModel::class.java))
                    return LostItemsViewModel(
                        LostItemRepository(SupabaseProvider.client, application.contentResolver)
                    ) as T
                }
            }
    }
}