package com.example.temubarang.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.temubarang.ui.theme.BrandBackground
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.theme.BrandSoftBlue

@Composable
fun FeaturePlaceholderScreen(title: String, table: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(BrandBackground).systemBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, color = BrandNavy)
        Surface(color = BrandSoftBlue, shape = RoundedCornerShape(20.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fitur dalam pengembangan", style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                Text("Anggota penanggung jawab akan membuat form, daftar data LazyColumn, dan ViewModel.", color = BrandMuted)
                Text("Tabel Supabase: $table", color = BrandNavy, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Kembali ke Beranda") }
    }
}
