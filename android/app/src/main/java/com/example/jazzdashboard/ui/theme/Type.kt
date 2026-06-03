package com.example.jazzdashboard.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.jazzdashboard.R

val RajdhaniFamily = FontFamily(
    Font(R.font.rajdhani_medium, FontWeight.Medium),
    Font(R.font.rajdhani_semibold, FontWeight.SemiBold),
    Font(R.font.rajdhani_bold, FontWeight.Bold)
)

val OrbitronFamily = FontFamily(
    Font(R.font.orbitron_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 120.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontFamily = OrbitronFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 60.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RajdhaniFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 2.sp
    )
)
