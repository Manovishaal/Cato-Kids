package com.catokids.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.theme.CatoPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The Pre-KG look: a hand-drawn playground for the youngest children.
 *
 * Same two rules as the rest of the motion toolkit — everything here is decoration on top
 * of a layout that already works, and one shared clock drives each scene rather than a
 * dozen separate animations. The scene stays pale and sits mostly at the edges, so text
 * and answer cards on top stay easy to read.
 */

// ---------------------------------------------------------------- scene

/**
 * A cartoon day out: smiling sun, drifting clouds, balloons floating up and rolling
 * hills with flowers along the bottom. Drop-in replacement for [CatoBackdrop].
 */
@Composable
fun PlaygroundBackdrop(
    modifier: Modifier = Modifier,
    sky: Color = CatoPalette.SkySoft,
    content: @Composable BoxScope.() -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "playground")
    // One slow loop for drifting things, one quicker one for the sun and flowers.
    val drift by transition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(40000, easing = LinearEasing)), label = "drift",
    )
    val beat by transition.animateFloat(
        0f, (2 * PI).toFloat(), infiniteRepeatable(tween(5200, easing = LinearEasing)), label = "beat",
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(sky, CatoPalette.Canvas))),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Peeks in from the top corner, clear of the top bar's text and round counter.
            drawSun(Offset(w * 0.95f, h * 0.022f), w * 0.06f, beat)

            // Clouds drift left to right and wrap around.
            listOf(0.10f to 0.0f, 0.19f to 0.45f, 0.06f to 0.72f).forEachIndexed { i, (y, start) ->
                val x = ((start + drift * (0.6f + i * 0.25f)) % 1.3f) * w * 1.0f - w * 0.15f
                drawCloud(Offset(x, h * y), w * (0.09f + i * 0.015f))
            }

            // Balloons rise slowly from below the hills and float off the top.
            val balloonColours = listOf(CatoPalette.Coral, CatoPalette.Amber, CatoPalette.Periwinkle, CatoPalette.BrandPink)
            listOf(0.08f, 0.93f, 0.22f, 0.78f).forEachIndexed { i, x ->
                val p = (drift * 2.2f + i * 0.27f) % 1f
                val y = h * (1.05f - p * 1.2f)
                val sway = sin(beat + i) * w * 0.012f
                drawBalloon(Offset(w * x + sway, y), w * 0.032f, balloonColours[i].copy(alpha = 0.55f))
            }

            drawHills(w, h, beat)
        }
        content()
    }
}

