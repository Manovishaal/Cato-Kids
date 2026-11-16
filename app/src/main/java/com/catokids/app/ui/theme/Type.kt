package com.catokids.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.catokids.app.R

/**
 * Nunito (SIL Open Font License 1.1) — rounded terminals that read as friendly without
 * the toy-like letter shapes that make early readers stumble: a single-storey "a" would
 * be nicer still, but Nunito's open counters and generous x-height keep words legible
 * at a glance. Bundled, so every device shows the same letters the workbooks teach.
 */
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

private val Rounded = Nunito

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

    titleLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.ExtraBold,    fontSize = 19.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.Bold,     fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp),

    bodyLarge   = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 25.sp),
    bodyMedium  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall   = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 19.sp),

    labelLarge  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.ExtraBold,    fontSize = 16.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelSmall  = TextStyle(fontFamily = Rounded, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)
