package com.localvoicejournal.mobile.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localvoicejournal.core.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.res.stringResource
import com.localvoicejournal.mobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    entries: List<JournalEntry>,
    isPremium: Boolean,
    onEntryClick: (JournalEntry) -> Unit,
    onBack: () -> Unit,
    onUnlockPremium: () -> Unit,
    showBackButton: Boolean = true
) {
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
                        text = "DASHBOARD & HISTORY",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC0B3FF),
                        letterSpacing = 2.sp
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                        }
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                // Weekly Analytics Section
                item {
                    Text(
                        text = "Weekly Analytics Dashboard",
                        fontSize = 14.sp,
                        color = Color(0xFFC0B3FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF16132C).copy(alpha = 0.6f))
                            .border(1.dp, Color(0xFF332D5E).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        // Blurred/masked content if not premium
                        val blurRadius = if (isPremium) 0.dp else 12.dp
                        
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .blur(blurRadius),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STRESS TRENDS (LAST 7 REFLECTIONS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8682A8),
                                letterSpacing = 1.sp
                            )

                            // Custom Line Graph
                            StressChart(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .padding(vertical = 8.dp),
                                entries = entries.take(7).reversed()
                            )

                            // Legend / Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(stringResource(R.string.low_calm), color = Color(0xFF00E676), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.medium), color = Color(0xFFFFB300), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.high_stress), color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Premium Block Overlay
                        if (!isPremium) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F0C20).copy(alpha = 0.65f))
                                    .clickable { onUnlockPremium() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Lock icon",
                                        tint = Color(0xFFC0B3FF),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Unlock Premium Weekly Insights",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Stress trendlines & detailed habit correlation",
                                        color = Color(0xFF8682A8),
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Habit Streaks summary
                item {
                    Text(
                        text = "Active Habit Insights",
                        fontSize = 14.sp,
                        color = Color(0xFFC0B3FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HabitStatCard(
                            title = "Hydration",
                            count = entries.count { it.habits.contains("Hydrated") },
                            color = Color(0xFF00B0FF),
                            modifier = Modifier.weight(1f)
                        )
                        HabitStatCard(
                            title = "Exercise",
                            count = entries.count { it.habits.contains("Exercised") },
                            color = Color(0xFFFF4081),
                            modifier = Modifier.weight(1f)
                        )
                        HabitStatCard(
                            title = "Good Sleep",
                            count = entries.count { it.habits.contains("Good Sleep") },
                            color = Color(0xFF7C4DFF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // History Header
                item {
                    Text(
                        text = "History (${entries.size} reflections)",
                        fontSize = 14.sp,
                        color = Color(0xFFC0B3FF),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (entries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No voice reflections saved yet.\nTap the microphone on the Home Screen to begin.",
                                color = Color(0xFF8682A8),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // List of reflections
                itemsIndexed(entries) { index, entry ->
                    val dateStr = remember(entry.timestamp) {
                        val sdf = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                        sdf.format(Date(entry.timestamp))
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF16132C).copy(alpha = 0.4f))
                            .border(1.dp, Color(0xFF2C2750), RoundedCornerShape(12.dp))
                            .clickable { onEntryClick(entry) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    color = Color(0xFF8682A8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                               )
 
                                val stressColor = when (entry.stressLevel) {
                                    "HIGH" -> Color(0xFFFF5252)
                                    "MEDIUM" -> Color(0xFFFFB300)
                                    else -> Color(0xFF00E676)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(stressColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = entry.transcript,
                                color = Color.White,
                                fontSize = 14.sp,
                                maxLines = 2,
                                lineHeight = 20.sp
                            )
                            if (entry.themes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    entry.themes.take(3).forEach { theme ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF211D3C))
                                                .padding(vertical = 2.dp, horizontal = 8.dp)
                                        ) {
                                            Text(
                                                text = theme,
                                                color = Color(0xFFC0B3FF),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!isPremium && (index + 1) % 4 == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        com.localvoicejournal.mobile.util.AdsHelper.NativeAd()
                    }
                }
 
                if (!isPremium) {
                    item {
                        com.localvoicejournal.mobile.util.AdsHelper.BannerAd()
                    }
                }
            }
        }
    }
}

@Composable
fun StressChart(modifier: Modifier, entries: List<JournalEntry>) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Draw horizontal grid guidelines
        val gridLines = 3
        val stepY = height / (gridLines - 1)
        for (i in 0 until gridLines) {
            drawLine(
                color = Color(0xFF2C2750),
                start = Offset(0f, i * stepY),
                end = Offset(width, i * stepY),
                strokeWidth = 1f
            )
        }

        if (entries.size >= 2) {
            val stepX = width / (entries.size - 1)
            val points = entries.mapIndexed { idx, entry ->
                val levelValue = when (entry.stressLevel) {
                    "HIGH" -> 0f       // Top of screen (least Y)
                    "MEDIUM" -> 0.5f   // Middle of screen
                    else -> 1f         // Bottom of screen (max Y)
                }
                Offset(idx * stepX, levelValue * height)
            }

            // Draw connecting path
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = Color(0xFF7A60FF),
                style = Stroke(width = 4f)
            )

            // Draw points
            points.forEachIndexed { index, point ->
                val entry = entries[index]
                val pointColor = when (entry.stressLevel) {
                    "HIGH" -> Color(0xFFFF5252)
                    "MEDIUM" -> Color(0xFFFFB300)
                    else -> Color(0xFF00E676)
                }
                drawCircle(
                    color = pointColor,
                    radius = 8f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = point
                )
            }
        } else {
            // Draw a flat placeholder line
            drawLine(
                color = Color(0xFF7A60FF),
                start = Offset(0f, height / 2),
                end = Offset(width, height / 2),
                strokeWidth = 4f
            )
        }
    }
}

@Composable
fun HabitStatCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF16132C).copy(alpha = 0.6f))
            .border(1.dp, Color(0xFF332D5E).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color(0xFF8682A8),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = "days",
                fontSize = 9.sp,
                color = Color(0xFF8682A8)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboardScreen() {
    DashboardScreen(
        entries = listOf(
            JournalEntry(1, System.currentTimeMillis(), "Today was good. Feeling productive and calm.", "LOW", listOf("Peace", "Work"), emptyList(), listOf("Exercised"), 30),
            JournalEntry(2, System.currentTimeMillis() - 86400000, "Stressed out about the meeting tomorrow.", "HIGH", listOf("Work"), listOf("Deadlines"), emptyList(), 60)
        ),
        isPremium = true,
        onEntryClick = {},
        onBack = {},
        onUnlockPremium = {}
    )
}
