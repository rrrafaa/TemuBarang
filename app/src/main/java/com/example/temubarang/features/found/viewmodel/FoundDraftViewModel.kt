package com.example.temubarang.features.found.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * Draft laporan ditemukan tetap tersimpan ketika kamera eksternal membuka Activity baru,
 * Activity direkreasi, atau Android menghentikan proses aplikasi sementara.
 *
 * Hanya teks dan URI (String) yang disimpan di SavedStateHandle; byte foto tetap di file.
 * ViewModel ini di-scope ke MainActivity, BUKAN ke route form yang dapat dilepas.
 */
class FoundDraftViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val title: StateFlow<String> = savedStateHandle.getStateFlow("found_draft.title", "")
    val description: StateFlow<String> = savedStateHandle.getStateFlow("found_draft.description", "")
    val location: StateFlow<String> = savedStateHandle.getStateFlow("found_draft.location", "")
    val foundAt: StateFlow<String> = savedStateHandle.getStateFlow(
        "found_draft.found_at", LocalDate.now().toString()
    )
    val selectedPhotoUri: StateFlow<String?> =
        savedStateHandle.getStateFlow("found_draft.selected_photo", null)
    val pendingCameraUri: StateFlow<String?> =
        savedStateHandle.getStateFlow("found_draft.pending_camera", null)
    val photoMessage: StateFlow<String?> =
        savedStateHandle.getStateFlow("found_draft.photo_message", null)
    val returnToForm: StateFlow<Boolean> =
        savedStateHandle.getStateFlow("found_draft.return_to_form", false)

    fun changeTitle(value: String) { savedStateHandle["found_draft.title"] = value }
    fun changeDescription(value: String) { savedStateHandle["found_draft.description"] = value }
    fun changeLocation(value: String) { savedStateHandle["found_draft.location"] = value }
    fun changeFoundAt(value: String) { savedStateHandle["found_draft.found_at"] = value }

    fun beginCameraCapture(destinationUri: String) {
        savedStateHandle["found_draft.pending_camera"] = destinationUri
        savedStateHandle["found_draft.photo_message"] = null
        // Bila Activity dibuat ulang ketika kamera terbuka, route form akan dipulihkan.
        savedStateHandle["found_draft.return_to_form"] = true
    }

    fun finishCameraCapture(photoSaved: Boolean) {
        val pending = pendingCameraUri.value
        if (photoSaved && !pending.isNullOrBlank()) {
            savedStateHandle["found_draft.selected_photo"] = pending
            savedStateHandle["found_draft.photo_message"] = null
        } else {
            savedStateHandle["found_draft.photo_message"] =
                "Foto tidak berhasil disimpan atau pengambilan dibatalkan. Silakan coba lagi."
        }
        savedStateHandle["found_draft.pending_camera"] = null
        savedStateHandle["found_draft.return_to_form"] = true
    }

    fun cameraLaunchFailed(message: String) {
        savedStateHandle["found_draft.pending_camera"] = null
        savedStateHandle["found_draft.photo_message"] = message
        savedStateHandle["found_draft.return_to_form"] = true
    }

    fun beginGallerySelection() {
        savedStateHandle["found_draft.photo_message"] = null
        savedStateHandle["found_draft.return_to_form"] = true
    }

    fun finishGallerySelection(uri: String?) {
        if (!uri.isNullOrBlank()) {
            savedStateHandle["found_draft.selected_photo"] = uri
            savedStateHandle["found_draft.photo_message"] = null
        } else {
            savedStateHandle["found_draft.photo_message"] = "Pemilihan foto dibatalkan."
        }
        savedStateHandle["found_draft.return_to_form"] = true
    }

    fun clearSelectedPhoto() {
        savedStateHandle["found_draft.selected_photo"] = null
        savedStateHandle["found_draft.photo_message"] = null
    }

    fun acknowledgeReturnToForm() {
        savedStateHandle["found_draft.return_to_form"] = false
    }

    /** Hapus draft hanya setelah database benar-benar berhasil menyimpan laporan. */
    fun clearAfterSuccessfulSave() {
        savedStateHandle["found_draft.title"] = ""
        savedStateHandle["found_draft.description"] = ""
        savedStateHandle["found_draft.location"] = ""
        savedStateHandle["found_draft.found_at"] = LocalDate.now().toString()
        savedStateHandle["found_draft.selected_photo"] = null
        savedStateHandle["found_draft.pending_camera"] = null
        savedStateHandle["found_draft.photo_message"] = null
        savedStateHandle["found_draft.return_to_form"] = false
    }
}
