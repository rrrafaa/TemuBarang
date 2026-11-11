package com.example.temubarang.features.found.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.ui.component.FoundPhoto
import com.example.temubarang.features.found.viewmodel.FoundDraftViewModel
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandNavy
import java.time.LocalDate

/**
 * Form hanya memanggil aksi; kamera diluncurkan oleh MainActivity (root Composable).
 * Draft create tersimpan di FoundDraftViewModel+SavedStateHandle sehingga tidak hilang
 * ketika aplikasi kamera menutup/merestart Activity TemuBarang.
 */
@Composable
fun FoundItemFormScreen(
    viewModel: FoundItemsViewModel,
    itemId: String?,
    draftViewModel: FoundDraftViewModel,
    onLaunchCamera: () -> Unit,
    onLaunchGallery: () -> Unit,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val editing = itemId != null
    val item = state.items.firstOrNull { it.id == itemId }
        ?: state.detail?.takeIf { it.id == itemId }
    LaunchedEffect(itemId) {
        if (editing && item == null) viewModel.load(itemId!!)
    }

    val draftTitle by draftViewModel.title.collectAsState()
    val draftDescription by draftViewModel.description.collectAsState()
    val draftLocation by draftViewModel.location.collectAsState()
    val draftDate by draftViewModel.foundAt.collectAsState()
    val photoUriString by draftViewModel.selectedPhotoUri.collectAsState()
    val pendingCameraUri by draftViewModel.pendingCameraUri.collectAsState()
    val cameraMessage by draftViewModel.photoMessage.collectAsState()

    var editTitle by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var editDescription by rememberSaveable(item?.id) { mutableStateOf(item?.description.orEmpty()) }
    var editLocation by rememberSaveable(item?.id) { mutableStateOf(item?.foundLocation.orEmpty()) }
    var editDate by rememberSaveable(item?.id) {
        mutableStateOf(item?.foundAt ?: LocalDate.now().toString())
    }
    var status by rememberSaveable(item?.id) { mutableStateOf(item?.status ?: "open") }

    val title = if (editing) editTitle else draftTitle
    val description = if (editing) editDescription else draftDescription
    val location = if (editing) editLocation else draftLocation
    val foundAt = if (editing) editDate else draftDate
    val photoUri = photoUriString?.let(Uri::parse)

    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground)
            .systemBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Kembali") }
        Text(
            if (editing) "Edit Laporan" else "Laporkan Barang Ditemukan",
            style = MaterialTheme.typography.headlineSmall, color = BrandNavy
        )
        Text("Lengkapi informasi barang. Hindari memuat data pribadi dalam foto dan deskripsi.")
        FoundMessage(state.error)
        if (editing && item == null) {
            if (state.isLoadingDetail) CircularProgressIndicator()
            else Text("Laporan tidak ditemukan.")
            return@Column
        }
        if (editing && item != null && item.reporterId != viewModel.userId) {
            Text("Kamu tidak memiliki izin untuk mengedit laporan ini.")
            return@Column
        }

        OutlinedTextField(
            value = title,
            onValueChange = { if (editing) editTitle = it else draftViewModel.changeTitle(it) },
            label = { Text("Nama barang *") }, singleLine = true,
            modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving
        )
        OutlinedTextField(
            value = description,
            onValueChange = { if (editing) editDescription = it else draftViewModel.changeDescription(it) },
            label = { Text("Deskripsi / ciri barang *") }, minLines = 3,
            modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving
        )
        OutlinedTextField(
            value = location,
            onValueChange = { if (editing) editLocation = it else draftViewModel.changeLocation(it) },
            label = { Text("Lokasi penemuan *") },
            modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving
        )
        OutlinedTextField(
            value = foundAt,
            onValueChange = { if (editing) editDate = it else draftViewModel.changeFoundAt(it) },
            label = { Text("Tanggal ditemukan (YYYY-MM-DD) *") },
            supportingText = { Text("Contoh: 2026-11-11") },
            modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving
        )

        if (!editing) {
            Text("Foto barang (opsional, maksimal 5 MB)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onLaunchCamera,
                    enabled = !state.isSaving && pendingCameraUri == null
                ) { Text("📷 Ambil Foto") }
                OutlinedButton(
                    onClick = onLaunchGallery,
                    enabled = !state.isSaving && pendingCameraUri == null
                ) { Text("Galeri") }
            }
            FoundMessage(cameraMessage)
            if (pendingCameraUri != null) {
                Text("Memproses foto dari kamera...", style = MaterialTheme.typography.bodySmall)
            }
            if (photoUri != null) {
                FoundPhoto(photoUri.toString(), Modifier.fillMaxWidth().height(190.dp))
                Text("Foto siap diunggah saat laporan disimpan.",
                    style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = draftViewModel::clearSelectedPhoto) {
                    Text("Hapus foto pilihan")
                }
            }
            Text("Format: JPG, PNG, WEBP. Foto kamera yang terlalu besar akan diperkecil.",
                style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Foto tetap menggunakan foto saat laporan pertama dibuat.",
                style = MaterialTheme.typography.bodySmall)
            FoundPhoto(viewModel.photoUrl(item?.imagePath), Modifier.fillMaxWidth().height(180.dp))
            Text("Status barang", style = MaterialTheme.typography.titleSmall)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                listOf("open", "returned", "closed").forEach { choice ->
                    FilterChip(
                        selected = status == choice,
                        onClick = { status = choice },
                        label = { Text(choice.foundStatusLabel()) },
                        enabled = !state.isSaving
                    )
                }
            }
        }

        GradientPrimaryButton(
            text = if (editing) "Simpan Perubahan" else "Kirim Laporan",
            enabled = !state.isSaving && pendingCameraUri == null,
            loading = state.isSaving,
            onClick = {
                viewModel.clearMessage()
                if (editing) {
                    viewModel.edit(itemId!!, title, description, location, foundAt, status, onSaved)
                } else {
                    viewModel.create(title, description, location, foundAt, photoUri) {
                        draftViewModel.clearAfterSuccessfulSave()
                        onSaved()
                    }
                }
            }
        )
    }
}
