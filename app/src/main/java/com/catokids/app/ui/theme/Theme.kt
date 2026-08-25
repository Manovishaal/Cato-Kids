package com.catokids.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Role

private val CatoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small      = RoundedCornerShape(14.dp),
    medium     = RoundedCornerShape(20.dp),
    large      = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

/** Colours that change per signed-in role, without rebuilding the whole scheme. */
data class RoleColors(
    val primary: Color,
    val soft: Color,
    val deep: Color,
)

fun roleColorsFor(role: Role?): RoleColors = when (role) {
    Role.TEACHER -> RoleColors(CatoPalette.Periwinkle, CatoPalette.PeriwinkleSoft, CatoPalette.PeriwinkleDeep)
    Role.PARENT  -> RoleColors(CatoPalette.Teal,       CatoPalette.TealSoft,       CatoPalette.TealDeep)
    Role.ADMIN   -> RoleColors(CatoPalette.Amber,      CatoPalette.AmberSoft,      Color(0xFFC98615))
    Role.SCHOOL  -> RoleColors(CatoPalette.Violet,     CatoPalette.VioletSoft,     CatoPalette.VioletDeep)
    else         -> RoleColors(CatoPalette.Coral,      CatoPalette.CoralTint,      CatoPalette.CoralDeep)
}

val LocalRoleColors = staticCompositionLocalOf { roleColorsFor(Role.STUDENT) }

private val CatoColorScheme = lightColorScheme(
    primary            = CatoPalette.Coral,
    onPrimary          = Color.White,
    primaryContainer   = CatoPalette.CoralSoft,
    onPrimaryContainer = CatoPalette.Ink,
    secondary          = CatoPalette.Amber,
    onSecondary        = CatoPalette.Ink,
    secondaryContainer = CatoPalette.AmberSoft,
    onSecondaryContainer = CatoPalette.Ink,
    tertiary           = CatoPalette.Teal,
    onTertiary         = Color.White,
    tertiaryContainer  = CatoPalette.TealSoft,
    background         = CatoPalette.Canvas,
    onBackground       = CatoPalette.Ink,
    surface            = CatoPalette.Surface,
    onSurface          = CatoPalette.Ink,
    surfaceVariant     = CatoPalette.Cloud,
    onSurfaceVariant   = CatoPalette.InkSoft,
    outline            = CatoPalette.Line,
    error              = CatoPalette.Error,
    onError            = Color.White,
    errorContainer     = CatoPalette.ErrorSoft,
    onErrorContainer   = CatoPalette.Ink,
)

@Composable
fun CatoKidsTheme(role: Role? = Role.STUDENT, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRoleColors provides roleColorsFor(role)) {
        MaterialTheme(
            colorScheme = CatoColorScheme,
            typography  = CatoTypography,
            shapes      = CatoShapes,
            content     = content,
        )
    }
}
