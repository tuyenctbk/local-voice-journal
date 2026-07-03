package com.localvoicejournal.mobile.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackLocalAnalyzerTest {

    private val analyzer = FallbackLocalAnalyzer()

    @Test
    fun testLowStressDailyReflection() = runBlocking {
        val input = "Today was a nice day. I went for a run in the morning, had a healthy meal, and stayed hydrated."
        val result = analyzer.analyze(input)
        
        assertEquals("LOW", result.stressLevel)
        assertTrue(result.stressors.isEmpty())
        assertTrue(result.themes.contains("Daily Reflection") || result.themes.contains("Fitness") || result.themes.contains("Nutrition"))
        assertTrue(result.habits.contains("Hydrated"))
        assertTrue(result.habits.contains("Exercised"))
    }

    @Test
    fun testHighStressWorkplaceAnxiety() = runBlocking {
        val input = "I am so overwhelmed and stressed about the coding project. The deadline is tomorrow and I feel so much pressure. I am extremely worried."
        val result = analyzer.analyze(input)
        
        assertEquals("HIGH", result.stressLevel)
        assertTrue(result.stressors.contains("Work Deadlines"))
        assertTrue(result.themes.contains("Career"))
    }

    @Test
    fun testHabitsAndThemesDetection() = runBlocking {
        val input = "I meditated today to feel calm. Then I read a book before sleep early."
        val result = analyzer.analyze(input)
        
        assertTrue(result.habits.contains("Meditated"))
        assertTrue(result.habits.contains("Read Book"))
        assertTrue(result.habits.contains("Good Sleep"))
    }
}
