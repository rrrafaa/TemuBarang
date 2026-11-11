package com.example.temubarang.features.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.home.model.HomeUiState
import com.example.temubarang.features.home.repository.HomeRepository
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

    fun refresh() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val items = repository.listLostItems()
                _uiState.update { it.copy(lostItems = items) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("TemuBarangHome", "Gagal memuat laporan barang hilang", e)
                _uiState.update { it.copy(error =
                    "Gagal memuat barang hilang. Periksa koneksi, sesi login, dan izin akses database."
                ) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
