package com.localvoicejournal.mobile.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object AdsHelper {

    private val TIPS = arrayOf(
        "Take a deep breath before reflecting for better clarity.",
        "Your voice never leaves this device. It's 100% private.",
        "Check your stress trends in the Dashboard weekly.",
        "Reflecting daily helps build a more resilient mindset.",
        "Upgrade to Premium for advanced AI theme insights!"
    )

    @Composable
    fun BannerAd(modifier: Modifier = Modifier) {
        val isPreview = LocalInspectionMode.current
        var isAdLoaded by remember { mutableStateOf(false) }
        val randomTip = remember { TIPS.random() }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color(0xFF1C1A30), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!isAdLoaded || isPreview) {
                Text(
                    text = "TIP: $randomTip",
                    color = Color(0xFFB5B3D6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            if (!isPreview) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.BANNER)
                            // Use test ad unit ID
                            adUnitId = "ca-app-pub-3940256099942544/6300978111"
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    isAdLoaded = true
                                }
                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isAdLoaded = false
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}
