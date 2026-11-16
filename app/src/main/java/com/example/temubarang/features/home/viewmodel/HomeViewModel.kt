package com.example.temubarang.features.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.home.model.ReportType
import com.example.temubarang.features.home.model.HomeUiState
import com.example.temubarang.features.home.model.buildHomeFeed
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.example.temubarang.features.home.repository.HomeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    fun photoUrl(path: String?): String? = repository.photoUrl(path)

    fun setFilter(type: ReportType?) = _uiState.update { it.copy(filter = type) }

    private var refreshJob: Job? = null
    private var refreshVersion = 0L

    fun refresh() {
        val version = ++refreshVersion
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = coroutineScope {
                    val lost = async { repository.listLostItems() }
                    val found = async { repository.listFoundItems() }
                    buildHomeFeed(lost.await(), found.await())
                }
                _uiState.update { it.copy(items = items) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("TemuBarangHome", "Gagal memuat feed laporan", e)
                _uiState.update { it.copy(error =
                    "Gagal memuat laporan hilang dan ditemukan. Periksa koneksi, sesi login, dan izin akses database."
                ) }
            } finally {
                if (version == refreshVersion) _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(HomeViewModel::class.java))
                return HomeViewModel(HomeRepository(SupabaseProvider.client)) as T
            }
        }
    }
}
