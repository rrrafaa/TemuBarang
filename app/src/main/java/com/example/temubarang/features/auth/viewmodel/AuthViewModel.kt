package com.example.temubarang.features.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.temubarang.core.supabase.SupabaseProvider
import com.example.temubarang.features.auth.model.AuthErrorMapper
import com.example.temubarang.features.auth.model.AuthUiState
import com.example.temubarang.features.auth.model.AuthValidator
import com.example.temubarang.features.auth.repository.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** UI membaca StateFlow dan memanggil aksi. Semua request berada di Repository. */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.sessionStatus.collect { status ->
                    _uiState.update { current ->
                        when (status) {
                            SessionStatus.Initializing -> current.copy(isInitializing = true)
                            is SessionStatus.Authenticated -> current.copy(
                                isInitializing = false,
                                isAuthenticated = true,
                                registrationComplete = false,
                                error = null
                            )
                            is SessionStatus.NotAuthenticated -> current.copy(
                                isInitializing = false, isAuthenticated = false
                            )
                            is SessionStatus.RefreshFailure -> current.copy(
                                isInitializing = false, isAuthenticated = false,
                                error = "Sesi berakhir atau gagal diperbarui. Periksa internet lalu login lagi."
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isInitializing = false, isAuthenticated = false, error = AuthErrorMapper.describe(e))
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(notice = null, error = null) }
    }

    /** Dipanggil setelah UI otomatis beralih dari Register ke Login. */
    fun acknowledgeRegistration() {
        _uiState.update { it.copy(registrationComplete = false) }
    }

    fun login(email: String, password: String) {
        if (_uiState.value.isSubmitting) return
        val cleanEmail = email.trim()
        val validation = AuthValidator.validateLogin(cleanEmail, password)
        if (validation != null) {
            _uiState.update { it.copy(error = validation, notice = null) }
            return
        }
        perform { repository.signIn(cleanEmail, password) }
    }

    fun register(name: String, email: String, password: String, confirmation: String) {
        if (_uiState.value.isSubmitting) return
        val validation = AuthValidator.validateRegister(name, email, password, confirmation)
        if (validation != null) {
            _uiState.update { it.copy(error = validation, notice = null) }
            return
        }
        perform(onSuccess = {
            _uiState.update {
                it.copy(
                    registrationComplete = true,
                    notice = "Pendaftaran diterima. Jika verifikasi email diaktifkan, periksa email dan folder spam sebelum login."
                )
            }
        }) { repository.signUp(name.trim(), email.trim(), password) }
    }

    fun logout() {
        perform { repository.signOut() }
    }

    private fun perform(onSuccess: (() -> Unit)? = null, action: suspend () -> Unit) {
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null, notice = null) }
            try {
                action()
                onSuccess?.invoke()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = AuthErrorMapper.describe(e)) }
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(AuthViewModel::class.java))
                return AuthViewModel(AuthRepository(SupabaseProvider.client)) as T
            }
        }
    }
}
