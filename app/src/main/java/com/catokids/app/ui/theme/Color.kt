package com.catokids.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Palette sampled directly from the CatoKidz Figma exports
 * (Student Home, Correct Answer, Teacher Home).
 */
object CatoPalette {
    // Coral / peach — the Cato Kids signature
    val Coral        = Color(0xFFF7906F)
    val CoralLight   = Color(0xFFF8B5A3)
    val CoralSoft    = Color(0xFFFEDDD4)
    val CoralTint    = Color(0xFFFDF2F0)
    val CoralDeep    = Color(0xFFE3654A)

    // Amber / gold — coins, rewards, "recommended"
    val Amber        = Color(0xFFF8B64C)
    val AmberLight   = Color(0xFFFFD18B)
    val AmberSoft    = Color(0xFFFFF0D4)
    val Gold         = Color(0xFFFFD15C)

    // Teal / mint
    val Teal         = Color(0xFF73DBD5)
    val TealSoft     = Color(0xFFDFF4F2)
    val TealDeep     = Color(0xFF2FA9A2)

    // Periwinkle — teacher surfaces
    val Periwinkle   = Color(0xFF91B0FF)
    val PeriwinkleSoft = Color(0xFFE7EDFF)
    val PeriwinkleDeep = Color(0xFF5578E0)

    // Violet — school surfaces
    val Violet       = Color(0xFFA78BFA)
    val VioletSoft   = Color(0xFFEDE7FF)
    val VioletDeep   = Color(0xFF7C5CE0)

    // Sky
    val Sky          = Color(0xFFA0EBFF)
    val SkySoft      = Color(0xFFE3F8FF)

    // Feedback
    val Success      = Color(0xFF04C051)
    val SuccessSoft  = Color(0xFFB0EF8F)
    val SuccessDeep  = Color(0xFF009045)
    val Error        = Color(0xFFF44321)
    val ErrorSoft    = Color(0xFFFFDDD6)

    // Brand — sampled from the Cato Kids tree logo
    val BrandBlue    = Color(0xFF2585BC)   // the trunk
    val BrandTeal    = Color(0xFF409395)
    val BrandPink    = Color(0xFFE57691)
    val BrandOrange  = Color(0xFFED9082)

    // Neutrals
    val Ink          = Color(0xFF363638)
    val InkSoft      = Color(0xFF6B6B70)
    val Line         = Color(0xFFE6E6E6)
    val Cloud        = Color(0xFFEEEFF3)
    val Surface      = Color(0xFFFFFFFF)
    val Canvas       = Color(0xFFFDF9F7)
}

/** Playful accent set used by subject cards and game tiles. */
val CatoAccents = listOf(
    CatoPalette.Coral,
    CatoPalette.Amber,
    CatoPalette.Teal,
    CatoPalette.Periwinkle,
    CatoPalette.Violet,
    CatoPalette.Sky,
)
