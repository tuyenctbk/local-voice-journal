package com.localvoicejournal.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localvoicejournal.core.data.JournalEntry
import com.localvoicejournal.core.data.HabitConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    entry: JournalEntry?,
    isPremium: Boolean = false,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onRateApp: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Deep sensory background
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C20),
            Color(0xFF15102A),
            Color(0xFF0A0915)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "JOURNAL ENTRY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC0B3FF),
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete entry",
                            tint = Color(0xFFFF5252)
                        )
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = {
                                Text(
                                    text = "Delete Reflection?",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Text(
                                    text = "Are you sure you want to permanently erase this reflection from your device? This action cannot be undone.",
                                    color = Color(0xFFB0AFC0),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showDeleteConfirm = false
                                        onDelete()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Delete", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel", color = Color(0xFFB0AFC0))
                                }
                            },
                            containerColor = Color(0xFF1C1A30),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0C20),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (entry == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7A60FF))
                    }
                } else {
                    // Date card
                    val dateFormatted = remember(entry.timestamp) {
                        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
                        sdf.format(Date(entry.timestamp))
                    }
                    
                    Text(
                        text = dateFormatted,
                        fontSize = 14.sp,
                        color = Color(0xFF8682A8),
                        fontWeight = FontWeight.Medium
                    )

                    // Transcript card
                    GlassCard(title = "Your Reflection") {
                        Text(
                            text = "\"${entry.transcript}\"",
                            fontSize = 16.sp,
                            color = Color.White,
                            lineHeight = 24.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duration: ${entry.durationSeconds}s",
                                fontSize = 12.sp,
                                color = Color(0xFF8682A8)
                            )
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val weatherVal = entry.weather ?: ""
                                val locationVal = entry.location ?: ""
                                
                                if (locationVal.isNotEmpty()) {
                                    Text(
                                        text = "📍 $locationVal",
                                        fontSize = 12.sp,
                                        color = if (locationVal.contains("Offline")) Color(0xFF6B6888) else Color(0xFFC0B3FF),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                                if (weatherVal.isNotEmpty() && weatherVal != "Unknown Weather") {
                                    Text(
                                        text = "🌦️ $weatherVal",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9E8EFF)
                                    )
                                }
                            }
                        }
                    }

                    // Local AI analysis card
                    GlassCard(title = "Local AI Insights (Privacy-First)") {
                        // Stress Level Gauge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stress Level:",
                                color = Color(0xFFB5B3D6),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val stressColor = when (entry.stressLevel) {
                                "HIGH" -> Color(0xFFFF5252)
                                "MEDIUM" -> Color(0xFFFFB300)
                                else -> Color(0xFF00E676)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(stressColor.copy(alpha = 0.2f))
                                    .border(1.dp, stressColor, RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = entry.stressLevel,
                                    color = stressColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (entry.stressors.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Detected Stressors:",
                                color = Color(0xFFB5B3D6),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            entry.stressors.forEach { stressor ->
                                Text(
                                    text = "• $stressor",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }

                        // Core Themes
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Extracted Themes:",
                            color = Color(0xFFB5B3D6),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            entry.themes.forEach { theme ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF28234D))
                                        .padding(vertical = 6.dp, horizontal = 14.dp)
                                ) {
                                    Text(
                                        text = theme,
                                        color = Color(0xFFC0B3FF),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Habits Checklist
                        if (entry.habits.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Habit Milestones:",
                                color = Color(0xFFB5B3D6),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            entry.habits.forEach { habit ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "✔",
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = habit,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Rate App suggestion
                    Button(
                        onClick = onRateApp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2A4F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rate app icon",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Enjoying AuraJournal? Rate Us",
                            color = Color(0xFFC0B3FF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // AdMob Banner Ad
                    if (!isPremium) {
                        com.localvoicejournal.mobile.util.AdsHelper.BannerAd()
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    DetailScreen(
        entry = JournalEntry(
            id = 1,
            timestamp = System.currentTimeMillis(),
            transcript = "I had a very productive day working on the new Android features. I felt focused but slightly tired by the end.",
            stressLevel = "MEDIUM",
            themes = listOf("Career", "Emotional State"),
            stressors = listOf("Deadlines"),
            habits = listOf(HabitConstants.HYDRATED, HabitConstants.EXERCISED),
            durationSeconds = 45
        ),
        onBack = {},
        onDelete = {},
        onRateApp = {}
    )
}

@Composable
fun GlassCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF16132C).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFF332D5E).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFFC0B3FF),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
