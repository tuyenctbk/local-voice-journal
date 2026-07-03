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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedPreferences = getSharedPreferences("aurajournal_prefs", Context.MODE_PRIVATE)
        sttManager = SpeechToTextManager(this)
        database = JournalDatabase.getInstance(this)
        aiAnalyzer = OnDeviceModelAnalyzer(this)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

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

            fun saveReflection(transcript: String, durationSeconds: Int) {
                lifecycleScope.launch {
                    val analysisResult = aiAnalyzer.analyze(transcript)
                    val newEntry = JournalEntry(
                        timestamp = System.currentTimeMillis(),
                        transcript = transcript,
                        stressLevel = analysisResult.stressLevel,
                        themes = analysisResult.themes,
                        stressors = analysisResult.stressors,
                        habits = analysisResult.habits,
                        durationSeconds = durationSeconds
                    )
                    val id = database.journalDao().insertEntry(newEntry)
                    
                    // Automatically open newly saved entry detail
                    database.journalDao().getEntryById(id).first()?.let { saved ->
                        selectedEntry = saved
                        currentScreen = AppScreen.DETAIL
                    }
                }
            }

            // Observe finalized STT transcripts
            LaunchedEffect(Unit) {
                sttManager.onTranscriptReady.collect { transcript ->
                    if (transcript.isNotEmpty()) {
                        saveReflection(transcript, 60)
                    } else {
                        Toast.makeText(this@MainActivity, "No transcription captured.", Toast.LENGTH_SHORT).show()
                    }
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
            val isProcessing by sttManager.isProcessing.collectAsState()
            val statusText by sttManager.status.collectAsState()
            val liveTranscript by sttManager.transcript.collectAsState()
            val soundLevel by sttManager.soundLevel.collectAsState()

            // Main UI Wrapper
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (useRail && currentScreen != AppScreen.ONBOARDING) {
                        NavigationRail(
                            containerColor = Color(0xFF141225),
                            contentColor = Color(0xFFC0B3FF),
                            header = {
                                Text(
                                    "AURA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC0B3FF),
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        ) {
                            NavigationRailItem(
                                selected = currentScreen == AppScreen.HOME,
                                onClick = { currentScreen = AppScreen.HOME },
                                icon = { Icon(Icons.Default.Mic, contentDescription = "Home") },
                                label = { Text("Home") },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color(0xFF8682A8),
                                    unselectedTextColor = Color(0xFF8682A8),
                                    indicatorColor = Color(0xFF7A60FF)
                                )
                            )
                            NavigationRailItem(
                                selected = currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.DETAIL,
                                onClick = { currentScreen = AppScreen.DASHBOARD },
                                icon = { Icon(Icons.Default.History, contentDescription = "History") },
                                label = { Text("History") },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color(0xFF8682A8),
                                    unselectedTextColor = Color(0xFF8682A8),
                                    indicatorColor = Color(0xFF7A60FF)
                                )
                            )
                            NavigationRailItem(
                                selected = currentScreen == AppScreen.SETTINGS,
                                onClick = { currentScreen = AppScreen.SETTINGS },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    unselectedIconColor = Color(0xFF8682A8),
                                    unselectedTextColor = Color(0xFF8682A8),
                                    indicatorColor = Color(0xFF7A60FF)
                                )
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
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
                                        isProcessing = isProcessing,
                                        isPremium = isPremium,
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
                                        },
                                        onSaveManualReflection = { text ->
                                            saveReflection(text, 0)
                                        },
                                        onCancelRecording = {
                                            sttManager.cancelListening()
                                        },
                                        onNavigateToHistory = {
                                            currentScreen = AppScreen.DASHBOARD
                                        },
                                        onNavigateToSettings = {
                                            currentScreen = AppScreen.SETTINGS
                                        },
                                        onNavigateToPremium = {
                                            currentScreen = AppScreen.PREMIUM
                                        },
                                        showBottomBar = !useRail
                                    )
                                }

                                AppScreen.DETAIL -> {
                                    DetailScreen(
                                        entry = selectedEntry,
                                        isPremium = isPremium,
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
                                        },
                                        showBackButton = !useRail
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
                                        onExportBackup = {
                                            exportBackup()
                                        },
                                        onBack = {
                                            currentScreen = AppScreen.HOME
                                        },
                                        onRequestReview = {
                                            triggerInAppReview()
                                        },
                                        showBackButton = !useRail
                                    )
                                }
                            }
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

    private fun exportBackup() {
        lifecycleScope.launch {
            try {
                val entries = database.journalDao().getAllEntriesList()
                if (entries.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No journal entries to export.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Convert list to simple JSON array
                val jsonBuilder = StringBuilder()
                jsonBuilder.append("[\n")
                entries.forEachIndexed { index, entry ->
                    val themesJson = entry.themes.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    val stressorsJson = entry.stressors.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    val habitsJson = entry.habits.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    
                    jsonBuilder.append("  {\n")
                    jsonBuilder.append("    \"id\": ${entry.id},\n")
                    jsonBuilder.append("    \"timestamp\": ${entry.timestamp},\n")
                    jsonBuilder.append("    \"transcript\": \"${entry.transcript.replace("\"", "\\\"").replace("\n", "\\n")}\",\n")
                    jsonBuilder.append("    \"stressLevel\": \"${entry.stressLevel}\",\n")
                    jsonBuilder.append("    \"themes\": [$themesJson],\n")
                    jsonBuilder.append("    \"stressors\": [$stressorsJson],\n")
                    jsonBuilder.append("    \"habits\": [$habitsJson],\n")
                    jsonBuilder.append("    \"durationSeconds\": ${entry.durationSeconds}\n")
                    jsonBuilder.append("  }")
                    if (index < entries.size - 1) {
                        jsonBuilder.append(",")
                    }
                    jsonBuilder.append("\n")
                }
                jsonBuilder.append("]")
                
                val jsonString = jsonBuilder.toString()
                
                // Write json to a temp file in cache
                val backupFile = java.io.File(cacheDir, "aurajournal_backup.json")
                backupFile.writeText(jsonString)
                
                // Share file using FileProvider
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    backupFile
                )
                
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(android.content.Intent.createChooser(shareIntent, "Export Backup JSON"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sttManager.destroy()
    }
}
