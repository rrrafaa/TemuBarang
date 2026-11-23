package com.example.temubarang.features.lost.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.component.FoundChoiceRow
import com.example.temubarang.features.found.ui.component.FoundFormField
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.lost.model.RecoverySubmission
import com.example.temubarang.features.lost.ui.component.DatePickerField
import com.example.temubarang.features.lost.ui.component.LostPhoto
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.*
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.UUID

private val periods = linkedMapOf("0" to "Pagi", "12" to "Siang", "15" to "Sore", "18" to "Malam")

@Composable
fun RecoveryDraftScreen(viewModel: LostItemsViewModel, itemId: String, onBack: () -> Unit, onSubmitted: () -> Unit) {
    val recovery by viewModel.recovery.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }
    var location by rememberSaveable(itemId) { mutableStateOf("") }
    var date by rememberSaveable(itemId) { mutableStateOf(LocalDate.now().toString()) }
    var period by rememberSaveable(itemId) { mutableStateOf("") }
    var condition by rememberSaveable(itemId) { mutableStateOf("") }
    var custodian by rememberSaveable(itemId) { mutableStateOf("") }
    var pickup by rememberSaveable(itemId) { mutableStateOf("") }
    var notes by rememberSaveable(itemId) { mutableStateOf("") }
    var submissionId by rememberSaveable(itemId) { mutableStateOf(UUID.randomUUID().toString()) }
    var photo by rememberSaveable(itemId) { mutableStateOf<String?>(null) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var firstError by remember { mutableStateOf<String?>(null) }
    var submitVersion by remember { mutableIntStateOf(0) }
    fun clearError(key: String) { errors = errors - key }
    val enabled = !recovery.busy
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            submissionId = UUID.randomUUID().toString()
            photo = it.toString()
        }
    }

    BackHandler(enabled = !enabled) {}
    LaunchedEffect(itemId) {
        viewModel.clearRecoveryMessage()
        viewModel.loadDetail(itemId)
    }
    AppPage("Konfirmasi Temuan", onBack = onBack, enabled = enabled) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (item == null || item.status != "open" || viewModel.ownsReport(itemId)) {
                Text(state.error ?: "Laporan tidak tersedia untuk konfirmasi temuan.")
            } else {
                FoundMessage(recovery.error)
                AppSectionCard("Laporan Kehilangan") {
                    AppDetailRow("Barang", item.title)
                    AppDetailRow("Lokasi terakhir", item.lastSeenLocation)
                    AppDetailRow("Tanggal hilang", item.lostAt)
                }
                AppSectionCard("Detail Penemuan") {
                    FoundFormField("Lokasi penemuan *", "Contoh: Perpustakaan", location,
                        { location = it; clearError("location") }, enabled = enabled, errorText = errors["location"],
                        isFirstError = firstError == "location", submitVersion = submitVersion)
                    AppValidationAnchor(firstError == "date", submitVersion) {
                        DatePickerField("Tanggal penemuan *", date,
                            { date = it; clearError("date"); clearError("period") }, enabled, errorText = errors["date"])
                    }
                    AppValidationAnchor(firstError == "period", submitVersion) {
                        Column {
                            val optionsModifier = if (errors["period"] == null) Modifier else
                                Modifier.border(1.dp, MaterialTheme.colorScheme.error, MaterialTheme.shapes.small).padding(8.dp)
                            Box(optionsModifier) {
                                FoundChoiceRow("Perkiraan waktu *", periods, period,
                                    { period = it; clearError("period") }, enabled)
                            }
                            FoundMessage(errors["period"])
                        }
                    }
                    FoundFormField("Kondisi barang *", "Jelaskan kondisi saat ditemukan", condition,
                        { condition = it; clearError("condition") }, enabled = enabled, errorText = errors["condition"],
                        isFirstError = firstError == "condition", submitVersion = submitVersion)
                    OutlinedButton(enabled = enabled, onClick = { picker.launch("image/*") }) {
                        Text(if (photo == null) "Pilih foto pendukung (opsional)" else "Ganti foto pendukung")
                    }
                    photo?.let { LostPhoto(it, Modifier.fillMaxWidth().height(160.dp)) }
                }
                AppSectionCard("Informasi Pengambilan") {
                    FoundFormField("Penyimpan barang *", "Contoh: Petugas kampus", custodian,
                        { custodian = it; clearError("custodian") }, enabled = enabled, errorText = errors["custodian"],
                        isFirstError = firstError == "custodian", submitVersion = submitVersion)
                    FoundFormField("Lokasi pengambilan *", "Contoh: Pos keamanan", pickup,
                        { pickup = it; clearError("pickup") }, enabled = enabled, errorText = errors["pickup"],
                        isFirstError = firstError == "pickup", submitVersion = submitVersion)
                    FoundFormField("Catatan tambahan", "Opsional", notes,
                        { notes = it; clearError("notes") }, enabled = enabled, errorText = errors["notes"],
                        isFirstError = firstError == "notes", submitVersion = submitVersion)
                }
                GradientPrimaryButton("Kirim Pengajuan", enabled = enabled, loading = !enabled, onClick = {
                    val now = ZonedDateTime.now()
                    val day = runCatching { LocalDate.parse(date) }.getOrNull()
                    val hour = period.toIntOrNull()
                    val foundAt = if (day != null && hour != null) day.atTime(hour, 0).atZone(now.zone) else null
                    errors = buildMap {
                        if (location.trim().length !in 2..200) put("location", "Isi lokasi penemuan (2–200 karakter).")
                        if (day == null || day.isAfter(now.toLocalDate())) put("date", "Pilih hari ini atau tanggal sebelumnya.")
                        if (hour == null) put("period", "Pilih perkiraan waktu penemuan.")
                        else if (foundAt?.isAfter(now) == true && !containsKey("date")) put("period", "Pilih waktu yang sudah lewat.")
                        if (condition.trim().length !in 2..1000) put("condition", "Isi kondisi barang (2–1000 karakter).")
                        if (custodian.trim().length !in 2..120) put("custodian", "Isi nama pihak penyimpan (2–120 karakter).")
                        if (pickup.trim().length !in 2..200) put("pickup", "Isi lokasi pengambilan (2–200 karakter).")
                        if (notes.trim().length > 1000) put("notes", "Catatan maksimal 1000 karakter.")
                    }
                    firstError = errors.keys.firstOrNull()
                    submitVersion++
                    if (errors.isEmpty() && foundAt != null) {
                        viewModel.submitRecovery(RecoverySubmission(
                            submissionId, itemId, location.trim(), foundAt.toInstant().toString(),
                            condition.trim(), custodian.trim(), pickup.trim(), notes.trim(), photo != null
                        ), photo?.let(Uri::parse), onSubmitted)
                    }
                })
            }
        }
    }
}
