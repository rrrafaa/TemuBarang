package com.example.temubarang.features.found.ui.screen

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
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.model.FoundHandover
import com.example.temubarang.features.found.model.custodyTypes
import com.example.temubarang.features.found.model.foundCategories
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.features.found.ui.component.FoundChoiceRow
import com.example.temubarang.features.found.ui.component.FoundFormField
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.ui.component.FoundPhoto
import com.example.temubarang.features.found.viewmodel.FoundDraftViewModel
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.AppDatePickerField
import com.example.temubarang.ui.component.AppDropdownField
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppPhotoPicker
import com.example.temubarang.ui.component.AppSectionCard
import com.example.temubarang.ui.component.AppValidationAnchor
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.validation.ReportValidation
import java.time.LocalDate

/** Draft create tetap di SavedStateHandle; editing memakai rememberSaveable dengan ID laporan. */
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
        if (itemId != null && item == null) viewModel.load(itemId)
    }

    val draftTitle by draftViewModel.title.collectAsState()
    val draftDescription by draftViewModel.description.collectAsState()
    val draftLocation by draftViewModel.location.collectAsState()
    val draftDate by draftViewModel.foundAt.collectAsState()
    val photoUriString by draftViewModel.selectedPhotoUri.collectAsState()
    val pendingCamera by draftViewModel.pendingCameraUri.collectAsState()
    val photoMessage by draftViewModel.photoMessage.collectAsState()
    val draftCampus by draftViewModel.campus.collectAsState()
    val draftCategory by draftViewModel.category.collectAsState()
    val draftCustody by draftViewModel.custodyType.collectAsState()
    val draftPickup by draftViewModel.pickupLocation.collectAsState()
    val draftNotes by draftViewModel.pickupNotes.collectAsState()

    var editTitle by rememberSaveable(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    var editDescription by rememberSaveable(item?.id) { mutableStateOf(item?.description.orEmpty()) }
    var editLocation by rememberSaveable(item?.id) { mutableStateOf(item?.foundLocation.orEmpty()) }
    var editDate by rememberSaveable(item?.id) {
        mutableStateOf(item?.foundAt ?: LocalDate.now().toString())
    }
    var editCampus by rememberSaveable(item?.id) { mutableStateOf(item?.campus.orEmpty()) }
    var editCategory by rememberSaveable(item?.id) { mutableStateOf(item?.category ?: "") }
    var editCustody by rememberSaveable(item?.id) { mutableStateOf(item?.custodyType ?: "reporter") }
    var editPickup by rememberSaveable(item?.id) { mutableStateOf(item?.pickupLocation.orEmpty()) }
    var editNotes by rememberSaveable(item?.id) { mutableStateOf(item?.pickupNotes.orEmpty()) }
    var editStatus by rememberSaveable(item?.id) { mutableStateOf(item?.status ?: "open") }

    val title = if (editing) editTitle else draftTitle
    val description = if (editing) editDescription else draftDescription
    val location = if (editing) editLocation else draftLocation
    val date = if (editing) editDate else draftDate
    val handover = FoundHandover(
        if (editing) editCampus else draftCampus,
        if (editing) editCategory else draftCategory,
        if (editing) editCustody else draftCustody,
        if (editing) editPickup else draftPickup,
        if (editing) editNotes else draftNotes
    )
    val photoUri = photoUriString?.let(Uri::parse)
    val canEdit = !editing || (item != null && item.reporterId == viewModel.userId)
    val busy = state.isSaving || pendingCamera != null

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var firstError by remember { mutableStateOf<String?>(null) }
    var submitVersion by rememberSaveable { mutableIntStateOf(0) }
    fun clearError(key: String) { errors = errors - key }
    LaunchedEffect(photoUriString) {
        if (photoUriString != null) clearError("photo")
    }

    AppPage(
        title = if (editing) "Edit Laporan Barang Ditemukan" else "Tambah Laporan Barang Ditemukan",
        onBack = onBack, enabled = !busy
    ) {
        Column(Modifier.fillMaxSize().background(BrandBackground)
            .imePadding().navigationBarsPadding()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Lengkapi data barang dan lokasi pengambilan. Jangan cantumkan ciri rahasia.",
                    style = MaterialTheme.typography.bodyMedium)
                FoundMessage(state.error)

                if (editing && item == null) {
                    if (state.isLoadingDetail) CircularProgressIndicator()
                    else Text("Laporan tidak ditemukan.")
                } else if (!canEdit) {
                    Text("Kamu tidak memiliki izin untuk mengedit laporan ini.")
                } else {
                    AppSectionCard("Detail barang") {
                        FoundFormField("Nama barang *", "Contoh: Kunci motor dengan gantungan biru",
                            title, { if (editing) editTitle = it else draftViewModel.changeTitle(it)
                                clearError("title") }, enabled = !busy,
                            errorText = errors["title"],
                            isFirstError = firstError == "title", submitVersion = submitVersion)
                        FoundFormField("Deskripsi / ciri barang *", "Warna, merek dan ciri khusus",
                            description, { if (editing) editDescription = it else draftViewModel.changeDescription(it)
                                clearError("description") }, enabled = !busy, minLines = 3,
                            errorText = errors["description"],
                            isFirstError = firstError == "description", submitVersion = submitVersion)
                        AppValidationAnchor(firstError == "category", submitVersion) {
                            AppDropdownField("Kategori barang *", foundCategories, handover.category,
                                onSelect = {
                                    if (editing) editCategory = it else draftViewModel.changeCategory(it)
                                    clearError("category")
                                }, enabled = !busy, errorText = errors["category"])
                        }
                    }
                    AppSectionCard("Lokasi dan waktu penemuan") {
                        FoundFormField("Lokasi penemuan *", "Contoh: Kantin Gedung A lantai 1",
                            location, {
                                if (editing) editLocation = it else draftViewModel.changeLocation(it)
                                clearError("location")
                            }, enabled = !busy, errorText = errors["location"],
                            isFirstError = firstError == "location", submitVersion = submitVersion)
                        AppValidationAnchor(firstError == "date", submitVersion) {
                            AppDatePickerField("Tanggal ditemukan *", date,
                                onValueChange = {
                                    if (editing) editDate = it else draftViewModel.changeFoundAt(it)
                                    clearError("date")
                                }, enabled = !busy, errorText = errors["date"])
                        }
                        FoundFormField("Kampus *", "Contoh: Universitas Brawijaya",
                            handover.campus, {
                                if (editing) editCampus = it else draftViewModel.changeCampus(it)
                                clearError("campus")
                            }, enabled = !busy, errorText = errors["campus"],
                            isFirstError = firstError == "campus", submitVersion = submitVersion)
                    }
                    AppSectionCard("Informasi penyimpanan") {
                        AppValidationAnchor(firstError == "custody", submitVersion) {
                            AppDropdownField("Barang disimpan oleh *", custodyTypes, handover.custodyType,
                                onSelect = {
                                    if (editing) editCustody = it else draftViewModel.changeCustodyType(it)
                                    clearError("custody")
                                }, enabled = !busy, errorText = errors["custody"])
                        }
                        FoundFormField("Lokasi pengambilan *", "Contoh: Pos keamanan Gedung A",
                            handover.pickupLocation, {
                                if (editing) editPickup = it else draftViewModel.changePickupLocation(it)
                                clearError("pickup")
                            }, enabled = !busy, errorText = errors["pickup"],
                            isFirstError = firstError == "pickup", submitVersion = submitVersion,
                            hint = "Gunakan lokasi umum di kampus, bukan alamat rumah.")
                        FoundFormField("Petunjuk pengambilan (opsional)",
                            "Jam operasional dan bukti kepemilikan",
                            handover.pickupNotes, {
                                if (editing) editNotes = it else draftViewModel.changePickupNotes(it)
                                clearError("notes")
                            }, enabled = !busy, minLines = 2, errorText = errors["notes"],
                            isFirstError = firstError == "notes", submitVersion = submitVersion)
                    }
                    AppSectionCard(if (editing) "Foto dan status barang" else "Foto barang *") {
                        if (!editing) {
                            Text("Foto barang wajib diunggah (JPG, PNG, atau WEBP, maksimal 5 MB).",
                                style = MaterialTheme.typography.bodyMedium)
                            AppValidationAnchor(firstError == "photo", submitVersion) {
                                AppPhotoPicker(
                                    hasPhoto = photoUri != null, required = true,
                                    showError = errors["photo"] != null,
                                    enabled = !busy,
                                    onGallery = onLaunchGallery, onCamera = onLaunchCamera,
                                    onRemove = if (photoUri != null) draftViewModel::clearSelectedPhoto else null,
                                    message = photoMessage,
                                    preview = {
                                        if (photoUri != null) {
                                            FoundPhoto(photoUri.toString(),
                                                Modifier.fillMaxWidth().height(190.dp))
                                        }
                                    }
                                )
                            }
                            if (pendingCamera != null) {
                                Text("Memproses foto dari kamera...",
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        } else {
                            FoundPhoto(viewModel.photoUrl(item?.imagePath),
                                Modifier.fillMaxWidth().height(190.dp))
                            AppValidationAnchor(firstError == "status", submitVersion) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                FoundChoiceRow(
                                    "Status barang",
                                    listOf("open", "returned", "closed")
                                        .associateWith { it.foundStatusLabel() },
                                    editStatus, { editStatus = it; clearError("status") },
                                    enabled = !busy
                                )
                                errors["status"]?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error)
                                }
                                }
                            }
                        }
                    }
                }
            }

            if (canEdit && (!editing || item != null)) {
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                    GradientPrimaryButton(
                        text = if (editing) "Simpan Perubahan" else "Kirim Laporan",
                        enabled = !busy, loading = state.isSaving,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        onClick = {
                            val foundErrors = ReportValidation.found(
                                title, description, location, date, handover,
                                if (editing) editStatus else "open",
                                hasPhoto = editing || photoUri != null
                            )
                            errors = foundErrors
                            firstError = foundErrors.keys.firstOrNull()
                            if (foundErrors.isNotEmpty()) {
                                submitVersion++
                            } else {
                                viewModel.clearMessage()
                                if (editing) {
                                    viewModel.edit(itemId!!, title, description, location, date,
                                        editStatus, handover, onSaved)
                                } else {
                                    viewModel.create(title, description, location, date,
                                        photoUri, handover) {
                                        draftViewModel.clearAfterSuccessfulSave()
                                        onSaved()
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
