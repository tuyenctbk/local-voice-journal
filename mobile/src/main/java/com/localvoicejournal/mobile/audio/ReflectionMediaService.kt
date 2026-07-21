package com.localvoicejournal.mobile.audio

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.localvoicejournal.core.data.JournalDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Provides a standard Android Media interface for AuraJournal.
 * This allows the app to pass Android Automotive 'Media' category guidelines
 * by allowing users to browse and play their past reflections.
 */
class ReflectionMediaService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, "ReflectionMediaService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { 
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
                override fun onPause() {
                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }
            })
            isActive = true
        }
        sessionToken = mediaSession?.sessionToken
        setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
    }

    private fun setPlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
            .setState(state, 0L, 1f)
        mediaSession?.setPlaybackState(stateBuilder.build())
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        if (parentId == "root") {
            result.detach()
            serviceScope.launch {
                val db = JournalDatabase.getInstance(applicationContext)
                val entries = db.journalDao().getAllEntriesList()
                
                val mediaItems = entries.take(10).map { entry ->
                    val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(entry.timestamp))
                    val description = MediaDescriptionCompat.Builder()
                        .setMediaId(entry.id.toString())
                        .setTitle("Reflection: $dateStr")
                        .setSubtitle(entry.transcript.take(100))
                        .build()
                    MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                }
                result.sendResult(mediaItems)
            }
        } else {
            result.sendResult(emptyList())
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        mediaSession?.release()
        super.onDestroy()
    }
}
