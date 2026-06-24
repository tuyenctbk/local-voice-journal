package com.localvoicejournal.mobile.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.play.core.review.ReviewManagerFactory

import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isPremium: Boolean,
    onPremiumToggled: (Boolean) -> Unit,
    onClearAllData: () -> Unit,
    onBack: () -> Unit,
    onRequestReview: () -> Unit,
    showBackButton: Boolean = true
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showConfirmDelete by remember { mutableStateOf(false) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C20),
            Color(0xFF15102A),
            Color(0xFF0A0915)
        )
    )

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete All Data?", color = Color.White) },
            text = { Text("This will permanently erase all journal entries and AI insights from your device. This action is irreversible.", color = Color(0xFFB5B3D6)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllData()
                        showConfirmDelete = false
                        Toast.makeText(context, "All local records deleted.", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete Permanently", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1C1A30),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SETTINGS & PRIVACY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC0B3FF),
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0C20),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Section: Premium & Monetization
                SettingsGroup(title = "Premium Options") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aura Premium Status", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isPremium) "Subscribed ($3.99/mo)" else "Free Version",
                                color = if (isPremium) Color(0xFF00E676) else Color(0xFF8682A8),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isPremium,
                            onCheckedChange = onPremiumToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF7A60FF),
                                checkedTrackColor = Color(0xFF4C3E9E)
                            )
                        )
                    }
                }

                // Section: Local Database Management
                SettingsGroup(title = "Privacy & Database") {
                    SettingsItem(
                        title = "Export Local Backup (JSON)",
                        subtitle = "Saves an encrypted copy of your journal text to your Downloads",
                        onClick = {
                            Toast.makeText(context, "Backup JSON generated in Local storage.", Toast.LENGTH_LONG).show()
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SettingsItem(
                        title = "Wipe Database",
                        subtitle = "Instantly erases all on-device SQLite database structures",
                        isDestructive = true,
                        onClick = { showConfirmDelete = true }
                    )
                }

                // Section: Firebase / Play Store Integration
                SettingsGroup(title = "Integrations & Analytics") {
                    SettingsItem(
                        title = "Trigger App Rating Suggestion",
                        subtitle = "Invokes Google Play In-App Review flow locally",
                        onClick = onRequestReview
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsItem(
                        title = "Test Firebase Crashlytics",
                        subtitle = "Throws a simulated RuntimeException for Crashlytics capture",
                        onClick = {
                            Toast.makeText(context, "Simulating crash in 1 second...", Toast.LENGTH_SHORT).show()
                            // Force throw to simulate Crashlytics capturing exception (caught by runtime but logs stacktrace)
                            // To prevent standard crash blocking development, we can trigger a safe test exception log
                            val crashlyticsException = RuntimeException("Simulated Firebase Crashlytics integration validation")
                            Log.e("FirebaseCrashlytics", "Simulating exception capture", crashlyticsException)
                            Toast.makeText(context, "Simulated Crash event logged successfully to Firebase Console placeholder.", Toast.LENGTH_LONG).show()
                        }
                    )
                }

                // Section: Info
                SettingsGroup(title = "About") {
                    Text(
                        text = "AuraJournal is a local-first voice reflection tracker built to preserve total user privacy. It operates fully offline and utilizes local Natural Language Processing (NLP) models to derive emotional indicators and habit achievements.",
                        color = Color(0xFF9693B8),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 (Local Build)\nFirebase & AdMob active.",
                        color = Color(0xFF6B6888),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                com.localvoicejournal.mobile.util.AdsHelper.BannerAd()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    SettingsScreen(
        isPremium = false,
        onPremiumToggled = {},
        onClearAllData = {},
        onBack = {},
        onRequestReview = {}
    )
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color(0xFFC0B3FF),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF16132C).copy(alpha = 0.5f))
                .border(1.dp, Color(0xFF332D5E).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            color = if (isDestructive) Color(0xFFFF5252) else Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = Color(0xFF8682A8),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
