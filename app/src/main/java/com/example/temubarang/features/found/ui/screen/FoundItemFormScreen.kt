package com.example.temubarang.features.found.ui.screen

import com.example.temubarang.R
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.features.found.ui.component.FoundFormField
import com.example.temubarang.features.found.ui.component.FoundChoiceRow
import androidx.compose.foundation.layout.imePadding
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.temubarang.features.found.model.FoundHandover
import com.example.temubarang.features.found.model.foundCategories
import com.example.temubarang.features.found.model.custodyTypes
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.ui.component.FoundPhoto
import com.example.temubarang.features.found.viewmodel.FoundDraftViewModel
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
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

    val draftCampus by draftViewModel.campus.collectAsState()
    val draftCategory by draftViewModel.category.collectAsState()
    val draftCustody by draftViewModel.custodyType.collectAsState()
    val draftPickup by draftViewModel.pickupLocation.collectAsState()
    val draftNotes by draftViewModel.pickupNotes.collectAsState()
    var editCampus by rememberSaveable(item?.id) { mutableStateOf(item?.campus.orEmpty()) }
    var editCategory by rememberSaveable(item?.id) { mutableStateOf(item?.category ?: "other") }
    var editCustody by rememberSaveable(item?.id) { mutableStateOf(item?.custodyType ?: "reporter") }
    var editPickup by rememberSaveable(item?.id) { mutableStateOf(item?.pickupLocation.orEmpty()) }
    var editNotes by rememberSaveable(item?.id) { mutableStateOf(item?.pickupNotes.orEmpty()) }
    val handover = FoundHandover(
        if (editing) editCampus else draftCampus, if (editing) editCategory else draftCategory,
        if (editing) editCustody else draftCustody, if (editing) editPickup else draftPickup,
        if (editing) editNotes else draftNotes
    )
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

    AppPage(if (editing) "Edit Laporan" else "Tambah Laporan", onBack = onBack, enabled = !state.isSaving) {
        Column(
            modifier = Modifier.fillMaxSize().background(BrandBackground)
                .imePadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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

            FoundFormField("Nama barang *", "Contoh: Kunci motor dengan gantungan biru", title,
                { if (editing) editTitle = it else draftViewModel.changeTitle(it) }, enabled = !state.isSaving)
            FoundFormField("Deskripsi / ciri barang *", "Contoh: Kunci motor ditemukan di meja kantin dekat pintu masuk.", description,
                { if (editing) editDescription = it else draftViewModel.changeDescription(it) }, enabled = !state.isSaving, minLines = 3)
            FoundFormField("Lokasi penemuan *", "Contoh: Kantin Gedung A, lantai 1", location,
                { if (editing) editLocation = it else draftViewModel.changeLocation(it) }, enabled = !state.isSaving)
            FoundFormField("Tanggal ditemukan *", "Contoh: 2026-11-16", foundAt,
                { if (editing) editDate = it else draftViewModel.changeFoundAt(it) }, enabled = !state.isSaving,
                hint = "Gunakan format YYYY-MM-DD.")
            FoundFormField("Kampus *", "Contoh: Universitas Brawijaya, kampus utama", handover.campus,
                { if (editing) editCampus = it else draftViewModel.changeCampus(it) }, enabled = !state.isSaving)
            FoundChoiceRow("Kategori barang", foundCategories, handover.category,
                { if (editing) editCategory = it else draftViewModel.changeCategory(it) }, enabled = !state.isSaving)
            FoundChoiceRow("Barang sekarang disimpan oleh", custodyTypes, handover.custodyType,
                { if (editing) editCustody = it else draftViewModel.changeCustodyType(it) }, enabled = !state.isSaving)
            FoundFormField("Lokasi pengambilan *", "Contoh: Pos keamanan Gedung A", handover.pickupLocation,
                { if (editing) editPickup = it else draftViewModel.changePickupLocation(it) }, enabled = !state.isSaving,
                hint = "Pilih tempat umum di kampus, bukan alamat rumah pribadi.")
            FoundFormField("Petunjuk pengambilan (opsional)", "Contoh: Senin–Jumat 08.00–16.00, bawa bukti kepemilikan.", handover.pickupNotes,
                { if (editing) editNotes = it else draftViewModel.changePickupNotes(it) }, enabled = !state.isSaving, minLines = 2,
                hint = "Jangan tulis ciri rahasia yang digunakan untuk memverifikasi kepemilikan.")

            if (!editing) {
                Text("Foto barang (opsional, maksimal 5 MB)", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onLaunchCamera,
                        enabled = !state.isSaving && pendingCameraUri == null
                    ) { AppIcon(R.drawable.ic_camera_duotone, null); Text(" Ambil Foto") }
                    OutlinedButton(
                        onClick = onLaunchGallery,
                        enabled = !state.isSaving && pendingCameraUri == null
                    ) { AppIcon(R.drawable.ic_image_duotone, null); Text(" Galeri") }
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
                FoundChoiceRow(
                    "Status barang", listOf("open", "returned", "closed").associateWith { it.foundStatusLabel() },
                    status, { status = it }, enabled = !state.isSaving
                )
            }

            GradientPrimaryButton(
                text = if (editing) "Simpan Perubahan" else "Kirim Laporan",
                enabled = !state.isSaving && pendingCameraUri == null,
                loading = state.isSaving,
                onClick = {
                    viewModel.clearMessage()
                    if (editing) {
                        viewModel.edit(itemId!!, title, description, location, foundAt, status, handover, onSaved)
                    } else {
                        viewModel.create(title, description, location, foundAt, photoUri, handover) {
                            draftViewModel.clearAfterSuccessfulSave()
                            onSaved()
                        }
                    }
                }
            )
        }
    }
}