private fun DrawScope.drawSun(center: Offset, r: Float, beat: Float) {
    rotate(degrees = beat * 180f / PI.toFloat() * 0.15f, pivot = center) {
        repeat(10) { i ->
            val a = (2 * PI * i / 10).toFloat()
            val inner = r * 1.25f
            val outer = r * (1.6f + 0.1f * sin(beat * 2 + i))
            drawLine(
                CatoPalette.Gold.copy(alpha = 0.75f),
                Offset(center.x + cos(a) * inner, center.y + sin(a) * inner),
                Offset(center.x + cos(a) * outer, center.y + sin(a) * outer),
                strokeWidth = r * 0.16f,
                cap = StrokeCap.Round,
            )
        }
    }
    drawCircle(CatoPalette.Gold, r, center)
    drawCircle(CatoPalette.AmberLight.copy(alpha = 0.6f), r * 0.75f, center - Offset(r * 0.15f, r * 0.15f))
    // Face: two dots and a smile, plus rosy cheeks.
    drawCircle(CatoPalette.Ink, r * 0.09f, center + Offset(-r * 0.32f, -r * 0.12f))
    drawCircle(CatoPalette.Ink, r * 0.09f, center + Offset(r * 0.32f, -r * 0.12f))
    drawCircle(CatoPalette.Coral.copy(alpha = 0.5f), r * 0.13f, center + Offset(-r * 0.52f, r * 0.18f))
    drawCircle(CatoPalette.Coral.copy(alpha = 0.5f), r * 0.13f, center + Offset(r * 0.52f, r * 0.18f))
    drawArc(
        CatoPalette.Ink, 20f, 140f, useCenter = false,
        topLeft = center + Offset(-r * 0.35f, -r * 0.15f), size = Size(r * 0.7f, r * 0.6f),
        style = Stroke(width = r * 0.08f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawCloud(at: Offset, r: Float) {
    val c = Color.White.copy(alpha = 0.92f)
    drawCircle(c, r, at)
    drawCircle(c, r * 0.75f, at + Offset(-r * 0.95f, r * 0.25f))
    drawCircle(c, r * 0.8f, at + Offset(r * 0.95f, r * 0.2f))
    drawRoundRect(
        c, topLeft = at + Offset(-r * 1.6f, r * 0.15f), size = Size(r * 3.2f, r * 0.85f),
        cornerRadius = CornerRadius(r * 0.45f),
    )
}

private fun DrawScope.drawBalloon(at: Offset, r: Float, colour: Color) {
    val string = Path().apply {
        moveTo(at.x, at.y + r * 1.2f)
        quadraticBezierTo(at.x - r * 0.4f, at.y + r * 2.0f, at.x, at.y + r * 2.8f)
    }
    drawPath(string, CatoPalette.InkSoft.copy(alpha = 0.35f), style = Stroke(width = r * 0.08f))
    drawOval(colour, topLeft = at - Offset(r, r * 1.2f), size = Size(r * 2f, r * 2.4f))
    drawOval(Color.White.copy(alpha = 0.45f), topLeft = at - Offset(r * 0.6f, r * 0.85f), size = Size(r * 0.45f, r * 0.7f))
}

private fun DrawScope.drawHills(w: Float, h: Float, beat: Float) {
    val back = Path().apply {
        moveTo(0f, h * 0.90f)
        cubicTo(w * 0.25f, h * 0.84f, w * 0.45f, h * 0.93f, w * 0.7f, h * 0.87f)
        cubicTo(w * 0.85f, h * 0.84f, w * 0.95f, h * 0.88f, w, h * 0.86f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(back, Color(0xFFCFEFB4).copy(alpha = 0.8f))
    val front = Path().apply {
        moveTo(0f, h * 0.94f)
        cubicTo(w * 0.3f, h * 0.90f, w * 0.6f, h * 0.97f, w, h * 0.92f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(front, Color(0xFFB4E59A).copy(alpha = 0.85f))

    // A few flowers that nod along with the beat.
    val petals = listOf(CatoPalette.BrandPink, CatoPalette.Amber, CatoPalette.Periwinkle, CatoPalette.Coral, Color.White)
    listOf(0.06f, 0.2f, 0.36f, 0.55f, 0.72f, 0.9f).forEachIndexed { i, x ->
        val base = Offset(w * x, h * (0.955f - (i % 2) * 0.012f))
        val nod = sin(beat + i * 1.1f) * 6f
        rotate(nod, pivot = base) {
            val head = base - Offset(0f, w * 0.035f)
            drawLine(Color(0xFF6BBF59), base, head, strokeWidth = w * 0.006f, cap = StrokeCap.Round)
            val pr = w * 0.011f
            repeat(5) { k ->
                val a = (2 * PI * k / 5).toFloat()
                drawCircle(petals[i % petals.size], pr, head + Offset(cos(a) * pr, sin(a) * pr))
            }
            drawCircle(CatoPalette.Gold, pr * 0.7f, head)
        }
    }
}

// ---------------------------------------------------------------- mascot

/** The goose with a speech bubble — used to read out what to do in a Pre-KG lesson. */
@Composable
fun MascotBubble(
    text: String,
    modifier: Modifier = Modifier,
    cheering: Boolean = false,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (LocalCartoons.current) {
            // A real cartoon friend waves hello while reading out the task.
            CartoonAnimation(Cartoons.hello, Modifier.size(66.dp))
        } else {
            GooseCharacter(
                modifier = Modifier
                    .size(58.dp)
                    .floaty(amplitude = 3.dp, periodMillis = 2000),
                cheering = cheering,
            )
        }
        Spacer(Modifier.width(6.dp))
        PopIn(resetKey = text, fromScale = 0.7f) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                    .background(Color.White)
                    .border(2.dp, CatoPalette.Amber, RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                EmojiText(text, style = MaterialTheme.typography.bodyLarge, color = CatoPalette.Ink)
            }
        }
    }
}

// ---------------------------------------------------------------- celebrations

private data class Flyer(val glyph: String, val x: Float, val delay: Float, val drift: Float, val spin: Float, val size: Float)

/**
 * A shower of stars, hearts and balloons that float up and fade — the Pre-KG "yay!".
 * Draws nothing until [trigger] changes to a new non-null value.
 */
@Composable
fun RewardShower(trigger: Any?, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(1700, easing = LinearEasing))
        }
    }
    val flyers = remember(trigger) {
        val r = kotlin.random.Random(trigger.hashCode())
        val glyphs = listOf("⭐", "🎈", "❤️", "🌟", "✨", "🎉")
        List(16) {
            Flyer(
                glyph = glyphs[it % glyphs.size],
                x = r.nextFloat(),
                delay = r.nextFloat() * 0.35f,
                drift = r.nextFloat() * 2f - 1f,
                spin = r.nextFloat() * 60f - 30f,
                size = 26f + r.nextFloat() * 22f,
            )
        }
    }
    val p = progress.value
    if (trigger == null || p <= 0f || p >= 1f) return

    BoxWithConstraints(modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        flyers.forEach { f ->
            val local = ((p - f.delay) / (1f - f.delay)).coerceIn(0f, 1f)
            if (local > 0f) {
                EmojiArt(
                    f.glyph,
                    size = f.size.dp,
                    modifier = Modifier.graphicsLayer {
                        translationX = f.x * (w - f.size.dp.toPx()) + sin(local * 6f) * f.drift * 40f
                        translationY = h * (1f - local * 0.9f)
                        rotationZ = f.spin * local
                        alpha = if (local < 0.75f) 1f else (1f - local) * 4f
                        val pop = if (local < 0.12f) local / 0.12f else 1f
                        scaleX = pop; scaleY = pop
                    },
                )
            }
        }
    }
}

// ---------------------------------------------------------------- playful modifiers

/** Gentle side-to-side rocking, like a toy on a shelf. [phase] keeps neighbours out of step. */
@Composable
fun Modifier.sway(degrees: Float = 6f, periodMillis: Int = 1800, phase: Int = 0): Modifier {
    val transition = rememberInfiniteTransition(label = "sway")
    val angle by transition.animateFloat(
        initialValue = -degrees,
        targetValue = degrees,
        animationSpec = infiniteRepeatable(
            tween(periodMillis, delayMillis = 0, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
            initialStartOffset = androidx.compose.animation.core.StartOffset(phase * 230),
        ),
        label = "swayAngle",
    )
    return this.graphicsLayer { rotationZ = angle }
}

/**
 * Squash-and-stretch pop, fired whenever [trigger] changes — the cartoon way of saying
 * "you touched me". Pass a tap count or the selected id.
 */
@Composable
fun Modifier.jelly(trigger: Any?): Modifier {
    val squash = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            squash.snapTo(1f)
            squash.animateTo(0f, spring(dampingRatio = 0.28f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    return this.graphicsLayer {
        val s = squash.value
        scaleX = 1f + 0.18f * s
        scaleY = 1f - 0.14f * s
    }
}

/** A soft paint splat drawn behind a picture, slowly turning — for craft cards. */
@Composable
fun PaintSplat(colour: Color, modifier: Modifier = Modifier, seed: Int = 0) {
    val transition = rememberInfiniteTransition(label = "splat")
    val turn by transition.animateFloat(
        0f, 360f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "turn",
    )
    val bumps = remember(seed) {
        val r = kotlin.random.Random(seed)
        List(9) { 0.78f + r.nextFloat() * 0.3f }
    }
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val base = size.minDimension / 2f
        rotate(turn, pivot = c) {
            val path = Path()
            bumps.forEachIndexed { i, k ->
                val a = (2 * PI * i / bumps.size).toFloat()
                val pt = Offset(c.x + cos(a) * base * k, c.y + sin(a) * base * k)
                if (i == 0) path.moveTo(pt.x, pt.y) else {
                    val mid = (2 * PI * (i - 0.5f) / bumps.size).toFloat()
                    path.quadraticBezierTo(c.x + cos(mid) * base * 1.05f, c.y + sin(mid) * base * 1.05f, pt.x, pt.y)
                }
            }
            val a0 = (2 * PI * (bumps.size - 0.5f) / bumps.size).toFloat()
            path.quadraticBezierTo(c.x + cos(a0) * base * 1.05f, c.y + sin(a0) * base * 1.05f,
                c.x + base * bumps[0], c.y)
            path.close()
            drawPath(path, colour)
            // Two little drips beside the splat.
            drawCircle(colour, base * 0.12f, c + Offset(base * 1.0f, base * 0.55f))
            drawCircle(colour, base * 0.08f, c + Offset(-base * 0.95f, -base * 0.6f))
        }
    }
}

/** Colours the playful pieces cycle through, so a list of cards isn't all one hue. */
val PlayColours: List<Color> = listOf(
    CatoPalette.CoralLight, CatoPalette.AmberLight, CatoPalette.Teal,
    CatoPalette.Periwinkle, CatoPalette.Violet, CatoPalette.SuccessSoft,
)

/** First picture in a string of glyphs, for a lesson's cover. */
fun firstGlyph(text: String): String {
    if (text.isEmpty()) return ""
    val cp = text.codePointAt(0)
    var end = Character.charCount(cp)
    while (end < text.length) {
        val next = text.codePointAt(end)
        val attaches = next == 0x200D || next == 0xFE0F || next == 0x20E3 || next in 0x1F3FB..0x1F3FF ||
            text.codePointBefore(end) == 0x200D
        if (!attaches) break
        end += Character.charCount(next)
    }
    return text.substring(0, end)
}
