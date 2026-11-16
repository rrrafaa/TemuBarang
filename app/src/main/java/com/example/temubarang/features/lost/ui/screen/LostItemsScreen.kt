package com.example.temubarang.features.lost.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.component.AppPage

@Composable
fun LostItemsScreen(onBack: () -> Unit) {
    AppPage("Barang Hilang", onBack = onBack) {
        Text("Fitur laporan barang hilang sedang dikembangkan.", Modifier.padding(24.dp))
    }
}
