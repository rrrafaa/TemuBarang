package com.example.temubarang.features.found.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.R
import com.example.temubarang.features.found.model.filterFoundItems
import com.example.temubarang.features.found.ui.component.FoundItemCard
import com.example.temubarang.features.found.ui.component.FoundMessage
import com.example.temubarang.features.found.viewmodel.FoundItemsViewModel
import com.example.temubarang.ui.component.AppPage
import com.example.temubarang.ui.component.AppIcon
import com.example.temubarang.ui.component.GradientPrimaryButton
import com.example.temubarang.ui.component.AppSearchBar
import com.example.temubarang.ui.component.AppFilterChip
import androidx.compose.ui.graphics.Color
import com.example.temubarang.ui.theme.BrandBackground

@Composable
fun FoundItemsScreen(
    viewModel: FoundItemsViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onDetail: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var onlyOpen by rememberSaveable { mutableStateOf(true) }
    var onlyMine by rememberSaveable { mutableStateOf(false) }
    val visible = filterFoundItems(state.items, query, onlyOpen, onlyMine, viewModel.userId)
    LaunchedEffect(Unit) { viewModel.refresh() }

    AppPage("Barang Ditemukan", onBack = onBack, onRefresh = viewModel::refresh, refreshing = state.isLoading) {
        Column(Modifier.fillMaxSize()) {
            // The list owns scrolling; the primary action stays above the parent navbar.
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    AppSearchBar(value = query, onValueChange = { query = it },
                        placeholder = "Cari barang, kampus, atau lokasi")
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppFilterChip(onlyOpen, { onlyOpen = !onlyOpen }, label = "Tersedia")
                        AppFilterChip(onlyMine, { onlyMine = !onlyMine }, label = "Laporan saya")
                    }
                }
                item { FoundMessage(state.error); FoundMessage(state.notice, isError = false) }
                if (state.isLoading && state.items.isEmpty()) item { CircularProgressIndicator() }
                else if (visible.isEmpty()) item { Text("Tidak ada laporan yang cocok. Coba ubah pencarian atau filter.") }
                else {
                    item { Text("${visible.size} laporan", style = MaterialTheme.typography.labelMedium) }
                    items(visible, key = { it.id }) { item ->
                        FoundItemCard(item, viewModel.photoUrl(item.imagePath)) {
                            viewModel.clearMessage()
                            onDetail(item.id)
                        }
                    }
                }
            }
            Surface(color = Color.Transparent) {
                GradientPrimaryButton(
                    text = "Tambah Laporan", modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    leadingIcon = { AppIcon(R.drawable.ic_circle_plus_duotone, null) },
                    onClick = { viewModel.clearMessage(); onCreate() }
                )
            }
        }
    }
}
