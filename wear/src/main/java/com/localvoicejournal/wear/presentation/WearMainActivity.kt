package com.localvoicejournal.wear.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.widget.Toast
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
import com.google.android.gms.wearable.Wearable

class WearMainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = this
            val coroutineScope = rememberCoroutineScope()
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

            var statusText by remember { mutableStateOf("Tap to record") }

            val speechLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                    if (!spokenText.isNullOrBlank()) {
                        statusText = "Syncing..."
                        sendTranscriptToPhone(context, spokenText) { finalStatus ->
                            statusText = finalStatus
                            coroutineScope.launch {
                                triggerVibration(context)
                                delay(2000)
                                statusText = "Tap to record"
                            }
                        }
                    }
                }
            }

            WearApp(
                hasPermission = hasPermission,
                onRequestPermission = { requestPermission.launch(Manifest.permission.RECORD_AUDIO) },
                statusText = statusText,
                onStartSpeech = {
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        }
                        speechLauncher.launch(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Voice input not supported on this device.", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }

    private fun triggerVibration(context: Context) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(60, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(60)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WearMainActivity", "Vibration failed", e)
        }
    }

    private fun sendTranscriptToPhone(context: Context, text: String, onComplete: (String) -> Unit) {
        val nodeClient = Wearable.getNodeClient(context)
        nodeClient.connectedNodes.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && task.result.isNotEmpty()) {
                val nodes = task.result
                val messageClient = Wearable.getMessageClient(context)
                var count = 0
                var anySent = false
                for (node in nodes) {
                    messageClient.sendMessage(node.id, "/reflection_sync", text.toByteArray(Charsets.UTF_8))
                        .addOnCompleteListener { msgTask ->
                            count++
                            if (msgTask.isSuccessful) {
                                anySent = true
                            }
                            if (count == nodes.size) {
                                if (anySent) {
                                    onComplete("Reflected!")
                                } else {
                                    onComplete("Sync failed")
                                }
                            }
                        }
                }
            } else {
                onComplete("Phone disconnected")
            }
        }.addOnFailureListener {
            onComplete("Sync failed")
        }
    }
}

@Composable
fun WearApp(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    statusText: String,
    onStartSpeech: () -> Unit
) {
    val isRecording = statusText == "Syncing..."
    val infiniteTransition = rememberInfiniteTransition(label = "wear_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
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
                text = statusText,
                fontSize = 10.sp,
                color = if (isRecording) Color(0xFFFF5252) else Color(0xFF9693B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
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

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) Color(0xFFFF5252) else Color(0xFF7A60FF)
                        )
                        .clickable {
                            if (!hasPermission) {
                                onRequestPermission()
                            } else {
                                onStartSpeech()
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.start_reflection),
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
        statusText = "Tap to record",
        onStartSpeech = {}
    )
}