package com.localvoicejournal.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val transcript: String,
    val stressLevel: String, // "LOW", "MEDIUM", "HIGH"
    val themes: List<String>,
    val stressors: List<String>,
    val habits: List<String>,
    val durationSeconds: Int,
    val title: String = ""
)