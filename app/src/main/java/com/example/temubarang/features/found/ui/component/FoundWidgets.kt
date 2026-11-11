package com.example.temubarang.features.found.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.temubarang.features.found.model.FoundItem
import com.example.temubarang.features.found.model.foundStatusLabel
import com.example.temubarang.ui.theme.BrandMuted
import com.example.temubarang.ui.theme.BrandNavy
import com.example.temubarang.ui.theme.BrandSoftMint

@Composable
fun FoundMessage(message: String?, isError: Boolean = true) {
    if (message.isNullOrBlank()) return
    Surface(
        color = if (isError) MaterialTheme.colorScheme.errorContainer else BrandSoftMint,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            modifier = Modifier.padding(12.dp),
            color = if (isError) MaterialTheme.colorScheme.error else BrandNavy
        )
    }
}

@Composable
fun FoundPhoto(imageUrl: String?, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    if (imageUrl.isNullOrBlank()) {
        Surface(color = Color(0xFFEBF2FD), shape = shape, modifier = modifier) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("📦", style = MaterialTheme.typography.headlineMedium)
                Text("Tanpa foto", color = BrandMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
    } else {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto barang ditemukan",
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape)
        )
    }
}

@Composable
fun FoundItemCard(item: FoundItem, photoUrl: String?, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoundPhoto(photoUrl, Modifier.size(96.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = BrandNavy)
                Text(item.foundLocation, style = MaterialTheme.typography.bodySmall, color = BrandMuted)
                Text("Ditemukan: ${item.foundAt}", style = MaterialTheme.typography.bodySmall)
                Surface(shape = RoundedCornerShape(8.dp), color = BrandSoftMint) {
                    Text(item.status.foundStatusLabel(), color = BrandNavy,
                        style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(6.dp))
                }
            }
        }
    }
}
