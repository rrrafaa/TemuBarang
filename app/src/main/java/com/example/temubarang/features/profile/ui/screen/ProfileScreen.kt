package com.example.temubarang.features.profile.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.temubarang.R
import com.example.temubarang.features.found.ui.component.FoundFormField
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.profile.viewmodel.ProfileViewModel
import com.example.temubarang.features.profile.model.ProfileValidator
import com.example.temubarang.ui.component.*
import com.example.temubarang.ui.theme.BrandIndigo

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLogout: () -> Unit, isLoggingOut: Boolean, logoutError: String?) {
    val state by viewModel.state.collectAsState()
    val profile = state.profile
    var editing by rememberSaveable { mutableStateOf(false) }
    var info by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable(profile?.id) { mutableStateOf(profile?.displayName.orEmpty()) }
    var campus by rememberSaveable(profile?.id) { mutableStateOf(profile?.campus.orEmpty()) }
    var email by rememberSaveable(profile?.id) { mutableStateOf(profile?.contactEmail.orEmpty()) }
    var phone by rememberSaveable(profile?.id) { mutableStateOf(profile?.phone.orEmpty()) }
    var share by rememberSaveable(profile?.id) { mutableStateOf(profile?.shareContact ?: false) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var firstError by remember { mutableStateOf<String?>(null) }
    var submitVersion by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    fun clearError(key: String) { errors = errors - key }
    val enabled = profile != null && !state.loading && !state.saving && !isLoggingOut
    fun edit() {
        name = profile?.displayName.orEmpty(); campus = profile?.campus.orEmpty()
        email = profile?.contactEmail.orEmpty(); phone = profile?.phone.orEmpty()
        share = profile?.shareContact ?: false; editing = true
        errors = emptyMap(); firstError = null
    }
    androidx.activity.compose.BackHandler(enabled = editing && !state.saving) { editing = false }
    if (info != null) AlertDialog(onDismissRequest = { info = null },
        title = { Text(if (info == "about") "Tentang TemuBarang" else "Panduan Pengambilan") },
        text = { Text(if (info == "about") "TemuBarang membantu warga kampus melaporkan barang hilang dan ditemukan, serta mengoordinasikan pengembalian kepada pemiliknya."
            else "Baca lokasi dan petunjuk pengambilan pada detail barang. Hubungi pelapor jika kontak dibagikan, lalu siapkan bukti kepemilikan. Jangan membagikan ciri rahasia barang di laporan publik.") },
        confirmButton = { TextButton(onClick = { info = null }) { Text("Mengerti") } })
    AppPage(if (editing) "Kelola Profil" else "Profil Saya",
        onBack = if (editing) ({ editing = false }) else null,
        onRefresh = if (editing) null else viewModel::load, refreshing = state.loading, enabled = !state.saving && !isLoggingOut) {
        LazyColumn(
            Modifier.fillMaxSize().imePadding(), state = listState,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { FoundMessage(state.error); FoundMessage(state.notice, isError = false); FoundMessage(logoutError) }
            if (state.loading) item { CircularProgressIndicator() }
            if (!editing) {
                item {
                    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
                        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Surface(Modifier.size(58.dp), shape = CircleShape, color = BrandIndigo.copy(alpha = .1f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(profile?.displayName?.trim()?.take(1)?.uppercase()?.ifBlank { "T" } ?: "T",
                                        style = MaterialTheme.typography.headlineSmall, color = BrandIndigo)
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(profile?.displayName?.ifBlank { "Teman Kampus" } ?: "Teman Kampus", fontWeight = FontWeight.Bold)
                                Text(profile?.campus?.ifBlank { "Lengkapi kampusmu" } ?: "Profil belum tersedia", style = MaterialTheme.typography.bodySmall)
                                if (!profile?.contactEmail.isNullOrBlank()) Text(profile!!.contactEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    ProfileGroup("Akun") {
                        ProfileMenuRow("Kelola profil", "Nama tampilan dan kampus", R.drawable.ic_user_round_duotone, enabled) { edit() }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))
                        ProfileMenuRow("Kontak dan privasi", if (profile?.shareContact == true) "Kontak dibagikan pada laporan" else "Kontak tidak dibagikan", R.drawable.ic_mail_duotone, enabled) { edit() }
                    }
                }
                item {
                    ProfileGroup("Bantuan") {
                        ProfileMenuRow("Panduan pengambilan", "Cara mengembalikan barang dengan aman", R.drawable.ic_package_check_duotone) { info = "help" }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .4f))
                        ProfileMenuRow("Tentang TemuBarang", "Barang kembali, kampus lebih peduli", R.drawable.ic_house_duotone) { info = "about" }
                    }
                }
                item {
                    OutlinedButton(onClick = onLogout, enabled = !state.saving && !isLoggingOut, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isLoggingOut) "Sedang keluar…" else "Keluar Akun", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else if (profile != null) {
                item { Text("Identitas", style = MaterialTheme.typography.titleMedium) }
                item {
                    FoundFormField("Nama tampilan *", "Contoh: Rafa", name,
                        { name = it; clearError("name") }, enabled = enabled,
                        errorText = errors["name"], isFirstError = firstError == "name",
                        submitVersion = submitVersion)
                }
                item {
                    FoundFormField("Kampus *", "Contoh: Universitas Brawijaya", campus,
                        { campus = it; clearError("campus") }, enabled = enabled,
                        errorText = errors["campus"], isFirstError = firstError == "campus",
                        submitVersion = submitVersion)
                }
                item { Text("Kontak dan privasi", style = MaterialTheme.typography.titleMedium) }
                item {
                    FoundFormField("Email kontak (opsional)", "Contoh: rafa@example.com", email,
                        { email = it; clearError("email") }, enabled = enabled,
                        errorText = errors["email"], isFirstError = firstError == "email",
                        submitVersion = submitVersion)
                }
                item {
                    FoundFormField("Telepon (opsional)", "Contoh: +628123456789", phone,
                        { phone = it; clearError("phone") }, enabled = enabled,
                        errorText = errors["phone"], isFirstError = firstError == "phone",
                        submitVersion = submitVersion)
                }
                item {
                    Surface(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Bagikan kontak pada laporan saya", Modifier.weight(1f))
                                Switch(share, { share = it; clearError("share") }, enabled = enabled)
                            }
                            errors["share"]?.let {
                                Text(it, color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Nama dan kampus terlihat oleh pengguna yang login melalui detail laporan. Email dan telepon hanya dibagikan jika opsi ini aktif. Email kontak tidak mengubah email login.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                item {
                    GradientPrimaryButton("Simpan Profil", enabled = enabled, loading = state.saving,
                        onClick = {
                            val found = ProfileValidator.validateFields(name, campus, email, phone, share)
                            errors = found
                            firstError = found.keys.firstOrNull()
                            if (found.isNotEmpty()) {
                                submitVersion++
                                val position = when (firstError) {
                                    "name" -> 2; "campus" -> 3; "email" -> 5
                                    "phone" -> 6; "share" -> 7; else -> 2
                                }
                                scope.launch { listState.animateScrollToItem(position) }
                            } else {
                                viewModel.save(name, campus, email, phone, share)
                            }
                        })
                }
            }
        }
    }
}

@Composable
private fun ProfileGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, Modifier.padding(start = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge)
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
            Column(content = content)
        }
    }
}

@Composable
private fun ProfileMenuRow(title: String, subtitle: String, @androidx.annotation.DrawableRes icon: Int, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppIcon(icon, null)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
