package com.example.jazzdashboard.ui.screens
import com.example.jazzdashboard.ui.components.OmegaWatchFace
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jazzdashboard.ui.theme.RajdhaniFamily

/**
 * Intro screen matching HTML lines 66-84.
 * Two stages:
 *  1. Shows Omega watch face. Tap watch → hide watch, show Engine Start button.
 *  2. Tap background → hide button, show watch again.
 */
@Composable
fun IntroScreen(
    onStartEngine: () -> Unit
) {
    var showStartButton by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Tap background to cancel (HTML handleIntroBgClick)
                if (showStartButton) {
                    showStartButton = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Stage 1: Omega Watch Face
        AnimatedVisibility(
            visible = !showStartButton,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            showStartButton = true
                        }
                ) {
                    OmegaWatchFace(size = 280.dp, speedKmh = 0f)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "TAP TO UNLOCK",
                    color = Color(0xFF52525B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontSize = 12.sp,
                    fontFamily = RajdhaniFamily
                )
            }
        }

        // Stage 2: Engine Start Button
        AnimatedVisibility(
            visible = showStartButton,
            enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.5f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Pulse glow ring (CSS anim-pulse-red)
                val pulseAlpha = if (pulseGlow < 0.7f) 0.7f - pulseGlow else 0f
                val pulseSize = 220.dp + (pulseGlow * 20).dp

                Box(contentAlignment = Alignment.Center) {
                    // Pulse ring behind button
                    Box(
                        modifier = Modifier
                            .size(pulseSize)
                            .background(Color(0xFFDC2626).copy(alpha = pulseAlpha), CircleShape)
                    )

                    // The button itself
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFDC2626), Color(0xFF7F1D1D))
                                )
                            )
                            .border(5.dp, Color(0xFF27272A), CircleShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onStartEngine() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner borders matching HTML
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .border(1.dp, Color.Black.copy(alpha = 0.3f), CircleShape)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "ENGINE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 3.sp,
                                fontFamily = RajdhaniFamily
                            )
                            Text(
                                "START",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp,
                                letterSpacing = (-1).sp,
                                fontFamily = RajdhaniFamily
                            )
                            Text(
                                "STOP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 3.sp,
                                modifier = Modifier.padding(top = 4.dp),
                                fontFamily = RajdhaniFamily
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF87171))
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "TAP BACKGROUND TO CANCEL",
                    color = Color(0xFF52525B),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    fontSize = 11.sp,
                    fontFamily = RajdhaniFamily
                )
            }
        }
    }
}


