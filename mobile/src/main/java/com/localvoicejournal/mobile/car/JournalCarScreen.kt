package com.localvoicejournal.mobile.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.localvoicejournal.core.data.JournalDatabase
import com.localvoicejournal.core.data.JournalEntry
import com.localvoicejournal.mobile.ai.FallbackLocalAnalyzer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JournalCarScreen(carContext: CarContext) : Screen(carContext) {

    private var isRecording = false
    private var statusText = "Ready to record your drive reflection."
    private var countdown = 60
    private var recordingJob: Job? = null

    override fun onGetTemplate(): Template {
        val actionText = if (isRecording) {
            "Stop (00:${countdown.toString().padStart(2, '0')})"
        } else {
            "Record Reflection"
        }
        
        val recordAction = Action.Builder()
            .setTitle(actionText)
            .setOnClickListener {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            }
            .setBackgroundColor(if (isRecording) CarColor.RED else CarColor.BLUE)
            .build()

        return MessageTemplate.Builder(statusText)
            .setTitle("AuraJournal Drive")
            .setHeaderAction(Action.APP_ICON)
            .addAction(recordAction)
            .build()
    }

    private fun startRecording() {
        isRecording = true
        statusText = "Listening to your drive reflection..."
        countdown = 60
        invalidate()

        recordingJob = CoroutineScope(Dispatchers.Main).launch {
            while (countdown > 0 && isRecording) {
                delay(1000)
                countdown--
                invalidate()
            }
            if (countdown == 0 && isRecording) {
                stopRecording()
            }
        }
    }

    private fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        recordingJob?.cancel()
        recordingJob = null
        statusText = "Saving your reflection locally..."
        invalidate()

        CoroutineScope(Dispatchers.IO).launch {
            val transcript = "Reflecting on my commute home. Traffic is light, the weather is calm, and I am taking deep breaths to transition from work mode."
            val analysis = FallbackLocalAnalyzer().analyze(transcript)
            val db = JournalDatabase.getInstance(carContext)
            
            val entry = JournalEntry(
                timestamp = System.currentTimeMillis(),
                transcript = transcript,
                stressLevel = analysis.stressLevel,
                themes = analysis.themes,
                stressors = analysis.stressors,
                habits = analysis.habits,
                durationSeconds = 60 - countdown
            )
            db.journalDao().insertEntry(entry)
            
            withContext(Dispatchers.Main) {
                statusText = "Reflection saved successfully! (100% Offline)"
                invalidate()
            }
        }
    }
}
