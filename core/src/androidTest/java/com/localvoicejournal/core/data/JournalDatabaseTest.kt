package com.localvoicejournal.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class JournalDatabaseTest {

    private lateinit var db: JournalDatabase
    private lateinit var dao: JournalDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JournalDatabase::class.java).build()
        dao = db.journalDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetEntry() = runBlocking {
        val entry = JournalEntry(
            timestamp = 1000L,
            transcript = "Testing insertion",
            stressLevel = "LOW",
            themes = listOf("Test"),
            stressors = emptyList(),
            habits = listOf("Good sleep"),
            durationSeconds = 10
        )
        val id = dao.insertEntry(entry)
        assertEquals(1L, id)

        val retrieved = dao.getEntryById(id).first()
        assertNotNull(retrieved)
        assertEquals("Testing insertion", retrieved?.transcript)
        assertEquals("LOW", retrieved?.stressLevel)
        assertEquals(listOf("Test"), retrieved?.themes)
    }

    @Test
    fun deleteEntry() = runBlocking {
        val entry = JournalEntry(
            id = 1L,
            timestamp = 1000L,
            transcript = "Testing deletion",
            stressLevel = "MEDIUM",
            themes = emptyList(),
            stressors = emptyList(),
            habits = emptyList(),
            durationSeconds = 5
        )
        dao.insertEntry(entry)
        
        val retrievedBefore = dao.getEntryById(1L).first()
        assertNotNull(retrievedBefore)

        dao.deleteEntry(retrievedBefore!!)
        val retrievedAfter = dao.getEntryById(1L).first()
        assertNull(retrievedAfter)
    }

    @Test
    fun getAllEntriesOrderedByTimestamp() = runBlocking {
        val entry1 = JournalEntry(
            id = 1L,
            timestamp = 1000L,
            transcript = "Older entry",
            stressLevel = "LOW",
            themes = emptyList(),
            stressors = emptyList(),
            habits = emptyList(),
            durationSeconds = 5
        )
        val entry2 = JournalEntry(
            id = 2L,
            timestamp = 2000L,
            transcript = "Newer entry",
            stressLevel = "HIGH",
            themes = emptyList(),
            stressors = emptyList(),
            habits = emptyList(),
            durationSeconds = 5
        )
        dao.insertEntry(entry1)
        dao.insertEntry(entry2)

        val allFlow = dao.getAllEntries().first()
        assertEquals(2, allFlow.size)
        // Order by timestamp DESC, so entry2 (2000L) should be first
        assertEquals(2L, allFlow[0].id)
        assertEquals(1L, allFlow[1].id)

        val list = dao.getAllEntriesList()
        assertEquals(2, list.size)
        assertEquals(2L, list[0].id)
        assertEquals(1L, list[1].id)
    }

    @Test
    fun clearAllEntries() = runBlocking {
        val entry = JournalEntry(
            id = 1L,
            timestamp = 1000L,
            transcript = "Sample",
            stressLevel = "LOW",
            themes = emptyList(),
            stressors = emptyList(),
            habits = emptyList(),
            durationSeconds = 5
        )
        dao.insertEntry(entry)
        dao.clearAllEntries()

        val list = dao.getAllEntriesList()
        assertEquals(0, list.size)
    }
}
