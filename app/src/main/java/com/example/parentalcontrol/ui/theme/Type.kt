package com.example.parentalcontrol.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = dmSerifDisplayFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 51.sp,
    ),


    bodyLarge = TextStyle(
        fontFamily = interFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = interFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.sp
    ),

    bodySmall = TextStyle(
        fontFamily = interFamily,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp
    ),

    labelSmall = TextStyle(
        fontFamily = interFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.sp
    ),

)
