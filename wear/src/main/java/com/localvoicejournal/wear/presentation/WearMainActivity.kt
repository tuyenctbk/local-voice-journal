package com.localvoicejournal.wear.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isRecording by remember { mutableStateOf(false) }
            var statusText by remember { mutableStateOf("Tap to record") }
            var countdown by remember { mutableStateOf(60) }
            val coroutineScope = rememberCoroutineScope()

            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val requestPermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasPermission = isGranted
            }

            val triggerVibration = {
                try {
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(60)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            LaunchedEffect(isRecording) {
                if (isRecording) {
                    countdown = 60
                    statusText = "Listening..."
                    while (countdown > 0 && isRecording) {
                        delay(1000)
                        countdown--
                    }
                    if (countdown == 0 && isRecording) {
                        isRecording = false
                        statusText = "Syncing with phone..."
                        triggerVibration()
                        delay(2000)
                        statusText = "Reflected!"
                    }
                }
            }

            // Infinite scaling for microphone glowing aura
            val infiniteTransition = rememberInfiniteTransition(label = "wear_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = if (isRecording) 1.3f else 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = if (isRecording) 700 else 1800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wearPulseScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "AURA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC0B3FF),
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isRecording) "00:${countdown.toString().padStart(2, '0')}" else statusText,
                        fontSize = 10.sp,
                        color = if (isRecording) Color(0xFFFF5252) else Color(0xFF9693B8),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Microphobe circle button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        // Pulsing aura ring
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF7A60FF).copy(alpha = if (isRecording) 0.35f else 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Main action circle
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRecording) Color(0xFFFF5252) else Color(0xFF7A60FF)
                                )
                                .clickable {
                                    triggerVibration()
                                    if (!hasPermission) {
                                        requestPermission.launch(Manifest.permission.RECORD_AUDIO)
                                    } else {
                                        if (isRecording) {
                                            isRecording = false
                                            coroutineScope.launch {
                                                statusText = "Syncing..."
                                                delay(1500)
                                                statusText = "Reflected!"
                                                delay(2000)
                                                statusText = "Tap to record"
                                            }
                                        } else {
                                            isRecording = true
                                        }
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (isRecording) "Stop reflection" else "Start reflection",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
