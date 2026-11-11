package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// TODO: Pemilik fitur menambahkan ViewModel, LazyColumn/Grid, form Create, dan Repository.
@Composable
fun LostItemsScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Laporan Barang Hilang", style = MaterialTheme.typography.headlineMedium)
        Text("Fitur anggota 1 belum diimplementasikan. Tabel: lost_items.")
        Button(onClick = onBack) { Text("Kembali") }
    }
}
