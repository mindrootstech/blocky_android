package com.example.parentalcontrol.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.R

data class OnboardingPage(
    val backgroundResId: Int,
    val firstTitle: String,
    val secondTitle: String,
    val highlightedText: String,
    val description: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var page by remember { mutableStateOf(0) }

    val primaryColor = colorResource(id = R.color.primaryColor)
    val whiteColor = Color.White

    val pages = listOf(
        OnboardingPage(
            backgroundResId = R.mipmap.onboard_one,
            firstTitle = "Reduce digital",
            secondTitle = "distractions",
            highlightedText = "distractions",
            description = "Limit distracting apps so you can stay present and focused."
        ),
        OnboardingPage(
            backgroundResId = R.mipmap.onboard_two,
            firstTitle = "Designed for",
            secondTitle = "everyday life",
            highlightedText = "everyday",
            description = "Create focus modes for work, family time, study, or rest."
        ),
        OnboardingPage(
            backgroundResId = R.mipmap.onboard_three,
            firstTitle = "Plan your",
            secondTitle = "focus time",
            highlightedText = "focus",
            description = "Choose when apps are blocked and when they’re available."
        )
    )

    // Using a black background for the root Box prevents the white "flash" during transitions
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Background Image with Fade Animation (Smoother than sliding full-screen images)
        AnimatedContent(
            targetState = page,
            transitionSpec =  {
                slideInHorizontally(
                    animationSpec = tween(500),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) with
                        slideOutHorizontally(
                            animationSpec = tween(500),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
            },
            modifier = Modifier.fillMaxSize(),
            label = "background_fade"
        ) { targetPage ->
            Image(
                painter = painterResource(id = pages[targetPage].backgroundResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Overlay Content
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {

                // Text Content with Slide Animation
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(500)
                        ) + fadeIn(animationSpec = tween(500)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(500)
                        ) + fadeOut(animationSpec = tween(500))
                    },
                    label = "text_slide"
                ) { targetPage ->
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.Start

                    ) {
                        val annotatedTitle = buildAnnotatedString {
                            val fullTitle = "${pages[targetPage].firstTitle}\n${pages[targetPage].secondTitle}"
                            val highlight = pages[targetPage].highlightedText
                            val startIndex = fullTitle.indexOf(highlight)

                            if (startIndex >= 0) {
                                append(fullTitle.substring(0, startIndex))
                                withStyle(
                                    style = SpanStyle(
                                        color = primaryColor,
                                    )
                                ) {
                                    append(highlight)
                                }
                                append(fullTitle.substring(startIndex + highlight.length))
                            } else {
                                append(fullTitle)
                            }
                        }

                        Text(
                            text = annotatedTitle,
                            style = MaterialTheme.typography.displayLarge,
                            textAlign = TextAlign.Start,
                            color = whiteColor,
                            lineHeight = 44.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            pages[targetPage].description,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall,
                            color = whiteColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(27.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Page Indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in pages.indices) {
                            val selected = i == page
                            val width by animateDpAsState(
                                targetValue = if (selected) 23.dp else 8.dp,
                                label = "indicator"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) primaryColor else whiteColor
                                    )
                            )
                        }
                    }

                    // Navigation Button
                    if (page < pages.lastIndex) {
                        Button(
                            onClick = { page++ },
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next",
                                tint = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = onFinished,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = CircleShape,
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                text = "Get Started",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
