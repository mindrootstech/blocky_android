package com.example.parentalcontrol.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.PreferenceManager
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(preferenceManager: PreferenceManager) {
    var isRunning by remember { mutableStateOf(preferenceManager.isServiceRunning) }

    // VARIABLE TO CONTROL PROGRESS FILL (0.0 to 1.0)
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
    // Access colors from resources
    val bgColor = colorResource(id = R.color.neumorphic_bg)
    val pureWhite = colorResource(id = R.color.white)
    val blackColor = colorResource(id = R.color.blackColor)

    val activeColor = colorResource(id = R.color.purple_start_vibrant)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pureWhite)
    ) {
        // MAIN CONTENT AREA (Grey background with curved bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp))
                .background(bgColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP HEADER (Inside Grey Box): "Blocky" centered
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = "App Icon",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(blackColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "BLOCKY",
                        style = MaterialTheme.typography.headlineSmall,
                        color = blackColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }

                // Interaction Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CenterButton(
                        isRunning = isRunning,
                        activeColor = activeColor,
                        progress = progress,
                        onToggle = onToggle
                    )
                }
            }
        }
    }
}

@Composable
fun CenterButton(
    isRunning: Boolean,
    activeColor: Color,
    progress: Float,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pureWhite = colorResource(id = R.color.white)
    val shadowDark = colorResource(id = R.color.neumorphic_shadow_dark)
    val purpleVibrant = colorResource(id = R.color.purple_start_vibrant)
    val purpleDeep = colorResource(id = R.color.purple_start_deep)
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(340.dp)
    ) {
        // OUTER NEUMORPHIC SURFACE
        Box(
            modifier = Modifier
                .size(250.dp)
                .shadow(
                    12.dp,
                    CircleShape,
                    ambientColor = shadowDark,
                    spotColor = shadowDark
                )
                .clip(CircleShape)
                .background(lightPurpleColor)
        )

        // 3D SUNKEN TRACK AND PROGRESS
        Canvas(modifier = Modifier.size(250.dp)) {
            val strokeWidth = 32.dp.toPx()

            // --- EMPTY TRACK ---
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
                        size = Size(
                            size.width - 20.dp.toPx(),
                            size.height - 20.dp.toPx()
                        )
                    )
                }

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

                drawCircle(
                    color = pureWhite,
                    radius = 13.dp.toPx(),
                    center = thumbCenter
                )

                drawCircle(
                    color = activeColor,
                    radius = 8.dp.toPx(),
                    center = thumbCenter
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 3.dp.toPx(),
                    center = thumbCenter - Offset(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }

        // ACTUAL BUTTON
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(106.dp)
                .shadow(25.dp, CircleShape, spotColor = activeColor)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(purpleVibrant, purpleDeep)
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
                text = if (isRunning) "Stop" else "Start",
                color = pureWhite,
                style = MaterialTheme.typography.displayMedium
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
