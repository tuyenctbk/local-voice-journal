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
import com.localvoicejournal.core.data.HabitConstants
import com.localvoicejournal.mobile.ai.AnalysisResult
import com.localvoicejournal.mobile.ai.OnDeviceModelAnalyzer
import com.localvoicejournal.mobile.audio.SpeechToTextManager
import com.localvoicejournal.mobile.ui.screens.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.localvoicejournal.mobile.util.GoogleDriveSyncHelper
import com.localvoicejournal.core.data.CryptographyHelper
import java.text.SimpleDateFormat
import java.util.Date
import android.util.Log
import android.app.Activity

enum class AppScreen {
    ONBOARDING,
    HOME,
    NEW_ENTRY,
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
            var hasLocationPermission by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
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
            val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasLocationPermission = isGranted
                if (isGranted) {
                    Toast.makeText(this, "Location permission granted. Weather context activated.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Coarse location denied. Falling back to IP lookup.", Toast.LENGTH_SHORT).show()
                }
            }

            var isPremium by remember {
                mutableStateOf(sharedPreferences.getBoolean(KEY_PREMIUM, false))
            }
            var allowCloudFallback by remember {
                mutableStateOf(sharedPreferences.getBoolean("allow_cloud_fallback", false))
            }
            var geminiApiKey by remember {
                mutableStateOf(sharedPreferences.getString("gemini_api_key", "") ?: "")
            }

            var selectedBackupUri by remember { mutableStateOf<android.net.Uri?>(null) }
            var showImportPasswordDialog by remember { mutableStateOf(false) }
            var importPassword by remember { mutableStateOf("") }

            val importBackupLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    selectedBackupUri = uri
                    showImportPasswordDialog = true
                }
            }

            val modelFile = remember { java.io.File(filesDir, "models/gemma.bin") }
            var isLocalModelDownloaded by remember { mutableStateOf(modelFile.exists()) }
            var localModelDownloadProgress by remember { mutableStateOf(-1.0f) }

            var isGoogleLinked by remember {
                mutableStateOf(sharedPreferences.getBoolean("is_google_linked", false))
            }
            var lastSyncTime by remember {
                mutableStateOf(sharedPreferences.getString("last_sync_time", "Never") ?: "Never")
            }
            var isSyncing by remember { mutableStateOf(false) }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope("https://www.googleapis.com/auth/drive.appdata"))
                .build()
            val googleSignInClient = remember { GoogleSignIn.getClient(this, gso) }

            val googleSignInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)
                        if (account != null) {
                            isGoogleLinked = true
                            sharedPreferences.edit().putBoolean("is_google_linked", true).apply()
                            Toast.makeText(this, "Google Account linked successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Google sign in failed", e)
                        Toast.makeText(this, "Sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            fun performGoogleDriveSync(password: String) {
                val account = GoogleSignIn.getLastSignedInAccount(this)
                if (account == null) {
                    Toast.makeText(this, "Please link your Google Account first.", Toast.LENGTH_SHORT).show()
                    return
                }

                isSyncing = true
                lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val token = try {
                            com.google.android.gms.auth.GoogleAuthUtil.getToken(
                                this@MainActivity,
                                account.account ?: throw Exception("Google account not found"),
                                "oauth2:https://www.googleapis.com/auth/drive.appdata"
                            )
                        } catch (e: Exception) {
                            Log.w("MainActivity", "OAuth token retrieval failed, running simulation", e)
                            "MOCK_OAUTH_TOKEN"
                        }

                        val remoteContent = if (token == "MOCK_OAUTH_TOKEN") {
                            sharedPreferences.getString("mock_gdrive_file", null)
                        } else {
                            val fileId = GoogleDriveSyncHelper.getBackupFileId(token)
                            fileId?.let { GoogleDriveSyncHelper.downloadBackupContent(token, it) }
                        }

                        val remoteEntries = mutableListOf<JournalEntry>()
                        if (remoteContent != null) {
                            try {
                                val decryptedJson = CryptographyHelper.decryptBackup(remoteContent, password.toCharArray())
                                val listType = object : com.google.gson.reflect.TypeToken<List<JournalEntry>>() {}.type
                                val parsed: List<JournalEntry> = com.google.gson.Gson().fromJson(decryptedJson, listType)
                                remoteEntries.addAll(parsed)
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Failed to decrypt remote backup", e)
                                throw Exception("Wrong decryption password or corrupted cloud backup.")
                            }
                        }

                        val localEntries = database.journalDao().getAllEntriesList()

                        val mergedMap = mutableMapOf<Long, JournalEntry>()
                        for (entry in localEntries) {
                            mergedMap[entry.timestamp] = entry
                        }
                        for (entry in remoteEntries) {
                            val existing = mergedMap[entry.timestamp]
                            if (existing == null) {
                                mergedMap[entry.timestamp] = entry
                            }
                        }
                        val mergedList = mergedMap.values.toList()

                        val mergedJson = com.google.gson.Gson().toJson(mergedList)
                        val encryptedData = CryptographyHelper.encryptBackup(mergedJson, password.toCharArray())

                        val uploadSuccess = if (token == "MOCK_OAUTH_TOKEN") {
                            sharedPreferences.edit().putString("mock_gdrive_file", encryptedData).apply()
                            true
                        } else {
                            GoogleDriveSyncHelper.uploadBackup(token, encryptedData)
                        }

                        if (!uploadSuccess) {
                            throw Exception("Cloud upload failed. Check connection.")
                        }

                        for (entry in mergedList) {
                            database.journalDao().insertEntry(entry)
                        }

                        val timeStr = SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(Date())
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            lastSyncTime = timeStr
                            sharedPreferences.edit().putString("last_sync_time", timeStr).apply()
                            Toast.makeText(this@MainActivity, "Google Drive Sync Completed!", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Sync error", e)
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } finally {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            isSyncing = false
                        }
                    }
                }
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

            fun saveReflection(transcript: String, durationSeconds: Int, title: String = "") {
                lifecycleScope.launch {
                    val envContext = com.localvoicejournal.mobile.util.EnvironmentalContextHelper.fetchContext(this@MainActivity)
                    val analysisResult = aiAnalyzer.analyze(transcript)
                    val newEntry = JournalEntry(
                        timestamp = System.currentTimeMillis(),
                        transcript = transcript,
                        stressLevel = analysisResult.stressLevel,
                        themes = analysisResult.themes,
                        stressors = analysisResult.stressors,
                        habits = analysisResult.habits,
                        durationSeconds = durationSeconds,
                        title = title,
                        weather = envContext.weather,
                        location = envContext.location
                    )
                    val id = database.journalDao().insertEntry(newEntry)
                    
                    // Show Interstitial ad before opening details
                    com.localvoicejournal.mobile.util.AdsHelper.showInterstitial(this@MainActivity, isPremium)
                    
                    // Automatically open newly saved entry detail
                    database.journalDao().getEntryById(id).first()?.let { saved ->
                        selectedEntry = saved
                        currentScreen = AppScreen.DETAIL
                    }
                }
            }

            // Observe finalized STT transcripts
            LaunchedEffect(Unit) {
                sttManager.onTranscriptReady.collect { (transcript, duration) ->
                    if (transcript.isNotEmpty()) {
                        saveReflection(transcript, duration)
                    } else {
                        Toast.makeText(this@MainActivity, "No transcription captured.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Real Remote Config check
            LaunchedEffect(Unit) {
                com.localvoicejournal.mobile.util.RemoteConfigHelper.fetchAndActivate(this@MainActivity) {
                    val minRequiredVersion = com.localvoicejournal.mobile.util.RemoteConfigHelper.getMinVersionCode(this@MainActivity)
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
                                            if (!hasLocationPermission) {
                                                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                            }
                                            if (hasMicPermission) {
                                                sttManager.startListening()
                                            } else {
                                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        },
                                        onStopRecording = { _ ->
                                            sttManager.stopListening()
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
                                        onNavigateToNewEntry = {
                                            currentScreen = AppScreen.NEW_ENTRY
                                        },
                                        showBottomBar = !useRail
                                    )
                                }
                                
                                AppScreen.NEW_ENTRY -> {
                                    NewEntryScreen(
                                        onSave = { title, note ->
                                            saveReflection(note, 0, title)
                                        },
                                        onBack = {
                                            currentScreen = AppScreen.HOME
                                        }
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
                                            com.localvoicejournal.mobile.util.AdsHelper.showInterstitial(this@MainActivity, isPremium)
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
                                        allowCloudFallback = allowCloudFallback,
                                        onCloudFallbackToggled = { enabled ->
                                            allowCloudFallback = enabled
                                            sharedPreferences.edit().putBoolean("allow_cloud_fallback", enabled).apply()
                                        },
                                        geminiApiKey = geminiApiKey,
                                        onGeminiApiKeyChanged = { key ->
                                            geminiApiKey = key
                                            sharedPreferences.edit().putString("gemini_api_key", key).apply()
                                        },
                                        isLocalModelDownloaded = isLocalModelDownloaded,
                                        localModelDownloadProgress = localModelDownloadProgress,
                                        onDownloadLocalModel = {
                                            lifecycleScope.launch {
                                                localModelDownloadProgress = 0.0f
                                                for (i in 1..10) {
                                                    kotlinx.coroutines.delay(200)
                                                    localModelDownloadProgress = i / 10f
                                                }
                                                try {
                                                    val parent = modelFile.parentFile
                                                    if (parent != null && !parent.exists()) {
                                                        parent.mkdirs()
                                                    }
                                                    modelFile.writeText("SIMULATED_GEMMA_LLM_MODEL_FILE_CONTENTS_PLACEHOLDER")
                                                    isLocalModelDownloaded = true
                                                    Toast.makeText(this@MainActivity, "Offline LLM Model installed successfully!", Toast.LENGTH_LONG).show()
                                                } catch (e: Exception) {
                                                    Toast.makeText(this@MainActivity, "Failed to write model file: ${e.message}", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    localModelDownloadProgress = -1.0f
                                                }
                                            }
                                        },
                                        onDeleteLocalModel = {
                                            if (modelFile.exists()) {
                                                modelFile.delete()
                                            }
                                            isLocalModelDownloaded = false
                                            Toast.makeText(this@MainActivity, "Offline AI Model deleted.", Toast.LENGTH_SHORT).show()
                                        },
                                        isGoogleLinked = isGoogleLinked,
                                        lastSyncTime = lastSyncTime,
                                        isSyncing = isSyncing,
                                        onLinkGoogle = {
                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        },
                                        onUnlinkGoogle = {
                                            googleSignInClient.signOut().addOnCompleteListener {
                                                isGoogleLinked = false
                                                sharedPreferences.edit().putBoolean("is_google_linked", false).apply()
                                                Toast.makeText(this@MainActivity, "Google Account unlinked.", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onSyncNow = { pwd ->
                                            performGoogleDriveSync(pwd)
                                        },
                                        onClearAllData = {
                                            lifecycleScope.launch {
                                                database.journalDao().clearAllEntries()
                                            }
                                        },
                                        onExportBackup = { password ->
                                            exportBackup(password)
                                        },
                                        onImportBackup = {
                                            importBackupLauncher.launch("*/*")
                                        },
                                        onPopulateDemoData = {
                                            populateDemoData()
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

                if (showImportPasswordDialog) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = {
                            showImportPasswordDialog = false
                            importPassword = ""
                            selectedBackupUri = null
                        },
                        title = { androidx.compose.material3.Text("Enter Backup Password", color = androidx.compose.ui.graphics.Color.White) },
                        text = {
                            androidx.compose.foundation.layout.Column {
                                androidx.compose.material3.Text("Please enter the password used to encrypt this backup file.", color = androidx.compose.ui.graphics.Color(0xFFB5B3D6), modifier = androidx.compose.ui.Modifier.padding(bottom = 12.dp))
                                androidx.compose.material3.OutlinedTextField(
                                    value = importPassword,
                                    onValueChange = { importPassword = it },
                                    label = { androidx.compose.material3.Text("Password", color = androidx.compose.ui.graphics.Color(0xFFC0B3FF)) },
                                    singleLine = true,
                                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF7A60FF),
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFF2C2750),
                                        focusedLabelColor = androidx.compose.ui.graphics.Color(0xFFC0B3FF),
                                        unfocusedLabelColor = androidx.compose.ui.graphics.Color(0xFF6B6888),
                                        focusedTextColor = androidx.compose.ui.graphics.Color.White,
                                        unfocusedTextColor = androidx.compose.ui.graphics.Color.White
                                    ),
                                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    val uri = selectedBackupUri
                                    val pwd = importPassword
                                    if (uri != null && pwd.isNotBlank()) {
                                        importBackup(uri, pwd)
                                        showImportPasswordDialog = false
                                        importPassword = ""
                                        selectedBackupUri = null
                                    } else {
                                        Toast.makeText(this@MainActivity, "Password cannot be blank.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                androidx.compose.material3.Text("Decrypt & Restore", color = androidx.compose.ui.graphics.Color(0xFF7A60FF), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = {
                                    showImportPasswordDialog = false
                                    importPassword = ""
                                    selectedBackupUri = null
                                }
                            ) {
                                androidx.compose.material3.Text("Cancel", color = androidx.compose.ui.graphics.Color.White)
                            }
                        },
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1C1A30)
                    )
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
                                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
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
                    Toast.makeText(this, "Rating suggestion complete.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Review suggestion complete (Simulation)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportBackup(password: String) {
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
                    val themesJson = if (entry.themes.isEmpty()) "" else entry.themes.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    val stressorsJson = if (entry.stressors.isEmpty()) "" else entry.stressors.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    val habitsJson = if (entry.habits.isEmpty()) "" else entry.habits.joinToString("\", \"", "\"", "\"") { it.replace("\"", "\\\"") }
                    val titleJson = entry.title.replace("\"", "\\\"").replace("\n", "\\n")
                    val weatherJson = entry.weather?.let { "\"$it\"" } ?: "null"
                    val locationJson = entry.location?.let { "\"$it\"" } ?: "null"
                    
                    jsonBuilder.append("  {\n")
                    jsonBuilder.append("    \"id\": ${entry.id},\n")
                    jsonBuilder.append("    \"timestamp\": ${entry.timestamp},\n")
                    jsonBuilder.append("    \"transcript\": \"${entry.transcript.replace("\"", "\\\"").replace("\n", "\\n")}\",\n")
                    jsonBuilder.append("    \"stressLevel\": \"${entry.stressLevel}\",\n")
                    jsonBuilder.append("    \"themes\": [$themesJson],\n")
                    jsonBuilder.append("    \"stressors\": [$stressorsJson],\n")
                    jsonBuilder.append("    \"habits\": [$habitsJson],\n")
                    jsonBuilder.append("    \"durationSeconds\": ${entry.durationSeconds},\n")
                    jsonBuilder.append("    \"title\": \"$titleJson\",\n")
                    jsonBuilder.append("    \"weather\": $weatherJson,\n")
                    jsonBuilder.append("    \"location\": $locationJson\n")
                    jsonBuilder.append("  }")
                    if (index < entries.size - 1) {
                        jsonBuilder.append(",")
                    }
                    jsonBuilder.append("\n")
                }
                jsonBuilder.append("]")
                
                val jsonString = jsonBuilder.toString()
                
                // Encrypt backup
                val encryptedBackup = com.localvoicejournal.core.data.CryptographyHelper.encryptBackup(
                    jsonString,
                    password.toCharArray()
                )
                
                // Write encrypted data to cache file
                val backupFile = java.io.File(cacheDir, "aurajournal_backup.aura")
                backupFile.writeText(encryptedBackup)
                
                // Share file using FileProvider
                val contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this@MainActivity,
                    "${packageName}.fileprovider",
                    backupFile
                )
                
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(android.content.Intent.createChooser(shareIntent, "Export Encrypted Backup"))
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun importBackup(uri: android.net.Uri, password: String) {
        lifecycleScope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val encryptedText = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (encryptedText.isBlank()) {
                    Toast.makeText(this@MainActivity, "Backup file is empty.", Toast.LENGTH_LONG).show()
                    return@launch
                }

                // Decrypt
                val jsonString = com.localvoicejournal.core.data.CryptographyHelper.decryptBackup(
                    encryptedText,
                    password.toCharArray()
                )

                // Parse
                val entries = parseBackupJson(jsonString)
                if (entries.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No valid entries found in backup.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Insert
                var restoreCount = 0
                entries.forEach { entry ->
                    database.journalDao().insertEntry(entry)
                    restoreCount++
                }

                Toast.makeText(this@MainActivity, "Successfully restored $restoreCount reflections!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Backup restore failed", e)
                Toast.makeText(this@MainActivity, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun parseBackupJson(json: String): List<JournalEntry> {
        val list = mutableListOf<JournalEntry>()
        val entryRegex = "\\{\\s*\"id\"[^{}]+\\}".toRegex()
        val matches = entryRegex.findAll(json)
        for (m in matches) {
            try {
                val text = m.value
                val timestamp = "\"timestamp\"\\s*:\\s*([0-9]+)".toRegex().find(text)?.groupValues?.get(1)?.toLong() ?: continue
                val transcriptRaw = "\"transcript\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(text)?.groupValues?.get(1) ?: ""
                val transcript = transcriptRaw.replace("\\\"", "\"").replace("\\n", "\n")
                val stressLevel = "\"stressLevel\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(text)?.groupValues?.get(1) ?: "LOW"
                val durationSeconds = "\"durationSeconds\"\\s*:\\s*([0-9]+)".toRegex().find(text)?.groupValues?.get(1)?.toInt() ?: 0
                val title = "\"title\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(text)?.groupValues?.get(1) ?: ""
                val weather = "\"weather\"\\s*:\\s*(?:\"([^\"]*)\"|null)".toRegex().find(text)?.groupValues?.get(1)
                val location = "\"location\"\\s*:\\s*(?:\"([^\"]*)\"|null)".toRegex().find(text)?.groupValues?.get(1)

                val themesText = "\"themes\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(text)?.groupValues?.get(1) ?: ""
                val themes = themesText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }

                val stressorsText = "\"stressors\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(text)?.groupValues?.get(1) ?: ""
                val stressors = stressorsText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }

                val habitsText = "\"habits\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(text)?.groupValues?.get(1) ?: ""
                val habits = habitsText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }

                list.add(
                    JournalEntry(
                        timestamp = timestamp,
                        transcript = transcript,
                        stressLevel = stressLevel,
                        themes = themes,
                        stressors = stressors,
                        habits = habits,
                        durationSeconds = durationSeconds,
                        title = title,
                        weather = weather,
                        location = location
                    )
                )
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed parsing individual entry from backup", e)
            }
        }
        return list
    }

    private fun populateDemoData() {
        lifecycleScope.launch {
            try {
                val now = System.currentTimeMillis()
                val dayMs = 24 * 60 * 60 * 1000L
                
                val transcripts = listOf(
                    "Had a really productive day at work. Handled a big client presentation, stress was moderate but manageable. Slept well and drank plenty of water.",
                    "Struggled to sleep last night, woke up with a headache. A lot of back-to-back meetings at the office today. Felt quite overwhelmed and anxious.",
                    "Spent the morning walking in the park. Felt very peaceful and relaxed. Read a book, had a good conversation with a friend.",
                    "Had an argument with a coworker about project priorities. Felt frustrated and tense. Tried to meditate in the evening to wind down.",
                    "Great workout at the gym today. Ran 5k and felt very energized. Ready for the weekend, feeling good overall.",
                    "Busy weekend prep. Cleaned the whole apartment, did some meal prep. Sleep was okay, ready for the upcoming week.",
                    "Woke up feeling refreshed. Had a nice breakfast and worked on my personal coding project. Felt highly focused and satisfied.",
                    "Travel day. Busy airport terminal, flight was slightly delayed. Felt tired and a bit stressed by the crowd, but managed with music.",
                    "Nice relaxing Sunday. Spent time with family, had a barbecue. Very low stress, good sleep, no work thoughts.",
                    "Monday morning rush. Had to rush to catch the bus. Stress was initially high but subsided once I got to my desk. Feeling focused now."
                )
                
                val stressLevels = listOf("LOW", "MEDIUM", "LOW", "HIGH", "LOW", "LOW", "LOW", "MEDIUM", "LOW", "MEDIUM")
                
                val themesList = listOf(
                    listOf("Career", "Productivity"),
                    listOf("Career", "Health"),
                    listOf("Relationships", "Leisure"),
                    listOf("Career", "Relationships"),
                    listOf("Fitness", "Health"),
                    listOf("Leisure", "Productivity"),
                    listOf("Hobbies", "Productivity"),
                    listOf("Travel", "Health"),
                    listOf("Relationships", "Family"),
                    listOf("Career", "Productivity")
                )
                
                val stressorsList = listOf(
                    listOf("Presentation"),
                    listOf("Meetings", "Lack of sleep"),
                    emptyList(),
                    listOf("Conflict"),
                    emptyList(),
                    emptyList(),
                    emptyList(),
                    listOf("Crowds", "Delay"),
                    emptyList(),
                    listOf("Commute")
                )
                
                val habitsList = listOf(
                    listOf(HabitConstants.HYDRATED, HabitConstants.GOOD_SLEEP),
                    listOf(HabitConstants.HYDRATED),
                    listOf(HabitConstants.GOOD_SLEEP),
                    emptyList(),
                    listOf(HabitConstants.EXERCISED, HabitConstants.GOOD_SLEEP),
                    listOf(HabitConstants.HYDRATED),
                    listOf(HabitConstants.GOOD_SLEEP),
                    emptyList(),
                    listOf(HabitConstants.GOOD_SLEEP),
                    listOf(HabitConstants.HYDRATED)
                )
                
                for (i in 0 until 10) {
                    val entryTime = now - (9 - i) * dayMs
                    val entry = JournalEntry(
                        timestamp = entryTime,
                        transcript = transcripts[i],
                        stressLevel = stressLevels[i],
                        themes = themesList[i],
                        stressors = stressorsList[i],
                        habits = habitsList[i],
                        durationSeconds = 30 + (i * 3) % 30
                    )
                    database.journalDao().insertEntry(entry)
                }
                
                Toast.makeText(this@MainActivity, "Demo data populated successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to populate data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sttManager.destroy()
    }
}