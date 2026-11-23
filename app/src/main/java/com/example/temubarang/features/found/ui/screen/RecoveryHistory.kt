package com.example.temubarang.features.found.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.AppDetailRow
import com.example.temubarang.ui.component.AppSectionCard

@Composable
fun RecoveryHistory(viewModel: LostItemsViewModel, query: String) {
    val state by viewModel.recovery.collectAsState()
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingStatus by rememberSaveable { mutableStateOf<String?>(null) }
    val requests = state.items.filter { it.title.contains(query.trim(), ignoreCase = true) }
    val selected = state.items.firstOrNull { it.id == selectedId }
    LaunchedEffect(Unit) { viewModel.loadRecovery() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        state.error?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
        item { TextButton(enabled = !state.busy, onClick = { viewModel.loadRecovery() }) { Text("Muat ulang") } }
        val groups = listOf(
            "Temuan yang Saya Ajukan" to requests.filter { it.finderId == viewModel.userId },
            "Temuan atas Laporan Saya" to requests.filter { it.reporterId == viewModel.userId }
        )
        groups.forEach { (title, rows) ->
            item { Text(title, style = MaterialTheme.typography.titleMedium) }
            if (rows.isEmpty()) item { Text("Tidak ada pengajuan yang ditampilkan.") }
            items(rows, key = { it.id }) { request ->
                AppSectionCard(request.title) {
                    Text(request.statusLabel)
                    TextButton(onClick = { selectedId = request.id }) { Text("Lihat Detail") }
                }
            }
        }
    }

    if (selected != null) {
        val isReporter = selected.reporterId == viewModel.userId
        val decision = pendingStatus
        AlertDialog(
            onDismissRequest = { selectedId = null; pendingStatus = null },
            title = { Text(selected.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(selected.statusLabel)
                    AppDetailRow("Lokasi penemuan", selected.location)
                    AppDetailRow("Waktu penemuan", selected.foundAt)
                    AppDetailRow("Kondisi", selected.condition)
                    AppDetailRow("Penyimpan", selected.custodian)
                    AppDetailRow("Lokasi pengambilan", selected.pickup)
                    if (selected.notes.isNotBlank()) AppDetailRow("Catatan", selected.notes)
                    if (isReporter && decision == null && selected.status in setOf("pending", "confirmed")) {
                        TextButton(onClick = {
                            pendingStatus = if (selected.status == "pending") "confirmed" else "completed"
                        }) { Text(if (selected.status == "pending") "Terima Temuan" else "Barang Sudah Diterima") }
                        TextButton(onClick = { pendingStatus = "rejected" }) {
                            Text(if (selected.status == "pending") "Tolak Temuan" else "Batalkan Serah Terima")
                        }
                    }
                    if (decision != null) Text("Simpan perubahan status pengajuan ini?")
                }
            },
            confirmButton = {
                TextButton(enabled = !state.busy, onClick = {
                    if (decision != null) viewModel.reviewRecovery(selected.id, decision)
                    pendingStatus = null
                    selectedId = null
                }) { Text(if (decision == null) "Tutup" else "Simpan") }
            },
            dismissButton = {
                if (decision != null) TextButton(onClick = { pendingStatus = null }) { Text("Kembali") }
            }
        )
    }
}
