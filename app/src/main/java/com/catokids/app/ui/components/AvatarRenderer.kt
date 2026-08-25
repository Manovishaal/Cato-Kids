package com.catokids.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.catokids.app.data.local.ShopCatalog
import com.catokids.app.data.model.AvatarConfig
import com.catokids.app.data.model.TapEffectType
import com.catokids.app.ui.theme.CatoPalette
import kotlin.math.cos
import kotlin.math.sin

private val RAINBOW = listOf(
    CatoPalette.Coral, CatoPalette.Amber, CatoPalette.Teal, CatoPalette.Periwinkle, CatoPalette.Violet,
)

private val SKIN_COLORS = mapOf(
    "peach" to Color(0xFFFFD9B3),
    "sand"  to Color(0xFFF0C08A),
    "tan"   to Color(0xFFC98B5D),
    "deep"  to Color(0xFF8B5A3C),
)
private val HAIR_COLORS = mapOf(
    "black"  to Color(0xFF2B2320),
    "brown"  to Color(0xFF6B4226),
    "blonde" to Color(0xFFE8C170),
    "red"    to Color(0xFFB3492C),
    "blue"   to Color(0xFF3E7CB1),
)
private val OUTFIT_COLORS = mapOf(
    "coral"      to CatoPalette.Coral,
    "teal"       to CatoPalette.Teal,
    "periwinkle" to CatoPalette.Periwinkle,
    "violet"     to CatoPalette.Violet,
    "gold"       to CatoPalette.Gold,
)
private val BACKGROUND_COLORS = mapOf(
    "sky"    to CatoPalette.SkySoft,
    "meadow" to CatoPalette.TealSoft,
    "sunset" to CatoPalette.AmberSoft,
    "space"  to CatoPalette.VioletSoft,
)

/**
 * The character creator's live preview and every place a child's avatar shows up.
 * Drawn as simple vector shapes, in keeping with the rest of the app's "no emoji as
 * primary artwork" rule — accessories and the pet are the one place a small emoji
 * glyph is layered on top, which is fine: they're decoration on a custom drawing,
 * not the picture a lesson question depends on.
 */
