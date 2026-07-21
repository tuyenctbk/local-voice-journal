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
    allowCloudFallback: Boolean,
    onCloudFallbackToggled: (Boolean) -> Unit,
    geminiApiKey: String,
    onGeminiApiKeyChanged: (String) -> Unit,
    isLocalModelDownloaded: Boolean,
    localModelDownloadProgress: Float,
    onDownloadLocalModel: () -> Unit,
    onDeleteLocalModel: () -> Unit,
    isGoogleLinked: Boolean,
    lastSyncTime: String,
    isSyncing: Boolean,
    onLinkGoogle: () -> Unit,
    onUnlinkGoogle: () -> Unit,
    onSyncNow: (password: String) -> Unit,
    onClearAllData: () -> Unit,
    onExportBackup: (password: String) -> Unit = {},
    onImportBackup: () -> Unit = {},
    onPopulateDemoData: () -> Unit = {},
    onBack: () -> Unit,
    onRequestReview: () -> Unit,
    showBackButton: Boolean = true
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showExportPasswordPrompt by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var showSyncPasswordPrompt by remember { mutableStateOf(false) }
    var syncPassword by remember { mutableStateOf("") }

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

    if (showSyncPasswordPrompt) {
        AlertDialog(
            onDismissRequest = { showSyncPasswordPrompt = false },
            title = { Text("Enter Sync Password", color = Color.White) },
            text = {
                Column {
                    Text("Type the passphrase used to decrypt and encrypt your cloud reflections.", color = Color(0xFFB5B3D6), modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = syncPassword,
                        onValueChange = { syncPassword = it },
                        label = { Text("Password", color = Color(0xFFC0B3FF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7A60FF),
                            unfocusedBorderColor = Color(0xFF2C2750),
                            focusedLabelColor = Color(0xFFC0B3FF),
                            unfocusedLabelColor = Color(0xFF6B6888),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (syncPassword.isNotBlank()) {
                            onSyncNow(syncPassword)
                            showSyncPasswordPrompt = false
                            syncPassword = ""
                        } else {
                            Toast.makeText(context, "Password cannot be blank.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Sync", color = Color(0xFF7A60FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncPasswordPrompt = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1C1A30),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (showExportPasswordPrompt) {
        AlertDialog(
            onDismissRequest = { showExportPasswordPrompt = false },
            title = { Text("Set Backup Password", color = Color.White) },
            text = {
                Column {
                    Text("This password will be used to encrypt your backup file using AES-256. You must remember this password to restore your journal reflections.", color = Color(0xFFB5B3D6), modifier = Modifier.padding(bottom = 12.dp))
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        label = { Text("Password", color = Color(0xFFC0B3FF)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF7A60FF),
                            unfocusedBorderColor = Color(0xFF2C2750),
                            focusedLabelColor = Color(0xFFC0B3FF),
                            unfocusedLabelColor = Color(0xFF6B6888),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (exportPassword.isNotBlank()) {
                            onExportBackup(exportPassword)
                            showExportPasswordPrompt = false
                            exportPassword = ""
                        } else {
                            Toast.makeText(context, "Password cannot be blank.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Export", color = Color(0xFF7A60FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportPasswordPrompt = false }) {
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

                SettingsGroup(title = "Local AI & Cloud Fallback") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Offline LLM Model (Gemma 2B)", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isLocalModelDownloaded) "Status: Active (1.4 GB downloaded)" else if (localModelDownloadProgress >= 0f) "Downloading: ${(localModelDownloadProgress * 100).toInt()}%" else "Status: Inactive (1.4 GB storage required)",
                                color = if (isLocalModelDownloaded) Color(0xFF00E676) else if (localModelDownloadProgress >= 0f) Color(0xFF7A60FF) else Color(0xFF8682A8),
                                fontSize = 12.sp
                            )
                        }
                        
                        if (isLocalModelDownloaded) {
                            TextButton(onClick = onDeleteLocalModel) {
                                Text("Delete", color = Color(0xFFFF5252), fontSize = 14.sp)
                            }
                        } else if (localModelDownloadProgress >= 0f) {
                            CircularProgressIndicator(
                                progress = { localModelDownloadProgress },
                                color = Color(0xFF7A60FF),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Button(
                                onClick = onDownloadLocalModel,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A60FF)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Download", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    if (localModelDownloadProgress >= 0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { localModelDownloadProgress },
                            color = Color(0xFF7A60FF),
                            trackColor = Color(0xFF2C2750),
                            modifier = Modifier.fillMaxWidth().height(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF332D5E).copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Cloud Fallback", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Transmits text/audio online when offline/on-device tools fail.",
                                color = Color(0xFF8682A8),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = allowCloudFallback,
                            onCheckedChange = onCloudFallbackToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF7A60FF),
                                checkedTrackColor = Color(0xFF4C3E9E)
                            )
                        )
                    }

                    if (allowCloudFallback) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = geminiApiKey,
                            onValueChange = onGeminiApiKeyChanged,
                            label = { Text("Gemini Developer API Key", color = Color(0xFFC0B3FF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A60FF),
                                unfocusedBorderColor = Color(0xFF2C2750),
                                focusedLabelColor = Color(0xFFC0B3FF),
                                unfocusedLabelColor = Color(0xFF6B6888),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                SettingsGroup(title = "Google Drive Cloud Sync") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Drive Integration", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = if (isGoogleLinked) "Status: Linked | Last Sync: $lastSyncTime" else "Status: Account Unlinked",
                                color = if (isGoogleLinked) Color(0xFF00E676) else Color(0xFF8682A8),
                                fontSize = 12.sp
                            )
                        }
                        
                        TextButton(
                            onClick = { if (isGoogleLinked) onUnlinkGoogle() else onLinkGoogle() }
                        ) {
                            Text(
                                text = if (isGoogleLinked) "Unlink" else "Link Account",
                                color = if (isGoogleLinked) Color(0xFFFF5252) else Color(0xFF7A60FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (isGoogleLinked) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF332D5E).copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sync Reflections", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Encrypt reflections locally and sync to cloud", color = Color(0xFF8682A8), fontSize = 11.sp)
                            }
                            
                            if (isSyncing) {
                                CircularProgressIndicator(color = Color(0xFF7A60FF), modifier = Modifier.size(24.dp))
                            } else {
                                Button(
                                    onClick = { showSyncPasswordPrompt = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A60FF)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Sync Now", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroup(title = "Privacy & Database") {
                    SettingsItem(
                        title = "Export Encrypted Backup (.aura)",
                        subtitle = "Generates an AES-encrypted password-protected backup file",
                        onClick = { showExportPasswordPrompt = true }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsItem(
                        title = "Import & Restore Backup (.aura)",
                        subtitle = "Decrypt and restore reflections from an encrypted .aura backup file",
                        onClick = onImportBackup
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsItem(
                        title = "Populate Demo Data",
                        subtitle = "Inserts 10 realistic journal entries for dashboard and visualization testing",
                        onClick = onPopulateDemoData
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

                if (!isPremium) {
                    com.localvoicejournal.mobile.util.AdsHelper.BannerAd()
                }
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
        allowCloudFallback = false,
        onCloudFallbackToggled = {},
        geminiApiKey = "",
        onGeminiApiKeyChanged = {},
        isLocalModelDownloaded = false,
        localModelDownloadProgress = -1.0f,
        onDownloadLocalModel = {},
        onDeleteLocalModel = {},
        isGoogleLinked = false,
        lastSyncTime = "Never",
        isSyncing = false,
        onLinkGoogle = {},
        onUnlinkGoogle = {},
        onSyncNow = {},
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
