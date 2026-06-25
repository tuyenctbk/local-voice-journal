package com.localvoicejournal.mobile.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object AdsHelper {
    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        // Ads hidden for clean screenshot generation
        Spacer(modifier = modifier.height(0.dp))
    }
}
