package com.localvoicejournal.core.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    fun getEntryById(id: Long): Flow<JournalEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries")
    suspend fun clearAllEntries()

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesList(): List<JournalEntry>
}
