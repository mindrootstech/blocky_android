package com.example.parentalcontrol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.parentalcontrol.R
import com.example.parentalcontrol.ui.theme.ParentalcontrolTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyToScanBottomSheet(
    isVerified: Boolean = false,
    onDismiss: () -> Unit
) {
    // skipPartiallyExpanded = true makes the sheet open fully by default
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colorResource(id = R.color.sheet_bg_grey),
        dragHandle = null,
        // Using navigationBars insets to ensure the sheet respects the bottom area
        contentWindowInsets = { WindowInsets.navigationBars },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        ReadyToScanBottomSheetContent(
            isVerified = isVerified,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun ReadyToScanBottomSheetContent(
    isVerified: Boolean,
    onDismiss: () -> Unit
) {
    val bgColor = colorResource(id = R.color.sheet_bg_grey)
    val iconBgColor = colorResource(id = R.color.icon_bg_grey)

    val compositionReady by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ready_to_scan))
    val compositionVerified by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nfc_verified))
    
    val progressVerified = animateLottieCompositionAsState(
        composition = compositionVerified,
        isPlaying = isVerified
    )

    LaunchedEffect(progressVerified.value) {
        if (isVerified && progressVerified.value >= 1f) {
            delay(500)
            onDismiss()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp), // Padding to ensure content is above navigation bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(36.dp))

            Text(
                text = if (isVerified) "Verified" else "Ready to Scan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isVerified) "NFC Scan Successful" else "Tap the top of your phone to your brick",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isVerified) {
                LottieAnimation(
                    composition = compositionReady,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LottieAnimation(
                    composition = compositionVerified,
                    progress = { progressVerified.value },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Cancel Button
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.icon_bg_grey)),
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                "Cancel",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReadyToScanBottomSheetPreview() {
    ParentalcontrolTheme {
        Surface {
            ReadyToScanBottomSheetContent(
                isVerified = false,
                onDismiss = {}
            )
        }
    }
}
