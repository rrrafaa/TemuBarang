package com.example.temubarang.features.lost.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.lost.model.LostItem
import com.example.temubarang.features.lost.ui.component.DatePickerField
import com.example.temubarang.features.lost.ui.component.FormSection
import com.example.temubarang.features.lost.ui.component.FormTextField
import com.example.temubarang.features.lost.ui.component.LostMessage
import com.example.temubarang.features.lost.ui.component.PhotoUploadField
import com.example.temubarang.features.lost.viewmodel.LostDraftViewModel
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppValidationAnchor
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.validation.ReportValidation

/** Draft berada pada Activity-scoped SavedStateHandle agar tidak hilang saat kamera/galeri aktif. */
@Composable
fun LostItemFormScreen(
    viewModel: LostItemsViewModel,
    itemId: String?,
    draftViewModel: LostDraftViewModel,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }
    LaunchedEffect(itemId) {
        if (itemId != null && item == null) viewModel.refresh()
    }
    if (itemId != null && item == null) {
        AppPage("Edit Laporan Barang Hilang", onBack = onBack) {
            Column(Modifier.fillMaxSize().padding(20.dp)) {
                if (state.isLoading) CircularProgressIndicator()
                else Text("Laporan tidak ditemukan. Muat ulang daftar lalu coba lagi.")
                LostMessage(state.error)
            }
        }
        return
    }
    LaunchedEffect(itemId, item?.id) { draftViewModel.prepare(itemId, item) }
    LostItemFormContent(
        viewModel = viewModel, draftViewModel = draftViewModel, itemId = itemId, item = item,
        onLaunchCamera = onLaunchCamera, onLaunchGallery = onLaunchGallery,
        onBack = onBack, onSaved = onSaved
    )
}

@Composable
private fun LostItemFormContent(
    viewModel: LostItemsViewModel,
    draftViewModel: LostDraftViewModel,
    itemId: String?,
    item: LostItem?,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val title by draftViewModel.title.collectAsState()
    val description by draftViewModel.description.collectAsState()
    val location by draftViewModel.location.collectAsState()
    val lostAt by draftViewModel.lostAt.collectAsState()
    val photoUri by draftViewModel.photoUri.collectAsState()
    val pendingCamera by draftViewModel.pendingCameraUri.collectAsState()
    val photoMessage by draftViewModel.photoMessage.collectAsState()
    val previewUrl = photoUri ?: viewModel.photoUrl(item?.imagePath)
    val hasPhoto = !previewUrl.isNullOrBlank()
    val busy = state.isSaving || pendingCamera != null
    val editing = itemId != null

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var firstError by remember { mutableStateOf<String?>(null) }
    var submitVersion by rememberSaveable { mutableIntStateOf(0) }
    fun clearError(key: String) { errors = errors - key }

    AppPage(
        title = if (editing) "Edit Laporan Barang Hilang" else "Tambah Laporan Barang Hilang",
        onBack = onBack, enabled = !busy
    ) {
        Column(Modifier.fillMaxSize().background(BrandBackground)
            .imePadding().navigationBarsPadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(if (editing) "Edit Laporan" else "Laporkan Barang Hilang",
                    style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                Text("Lengkapi data barang. Kolom bertanda * wajib diisi.",
                    style = MaterialTheme.typography.bodyMedium, color = BrandMuted)
                LostMessage(state.error)

                FormSection("Detail barang") {
                    FormTextField(value = title,
                        onValueChange = { draftViewModel.changeTitle(it); clearError("title") },
                        label = "Nama barang *", hint = "${title.trim().length}/120 karakter",
                        enabled = !busy, singleLine = true, errorText = errors["title"],
                        isFirstError = firstError == "title", submitVersion = submitVersion)
                    FormTextField(value = description,
                        onValueChange = { draftViewModel.changeDescription(it); clearError("description") },
                        label = "Deskripsi / ciri-ciri *",
                        hint = "Warna, merek, ciri khusus (minimal 10 karakter)",
                        enabled = !busy, minLines = 3, errorText = errors["description"],
                        isFirstError = firstError == "description", submitVersion = submitVersion)
                }
                FormSection("Kapan dan di mana") {
                    FormTextField(value = location,
                        onValueChange = { draftViewModel.changeLocation(it); clearError("location") },
                        label = "Lokasi terakhir terlihat *",
                        hint = "Contoh: Kantin Fakultas Ilmu Komputer",
                        enabled = !busy, singleLine = true, imeAction = ImeAction.Done,
                        errorText = errors["location"],
                        isFirstError = firstError == "location", submitVersion = submitVersion)
                    AppValidationAnchor(firstError == "date", submitVersion) {
                        DatePickerField(label = "Tanggal hilang *", value = lostAt,
                            onValueChange = { draftViewModel.changeLostAt(it); clearError("date") },
                            enabled = !busy, errorText = errors["date"])
                    }
                }
                FormSection("Foto barang *", "Foto jelas membantu orang mengenali barangmu.") {
                    AppValidationAnchor(firstError == "photo", submitVersion) {
                        PhotoUploadField(
                            previewUrl = previewUrl,
                            showError = errors["photo"] != null,
                            enabled = !busy,
                            message = photoMessage,
                            onPickFile = onLaunchGallery,
                            onTakePhoto = onLaunchCamera,
                            onRemove = if (photoUri != null) ({ draftViewModel.clearPhoto() }) else null
                        )
                    }
                }
            }
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                GradientPrimaryButton(
                    text = if (editing) "Simpan Perubahan" else "Kirim Laporan",
                    enabled = !busy, loading = state.isSaving,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    onClick = {
                        val foundErrors = ReportValidation.lost(
                            title, description, location, lostAt, hasPhoto
                        )
                        errors = foundErrors
                        firstError = foundErrors.keys.firstOrNull()
                        if (foundErrors.isNotEmpty()) {
                            submitVersion++
                        } else {
                            viewModel.clearMessages()
                            val success: () -> Unit = {
                                draftViewModel.clearAfterSuccess()
                                onSaved()
                            }
                            val selectedPhoto = photoUri?.let(Uri::parse)
                            if (itemId == null) {
                                viewModel.create(title, description, location, lostAt, selectedPhoto, success)
                            } else {
                                viewModel.update(itemId, title, description, location, lostAt, selectedPhoto, success)
                            }
                        }
                    }
                )
            }
        }
    }
}
