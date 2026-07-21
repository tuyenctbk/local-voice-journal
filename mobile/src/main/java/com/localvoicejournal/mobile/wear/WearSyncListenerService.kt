package com.localvoicejournal.mobile.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.localvoicejournal.core.data.JournalDatabase
import com.localvoicejournal.core.data.JournalEntry
import com.localvoicejournal.mobile.ai.OnDeviceModelAnalyzer
import com.localvoicejournal.mobile.util.EnvironmentalContextHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearSyncListenerService : WearableListenerService() {
    private val tag = "WearSyncListenerService"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/reflection_sync") {
            val transcript = String(messageEvent.data, Charsets.UTF_8)
            Log.d(tag, "Received transcript from Wear OS: $transcript")
            
            if (transcript.isNotBlank()) {
                serviceScope.launch {
                    try {
                        val database = JournalDatabase.getInstance(applicationContext)
                        val analyzer = OnDeviceModelAnalyzer(applicationContext)
                        val envContext = EnvironmentalContextHelper.fetchContext(applicationContext)
                        
                        val analysisResult = analyzer.analyze(transcript)
                        
                        val newEntry = JournalEntry(
                            timestamp = System.currentTimeMillis(),
                            transcript = transcript,
                            stressLevel = analysisResult.stressLevel,
                            themes = analysisResult.themes,
                            stressors = analysisResult.stressors,
                            habits = analysisResult.habits,
                            durationSeconds = 15, // Standard duration for quick watch dictations
                            title = "Wear OS Entry",
                            weather = envContext.weather,
                            location = envContext.location
                        )
                        
                        database.journalDao().insertEntry(newEntry)
                        Log.d(tag, "Successfully processed and saved Wear OS journal entry.")
                    } catch (e: Exception) {
                        Log.e(tag, "Failed to process Wear OS synced entry: ${e.message}", e)
                    }
                }
            }
        } else {
            super.onMessageReceived(messageEvent)
        }
    }
}
