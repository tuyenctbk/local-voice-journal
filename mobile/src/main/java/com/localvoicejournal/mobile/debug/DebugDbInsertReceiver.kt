package com.localvoicejournal.mobile.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.localvoicejournal.core.data.JournalDatabase
import com.localvoicejournal.core.data.JournalEntry

/**
 * Debug receiver that inserts a test JournalEntry into the database when a broadcast is sent.
 * Action: com.localvoicejournal.DEBUG_INSERT
 */
class DebugDbInsertReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = JournalDatabase.getInstance(context)
                val entry = JournalEntry(
                    timestamp = System.currentTimeMillis(),
                    transcript = "Test entry inserted by DebugDbInsertReceiver",
                    stressLevel = "LOW",
                    themes = emptyList(),
                    stressors = emptyList(),
                    habits = emptyList(),
                    durationSeconds = 5,
                    title = "Debug Test"
                )
                db.journalDao().insertEntry(entry)
            } catch (t: Throwable) {
                // Ignore in debug helper
            }
        }
    }
}
