package com.catokids.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Rounded = FontFamily.SansSerif

/**
 * Type scale tuned for 3–6 year olds: bigger than Material defaults,
 * heavier weights, generous line height.
 */
val CatoTypography = Typography(
    displayLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Black,     fontSize = 44.sp, lineHeight = 50.sp),
    displayMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Black,     fontSize = 36.sp, lineHeight = 42.sp),
    displaySmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp),

    headlineLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold,      fontSize = 21.sp, lineHeight = 27.sp),

    titleLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold,     fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),

    bodyLarge   = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Normal,   fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Normal,   fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall   = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 19.sp),

    labelLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold,     fontSize = 16.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelSmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)
