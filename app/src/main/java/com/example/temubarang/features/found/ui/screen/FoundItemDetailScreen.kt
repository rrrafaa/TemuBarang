package com.example.temubarang.features.found.ui.screen

import com.example.temubarang.R
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.ui.component.AppDetailRow
import com.example.temubarang.ui.component.AppStatusBadge
import com.example.temubarang.features.found.ui.component.FoundSectionCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.temubarang.features.found.model.canManage
import com.example.temubarang.features.found.model.canConfirmReturn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalUriHandler
import android.net.Uri
import com.example.temubarang.features.found.model.foundCategories
import com.example.temubarang.features.found.model.custodyTypes
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.ui.component.FoundPhoto
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandNavy

@Composable
fun FoundItemDetailScreen(
    viewModel: FoundItemsViewModel,
    itemId: String,
    onBack: () -> Unit,
    onManaged: () -> Unit,
    onEdit: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var contactError by rememberSaveable(itemId) { mutableStateOf<String?>(null) }
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(itemId) { viewModel.load(itemId) }
    var confirmArchive by rememberSaveable(itemId) { mutableStateOf(false) }
    val item = state.detail?.takeIf { it.id == itemId }
    val canManage = state.canManage(itemId, viewModel.userId)
    val canConfirmReturn = state.canConfirmReturn(itemId, viewModel.userId)
    var confirmReturn by rememberSaveable(itemId) { mutableStateOf(false) }

    if (confirmArchive && item != null && canManage) {
        AlertDialog(
            onDismissRequest = { if (!state.isSaving) confirmArchive = false },
            title = { Text("Arsipkan laporan?") },
            text = { Text("Laporan dan foto akan disimpan bersama riwayat klaim. Laporan tidak lagi tampil di daftar.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    confirmArchive = false
                    viewModel.archive(itemId, onManaged)
                }) { Text("Arsipkan", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(enabled = !state.isSaving, onClick = { confirmArchive = false }) { Text("Batal") }
            }
        )
    }
    if (confirmReturn && canConfirmReturn) {
        AlertDialog(
            onDismissRequest = { confirmReturn = false },
            title = { Text("Konfirmasi pengembalian?") },
            text = { Text("Lanjutkan jika barang sudah diserahkan kepada pemiliknya.") },
            confirmButton = {
                TextButton(enabled = !state.isSaving, onClick = {
                    confirmReturn = false
                    viewModel.confirmReturn(itemId, onManaged)
                }) { Text("Konfirmasi") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReturn = false }) { Text("Batal") }
            }
        )
    }
    AppPage("Detail Barang Ditemukan", onBack = onBack, onRefresh = { viewModel.load(itemId) },
        refreshing = state.isLoadingDetail || state.reporterLoading, enabled = !state.isSaving) {
        Column(
            modifier = Modifier.fillMaxSize().background(BrandBackground)
                .navigationBarsPadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FoundMessage(state.error)
            FoundMessage(state.notice, isError = false)
            if (state.isLoadingDetail) {
                CircularProgressIndicator()
            } else if (item == null) {
                Text("Detail tidak tersedia atau laporan sudah dihapus.")
            } else {
                FoundSectionCard("Informasi Barang") {
                    FoundPhoto(viewModel.photoUrl(item.imagePath), Modifier.fillMaxWidth().height(230.dp))
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, color = BrandNavy)
                    AppDetailRow("Deskripsi", item.description)
                    AppDetailRow("Lokasi penemuan", item.foundLocation)
                    AppDetailRow("Tanggal ditemukan", item.foundAt)
                    AppDetailRow("Kampus", item.campus.ifBlank { "Belum diisi" })
                    AppDetailRow("Kategori", foundCategories[item.category] ?: item.category)
                    AppDetailRow("Penyimpan barang", custodyTypes[item.custodyType] ?: item.custodyType)
                    AppDetailRow("Lokasi pengambilan", item.pickupLocation.ifBlank { "Belum diisi oleh pelapor" })
                    if (item.pickupNotes.isNotBlank()) AppDetailRow("Petunjuk pengambilan", item.pickupNotes)
                    AppStatusBadge(item.status.foundStatusLabel(), positive = item.status == "open")
                }
                FoundSectionCard("Pelapor") {
                    if (state.reporterLoading) CircularProgressIndicator()
                    FoundMessage(state.reporterError)
                    if (state.reporterError != null) TextButton(onClick = { viewModel.load(itemId) }) { AppIcon(R.drawable.ic_refresh_cw, null); Text(" Muat ulang pelapor") }
                    state.reporter?.let { reporter ->
                        AppDetailRow("Nama", reporter.displayName)
                        AppDetailRow("Kampus pelapor", reporter.campus.ifBlank { "Belum diisi" })
                        if (reporter.contactEmail.isNullOrBlank() && reporter.phone.isNullOrBlank()) {
                            Text("Pelapor tidak membagikan kontak. Ikuti petunjuk pengambilan yang tersedia.")
                        }
                        reporter.contactEmail?.takeIf { it.isNotBlank() }?.let { email ->
                            TextButton(onClick = {
                                try { uriHandler.openUri("mailto:" + Uri.encode(email, "@")) }
                                catch (_: Exception) { contactError = "Aplikasi email tidak tersedia. Gunakan alamat kontak yang ditampilkan." }
                            }) { AppIcon(R.drawable.ic_mail_duotone, null); Text(" Email: $email") }
                        }
                        reporter.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                            TextButton(onClick = {
                                try { uriHandler.openUri("tel:" + Uri.encode(phone, "+")) }
                                catch (_: Exception) { contactError = "Aplikasi telepon tidak tersedia. Gunakan nomor yang ditampilkan." }
                            }) { AppIcon(R.drawable.ic_phone_duotone, null); Text(" Telepon: $phone") }
                        }
                        Text("Hubungi pelapor untuk koordinasi pengambilan dan siapkan bukti kepemilikan.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (!state.reporterLoading && state.reporter == null && state.reporterError == null) {
                        Text("Pelapor belum melengkapi profil.")
                    }
                    FoundMessage(contactError)
                }
                if (canManage) {
                    GradientPrimaryButton("Edit Laporan / Ubah Status", enabled = !state.isSaving,
                        onClick = { onEdit(item.id) })
                    OutlinedButton(
                        onClick = { confirmArchive = true }, enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Arsipkan Laporan") }
                } else if (canConfirmReturn) {
                    Text("Laporan terkunci karena klaim telah disetujui.")
                    GradientPrimaryButton("Konfirmasi Pengembalian", enabled = !state.isSaving,
                        onClick = { confirmReturn = true })
                } else if (viewModel.userId == item.reporterId && state.claimStatus == "pending") {
                    Text("Laporan terkunci selama klaim kepemilikan sedang diproses.")
                }
            }
        }
    }
}

