package com.example.temubarang.features.found.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.*
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecoveryHistory(viewModel: LostItemsViewModel, query: String, onDetail: (String) -> Unit) {
    val state by viewModel.recovery.collectAsState()
    var incoming by rememberSaveable { mutableStateOf(false) }
    val requests = state.items.filter {
        (if (incoming) it.reporterId else it.finderId) == viewModel.userId &&
            it.title.contains(query.trim(), ignoreCase = true)
    }
    LaunchedEffect(Unit) { viewModel.loadRecovery() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppFilterChip(!incoming, { incoming = false }, label = "Temuan yang Saya Ajukan")
                AppFilterChip(incoming, { incoming = true }, label = "Temuan atas Laporan Saya")
            }
        }
        if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        item {
            FoundMessage(state.error)
            FoundMessage(state.notice, isError = false)
        }
        if (requests.isEmpty() && !state.busy && state.error == null) {
            item {
                Text(when {
                    query.isNotBlank() -> "Tidak ada pengajuan yang cocok."
                    incoming -> "Belum ada temuan atas laporanmu."
                    else -> "Kamu belum mengajukan konfirmasi temuan."
                })
            }
        }
        items(requests, key = { it.id }) { request ->
            AppSectionCard(request.title) {
                AppStatusBadge(request.statusLabel, positive = request.status == "completed")
                TextButton(onClick = { onDetail(request.id) }) { Text("Lihat Detail") }
            }
        }
    }
}

@Composable
fun RecoveryDetailScreen(viewModel: LostItemsViewModel, requestId: String, onBack: () -> Unit) {
    val state by viewModel.recovery.collectAsState()
    val request = state.items.firstOrNull { it.id == requestId }
    var decision by rememberSaveable(requestId) { mutableStateOf<String?>(null) }
    LaunchedEffect(requestId) { if (request == null) viewModel.loadRecovery() }

    AppPage("Detail Konfirmasi Temuan", onBack = onBack, enabled = !state.busy) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FoundMessage(state.error)
            if (request == null) {
                Text(if (state.busy) "Memuat pengajuan…" else "Pengajuan tidak tersedia.")
            } else {
                AppSectionCard("Detail Penemuan") {
                    Text(request.title, style = MaterialTheme.typography.headlineSmall)
                    AppStatusBadge(request.statusLabel, positive = request.status == "completed")
                    AppDetailRow("Lokasi penemuan", request.location)
                    AppDetailRow("Perkiraan waktu", formatRecoveryTime(request.foundAt))
                    AppDetailRow("Kondisi barang", request.condition)
                }
                AppSectionCard("Informasi Pengambilan") {
                    AppDetailRow("Penyimpan barang", request.custodian)
                    AppDetailRow("Lokasi pengambilan", request.pickup)
                    if (request.notes.isNotBlank()) AppDetailRow("Catatan tambahan", request.notes)
                }
                if (request.reporterId == viewModel.userId && request.status in setOf("pending", "confirmed")) {
                    val pending = request.status == "pending"
                    GradientPrimaryButton(if (pending) "Terima Temuan" else "Barang Sudah Diterima",
                        enabled = !state.busy, onClick = { decision = if (pending) "confirmed" else "completed" })
                    OutlinedButton(enabled = !state.busy, modifier = Modifier.fillMaxWidth(),
                        onClick = { decision = "rejected" }) {
                        Text(if (pending) "Tolak Temuan" else "Batalkan Serah Terima")
                    }
                }
            }
        }
    }
    val nextStatus = decision
    if (request != null && nextStatus != null) {
        AlertDialog(onDismissRequest = { decision = null },
            title = { Text("Konfirmasi tindakan") },
            text = { Text(if (nextStatus == "completed") "Pastikan barang sudah kamu terima."
                else "Simpan perubahan status pengajuan ini?") },
            confirmButton = {
                TextButton(enabled = !state.busy, onClick = {
                    viewModel.reviewRecovery(request.id, nextStatus)
                    decision = null
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { decision = null }) { Text("Batal") } }
        )
    }
}

private fun formatRecoveryTime(raw: String): String = runCatching {
    val time = OffsetDateTime.parse(raw).atZoneSameInstant(ZoneId.systemDefault())
    val period = when (time.hour) {
        in 0..11 -> "Pagi"
        in 12..14 -> "Siang"
        in 15..17 -> "Sore"
        else -> "Malam"
    }
    "${time.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID")))} · $period"
}.getOrDefault(raw)
