package com.localvoicejournal.mobile.ai

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.localvoicejournal.core.data.HabitConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            habits.add(HabitConstants.HYDRATED)
        }
        if (lowercase.contains("walk") || lowercase.contains("gym") || lowercase.contains("workout") || lowercase.contains("exercise") || lowercase.contains("run")) {
            habits.add(HabitConstants.EXERCISED)
        }
        if (lowercase.contains("read") || lowercase.contains("book")) {
            habits.add(HabitConstants.READ_BOOK)
        }
        if (lowercase.contains("meditat") || lowercase.contains("breathed") || lowercase.contains("calm")) {
            habits.add(HabitConstants.MEDITATED)
        }
        if (lowercase.contains("sleep early") || lowercase.contains("rested well") || lowercase.contains("rested")) {
            habits.add(HabitConstants.GOOD_SLEEP)
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

    private val cloudAnalyzer = CloudGeminiAnalyzer(context)

    override suspend fun analyze(text: String): AnalysisResult {
        Log.d("OnDeviceModelAnalyzer", "Attempting local on-device LLM analysis")
        
        val modelFile = java.io.File(context.filesDir, "models/gemma.bin")
        val isLocalModelAvailable = modelFile.exists()
        
        if (isLocalModelAvailable) {
            var llmInference: com.google.mediapipe.tasks.genai.llminference.LlmInference? = null
            try {
                Log.d("OnDeviceModelAnalyzer", "Loading local LLM from: ${modelFile.absolutePath}")
                val options = com.google.mediapipe.tasks.genai.llminference.LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(512)
                    .setTemperature(0.2f)
                    .build()
                llmInference = com.google.mediapipe.tasks.genai.llminference.LlmInference.createFromOptions(context, options)

                val prompt = """
                    You are a sensitive mental health analyzer. Analyze the user's daily journal reflection text.
                    Return ONLY a valid raw JSON object representing the analysis. DO NOT output markdown blocks or extra texts.
                    
                    JSON Structure:
                    {
                      "stressLevel": "LOW" or "MEDIUM" or "HIGH",
                      "themes": ["Work", "Personal", "Family", "Health", "Fitness", "Nutrition", "Sleep", "Social", "Leisure", "Career", "Finance", "Education", "Mental Health", "Other"],
                      "stressors": ["Work Deadlines", "Relationship Issues", "Health Concerns", "Financial Pressure", "Lack of Sleep", "Loneliness", "Academic Stress", "Arguments", "Trauma", "General Anxiety", "Other"],
                      "habits": ["Exercised", "Meditated", "Hydrated", "Ate Healthy", "Read Book", "Good Sleep", "Spent Time with Family", "Journaled", "Took Break", "Socialized", "Other"]
                    }
                    
                    Text to analyze:
                    "$text"
                    
                    JSON:
                """.trimIndent()

                Log.d("OnDeviceModelAnalyzer", "Running local LLM inference...")
                val response = llmInference.generateResponse(prompt)
                Log.d("OnDeviceModelAnalyzer", "Local LLM response received: $response")

                val result = parseLlmJsonResponse(response)
                if (result != null) {
                    return result
                } else {
                    Log.w("OnDeviceModelAnalyzer", "Local LLM JSON parsing failed, trying fallbacks.")
                }
            } catch (e: Exception) {
                Log.e("OnDeviceModelAnalyzer", "Error running on-device MediaPipe local model, trying fallback", e)
            } finally {
                try {
                    llmInference?.close()
                } catch (ce: Exception) {
                    Log.e("OnDeviceModelAnalyzer", "Error closing llmInference", ce)
                }
            }
        }
        
        // Secondary / Cloud backup:
        val sharedPrefs = context.getSharedPreferences("aura_journal_prefs", Context.MODE_PRIVATE)
        val isCloudFallbackAllowed = sharedPrefs.getBoolean("allow_cloud_fallback", false)
        val hasKey = !sharedPrefs.getString("gemini_api_key", null).isNullOrBlank()

        if (isCloudFallbackAllowed && hasKey) {
            try {
                Log.d("OnDeviceModelAnalyzer", "Running Gemini Cloud Analyzer fallback...")
                return cloudAnalyzer.analyze(text)
            } catch (e: Exception) {
                Log.e("OnDeviceModelAnalyzer", "Gemini Cloud Analyzer failed, using local offline heuristics", e)
                withContext(Dispatchers.Main) {
                    val msg = when {
                        e.message?.contains("429") == true -> "Gemini API rate limit reached (Free Tier). Running offline analysis."
                        e.message?.contains("403") == true || e.message?.contains("401") == true -> "Gemini API Key is invalid or expired. Running offline analysis."
                        else -> "Gemini Cloud connection failed. Running offline analysis."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Log.d("OnDeviceModelAnalyzer", "Cloud analyzer fallback skipped (consent or key missing)")
        }

        // Offline / Local heuristics fallback:
        return fallbackAnalyzer.analyze(text)
    }

    private fun parseLlmJsonResponse(response: String): AnalysisResult? {
        try {
            val start = response.indexOf("{")
            val end = response.lastIndexOf("}")
            if (start == -1 || end == -1 || end <= start) return null
            
            val json = response.substring(start, end + 1)
            
            val stressLevel = "\"stressLevel\"\\s*:\\s*\"([^\"]*)\"".toRegex().find(json)?.groupValues?.get(1) ?: "LOW"
            
            val themesText = "\"themes\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(json)?.groupValues?.get(1) ?: ""
            val themes = themesText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }
            
            val stressorsText = "\"stressors\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(json)?.groupValues?.get(1) ?: ""
            val stressors = stressorsText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }
            
            val habitsText = "\"habits\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(json)?.groupValues?.get(1) ?: ""
            val habits = habitsText.split(",").map { it.trim().trim('"') }.filter { it.isNotEmpty() }
            
            return AnalysisResult(
                stressLevel = stressLevel,
                themes = themes,
                stressors = stressors,
                habits = habits
            )
        } catch (e: Exception) {
            Log.e("OnDeviceModelAnalyzer", "Failed to parse LLM JSON response", e)
            return null
        }
    }
}

class CloudGeminiAnalyzer(private val context: Context) : LocalAIAnalyzer {
    private val tag = "CloudGeminiAnalyzer"
    private val sharedPrefs = context.getSharedPreferences("aura_journal_prefs", Context.MODE_PRIVATE)

    override suspend fun analyze(text: String): AnalysisResult {
        val apiKey = sharedPrefs.getString("gemini_api_key", null)
        if (apiKey.isNullOrBlank()) {
            throw IllegalStateException("Gemini API key is not configured.")
        }

        return withContext(Dispatchers.IO) {
            val urlConnection = java.net.URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey").openConnection() as java.net.HttpURLConnection
            try {
                urlConnection.requestMethod = "POST"
                urlConnection.setRequestProperty("Content-Type", "application/json")
                urlConnection.doOutput = true

                val prompt = "Analyze the emotional content of this journal entry: \"$text\". " +
                        "Identify the stress level (LOW, MEDIUM, or HIGH), the primary themes (e.g. Career, Relationships, Fitness, Rest & Sleep, Nutrition, Emotional State), any stressors (e.g. Work Deadlines, Lack of Sleep, Financial Anxiety, Social / Family Pressure, General Overwhelm), and any positive habits mentioned actively (e.g. Hydrated, Exercised, Read Book, Meditated, Good Sleep). " +
                        "Return only a JSON object matching this structure: " +
                        "{\"stressLevel\": \"LOW\", \"themes\": [\"Career\"], \"stressors\": [], \"habits\": [\"Hydrated\"]}"

                val escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n")
                val jsonPayload = "{\"contents\":[{\"parts\":[{\"text\":\"$escapedPrompt\"}]}]}"

                urlConnection.outputStream.use { os ->
                    os.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }

                val responseCode = urlConnection.responseCode
                if (responseCode == 200) {
                    val responseText = urlConnection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(tag, "Gemini Cloud response: $responseText")
                    parseGeminiResponse(responseText)
                } else {
                    val errorText = urlConnection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    Log.w(tag, "Gemini Cloud API failed with code $responseCode: $errorText")
                    throw Exception("API response code: $responseCode")
                }
            } finally {
                urlConnection.disconnect()
            }
        }
    }

    private fun parseGeminiResponse(json: String): AnalysisResult {
        val textMatch = "\"text\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(json)
        val rawContent = textMatch?.groupValues?.get(1)
            ?.replace("\\\"", "\"")
            ?.replace("\\n", "\n")
            ?: json

        val jsonStart = rawContent.indexOf("{")
        val jsonEnd = rawContent.lastIndexOf("}")
        val jsonBlock = if (jsonStart >= 0 && jsonEnd >= 0 && jsonEnd > jsonStart) {
            rawContent.substring(jsonStart, jsonEnd + 1)
        } else {
            rawContent
        }

        val stressLevelMatch = "\"stressLevel\"\\s*:\\s*\"([^\"]+)\"".toRegex().find(jsonBlock)
        val stressLevel = stressLevelMatch?.groupValues?.get(1) ?: "LOW"

        val themesMatch = "\"themes\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(jsonBlock)
        val themes = themesMatch?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim().removeSurrounding("\"") }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val stressorsMatch = "\"stressors\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(jsonBlock)
        val stressors = stressorsMatch?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim().removeSurrounding("\"") }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        val habitsMatch = "\"habits\"\\s*:\\s*\\[([^\\]]*)\\]".toRegex().find(jsonBlock)
        val habits = habitsMatch?.groupValues?.get(1)
            ?.split(",")
            ?.map { it.trim().removeSurrounding("\"") }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        return AnalysisResult(stressLevel, themes, stressors, habits)
    }
}
