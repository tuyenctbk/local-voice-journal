package com.localvoicejournal.mobile.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumScreen(
    onClose: () -> Unit,
    onSubscribeSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C20),
            Color(0xFF1B143F), // Lighter purple highlight
            Color(0xFF0A0915)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Close header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close paywall",
                        tint = Color.White
                    )
                }
            }

            // Top Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AURA PREMIUM",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC0B3FF),
                    letterSpacing = 4.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Unlock the full power of offline reflection",
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Features List
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PremiumFeatureItem(
                    title = "Weekly Stress Trends",
                    description = "Access interactive timeline graphs tracking your emotional and stress level distributions over time."
                )
                PremiumFeatureItem(
                    title = "Encrypted Local Backups",
                    description = "Export and import your SQLite database locally with passcode encryption to ensure data survival."
                )
                PremiumFeatureItem(
                    title = "Advanced AI Themes",
                    description = "Unlock granular local theme categorizations and smart sleep/work stress suggestions."
                )
                PremiumFeatureItem(
                    title = "Zero Ad Placement",
                    description = "Enjoy a completely clean interface with all AdMob promotional placeholder cards fully removed."
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Pricing & Action Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$3.99 / month",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Cancel anytime. Billed locally.",
                    fontSize = 12.sp,
                    color = Color(0xFF8682A8),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Aura Premium Activated!", Toast.LENGTH_SHORT).show()
                        onSubscribeSuccess()
                        onClose()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A60FF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "Subscribe Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        Toast.makeText(context, "Purchases restored.", Toast.LENGTH_SHORT).show()
                        onSubscribeSuccess()
                        onClose()
                    }
                ) {
                    Text(
                        text = "Restore Purchase",
                        color = Color(0xFF9693B8),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF16132C).copy(alpha = 0.5f))
            .border(1.dp, Color(0xFF332D5E).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✦",
                    color = Color(0xFFC0B3FF),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = Color(0xFF9693B8),
                lineHeight = 18.sp
            )
        }
    }
}
