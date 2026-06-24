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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingScreen(
    onRequestPermission: () -> Unit,
    hasMicrophonePermission: Boolean,
    onFinishOnboarding: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    
    val steps = listOf(
        OnboardingStep(
            title = "AuraJournal",
            subtitle = "Your voice reflection space",
            description = "Welcome to a sensory-friendly, minimalist space where you can capture your daily thoughts by speaking for just 60 seconds."
        ),
        OnboardingStep(
            title = "100% Privacy-First",
            subtitle = "Local processing, zero servers",
            description = "Your intimate reflections, audio transcripts, and analysis never leave this phone. Processing runs completely offline using local models."
        ),
        OnboardingStep(
            title = "Microphone Access",
            subtitle = "Grant audio recording permission",
            description = "We need audio access to transcribe your reflections locally. Click the button below to grant permission."
        )
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

            // Dynamic Step Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val step = steps[currentStep]
                
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

                if (currentStep == 2) {
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
                                .size(width = if (index == currentStep) 24.dp else 8.dp, height = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (index == currentStep) Color(0xFF7A60FF) else Color(0xFF423E5D))
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
                    if (currentStep > 0) {
                        TextButton(onClick = { currentStep-- }) {
                            Text("Back", color = Color(0xFFB0AFC0))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    Button(
                        onClick = {
                            if (currentStep < 2) {
                                currentStep++
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
                            .width(140.dp)
                    ) {
                        Text(
                            text = if (currentStep == 2) "Get Started" else "Next",
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

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() {
    OnboardingScreen(
        onRequestPermission = {},
        hasMicrophonePermission = false,
        onFinishOnboarding = {}
    )
}
