package com.localvoicejournal.mobile

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.review.ReviewManagerFactory
import com.localvoicejournal.core.data.JournalDatabase
import com.localvoicejournal.core.data.JournalEntry
import com.localvoicejournal.mobile.ai.AnalysisResult
import com.localvoicejournal.mobile.ai.OnDeviceModelAnalyzer
import com.localvoicejournal.mobile.audio.SpeechToTextManager
import com.localvoicejournal.mobile.ui.screens.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppScreen {
    ONBOARDING,
    HOME,
    DETAIL,
    DASHBOARD,
    PREMIUM,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sttManager: SpeechToTextManager
    private lateinit var database: JournalDatabase
    private lateinit var aiAnalyzer: OnDeviceModelAnalyzer

    private val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private val KEY_PREMIUM = "premium_unlocked"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("aurajournal_prefs", Context.MODE_PRIVATE)
        sttManager = SpeechToTextManager(this)
        database = JournalDatabase.getInstance(this)
        aiAnalyzer = OnDeviceModelAnalyzer(this)

        setContent {
            var currentScreen by remember {
                mutableStateOf(
                    if (sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false)) {
                        AppScreen.HOME
                    } else {
                        AppScreen.ONBOARDING
                    }
                )
            }

            var hasMicPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasMicPermission = isGranted
                if (isGranted) {
                    Toast.makeText(this, "Microphone permission granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Microphone permission is required to transcribe reflections.", Toast.LENGTH_LONG).show()
                }
            }

            var isPremium by remember {
                mutableStateOf(sharedPreferences.getBoolean(KEY_PREMIUM, false))
            }

            // Room Database entities state
            var journalEntries by remember { mutableStateOf(emptyList<JournalEntry>()) }
            var selectedEntry by remember { mutableStateOf<JournalEntry?>(null) }

            var showUpdateDialog by remember { mutableStateOf(false) }

            // Observe room flow
            LaunchedEffect(Unit) {
                database.journalDao().getAllEntries().collect {
                    journalEntries = it
                }
            }

            // Real Remote Config check
            LaunchedEffect(Unit) {
                com.localvoicejournal.mobile.util.RemoteConfigHelper.fetchAndActivate {
                    val minRequiredVersion = com.localvoicejournal.mobile.util.RemoteConfigHelper.getMinVersionCode()
                    val currentVersionCode = try {
                        packageManager.getPackageInfo(packageName, 0).versionCode
                    } catch (e: Exception) {
                        1
                    }
                    if (minRequiredVersion > currentVersionCode) {
                        showUpdateDialog = true
                    }
                }
            }

            // STT Manager states
            val isRecording by sttManager.isRecording.collectAsState()
            val statusText by sttManager.status.collectAsState()
            val liveTranscript by sttManager.transcript.collectAsState()
            val soundLevel by sttManager.soundLevel.collectAsState()

            // Main UI Wrapper
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Crossfade(targetState = currentScreen, label = "screen_nav") { screen ->
                    when (screen) {
                        AppScreen.ONBOARDING -> {
                            OnboardingScreen(
                                onRequestPermission = {
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                },
                                hasMicrophonePermission = hasMicPermission,
                                onFinishOnboarding = {
                                    sharedPreferences.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                isRecording = isRecording,
                                statusText = statusText,
                                liveTranscript = liveTranscript,
                                soundLevel = soundLevel,
                                onStartRecording = {
                                    if (hasMicPermission) {
                                        sttManager.startListening()
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onStopRecording = {
                                    sttManager.stopListening()
                                    // Complete and save to DB
                                    lifecycleScope.launch {
                                        val transcript = sttManager.transcript.value
                                        if (transcript.isNotEmpty()) {
                                            val analysisResult = aiAnalyzer.analyze(transcript)
                                            val newEntry = JournalEntry(
                                                timestamp = System.currentTimeMillis(),
                                                transcript = transcript,
                                                stressLevel = analysisResult.stressLevel,
                                                themes = analysisResult.themes,
                                                stressors = analysisResult.stressors,
                                                habits = analysisResult.habits,
                                                durationSeconds = 60 // Simulated max duration
                                            )
                                            val id = database.journalDao().insertEntry(newEntry)
                                            
                                            // Automatically open newly saved entry detail
                                            database.journalDao().getEntryById(id).first()?.let { saved ->
                                                selectedEntry = saved
                                                currentScreen = AppScreen.DETAIL
                                            }
                                        } else {
                                            Toast.makeText(this@MainActivity, "No transcription captured.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onNavigateToHistory = {
                                    currentScreen = AppScreen.DASHBOARD
                                },
                                onNavigateToSettings = {
                                    currentScreen = AppScreen.SETTINGS
                                },
                                onNavigateToPremium = {
                                    currentScreen = AppScreen.PREMIUM
                                }
                            )
                        }

                        AppScreen.DETAIL -> {
                            DetailScreen(
                                entry = selectedEntry,
                                onBack = {
                                    currentScreen = AppScreen.DASHBOARD
                                },
                                onDelete = {
                                    selectedEntry?.let {
                                        lifecycleScope.launch {
                                            database.journalDao().deleteEntry(it)
                                            Toast.makeText(this@MainActivity, "Reflection deleted.", Toast.LENGTH_SHORT).show()
                                            currentScreen = AppScreen.DASHBOARD
                                        }
                                    }
                                },
                                onRateApp = {
                                    triggerInAppReview()
                                }
                            )
                        }

                        AppScreen.DASHBOARD -> {
                            DashboardScreen(
                                entries = journalEntries,
                                isPremium = isPremium,
                                onEntryClick = { entry ->
                                    selectedEntry = entry
                                    currentScreen = AppScreen.DETAIL
                                },
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                },
                                onUnlockPremium = {
                                    currentScreen = AppScreen.PREMIUM
                                }
                            )
                        }

                        AppScreen.PREMIUM -> {
                            PremiumScreen(
                                onClose = {
                                    currentScreen = AppScreen.HOME
                                },
                                onSubscribeSuccess = {
                                    isPremium = true
                                    sharedPreferences.edit().putBoolean(KEY_PREMIUM, true).apply()
                                }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                isPremium = isPremium,
                                onPremiumToggled = { enabled ->
                                    isPremium = enabled
                                    sharedPreferences.edit().putBoolean(KEY_PREMIUM, enabled).apply()
                                },
                                onClearAllData = {
                                    lifecycleScope.launch {
                                        database.journalDao().clearAllEntries()
                                    }
                                },
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                },
                                onRequestReview = {
                                    triggerInAppReview()
                                }
                            )
                        }
                    }
                }

                if (showUpdateDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        title = { androidx.compose.material3.Text("Update Available", color = androidx.compose.ui.graphics.Color.White) },
                        text = { androidx.compose.material3.Text("A new version of AuraJournal is available with improved local AI models and performance. Please update to continue.", color = androidx.compose.ui.graphics.Color(0xFFB5B3D6)) },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.localvoicejournal.mobile")
                                    }
                                    startActivity(intent)
                                }
                            ) {
                                androidx.compose.material3.Text("Update Now", color = androidx.compose.ui.graphics.Color(0xFFC0B3FF), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { showUpdateDialog = false }) {
                                androidx.compose.material3.Text("Later", color = androidx.compose.ui.graphics.Color.White)
                            }
                        },
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1C1A30)
                    )
                }
            }
        }
    }

    private fun triggerInAppReview() {
        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener {
                    Toast.makeText(this, "Rating suggest complete.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Review suggestion complete (Simulation)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sttManager.destroy()
    }
}
