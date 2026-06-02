package com.example.parentalcontrol.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.parentalcontrol.R
import com.example.parentalcontrol.ui.theme.ParentalcontrolTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreenUI() {
    val context = LocalContext.current
    val activity = context as? Activity
    val blackColor = colorResource(id = R.color.blackColor)
    val greyColor = colorResource(id = R.color.greyColor)
    val primaryColor = colorResource(id = R.color.primaryColor)

    val goHome = {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        activity?.finish()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Restricted",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = blackColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { goHome() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = blackColor
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.restricted_foreground),
                contentDescription = null,
                modifier = Modifier.size(240.dp)
            )

            Text(
                text = "App blocked",
                fontSize = 22.sp,
                fontWeight = FontWeight.W600, // Semi-bold
                color = blackColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-20).dp)
            )
            
            Spacer(modifier = Modifier.height(0.dp)) // Reduced from 8.dp and will be offset
            
            Text(
                text = "This app is restricted to help you stay focused. You can open it if it’s really important right now.",
                fontSize = 14.sp,
                fontWeight = FontWeight.W400, // Normal
                color = greyColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = (-10).dp)
            )
        }
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = "Go back",
                fontSize = 16.sp,
                fontWeight = FontWeight.W600, // Semi-bold
                color = primaryColor,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .padding(bottom = 64.dp) // Increased from 48.dp to add space below
                    .clickable { goHome() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LockScreenUIPreview() {
    ParentalcontrolTheme {
        LockScreenUI()
    }
}
