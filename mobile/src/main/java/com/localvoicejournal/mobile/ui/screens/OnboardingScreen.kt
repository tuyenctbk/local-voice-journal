package com.localvoicejournal.mobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localvoicejournal.mobile.R
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onRequestPermission: () -> Unit,
    hasMicrophonePermission: Boolean,
    onFinishOnboarding: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    val steps = listOf(
        OnboardingStep(
            title = stringResource(R.string.app_name),
            subtitle = "Your voice reflection space",
            description = stringResource(R.string.onboarding_welcome)
        ),
        OnboardingStep(
            title = stringResource(R.string.privacy_first),
            subtitle = "Local processing, zero servers",
            description = "Your intimate reflections, audio transcripts, and analysis never leave this phone. Processing runs completely offline using local models."
        ),
        OnboardingStep(
            title = "Microphone Access",
            subtitle = "Grant audio recording permission",
            description = stringResource(R.string.mic_permission_required)
        )
    )

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { steps.size }
    )

    // Deep sensory-friendly dark theme gradient (HSL derived)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0C20), // Deep indigo
            Color(0xFF15102A), // Dark purple
            Color(0xFF0A0915)  // Near black
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top branding
            Text(
                text = "AURA",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC0B3FF),
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Dynamic Step Content (Swipeable Pager)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = step.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 38.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = step.subtitle,
                        fontSize = 16.sp,
                        color = Color(0xFFC0B3FF),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = step.description,
                        fontSize = 15.sp,
                        color = Color(0xFFB0AFC0),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (page == 2) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasMicrophonePermission) Color(0xFF2E7D32) else Color(0xFF7A60FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (hasMicrophonePermission) "✓ Permission Granted" else "Grant Microphone Permission",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Navigation Indicators & Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (index == pagerState.currentPage) 24.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (index == pagerState.currentPage) Color(0xFF7A60FF) else Color(0xFF423E5D))
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Text(stringResource(R.string.back), color = Color(0xFFB0AFC0))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                if (hasMicrophonePermission) {
                                    onFinishOnboarding()
                                } else {
                                    onRequestPermission()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7A60FF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .width(160.dp)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 2) stringResource(R.string.get_started) else stringResource(R.string.next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() {
    OnboardingScreen(
        onRequestPermission = {},
        hasMicrophonePermission = false,
        onFinishOnboarding = {}
    )
}
