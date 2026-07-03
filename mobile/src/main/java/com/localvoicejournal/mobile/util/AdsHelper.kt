package com.localvoicejournal.mobile.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object AdsHelper {
    /**
     * Standard banner ad at bottom of screens.
     */
    @Composable
    fun BannerAd(modifier: Modifier = Modifier, isPremium: Boolean = false) {
        if (isPremium) return
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color(0xFF141225))
                .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Ad Banner", color = Color(0xFF4C4670), fontSize = 10.sp)
        }
    }

    /**
     * Native ad designed to look like a journal entry.
     * Ideal for the History list (DashboardScreen).
     */
    @Composable
    fun NativeAd(modifier: Modifier = Modifier, isPremium: Boolean = false) {
        if (isPremium) return

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF16132C).copy(alpha = 0.4f))
                .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPONSORED",
                        color = Color(0xFFC0B3FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7A60FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Ad", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AuraJournal Premium: Experience the deepest sensory reflections without any distractions.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }

    /**
     * Full-screen interstitial logic (Placeholder)
     */
    fun showInterstitial(context: android.content.Context) {
        // In real AdMob, we would load and show the InterstitialAd here.
        // For development, we log it or show a toast if needed.
        android.util.Log.d("AdsHelper", "Interstitial Ad would be shown here.")
    }
}
