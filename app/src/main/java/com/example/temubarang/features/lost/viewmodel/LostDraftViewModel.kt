package com.example.temubarang.features.lost.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.temubarang.features.lost.model.LostItem
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/** Menyimpan teks dan URI foto selama kamera/galeri membuka Activity lain. */
class LostDraftViewModel(private val state: SavedStateHandle) : ViewModel() {
    val title: StateFlow<String> = state.getStateFlow("lost.title", "")
    val description: StateFlow<String> = state.getStateFlow("lost.description", "")
    val location: StateFlow<String> = state.getStateFlow("lost.location", "")
    val lostAt: StateFlow<String> = state.getStateFlow("lost.date", LocalDate.now().toString())
    val photoUri: StateFlow<String?> = state.getStateFlow("lost.photo", null)
    val pendingCameraUri: StateFlow<String?> = state.getStateFlow("lost.pendingCamera", null)
    val photoMessage: StateFlow<String?> = state.getStateFlow("lost.photoMessage", null)
    val returnToForm: StateFlow<Boolean> = state.getStateFlow("lost.returnToForm", false)
    val returnRoute: StateFlow<String> = state.getStateFlow("lost.returnRoute", "lost_items/new")

    fun prepare(itemId: String?, item: LostItem?) {
        if (itemId != null && item == null) return
        val key = itemId ?: "new"
        if (state.get<String>("lost.formKey") == key) return
        state["lost.formKey"] = key
        changeTitle(item?.title.orEmpty())
        changeDescription(item?.description.orEmpty())
        changeLocation(item?.lastSeenLocation.orEmpty())
        changeLostAt(item?.lostAt ?: LocalDate.now().toString())
        state["lost.photo"] = null
        state["lost.pendingCamera"] = null
        state["lost.photoMessage"] = null
    }

    fun changeTitle(v: String) { state["lost.title"] = v }
    fun changeDescription(v: String) { state["lost.description"] = v }
    fun changeLocation(v: String) { state["lost.location"] = v }
    fun changeLostAt(v: String) { state["lost.date"] = v }

    private fun rememberReturnRoute(itemId: String?) {
        state["lost.returnRoute"] = if (itemId == null) "lost_items/new" else "lost_items/edit/$itemId"
        state["lost.returnToForm"] = true
    }

    fun beginCamera(uri: String, itemId: String?) {
        rememberReturnRoute(itemId)
        state["lost.pendingCamera"] = uri
        state["lost.photoMessage"] = null
    }

    fun finishCamera(success: Boolean) {
        val uri = pendingCameraUri.value
        if (success && !uri.isNullOrBlank()) {
            state["lost.photo"] = uri
            state["lost.photoMessage"] = null
        } else {
            state["lost.photoMessage"] = "Foto tidak berhasil disimpan. Coba ambil ulang atau pilih dari galeri."
        }
        state["lost.pendingCamera"] = null
        state["lost.returnToForm"] = true
    }

    fun beginGallery(itemId: String?) {
        rememberReturnRoute(itemId)
        state["lost.photoMessage"] = null
    }

    fun finishGallery(uri: String?) {
        if (!uri.isNullOrBlank()) {
            state["lost.photo"] = uri
            state["lost.photoMessage"] = null
        } else {
            state["lost.photoMessage"] = "Pemilihan foto dibatalkan."
        }
        state["lost.returnToForm"] = true
    }

    fun failedToLaunch(message: String) {
        state["lost.pendingCamera"] = null
        state["lost.photoMessage"] = message
        state["lost.returnToForm"] = true
    }

    fun clearPhoto() { state["lost.photo"] = null; state["lost.photoMessage"] = null }
    fun acknowledgeReturn() { state["lost.returnToForm"] = false }

    /** Dipanggil hanya setelah repository selesai menyimpan dengan sukses. */
    fun clearAfterSuccess() {
        state["lost.formKey"] = null
        changeTitle(""); changeDescription(""); changeLocation("")
        changeLostAt(LocalDate.now().toString())
        state["lost.photo"] = null
        state["lost.pendingCamera"] = null
        state["lost.photoMessage"] = null
        state["lost.returnToForm"] = false
    }
}
