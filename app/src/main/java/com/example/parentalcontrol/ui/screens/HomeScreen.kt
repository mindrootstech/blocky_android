package com.example.parentalcontrol.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.utils.PreferenceManager
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(preferenceManager: PreferenceManager) {
    var isRunning by remember { mutableStateOf(preferenceManager.isServiceRunning) }

    // VARIABLE TO CONTROL PROGRESS FILL (0.0 to 1.0)
    // Change this value to test different progress states
    val targetProgress = if (isRunning) 0.75f else 0f

    HomeContent(
        isRunning = isRunning,
        progress = targetProgress,
        onToggle = {
            val newState = !isRunning
            preferenceManager.isServiceRunning = newState
            isRunning = newState
            if (newState) {
                preferenceManager.lastServiceStartTime = System.currentTimeMillis()
            }
        }
    )
}

@Composable
fun HomeContent(
    isRunning: Boolean,
    progress: Float,
    onToggle: () -> Unit
) {
    val bgColor = Color(0xFFF0F3F8)
    val pureWhite = Color.White
    val shadowDark = Color(0xFFD1D9E6)

    // Gradient Colors matching the button
    val purplePrimary = Color(0xFF9C27B0)
    val redPrimary = Color(0xFFEF5350)
    val activeColor = if (isRunning) redPrimary else purplePrimary

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isRunning) "SYSTEM PROTECTED" else "READY TO PROTECT",
                style = MaterialTheme.typography.labelLarge,
                color = shadowDark,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(64.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(340.dp)
            ) {
                // 1. OUTER NEUMORPHIC SURFACE
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .shadow(
                            12.dp,
                            CircleShape,
                            ambientColor = shadowDark,
                            spotColor = shadowDark
                        )
                        .clip(CircleShape)
                        .background(pureWhite)
                )

                // 2. 3D SUNKEN TRACK AND PROGRESS
                Canvas(modifier = Modifier.size(300.dp)) {
                    val strokeWidth = 32.dp.toPx()

                    // --- EMPTY TRACK (DON'T CHANGE) ---
                    for (i in 1..12) {
                        drawArc(
                            color = shadowDark.copy(alpha = 0.05f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(
                                width = strokeWidth - (i * 1.5).dp.toPx(),
                                cap = StrokeCap.Round
                            ),
                            topLeft = Offset(
                                10.dp.toPx() + i.dp.toPx(),
                                10.dp.toPx() + i.dp.toPx()
                            ),
                            size = Size(
                                size.width - 20.dp.toPx() - (i * 2).dp.toPx(),
                                size.height - 20.dp.toPx() - (i * 2).dp.toPx()
                            )
                        )
                    }

                    // --- FILLED PROGRESS PART ---
                    if (animatedProgress > 0f) {
                        // A. Ambient Shadow/Glow for the filled part
                        for (i in 1..8) {
                            drawArc(
                                color = activeColor.copy(alpha = 0.04f),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth + i.dp.toPx(),
                                    cap = StrokeCap.Round
                                ),
                                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                                size = Size(size.width - 20.dp.toPx(), size.height - 20.dp.toPx())
                            )
                        }

                        // B. The Colored 3D Progress Arc
                        for (i in 1..12) {
                            drawArc(
                                color = activeColor.copy(alpha = 0.5f),
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(
                                    width = strokeWidth - (i * 1.5).dp.toPx(),
                                    cap = StrokeCap.Round
                                ),
                                topLeft = Offset(
                                    10.dp.toPx() - (i / 2).dp.toPx(),
                                    10.dp.toPx() - (i / 2).dp.toPx()
                                ),
                                size = Size(
                                    size.width - 20.dp.toPx() + i.dp.toPx(),
                                    size.height - 20.dp.toPx() + i.dp.toPx()
                                )
                            )
                        }

                        // C. Thumb Pointer
                        val angle = (360f * animatedProgress - 90f) * (Math.PI / 180f).toFloat()
                        val r = (size.width - 20.dp.toPx()) / 2f
                        val thumbCenter = Offset(
                            x = (size.width / 2f) + r * cos(angle.toDouble()).toFloat(),
                            y = (size.height / 2f) + r * sin(angle.toDouble()).toFloat()
                        )

                        drawCircle(
                            color = Color.Black.copy(alpha = 0.2f),
                            radius = 15.dp.toPx(),
                            center = thumbCenter + Offset(2.dp.toPx(), 2.dp.toPx())
                        )

                        // 2. Thumb White Border (Ring)
                        drawCircle(
                            color = pureWhite,
                            radius = 13.dp.toPx(),
                            center = thumbCenter
                        )

                        // 3. Thumb Colored Core (Matches active state)
                        drawCircle(
                            color = activeColor,
                            radius = 8.dp.toPx(),
                            center = thumbCenter
                        )

                        // 4. Specular Highlight (3D sphere effect)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = 3.dp.toPx(),
                            center = thumbCenter - Offset(3.dp.toPx(), 3.dp.toPx())
                        )
                    }
                }

                // 3. CENTER BUTTON
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(170.dp)
                        .shadow(25.dp, CircleShape, spotColor = activeColor)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isRunning)
                                    listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
                                else
                                    listOf(Color(0xFFAB47BC), Color(0xFF7B1FA2))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color.White),
                            onClick = onToggle
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        pureWhite.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ), startY = 0f, endY = 150f
                                )
                            )
                    )
                    Text(
                        text = if (isRunning) "STOP" else "START",
                        color = pureWhite,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(72.dp))
            Text(
                text = if (isRunning) "MONITORING ACTIVE" else "READY TO SECURE",
                style = MaterialTheme.typography.bodyMedium,
                color = shadowDark.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, name = "Test Case 75%")
@Composable
fun PreviewHomeTest() {
    MaterialTheme {
        HomeContent(isRunning = true, progress = 0.55f, onToggle = {})
    }
}
