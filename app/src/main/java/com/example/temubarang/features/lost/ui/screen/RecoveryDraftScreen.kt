package com.example.temubarang.features.lost.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.ui.component.FoundFormField
import com.example.temubarang.features.lost.viewmodel.LostItemsViewModel
import com.example.temubarang.ui.component.*

@Composable
fun RecoveryDraftScreen(viewModel: LostItemsViewModel, itemId: String, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val item = state.items.firstOrNull { it.id == itemId }
    var location by rememberSaveable(itemId) { mutableStateOf("") }
    var time by rememberSaveable(itemId) { mutableStateOf("") }
    var condition by rememberSaveable(itemId) { mutableStateOf("") }
    var custodian by rememberSaveable(itemId) { mutableStateOf("") }
    var pickup by rememberSaveable(itemId) { mutableStateOf("") }
    var notes by rememberSaveable(itemId) { mutableStateOf("") }
    var photo by rememberSaveable(itemId) { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) photo = it.toString()
    }

    LaunchedEffect(itemId) { viewModel.loadDetail(itemId) }
    AppPage("Konfirmasi Temuan", onBack = onBack) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (item == null || item.status != "open" || viewModel.ownsReport(itemId)) {
                Text(state.error ?: "Laporan tidak tersedia untuk konfirmasi temuan.")
            } else {
                AppSectionCard("Laporan kehilangan") {
                    AppDetailRow("Barang", item.title)
                    AppDetailRow("Lokasi terakhir", item.lastSeenLocation)
                    AppDetailRow("Tanggal hilang", item.lostAt)
                }
                Text("Form ini belum dapat mengirim pengajuan.")
                FoundFormField("Lokasi penemuan", "Contoh: Perpustakaan", location, { location = it })
                FoundFormField("Waktu penemuan", "Tanggal dan jam", time, { time = it })
                FoundFormField("Kondisi barang", "Jelaskan kondisi saat ditemukan", condition, { condition = it })
                OutlinedButton(onClick = { picker.launch("image/*") }) {
                    Text(if (photo == null) "Pilih foto pendukung" else "Ganti foto pendukung")
                }
                photo?.let {
                    com.example.temubarang.features.lost.ui.component.LostPhoto(it, Modifier.height(160.dp))
                }
                FoundFormField("Penyimpan barang", "Contoh: Petugas kampus", custodian, { custodian = it })
                FoundFormField("Lokasi pengambilan", "Contoh: Pos keamanan", pickup, { pickup = it })
                FoundFormField("Catatan tambahan", "Opsional", notes, { notes = it })
                GradientPrimaryButton("Kirim Pengajuan", onClick = {}, enabled = false)
            }
        }
    }
}
