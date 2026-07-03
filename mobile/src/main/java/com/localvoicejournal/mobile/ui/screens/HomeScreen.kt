package com.localvoicejournal.mobile.ui.screens

import android.content.Context
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.localvoicejournal.mobile.R

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(
    isRecording: Boolean,
    isProcessing: Boolean = false,
    isPremium: Boolean = false,
    statusText: String,
    liveTranscript: String,
    soundLevel: Float,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSaveManualReflection: (String) -> Unit = {},
    onCancelRecording: () -> Unit = {},
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPremium: () -> Unit,
    showBottomBar: Boolean = true
) {
    val context = LocalContext.current
    var recordingSeconds by remember { mutableStateOf(60) }

    // Haptic feedback trigger
    val triggerHaptic = {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(80)
        } catch (e: Exception) {
            // Ignore
        }
    }

    // 60-second recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 60
            while (recordingSeconds > 0 && isRecording) {
                delay(1000)
                recordingSeconds--
            }
            if (recordingSeconds == 0 && isRecording) {
                triggerHaptic()
                onStopRecording()
            }
        } else {
            recordingSeconds = 60
        }
    }

    // Pulsing aura animation during recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.4f + (soundLevel * 0.2f) else 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRecording) 800 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isRecording) 0.1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isRecording) 800 else 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Deep sensory background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0C091A), // Dark indigo
            Color(0xFF100D22), // Soft night
            Color(0xFF05040B)  // Pitch black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AURA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFC0B3FF),
                    letterSpacing = 4.sp
                )
                
                IconButton(onClick = onNavigateToPremium) {
                    Text(
                        text = "★",
                        color = Color(0xFFFFD700),
                        fontSize = 22.sp
                    )
                }
            }

            // Central Recording Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when {
                        isProcessing -> "PROCESSING..."
                        isRecording -> "00:${recordingSeconds.toString().padStart(2, '0')}"
                        else -> stringResource(R.string.tap_to_reflect)
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isProcessing -> Color(0xFFC0B3FF)
                        isRecording -> Color(0xFFFF7B7B)
                        else -> Color(0xFF908DB5)
                    },
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Microphone button with animated glowing aura
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    // Pulsing aura ring 1
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF7A60FF).copy(alpha = pulseAlpha),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Main circular button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isProcessing) {
                                        listOf(Color(0xFF4C3E9E), Color(0xFF7A60FF))
                                    } else if (isRecording) {
                                        listOf(Color(0xFFFF5252), Color(0xFFFF8A80))
                                    } else {
                                        listOf(Color(0xFF7A60FF), Color(0xFFC0B3FF))
                                    }
                                )
                            )
                            .clickable(
                                enabled = !isProcessing,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                triggerHaptic()
                                if (isRecording) onStopRecording() else onStartRecording()
                            }
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop recording" else "Start recording",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Live feedback/transcript status
                Text(
                    text = statusText,
                    fontSize = 15.sp,
                    color = Color(0xFFB5B3D6),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rolling real-time transcription box
                if (liveTranscript.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp)
                            .padding(horizontal = 24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "\"$liveTranscript\"",
                            fontSize = 15.sp,
                            color = Color(0xFF8682A8),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // UI Improvement: Cancel Recording & Manual Input Fallback
                var showManualInputDialog by remember { mutableStateOf(false) }
                var manualInputText by remember { mutableStateOf("") }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onCancelRecording) {
                        Text(
                            text = "✕ Cancel Recording",
                            color = Color(0xFFFF5252),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (!isProcessing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showManualInputDialog = true }) {
                        Text(
                            text = "✍ Or Write a Reflection Manually",
                            color = Color(0xFFC0B3FF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }

                if (showManualInputDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showManualInputDialog = false
                            manualInputText = ""
                        },
                        title = {
                            Text(
                                text = "Write Down Your Thoughts",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            OutlinedTextField(
                                value = manualInputText,
                                onValueChange = { manualInputText = it },
                                placeholder = { Text("How are you feeling today?", color = Color(0xFF8682A8)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF7A60FF),
                                    unfocusedBorderColor = Color(0xFF2C2750),
                                    focusedContainerColor = Color(0xFF141225),
                                    unfocusedContainerColor = Color(0xFF141225)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (manualInputText.trim().isNotEmpty()) {
                                        onSaveManualReflection(manualInputText.trim())
                                        showManualInputDialog = false
                                        manualInputText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A60FF)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Analyze & Save", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showManualInputDialog = false
                                manualInputText = ""
                            }) {
                                Text("Cancel", color = Color(0xFFB0AFC0))
                            }
                        },
                        containerColor = Color(0xFF1C1A30),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            // Ad Banner Placeholder
            if (!isPremium) {
                com.localvoicejournal.mobile.util.AdsHelper.BannerAd(
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Bottom Navigation Bar
            if (showBottomBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFF141225).copy(alpha = 0.8f), shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onNavigateToHistory) {
                        Text(
                            text = stringResource(R.string.history),
                            color = Color(0xFFC0B3FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4C4670))
                    )

                    TextButton(onClick = onNavigateToSettings) {
                        Text(
                            text = stringResource(R.string.settings),
                            color = Color(0xFF9693B8),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0C091A)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        isRecording = false,
        statusText = "Tap to record",
        liveTranscript = "",
        soundLevel = 0f,
        onStartRecording = {},
        onStopRecording = {},
        onNavigateToHistory = {},
        onNavigateToSettings = {},
        onNavigateToPremium = {}
    )
}
