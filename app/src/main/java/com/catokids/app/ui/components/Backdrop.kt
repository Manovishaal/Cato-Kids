package com.catokids.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.LocalRoleColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class Floater(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float,
    val phase: Float,
    val shape: Int,      // 0 circle · 1 rounded blob · 2 four-point star
    val tint: Int,       // index into the drift palette
)

/**
 * The soft world every screen sits on: a two-stop wash, a drift of translucent shapes,
 * and a scatter of slow twinkles.
 *
 * The shapes are laid out from a fixed seed rather than randomly, so the composition is
 * the same every launch and never lands something distracting behind a heading. Motion
 * is one shared infinite transition driving every element by phase — one animation
 * clock for the whole backdrop, not thirty.
 */
@Composable
fun CatoBackdrop(
    modifier: Modifier = Modifier,
    top: Color = LocalRoleColors.current.soft,
    bottom: Color = CatoPalette.Canvas,
    bubbles: Boolean = true,
    twinkles: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val accent = LocalRoleColors.current.primary
    val drift = remember(accent) {
        listOf(
            accent,
            CatoPalette.BrandTeal,
            CatoPalette.BrandPink,
            CatoPalette.Amber,
        )
    }

    val floaters = remember {
        val seeded = kotlin.random.Random(20260825)
        List(11) { i ->
            Floater(
                x = 0.06f + seeded.nextFloat() * 0.88f,
                y = 0.04f + seeded.nextFloat() * 0.92f,
                radius = 0.035f + seeded.nextFloat() * 0.10f,
                alpha = 0.05f + seeded.nextFloat() * 0.07f,
                speed = 0.6f + seeded.nextFloat() * 0.9f,
                phase = seeded.nextFloat() * (2 * PI).toFloat(),
                shape = i % 3,
                tint = i % 4,
            )
        }
    }
    val sparks = remember {
        val seeded = kotlin.random.Random(6413)
        List(14) {
            Triple(
                0.04f + seeded.nextFloat() * 0.92f,
                0.03f + seeded.nextFloat() * 0.94f,
                seeded.nextFloat() * (2 * PI).toFloat(),
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "backdrop")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing)),
        label = "drift",
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom))),
    ) {
        if (bubbles || twinkles) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                if (bubbles) {
                    floaters.forEach { f ->
                        val angle = t * f.speed + f.phase
                        val cx = w * f.x + sin(angle) * w * 0.022f
                        val cy = h * f.y + cos(angle * 0.8f) * h * 0.016f
                        val r = w * f.radius * (1f + 0.06f * sin(angle * 1.4f))
                        val colour = drift[f.tint].copy(alpha = f.alpha)
                        when (f.shape) {
                            0 -> drawCircle(colour, radius = r, center = Offset(cx, cy))
                            1 -> rotate(degrees = angle * 12f, pivot = Offset(cx, cy)) {
                                drawRoundRect(
                                    color = colour,
                                    topLeft = Offset(cx - r, cy - r * 0.82f),
                                    size = Size(r * 2f, r * 1.64f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.75f),
                                )
                            }
                            else -> rotate(degrees = angle * 8f, pivot = Offset(cx, cy)) {
                                drawSparkle(Offset(cx, cy), r * 1.15f, colour)
                            }
                        }
                    }
                }

                if (twinkles) {
                    sparks.forEach { (sx, sy, phase) ->
                        val pulse = (sin(t * 2.4f + phase) + 1f) / 2f
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f + 0.30f * pulse),
                            radius = w * (0.0035f + 0.0028f * pulse),
                            center = Offset(w * sx, h * sy),
                        )
                    }
                }
            }
        }
        content()
    }
}

/** A four-point sparkle — concave sides, so it reads as a twinkle rather than a diamond. */
private fun DrawScope.drawSparkle(center: Offset, radius: Float, color: Color) {
    val waist = radius * 0.26f
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticBezierTo(center.x + waist, center.y - waist, center.x + radius, center.y)
        quadraticBezierTo(center.x + waist, center.y + waist, center.x, center.y + radius)
        quadraticBezierTo(center.x - waist, center.y + waist, center.x - radius, center.y)
        quadraticBezierTo(center.x - waist, center.y - waist, center.x, center.y - radius)
        close()
    }
    drawPath(path, color)
}
