package com.example.temubarang.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.temubarang.R

/** File harus berada di res/drawable-nodpi/temubarang_logo.png. */
@Composable
fun BrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.temubarang_logo),
        contentDescription = "Logo TemuBarang",
        modifier = modifier.width(248.dp).height(192.dp),
        contentScale = ContentScale.Fit
    )
}
