//package com.example.parentalcontrol.ui.screens
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.size
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import com.example.parentalcontrol.R
//import com.example.parentalcontrol.ui.theme.primaryColor
//import kotlinx.coroutines.delay
//
//@Composable
//fun SplashScreen(onFinished: () -> Unit) {
//    LaunchedEffect(Unit) {
//        delay(3000) // Increased to 3 seconds
//        onFinished()
//    }
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(primaryColor),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.app_icon),
//            contentDescription = "App Logo",
//            modifier = Modifier.size(120.dp)
//        )
//    }
//}
