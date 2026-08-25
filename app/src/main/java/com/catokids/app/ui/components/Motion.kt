package com.catokids.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.theme.CatoPalette
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Motion toolkit.
 *
 * Two rules run through all of it. Nothing moves without meaning — every animation is
 * either feedback for something the child just did, or a cue about where to look next.
 * And nothing blocks: every effect is decorative on top of a layout that is already
 * correct and tappable, so a slow device degrades to a static screen rather than a
 * broken one.
 */

// ---------------------------------------------------------------- touch feedback

/** Squashes slightly under a finger, then springs back. */
@Composable
fun Modifier.bounceOnPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "bounce",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** A slow breathing scale, for the one thing on screen the child should tap next. */
@Composable
fun Modifier.pulse(
    enabled: Boolean = true,
    maxScale: Float = 1.045f,
    periodMillis: Int = 1300,
): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (enabled) maxScale else 1f,
        animationSpec = infiniteRepeatable(
            tween(periodMillis, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Idle bob. [phase] staggers neighbours so a row doesn't move in lockstep. */
@Composable
fun Modifier.floaty(
    amplitude: Dp = 5.dp,
    periodMillis: Int = 2600,
    phase: Float = 0f,
): Modifier {
    val transition = rememberInfiniteTransition(label = "float")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(periodMillis, easing = LinearEasing)),
        label = "floatT",
    )
    val px = with(LocalDensity.current) { amplitude.toPx() }
    return this.graphicsLayer { translationY = sin(t + phase) * px }
}

/**
 * A short head-shake. Fires whenever [trigger] changes to a new non-null value, so
 * pass the attempt count or the wrong answer itself rather than a boolean.
 */
@Composable
fun Modifier.wiggle(trigger: Any?, distance: Dp = 9.dp): Modifier {
    val shift = remember { Animatable(0f) }
    val px = with(LocalDensity.current) { distance.toPx() }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            shift.snapTo(0f)
            repeat(3) {
                shift.animateTo(1f, tween(55))
                shift.animateTo(-1f, tween(55))
            }
            shift.animateTo(0f, tween(55))
        }
    }
    return this.graphicsLayer { translationX = shift.value * px }
}

/** A single celebratory jump, fired when [trigger] changes. */
@Composable
fun Modifier.hop(trigger: Any?, height: Dp = 14.dp): Modifier {
    val lift = remember { Animatable(0f) }
    val px = with(LocalDensity.current) { height.toPx() }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            lift.snapTo(0f)
            lift.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow))
            lift.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    return this.graphicsLayer { translationY = -lift.value * px }
}

// ---------------------------------------------------------------- entrances

/**
 * Scale-and-rise entrance. Give siblings an increasing [delayMillis] and a list
 * assembles itself instead of appearing all at once.
 */
@Composable
fun PopIn(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    /** Change this and the entrance replays — pass a round or lesson id. */
    resetKey: Any? = Unit,
    fromScale: Float = 0.88f,
    rise: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    var shown by remember(resetKey) { mutableStateOf(false) }
    LaunchedEffect(resetKey) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        shown = true
    }
    val progress by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = Spring.StiffnessLow),
        label = "popIn",
    )
    val risePx = with(LocalDensity.current) { rise.toPx() }
    Box(
        modifier.graphicsLayer {
            alpha = progress.coerceIn(0f, 1f)
            val s = fromScale + (1f - fromScale) * progress
            scaleX = s
            scaleY = s
            translationY = (1f - progress) * risePx
        },
    ) { content() }
}

/** Stagger helper so screens agree on rhythm: 55ms apart, capped so long lists stay snappy. */
fun stagger(index: Int, step: Int = 55, max: Int = 440): Int = (index * step).coerceAtMost(max)

// ---------------------------------------------------------------- numbers

/** Counts up to [value] instead of snapping — small, and children love watching it. */
@Composable
fun AnimatedCounter(
    value: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = CatoPalette.Ink,
    suffix: String = "",
    durationMillis: Int = 1100,
) {
    var target by remember { mutableStateOf(0) }
    LaunchedEffect(value) {
        delay(180)
        target = value
    }
    val shown by animateIntAsState(
        targetValue = target,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "counter",
    )
    Text(text = "$shown$suffix", style = style, color = color, modifier = modifier)
}

// ---------------------------------------------------------------- particles

/**
 * A one-shot ring of sparks. Draws nothing until [trigger] changes, so it is free to
 * leave in a layout that only occasionally celebrates.
 */
@Composable
fun SparkleBurst(
    trigger: Any?,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(CatoPalette.Amber, CatoPalette.Gold, CatoPalette.Coral, CatoPalette.Teal),
    count: Int = 12,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
        }
    }
    Canvas(modifier) {
        val p = progress.value
        if (p <= 0f || p >= 1f) return@Canvas
        val cx = size.width / 2f
        val cy = size.height / 2f
        val reach = minOf(size.width, size.height) * 0.55f
        repeat(count) { i ->
            val angle = (2 * PI * i / count).toFloat()
            val d = reach * p
            drawCircle(
                color = colors[i % colors.size].copy(alpha = (1f - p).coerceIn(0f, 1f)),
                radius = size.minDimension * 0.035f * (1f - p * 0.6f),
                center = Offset(cx + cos(angle) * d, cy + sin(angle) * d),
            )
        }
    }
}

// ---------------------------------------------------------------- waiting

/** Three dots taking turns. Friendlier than a spinner for this audience. */
@Composable
fun BouncingDots(
    modifier: Modifier = Modifier,
    color: Color = CatoPalette.Coral,
    dot: Dp = 11.dp,
) {
    val transition = rememberInfiniteTransition(label = "dots")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        repeat(3) { i ->
            val lift by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(520, delayMillis = i * 130, easing = FastOutSlowInEasing),
                    RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Canvas(Modifier.size(dot)) {
                drawCircle(
                    color = color.copy(alpha = 0.45f + 0.55f * lift),
                    radius = size.minDimension / 2f * (0.72f + 0.28f * lift),
                    center = Offset(size.width / 2f, size.height / 2f),
                )
            }
        }
    }
}