@Composable
fun CharacterAvatar(config: AvatarConfig, modifier: Modifier = Modifier) {
    val skin = SKIN_COLORS[config.skinTone] ?: SKIN_COLORS.getValue("peach")
    val hair = HAIR_COLORS[config.hairColor] ?: HAIR_COLORS.getValue("brown")
    val hairBrush = if (config.hairColor == "hair_rainbow") Brush.sweepGradient(RAINBOW) else Brush.linearGradient(listOf(hair, hair))
    val outfit = OUTFIT_COLORS[config.outfit] ?: OUTFIT_COLORS.getValue("coral")
    val outfitBrush = if (config.outfit == "outfit_rainbow") Brush.sweepGradient(RAINBOW) else Brush.linearGradient(listOf(outfit, outfit))
    val background = BACKGROUND_COLORS[config.background] ?: BACKGROUND_COLORS.getValue("sky")

    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(28.dp))
            .background(background),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // ---- body ----
            drawRoundRect(
                brush = outfitBrush,
                topLeft = Offset(w * 0.24f, h * 0.52f),
                size = Size(w * 0.52f, h * 0.40f),
                cornerRadius = CornerRadius(w * 0.14f),
            )

            // ---- hair behind the head, for long styles ----
            if (config.hairStyle == "long") {
                drawRoundRect(
                    brush = hairBrush,
                    topLeft = Offset(w * 0.24f, h * 0.20f),
                    size = Size(w * 0.52f, h * 0.42f),
                    cornerRadius = CornerRadius(w * 0.22f),
                )
            }

            // ---- head ----
            val headCenter = Offset(w * 0.5f, h * 0.38f)
            val headRadius = w * 0.22f
            drawCircle(color = skin, radius = headRadius, center = headCenter)

            // ---- hair on top ----
            when (config.hairStyle) {
                "short" -> drawArc(
                    brush = hairBrush,
                    startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(headCenter.x - headRadius * 1.05f, headCenter.y - headRadius * 1.15f),
                    size = Size(headRadius * 2.1f, headRadius * 1.7f),
                )
                "curly" -> {
                    val bumps = 6
                    repeat(bumps) { i ->
                        val angle = Math.PI * (1.0 + i.toDouble() / (bumps - 1))
                        val cx = headCenter.x + cos(angle).toFloat() * headRadius * 0.95f
                        val cy = headCenter.y + sin(angle).toFloat() * headRadius * 0.95f
                        drawCircle(brush = hairBrush, radius = headRadius * 0.4f, center = Offset(cx, cy))
                    }
                }
                "pigtails" -> {
                    drawArc(
                        brush = hairBrush, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                        topLeft = Offset(headCenter.x - headRadius * 1.05f, headCenter.y - headRadius * 1.15f),
                        size = Size(headRadius * 2.1f, headRadius * 1.6f),
                    )
                    drawCircle(brush = hairBrush, radius = headRadius * 0.42f, center = Offset(headCenter.x - headRadius * 1.05f, headCenter.y + headRadius * 0.1f))
                    drawCircle(brush = hairBrush, radius = headRadius * 0.42f, center = Offset(headCenter.x + headRadius * 1.05f, headCenter.y + headRadius * 0.1f))
                }
                "long" -> drawArc(
                    brush = hairBrush, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(headCenter.x - headRadius * 1.05f, headCenter.y - headRadius * 1.15f),
                    size = Size(headRadius * 2.1f, headRadius * 1.7f),
                )
                // "bald" -> nothing
            }

            // ---- face ----
            val eyeY = headCenter.y + headRadius * 0.05f
            drawCircle(color = CatoPalette.Ink, radius = headRadius * 0.09f, center = Offset(headCenter.x - headRadius * 0.35f, eyeY))
            drawCircle(color = CatoPalette.Ink, radius = headRadius * 0.09f, center = Offset(headCenter.x + headRadius * 0.35f, eyeY))
            drawArc(
                color = CatoPalette.CoralDeep,
                startAngle = 20f, sweepAngle = 140f, useCenter = false,
                topLeft = Offset(headCenter.x - headRadius * 0.4f, headCenter.y + headRadius * 0.1f),
                size = Size(headRadius * 0.8f, headRadius * 0.6f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = headRadius * 0.09f),
            )
        }

        // ---- accessories & pet, layered on top as small artwork ----
        config.accessoryHead?.let { key ->
            ShopCatalog.item(key)?.let { item ->
                EmojiArt(item.emoji, size = 40.dp, modifier = Modifier.align(Alignment.TopCenter).offset(y = 6.dp))
            }
        }
        config.accessoryFace?.let { key ->
            ShopCatalog.item(key)?.let { item ->
                EmojiArt(item.emoji, size = 30.dp, modifier = Modifier.align(Alignment.Center).offset(y = (-14).dp))
            }
        }
        config.accessoryHand?.let { key ->
            ShopCatalog.item(key)?.let { item ->
                EmojiArt(item.emoji, size = 32.dp, modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-8).dp))
            }
        }
        config.pet?.let { key ->
            ShopCatalog.item(key)?.let { item ->
                EmojiArt(item.emoji, size = 36.dp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 6.dp, y = (-6).dp).floaty(amplitude = 3.dp))
            }
        }
    }
}

// ---------------------------------------------------------------- tap effects

private data class TapBurst(val id: Long, val position: Offset)

/**
 * A transparent, non-blocking layer: it listens for taps (after everything underneath
 * has already handled them, via [PointerEventPass.Final], and never calls `consume()`)
 * and fires a small particle burst at the tap point. Wrap the whole app content in this
 * and it never interferes with a single button click.
 */
@Composable
fun TapEffectOverlay(effectType: TapEffectType, modifier: Modifier = Modifier) {
    if (effectType == TapEffectType.NONE) return
    var bursts by remember { mutableStateOf(listOf<TapBurst>()) }
    var counter by remember { mutableStateOf(0L) }

    Box(
        modifier.pointerInput(effectType) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val down = event.changes.firstOrNull { it.changedToDown() }
                    if (down != null) {
                        counter += 1
                        bursts = (bursts + TapBurst(counter, down.position)).takeLast(6)
                    }
                }
            }
        },
    ) {
        bursts.forEach { burst ->
            androidx.compose.runtime.key(burst.id) {
                TapBurstEffect(
                    position = burst.position,
                    effectType = effectType,
                    onDone = { bursts = bursts.filterNot { it.id == burst.id } },
                )
            }
        }
    }
}

