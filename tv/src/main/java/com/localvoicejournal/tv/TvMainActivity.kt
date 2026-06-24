package com.localvoicejournal.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.localvoicejournal.core.data.JournalDatabase
import com.localvoicejournal.core.data.JournalEntry
import kotlinx.coroutines.flow.collect

class TvMainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val database = JournalDatabase.getInstance(this)
            var entries by remember { mutableStateOf(emptyList<JournalEntry>()) }

            LaunchedEffect(Unit) {
                database.journalDao().getAllEntries().collect {
                    entries = it
                }
            }

            TvDashboard(entries)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDashboard(entries: List<JournalEntry>) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C20),
            Color(0xFF05040B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(48.dp)
    ) {
        Column {
            Text(
                text = "AURAJOURNAL TV",
                style = MaterialTheme.typography.displayMedium,
                color = Color(0xFFC0B3FF),
                fontWeight = FontWeight.ExtraBold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (entries.isEmpty()) {
                Text(
                    text = "No reflections yet. Use your phone or watch to record.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            } else {
                Text(
                    text = "Latest Reflection: \"${entries.first().transcript}\"",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
fun PreviewTvDashboard() {
    TvDashboard(
        entries = listOf(
            JournalEntry(1, System.currentTimeMillis(), "Today was an amazing day for coding.", "LOW", listOf("Career"), emptyList(), emptyList(), 30)
        )
    )
}
