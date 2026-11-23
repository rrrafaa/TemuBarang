package com.example.temubarang.features.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.found.model.FoundErrorMapper
import com.example.temubarang.features.profile.model.Profile
import com.example.temubarang.features.profile.model.ProfileValidator
import com.example.temubarang.features.profile.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: Profile? = null, val loading: Boolean = true, val saving: Boolean = false,
    val error: String? = null, val notice: String? = null
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()
    val accountEmail: String? get() = repository.accountEmail()
    init { load() }

    fun consumeSaveNotice() = _state.update { it.copy(notice = null) }

    fun load() {
        if (_state.value.saving) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val profile = repository.load()
                _state.update { it.copy(profile = profile) }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _state.update { it.copy(error = FoundErrorMapper.describe(e)) }
            } finally { _state.update { it.copy(loading = false) } }
        }
    }

    fun save(name: String, campus: String, email: String, phone: String, share: Boolean) {
        val current = _state.value
        if (current.saving || current.loading || current.profile == null) return
        val error = ProfileValidator.validate(name, campus, email, phone, share)
        if (error != null) { _state.update { it.copy(error = error, notice = null) }; return }
        _state.update { it.copy(saving = true, error = null, notice = null) }
        viewModelScope.launch {
            try {
                val saved = repository.save(Profile(current.profile.id, name.trim(), campus.trim(), email.trim(), phone.trim(), share))
                _state.update { it.copy(profile = saved, notice = "Profil berhasil disimpan.") }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _state.update { it.copy(error = FoundErrorMapper.describe(e)) }
            } finally { _state.update { it.copy(saving = false) } }
        }
    }

    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
                return ProfileViewModel(ProfileRepository(SupabaseProvider.client)) as T
            }
        }
    }
}
