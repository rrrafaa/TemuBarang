package com.example.temubarang.features.lost.ui.screen

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.camera.CameraPhotoUri
import com.example.temubarang.features.lost.model.LostItem
import com.example.temubarang.features.lost.ui.component.DatePickerField
import com.example.temubarang.features.lost.ui.component.FormSection
import com.example.temubarang.features.lost.ui.component.FormTextField
import com.example.temubarang.features.lost.ui.component.LostMessage
import com.example.temubarang.features.lost.ui.component.PhotoUploadField
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import java.time.LocalDate

/**
 * Form tambah dan edit laporan. itemId == null berarti membuat laporan baru.
 * Form hanya menampung input dan memanggil aksi. Validasi dan Supabase ada di ViewModel.
 */
@Composable
fun LostItemFormScreen(
    viewModel: LostItemsViewModel,
    itemId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }

    // Setelah proses dipulihkan, daftar bisa kosong. Muat ulang agar data edit tersedia.
    LaunchedEffect(itemId) { if (itemId != null && item == null) viewModel.refresh() }

    if (itemId != null && item == null) {
        AppPage("Edit Laporan Barang Hilang", onBack = onBack) {
            Column(Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (state.isLoading) "Memuat laporan..." else "Laporan tidak ditemukan.")
            }
        }
    } else {
        LostItemFormContent(viewModel, itemId, item, onBack, onSaved)
    }
}

@Composable
private fun LostItemFormContent(
    viewModel: LostItemsViewModel,
    itemId: String?,
    item: LostItem?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val editing = itemId != null

    var title by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var description by rememberSaveable(item?.id) { mutableStateOf(item?.description.orEmpty()) }
    var location by rememberSaveable(item?.id) { mutableStateOf(item?.lastSeenLocation.orEmpty()) }
    var lostAt by rememberSaveable(item?.id) {
        mutableStateOf(item?.lostAt ?: LocalDate.now().toString())
    }
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCamera by rememberSaveable { mutableStateOf<String?>(null) }
    var photoMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    // Saat edit, foto lama tetap dipakai kecuali pengguna memilih foto pengganti.
    val previewUrl = photoUri ?: viewModel.photoUrl(item?.imagePath)
    val hasPhoto = previewUrl != null

    // Sebagian aplikasi kamera butuh izin URI eksplisit untuk menulis ke FileProvider.
    val cameraContract = remember {
        object : ActivityResultContracts.TakePicture() {
            override fun createIntent(context: Context, input: Uri): Intent =
                super.createIntent(context, input).apply {
                    clipData = ClipData.newRawUri("Foto TemuBarang", input)
                    addFlags(
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(cameraContract) { success ->
        val uri = pendingCamera?.let(Uri::parse)
        if (success && uri != null && CameraPhotoUri.hasPhoto(context, uri)) {
            photoUri = uri.toString()
            photoMessage = null
        } else {
            photoMessage = "Foto tidak berhasil disimpan atau pengambilan dibatalkan."
        }
        pendingCamera = null
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
            photoMessage = null
        } else {
            photoMessage = "Pemilihan foto dibatalkan."
        }
    }

    val busy = state.isSaving || pendingCamera != null

    AppPage(
        title = if (editing) "Edit Laporan Barang Hilang" else "Tambah Laporan Barang Hilang",
        onBack = onBack, enabled = !state.isSaving
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(BrandBackground)
                .imePadding().navigationBarsPadding()
        ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (editing) "Edit Laporan" else "Laporkan Barang Hilang",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandNavy
                )
                Text(
                    "Lengkapi data berikut agar barangmu lebih mudah dikenali. " +
                            "Kolom bertanda * wajib diisi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BrandMuted
                )
            }

            LostMessage(state.error)

            FormSection(
                title = "Foto barang *",
                subtitle = "Foto yang jelas membantu orang lain mengenali barangmu."
            ) {
                PhotoUploadField(
                    previewUrl = previewUrl,
                    showError = attemptedSubmit && !hasPhoto,
                    enabled = !busy,
                    message = photoMessage,
                    onPickFile = { filePickerLauncher.launch("image/*") },
                    onTakePhoto = {
                        try {
                            val destination = CameraPhotoUri.create(context)
                            pendingCamera = destination.toString()
                            cameraLauncher.launch(destination)
                        } catch (e: Exception) {
                            pendingCamera = null
                            photoMessage = "Kamera tidak dapat dibuka. Coba unggah dari file."
                        }
                    },
                    onRemove = if (photoUri != null) ({ photoUri = null }) else null
                )
            }

            FormSection(title = "Detail barang") {
                FormTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Nama barang *",
                    hint = "${title.trim().length}/120 karakter",
                    enabled = !state.isSaving,
                    singleLine = true
                )
                FormTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Deskripsi / ciri-ciri *",
                    hint = "Warna, merek, ciri khusus. Minimal 10 karakter (${description.trim().length})",
                    enabled = !state.isSaving,
                    minLines = 3
                )
            }

            FormSection(title = "Kapan dan di mana") {
                FormTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = "Lokasi terakhir terlihat *",
                    hint = "Contoh: Kantin Fakultas Ilmu Komputer",
                    enabled = !state.isSaving,
                    singleLine = true,
                    imeAction = ImeAction.Done
                )
                DatePickerField(
                    label = "Tanggal hilang *",
                    value = lostAt,
                    onValueChange = { lostAt = it },
                    enabled = !state.isSaving
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        // Tombol aksi tetap terlihat di bawah layar saat formulir di-scroll.
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            GradientPrimaryButton(
                text = if (editing) "Simpan Perubahan" else "Kirim Laporan",
                enabled = !busy,
                loading = state.isSaving,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    attemptedSubmit = true
                    viewModel.clearMessages()
                    if (hasPhoto) {
                        val newPhoto = photoUri?.let(Uri::parse)
                        if (itemId != null) {
                            viewModel.update(itemId, title, description, location, lostAt, newPhoto, onSaved)
                        } else {
                            viewModel.create(title, description, location, lostAt, newPhoto, onSaved)
                        }
                    }
                }
            )
        }
    }
}