package com.example.parentalcontrol.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.parentalcontrol.R
import com.example.parentalcontrol.utils.Mode
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun HomeContent(
    isRunning: Boolean,
    progress: Float,
    elapsedSeconds: Long, // Pass explicit elapsed time for increasing order
    modes: List<Mode>,
    onToggle: () -> Unit,
    onCreateModeClick: () -> Unit,
    onModeToggle: (Mode, Boolean) -> Unit
) {
    val pureWhite = colorResource(id = R.color.white)
    val blackColor = colorResource(id = R.color.blackColor)
    val primaryColor = colorResource(id = R.color.primaryColor)
    val activeColor = colorResource(id = R.color.purple_start_vibrant)
    val activeBgColor = colorResource(id = R.color.lightPurpleColor)
    val inactiveBgColor = colorResource(id = R.color.neumorphic_bg)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pureWhite)
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(if (isRunning) activeBgColor else inactiveBgColor)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(25.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
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

                CenterButton(
                    isRunning = isRunning,
                    activeColor = activeColor,
                    progress = progress,
                    elapsedSeconds = elapsedSeconds, // Pass to center button
                    onToggle = onToggle
                )

                if (isRunning) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .padding(bottom = 24.dp)
                            .height(38.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Take a break", 
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Modes:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = blackColor
                )
                
                if (modes.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .then(
                                if (!isRunning) {
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onCreateModeClick
                                    )
                                } else Modifier
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Mode",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (modes.isEmpty()) {
                EmptyModesSection(isRunning, onCreateModeClick)
            } else {
                modes.forEach { mode ->
                    ModeItem(mode, isRunning, onModeToggle)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
fun ModeItem(mode: Mode, isRunning: Boolean, onToggle: (Mode, Boolean) -> Unit) {
    val context = LocalContext.current
    val primaryColor = colorResource(id = R.color.primaryColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val whiteColor = Color.White
    val activeBgColor = colorResource(id = R.color.lightPurpleColor)
    val borderColor = colorResource(id = R.color.borderColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isRunning && !mode.isEnabled) 0.6f else 1f)
            .then(
                if (!isRunning) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(mode, !mode.isEnabled) }
                } else Modifier
            ),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (mode.isEnabled) activeBgColor else whiteColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // TOP ROW: Name and Radio Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mode.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                RadioButton(
                    selected = mode.isEnabled,
                    onClick = if (!isRunning) { { onToggle(mode, !mode.isEnabled) } } else null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = primaryColor,
                        unselectedColor = greyColor.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))

            // BOTTOM ROW: Duration and Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${mode.durationMinutes / 60}h ${mode.durationMinutes % 60}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = greyColor,
                    modifier = Modifier.weight(1f)
                )
                
                // Icons block at the end of the row
                SelectedAppsIcons(context, mode.packageNames, useBodySmall = true)
            }
        }
    }
}

@Composable
fun SelectedAppsIcons(context: Context, packageNames: Set<String>, useBodySmall: Boolean = false) {
    val pm = context.packageManager
    val appIcons = packageNames.take(4).mapNotNull { packageName ->
        try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            appIcons.forEach { icon ->
                Image(
                    bitmap = icon.toBitmap().asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(if (useBodySmall) 24.dp else 32.dp)
                        .clip(CircleShape)
                )
            }
        }
        if (packageNames.size > 4) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "+${packageNames.size - 4} others",
                style = if (useBodySmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
                color = colorResource(id = R.color.greyColor)
            )
        }
    }
}


@Composable
fun EmptyModesSection(isRunning: Boolean, onCreateModeClick: () -> Unit) {
    val blackColor = colorResource(id = R.color.blackColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val primaryColor = colorResource(id = R.color.primaryColor)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.modes_icon),
            contentDescription = null,
            modifier = Modifier.size(34.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Modes Yet",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = blackColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Create a custom profile to stay focused.",
            style = MaterialTheme.typography.labelSmall,
            color = greyColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateModeClick,
            // Button functionality is blocked when running, but appearance remains same
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            ),
            shape = RoundedCornerShape(100.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "New Mode",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun CenterButton(
    isRunning: Boolean,
    activeColor: Color,
    progress: Float,
    elapsedSeconds: Long, // NEW: Use elapsed time for increasing count
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pureWhite = colorResource(id = R.color.white)
    val shadowDark = colorResource(id = R.color.neumorphic_shadow_dark)
    val purpleVibrant = colorResource(id = R.color.purple_start_vibrant)
    val purpleDeep = colorResource(id = R.color.purple_start_deep)
    val lightPurpleColor = colorResource(id = R.color.lightPurpleColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val primaryColor = colorResource(id = R.color.primaryColor)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "ProgressAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(300.dp)
    ) {
        // OUTER NEUMORPHIC SURFACE
        Box(
            modifier = Modifier
                .size(240.dp)
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
        Canvas(modifier = Modifier.size(240.dp)) {
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

        // ACTUAL BUTTON AREA
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .then(if (isRunning) Modifier.fillMaxWidth() else Modifier.size(106.dp))
                .then(
                    if (!isRunning) {
                        Modifier
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
                    } else {
                        Modifier // No background, no shadow, no click when running
                    }
                )
        ) {
            if (!isRunning) {
                // Glossy effect only for start button
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
                    text = "Start",
                    color = pureWhite,
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
            } else {
                // SMART TIME FORMATTING (Increasing Order)
                val days = elapsedSeconds / 86400
                val hours = (elapsedSeconds % 86400) / 3600
                val mins = (elapsedSeconds % 3600) / 60
                val secs = elapsedSeconds % 60
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when {
                        days > 0 -> {
                            TimeUnitColumn(value = days, label = "day", primaryColor = primaryColor, greyColor = greyColor)
                            TimeDivider(primaryColor = primaryColor)
                            TimeUnitColumn(value = hours, label = "hr", primaryColor = primaryColor, greyColor = greyColor)
                        }
                        hours > 0 -> {
                            TimeUnitColumn(value = hours, label = "hr", primaryColor = primaryColor, greyColor = greyColor)
                            TimeDivider(primaryColor = primaryColor)
                            TimeUnitColumn(value = mins, label = "min", primaryColor = primaryColor, greyColor = greyColor)
                        }
                        else -> {
                            TimeUnitColumn(value = mins, label = "min", primaryColor = primaryColor, greyColor = greyColor)
                            TimeDivider(primaryColor = primaryColor)
                            TimeUnitColumn(value = secs, label = "sec", primaryColor = primaryColor, greyColor = greyColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeUnitColumn(value: Long, label: String, primaryColor: Color, greyColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(45.dp)
    ) {
        Text(
            text = "%02d".format(value),
            color = primaryColor,
            maxLines = 1, softWrap = false,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = greyColor,
            softWrap = false,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
        )
    }
}

@Composable
fun TimeDivider(primaryColor: Color) {
    Text(
        text = " : ",
        color = primaryColor,
        maxLines = 1,
        softWrap = false,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset(y = (-4).dp)
    )
}

@Preview(showBackground = true, name = "Home with Empty Modes")
@Composable
fun PreviewHomeModes() {
    MaterialTheme {
        HomeContent(
            isRunning = false, 
            progress = 0f, 
            elapsedSeconds = 0, 
            modes = emptyList(), 
            onToggle = {}, 
            onCreateModeClick = {}, 
            onModeToggle = {_,_ ->}
        )
    }
}
