package com.localvoicejournal.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
abstract class JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    protected abstract fun _getAllEntries(): Flow<List<JournalEntry>>

    fun getAllEntries(): Flow<List<JournalEntry>> {
        return _getAllEntries().map { list ->
            list.map { it.copy(transcript = CryptographyHelper.decryptTranscript(it.transcript)) }
        }
    }

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    protected abstract fun _getEntryById(id: Long): Flow<JournalEntry?>

    fun getEntryById(id: Long): Flow<JournalEntry?> {
        return _getEntryById(id).map { entry ->
            entry?.copy(transcript = CryptographyHelper.decryptTranscript(entry.transcript))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun _insertEntry(entry: JournalEntry): Long

    suspend fun insertEntry(entry: JournalEntry): Long {
        val encryptedEntry = entry.copy(transcript = CryptographyHelper.encryptTranscript(entry.transcript))
        return _insertEntry(encryptedEntry)
    }

    @Delete
    abstract suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    abstract suspend fun clearAllEntries()

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    protected abstract suspend fun _getAllEntriesList(): List<JournalEntry>

    suspend fun getAllEntriesList(): List<JournalEntry> {
        return _getAllEntriesList().map { it.copy(transcript = CryptographyHelper.decryptTranscript(it.transcript)) }
    }
}
