package com.localvoicejournal.mobile.ai

import android.content.Context
import android.util.Log

data class AnalysisResult(
    val stressLevel: String, // "LOW", "MEDIUM", "HIGH"
    val themes: List<String>,
    val stressors: List<String>,
    val habits: List<String>
)

interface LocalAIAnalyzer {
    suspend fun analyze(text: String): AnalysisResult
}

class FallbackLocalAnalyzer : LocalAIAnalyzer {
    override suspend fun analyze(text: String): AnalysisResult {
        val lowercase = text.lowercase()

        // 1. Analyze Stress Level
        val stressKeywords = listOf(
            "stress", "anxious", "anxiety", "overwhelmed", "deadline", "exhausted",
            "tired", "pressure", "busy", "hurry", "worry", "worried", "hard", "difficult", "struggle"
        )
        var stressScore = 0
        stressKeywords.forEach { keyword ->
            if (lowercase.contains(keyword)) {
                stressScore++
            }
        }
        val stressLevel = when {
            stressScore >= 4 -> "HIGH"
            stressScore >= 1 -> "MEDIUM"
            else -> "LOW"
        }

        // 2. Identify Stressors
        val stressors = mutableListOf<String>()
        if (lowercase.contains("work") || lowercase.contains("job") || lowercase.contains("deadline") || lowercase.contains("coding")) {
            stressors.add("Work Deadlines")
        }
        if (lowercase.contains("sleep") || lowercase.contains("tired") || lowercase.contains("exhausted") || lowercase.contains("insomnia")) {
            stressors.add("Lack of Sleep")
        }
        if (lowercase.contains("money") || lowercase.contains("bill") || lowercase.contains("pay") || lowercase.contains("cost")) {
            stressors.add("Financial Anxiety")
        }
        if (lowercase.contains("family") || lowercase.contains("relationship") || lowercase.contains("argument") || lowercase.contains("friend")) {
            stressors.add("Social / Family Pressure")
        }
        if (stressors.isEmpty() && stressLevel != "LOW") {
            stressors.add("General Overwhelm")
        }

        // 3. Extract Themes
        val themes = mutableListOf<String>()
        if (lowercase.contains("work") || lowercase.contains("job") || lowercase.contains("office") || lowercase.contains("project") || lowercase.contains("code")) {
            themes.add("Career")
        }
        if (lowercase.contains("sleep") || lowercase.contains("tired") || lowercase.contains("rest") || lowercase.contains("bed")) {
            themes.add("Rest & Sleep")
        }
        if (lowercase.contains("run") || lowercase.contains("walk") || lowercase.contains("gym") || lowercase.contains("workout") || lowercase.contains("exercise") || lowercase.contains("sport")) {
            themes.add("Fitness")
        }
        if (lowercase.contains("sad") || lowercase.contains("happy") || lowercase.contains("glad") || lowercase.contains("depressed") || lowercase.contains("feel") || lowercase.contains("mood")) {
            themes.add("Emotional State")
        }
        if (lowercase.contains("family") || lowercase.contains("mom") || lowercase.contains("dad") || lowercase.contains("friend") || lowercase.contains("wife") || lowercase.contains("husband") || lowercase.contains("kid")) {
            themes.add("Relationships")
        }
        if (lowercase.contains("water") || lowercase.contains("food") || lowercase.contains("eat") || lowercase.contains("meal") || lowercase.contains("diet")) {
            themes.add("Nutrition")
        }
        if (themes.isEmpty()) {
            themes.add("Daily Reflection")
        }

        // 4. Track Habits mentioned positively/actively
        val habits = mutableListOf<String>()
        if (lowercase.contains("water") || lowercase.contains("hydrat")) {
            habits.add("Hydrated")
        }
        if (lowercase.contains("walk") || lowercase.contains("gym") || lowercase.contains("workout") || lowercase.contains("exercise") || lowercase.contains("run")) {
            habits.add("Exercised")
        }
        if (lowercase.contains("read") || lowercase.contains("book")) {
            habits.add("Read Book")
        }
        if (lowercase.contains("meditat") || lowercase.contains("breathed") || lowercase.contains("calm")) {
            habits.add("Meditated")
        }
        if (lowercase.contains("sleep early") || lowercase.contains("rested well") || lowercase.contains("rested")) {
            habits.add("Good Sleep")
        }

        return AnalysisResult(
            stressLevel = stressLevel,
            themes = themes.distinct(),
            stressors = stressors.distinct(),
            habits = habits.distinct()
        )
    }
}

class OnDeviceModelAnalyzer(
    private val context: Context,
    private val fallbackAnalyzer: LocalAIAnalyzer = FallbackLocalAnalyzer()
) : LocalAIAnalyzer {

    override suspend fun analyze(text: String): AnalysisResult {
        Log.d("OnDeviceModelAnalyzer", "Attempting on-device Gemini Nano/AICore analysis")
        
        // This is a placeholder demonstrating where the MediaPipe GenAI / AICore client initialization
        // would take place at runtime. Since AICore is hardware-limited to high-end flagship devices,
        // we check for model compatibility/availability and fallback to FallbackLocalAnalyzer.
        val isAICoreSupported = false // Check for Google Play Services AICore presence
        
        return if (isAICoreSupported) {
            try {
                // Initialize MediaPipe LLM Tasks GenAI:
                // val options = LlmInferenceOptions.builder().setModelPath("/data/local/tmp/gemini-nano.bin").build()
                // val llmInference = LlmInference.createFromOptions(context, options)
                // val response = llmInference.generateResponse("Analyze this journal entry: $text...")
                // Parse response to AnalysisResult...
                
                AnalysisResult(
                    stressLevel = "LOW",
                    themes = listOf("On-Device AI"),
                    stressors = emptyList(),
                    habits = listOf("On-Device Processing")
                )
            } catch (e: Exception) {
                Log.e("OnDeviceModelAnalyzer", "Error running on-device model, falling back to local NLP heuristics", e)
                fallbackAnalyzer.analyze(text)
            }
        } else {
            Log.d("OnDeviceModelAnalyzer", "AICore not available on this device, using fallback local NLP")
            fallbackAnalyzer.analyze(text)
        }
    }
}
