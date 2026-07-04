package com.localvoicejournal.wear.service

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.localvoicejournal.wear.R
import com.localvoicejournal.wear.presentation.WearMainActivity

class RecordingService : Service() {

    private val binder = LocalBinder()
    private var isRecording = false
    private var startTime = 0L

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "recording_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    fun startRecording() {
        if (isRecording) return
        isRecording = true
        startTime = SystemClock.elapsedRealtime()

        val notificationIntent = Intent(this, WearMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Using existing launcher icon
            .setContentTitle("Recording Reflection")
            .setContentText("Listening...")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)

        val ongoingActivityStatus = Status.Builder()
            .addPart(Status.StopwatchPart(startTime))
            .build()

        val ongoingActivity = OngoingActivity.Builder(
            applicationContext, NOTIFICATION_ID, notificationBuilder
        )
            .setAnimatedIcon(R.mipmap.ic_launcher) // In real app, use a dedicated animated icon
            .setStaticIcon(R.mipmap.ic_launcher)
            .setTouchIntent(pendingIntent)
            .setStatus(ongoingActivityStatus)
            .build()

        ongoingActivity.apply(applicationContext)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun stopRecording() {
        isRecording = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Recording Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService(): RecordingService = this@RecordingService
    }
}
