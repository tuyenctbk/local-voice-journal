package com.localvoicejournal.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun testFromString_emptyOrNull() {
        assertTrue(converters.fromString(null).isEmpty())
        assertTrue(converters.fromString("").isEmpty())
    }

    @Test
    fun testFromString_valid() {
        val input = "Work,,Life,,Balance"
        val result = converters.fromString(input)
        assertEquals(3, result.size)
        assertEquals("Work", result[0])
        assertEquals("Life", result[1])
        assertEquals("Balance", result[2])
    }

    @Test
    fun testFromList_emptyOrNull() {
        assertEquals("", converters.fromList(null))
        assertEquals("", converters.fromList(emptyList()))
    }

    @Test
    fun testFromList_valid() {
        val input = listOf("Work", "Life", "Balance")
        val result = converters.fromList(input)
        assertEquals("Work,,Life,,Balance", result)
    }

    @Test
    fun testRoundTrip() {
        val original = listOf("Career", "Health", "Social")
        val serialized = converters.fromList(original)
        val deserialized = converters.fromString(serialized)
        assertEquals(original, deserialized)
    }
}
