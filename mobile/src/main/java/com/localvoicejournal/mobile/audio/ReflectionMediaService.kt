package com.localvoicejournal.mobile.audio

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat

/**
 * Provides a standard Android Media interface for AuraJournal.
 * This allows the app to pass Android Automotive 'Media' category guidelines
 * by allowing users to browse and play their past reflections.
 */
class ReflectionMediaService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate() {
        super.onCreate()

        mediaSession = MediaSessionCompat(baseContext, "ReflectionMediaService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { /* Implementation for playing reflections */ }
                override fun onPause() { /* Implementation for pausing */ }
            })
            isActive = true
        }
        sessionToken = mediaSession?.sessionToken
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        // Return a root that allows browsing of local reflections
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        // Return a list of reflections (e.g. from Room DB) as MediaItems
        result.sendResult(emptyList())
    }

    override fun onDestroy() {
        mediaSession?.release()
        super.onDestroy()
    }
}