@Composable
private fun TapBurstEffect(position: Offset, effectType: TapEffectType, onDone: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
        onDone()
    }
    Canvas(Modifier.fillMaxSize()) {
        drawTapEffect(position, progress.value, effectType)
    }
}

private val TAP_PALETTE = listOf(CatoPalette.Coral, CatoPalette.Amber, CatoPalette.Teal, CatoPalette.Periwinkle, CatoPalette.Violet, CatoPalette.Gold)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTapEffect(center: Offset, p: Float, type: TapEffectType) {
    if (p <= 0f || p >= 1f) return
    val reach = 90f + 90f * p
    val fade = (1f - p).coerceIn(0f, 1f)
    when (type) {
        TapEffectType.SPARKLE -> repeat(8) { i ->
            val angle = (2 * Math.PI * i / 8).toFloat()
            val d = reach * (0.5f + 0.5f * p)
            val pos = Offset(center.x + cos(angle) * d, center.y + sin(angle) * d)
            drawCircle(TAP_PALETTE[i % TAP_PALETTE.size].copy(alpha = fade), radius = 5f + 4f * fade, center = pos)
        }
        TapEffectType.STARS -> repeat(6) { i ->
            val angle = (2 * Math.PI * i / 6 + p * 1.5).toFloat()
            val d = reach
            val pos = Offset(center.x + cos(angle) * d, center.y + sin(angle) * d)
            drawCircle(CatoPalette.Amber.copy(alpha = fade), radius = 7f * fade + 3f, center = pos)
        }
        TapEffectType.CONFETTI -> repeat(10) { i ->
            val angle = (2 * Math.PI * i / 10).toFloat()
            val d = reach * 1.1f
            val pos = Offset(center.x + cos(angle) * d, center.y + sin(angle) * d + 40f * p)
            rotate(degrees = p * 240f + i * 30f, pivot = pos) {
                drawRect(TAP_PALETTE[i % TAP_PALETTE.size].copy(alpha = fade), topLeft = pos - Offset(5f, 8f), size = Size(10f, 16f))
            }
        }
        TapEffectType.HEARTS -> repeat(5) { i ->
            val angle = (2 * Math.PI * i / 5).toFloat()
            val pos = Offset(center.x + cos(angle) * reach * 0.6f, center.y + sin(angle) * reach * 0.6f - 30f * p)
            drawMiniHeart(pos, 8f + 4f * (1f - p), Color(0xFFE85D75).copy(alpha = fade))
        }
        TapEffectType.RAINBOW -> RAINBOW.forEachIndexed { i, color ->
            drawCircle(
                color = color.copy(alpha = fade * 0.7f),
                radius = reach * (0.5f + i * 0.12f),
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f),
            )
        }
        TapEffectType.BUBBLES -> repeat(7) { i ->
            val angle = (2 * Math.PI * i / 7).toFloat()
            val d = reach * 0.8f
            val pos = Offset(center.x + cos(angle) * d * 0.4f, center.y + sin(angle) * d * 0.2f - 60f * p)
            drawCircle(Color.White.copy(alpha = fade * 0.5f), radius = 6f + 5f * (i % 3), center = pos, style = Fill)
            drawCircle(Color.White.copy(alpha = fade), radius = 6f + 5f * (i % 3), center = pos, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f))
        }
        TapEffectType.NONE -> Unit
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniHeart(center: Offset, r: Float, color: Color) {
    drawCircle(color, radius = r, center = center + Offset(-r * 0.55f, -r * 0.25f))
    drawCircle(color, radius = r, center = center + Offset(r * 0.55f, -r * 0.25f))
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x - r * 1.4f, center.y - r * 0.1f)
        lineTo(center.x, center.y + r * 1.5f)
        lineTo(center.x + r * 1.4f, center.y - r * 0.1f)
        close()
    }
    drawPath(path, color)
}
