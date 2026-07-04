package com.localvoicejournal.wear.presentation

import android.Manifest
import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.localvoicejournal.wear.R
import com.localvoicejournal.wear.service.RecordingService

class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = this
            var hasPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val requestPermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasPermission = isGranted
            }

            WearApp(
                hasPermission = hasPermission,
                onRequestPermission = { requestPermission.launch(Manifest.permission.RECORD_AUDIO) },
                triggerVibration = {
                    try {
                        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        vibrator?.vibrate(60)
                    } catch (e: Exception) { }
                },
                onStartRecordingService = {
                    val intent = Intent(context, RecordingService::class.java)
                    context.startForegroundService(intent)
                },
                onStopRecordingService = {
                    val intent = Intent(context, RecordingService::class.java)
                    context.stopService(intent)
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, RecordingService::class.java)
        stopService(intent)
    }
}

@Composable
fun WearApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    triggerVibration: () -> Unit,
    onStartRecordingService: () -> Unit,
    onStopRecordingService: () -> Unit
) {
    val initialStatus = stringResource(R.string.tap_to_record)
    var isRecording by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf(initialStatus) }
    var countdown by remember { mutableStateOf(60) }
    val coroutineScope = rememberCoroutineScope()

    val listeningText = stringResource(R.string.listening)
    val syncingText = stringResource(R.string.syncing)
    val reflectedText = stringResource(R.string.reflected)
    val tapToRecordText = stringResource(R.string.tap_to_record)

    LaunchedEffect(isRecording) {
        if (isRecording) {
            onStartRecordingService()
            countdown = 60
            statusText = listeningText
            while (countdown > 0 && isRecording) {
                delay(1000)
                countdown--
            }
            if (countdown == 0 && isRecording) {
                isRecording = false
                onStopRecordingService()
                statusText = syncingText
                triggerVibration()
                delay(2000)
                statusText = reflectedText
            }
        } else {
            onStopRecordingService()
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
                                onRequestPermission()
                            } else {
                                if (isRecording) {
                                    isRecording = false
                                    coroutineScope.launch {
                                        statusText = syncingText
                                        delay(1500)
                                        statusText = reflectedText
                                        delay(2000)
                                        statusText = tapToRecordText
                                    }
                                } else {
                                    isRecording = true
                                }
                            }
                        }
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) stringResource(R.string.stop_reflection) else stringResource(R.string.start_reflection),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(device = "id:wearos_large_round", showSystemUi = true)
@Composable
fun PreviewWearApp() {
    WearApp(
        hasPermission = true,
        onRequestPermission = {},
        triggerVibration = {},
        onStartRecordingService = {},
        onStopRecordingService = {}
    )
}