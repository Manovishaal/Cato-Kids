package com.catokids.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

/**
 * Draws a picture key — or a short run of them — as real artwork. Content still names its
 * pictures with emoji code points (it keeps the curriculum readable), but no emoji font is
 * ever shown: each key resolves through [ArtCatalog] to a bundled illustration, a Material
 * icon, or a shape the app draws itself.
 *
 * This matters more than it sounds. Emoji render completely differently across Android
 * versions and manufacturer skins: the apple a child sees on a Samsung is not the apple
 * on a Pixel, and on an old device some of these glyphs are a blank box. For an app where
 * the picture *is* the question — "which one starts with A?" — that is a correctness
 * problem, not a cosmetic one. Bundling the artwork makes every child see the same thing.
 *
 * Runs like "🍎🍎🍎" are split and laid out as repeats, which is exactly what the
 * more-and-less questions need.
 *
 * A key with no artwork draws nothing (and ArtCoverageTest fails the build), so a stray
 * emoji can never reach a child's screen.
 */
@Composable
fun EmojiArt(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    contentDescription: String? = null,
    maxGlyphs: Int = 10,
) {
    val glyphs = remember(emoji, maxGlyphs) { splitGlyphs(emoji).take(maxGlyphs) }
    if (glyphs.isEmpty()) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(size * 0.05f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cartoons = LocalCartoons.current
        glyphs.forEach { glyph ->
            val cartoon = if (cartoons) cartoonFor(glyph) else null
            if (cartoon != null) {
                // Pre-KG: the real animation. Drawn a touch larger because cartoon
                // canvases carry their own padding around the character.
                val aspect = CARTOON_ASPECT[cartoon] ?: 1f
                CartoonAnimation(cartoon, Modifier.size(width = size * 1.25f * aspect, height = size * 1.25f))
            } else {
                ArtPicture(glyph, Modifier.size(size), contentDescription)
            }
        }
    }
}

/** True when [glyph] is a picture key the app can draw. */
fun hasArt(glyph: String): Boolean = normaliseGlyph(glyph).let {
    it in ART_IMAGES || it in ART_ICONS || it in ART_DRAWN
}

/** One picture key, filling [modifier]'s bounds. */
@Composable
fun ArtPicture(glyph: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    val key = normaliseGlyph(glyph)
    val image = ART_IMAGES[key]
    val icon = ART_ICONS[key]
    val drawn = ART_DRAWN[key]
    when {
        image != null -> Image(
            painter = painterResource(image),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
        icon != null -> Icon(icon, contentDescription, modifier, tint = iconTint(key))
        drawn != null -> DrawnArt(drawn, modifier)
        else -> Box(modifier)
    }
}

/** UI symbols keep the colour people expect: gold stars, red hearts, green ticks. */
private fun iconTint(key: String): Color = when (key) {
    "⭐", "🌟", "✨", "🏆", "🏅", "🪙", "💡", "🎉" -> Color(0xFFF5A623)
    "❤", "💕", "💖", "💗", "🔥" -> Color(0xFFEF5B6B)
    "💙" -> Color(0xFF4A8BF5); "💚" -> Color(0xFF34B36B); "💛" -> Color(0xFFF5C518)
    "💜" -> Color(0xFF9B6BE8); "🧡" -> Color(0xFFF5893A)
    "✅" -> Color(0xFF16A35A); "🚫", "🛑", "⚠", "🙅" -> Color(0xFFE5483A)
    "🔊", "🎵" -> Color(0xFF5578E0)
    "✋", "🖐", "👋", "🤏", "🖖", "🤟" -> Color(0xFFE8A26A)
    "🔑" -> Color(0xFFD99A1E)
    else -> Color(0xFF55607A)
}

private val ArtColours = mapOf(
    "red" to Color(0xFFEF4B3F), "blue" to Color(0xFF3D7BF0), "yellow" to Color(0xFFFFCC2E),
    "green" to Color(0xFF34B36B), "orange" to Color(0xFFFF8A2B), "purple" to Color(0xFF9B5DE5),
    "brown" to Color(0xFF9A6233), "white" to Color(0xFFFFFFFF), "black" to Color(0xFF2B2B31),
)

/**
 * Colours and shapes for the colours-and-shapes lessons, drawn as real geometry so a
 * "rectangle" is always a rectangle, plus simple clock faces for telling the time.
 */
@Composable
fun DrawnArt(spec: String, modifier: Modifier = Modifier) {
    val (kind, arg) = spec.split(":").let { it[0] to it.getOrElse(1) { "" } }
    val fill = ArtColours[arg] ?: Color(0xFF3D7BF0)
    val edge = Color(0x33000000)
    Canvas(modifier) {
        val s = size.minDimension
        val o = Offset((size.width - s) / 2, (size.height - s) / 2)
        val stroke = Stroke(width = s * 0.04f)
        fun poly(vararg pts: Pair<Float, Float>): Path = Path().apply {
            pts.forEachIndexed { i, (x, y) ->
                if (i == 0) moveTo(o.x + x * s, o.y + y * s) else lineTo(o.x + x * s, o.y + y * s)
            }
            close()
        }
        when (kind) {
            "circle" -> {
                drawCircle(fill, s * 0.42f, center)
                drawCircle(edge, s * 0.42f, center, style = stroke)
            }
            "square" -> {
                drawRect(fill, Offset(o.x + s * 0.12f, o.y + s * 0.12f), Size(s * 0.76f, s * 0.76f))
                drawRect(edge, Offset(o.x + s * 0.12f, o.y + s * 0.12f), Size(s * 0.76f, s * 0.76f), style = stroke)
            }
            "rectangle" -> {
                drawRect(fill, Offset(o.x + s * 0.06f, o.y + s * 0.25f), Size(s * 0.88f, s * 0.5f))
                drawRect(edge, Offset(o.x + s * 0.06f, o.y + s * 0.25f), Size(s * 0.88f, s * 0.5f), style = stroke)
            }
            "oval" -> {
                drawOval(fill, Offset(o.x + s * 0.06f, o.y + s * 0.22f), Size(s * 0.88f, s * 0.56f))
                drawOval(edge, Offset(o.x + s * 0.06f, o.y + s * 0.22f), Size(s * 0.88f, s * 0.56f), style = stroke)
            }
            "triangle" -> poly(0.5f to 0.1f, 0.92f to 0.86f, 0.08f to 0.86f).let {
                drawPath(it, fill); drawPath(it, edge, style = stroke)
            }
            "diamond" -> poly(0.5f to 0.06f, 0.9f to 0.5f, 0.5f to 0.94f, 0.1f to 0.5f).let {
                drawPath(it, fill); drawPath(it, edge, style = stroke)
            }
            "pentagon" -> poly(0.5f to 0.07f, 0.93f to 0.39f, 0.77f to 0.9f, 0.23f to 0.9f, 0.07f to 0.39f).let {
                drawPath(it, fill); drawPath(it, edge, style = stroke)
            }
            "clock" -> {
                val hour = arg.toIntOrNull() ?: 12
                val r = s * 0.44f
                drawCircle(Color.White, r, center)
                drawCircle(Color(0xFF3D7BF0), r, center, style = Stroke(width = s * 0.07f))
                for (h in 0 until 12) {
                    val a = Math.toRadians(h * 30.0 - 90)
                    val p = Offset(center.x + (r * 0.8f) * Math.cos(a).toFloat(), center.y + (r * 0.8f) * Math.sin(a).toFloat())
                    drawCircle(Color(0xFF2B2B31), s * 0.018f, p)
                }
                fun hand(deg: Double, len: Float, w: Float) {
                    val a = Math.toRadians(deg - 90)
                    drawLine(
                        Color(0xFF2B2B31), center,
                        Offset(center.x + len * Math.cos(a).toFloat(), center.y + len * Math.sin(a).toFloat()),
                        strokeWidth = w, cap = StrokeCap.Round,
                    )
                }
                hand(hour % 12 * 30.0, r * 0.5f, s * 0.06f)
                hand(0.0, r * 0.75f, s * 0.04f)
                drawCircle(Color(0xFFEF4B3F), s * 0.04f, center)
            }
        }
    }
}

/** Variation selectors carry no meaning for artwork; everything else does. */
internal fun normaliseGlyph(glyph: String): String =
    glyph.filter { it != '️' && it != '︎' }

/**
 * Splits a string into renderable clusters.
 *
 * A cluster keeps together anything joined by a zero-width joiner (so "woman" + "school"
 * stays one teacher rather than becoming two pictures), plus trailing variation
 * selectors, skin-tone modifiers and keycap marks.
 */
internal fun splitGlyphs(text: String): List<String> {
    if (text.isEmpty()) return emptyList()
    val out = ArrayList<String>(4)
    val current = StringBuilder()
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val width = Character.charCount(cp)
        val attaches = cp == ZWJ ||
            cp == 0xFE0F || cp == 0xFE0E ||
            cp == 0x20E3 ||
            cp in 0x1F3FB..0x1F3FF
        val continuing = current.isNotEmpty() &&
            (attaches || current.codePointBefore(current.length) == ZWJ)
        if (current.isNotEmpty() && !continuing) {
            out.add(current.toString())
            current.setLength(0)
        }
        current.appendCodePoint(cp)
        i += width
    }
    if (current.isNotEmpty()) out.add(current.toString())
    return out
}

private const val ZWJ = 0x200D

/**
 * A drop-in replacement for [Text] that swaps any glyph we have artwork for into the
 * line, sized and aligned with the surrounding type.
 *
 * Quiz prompts carry their pictures inline — "🍎🍎🍎  How many apples?" — so text and
 * artwork have to flow together rather than sit in separate composables. Compose's
 * inline content does exactly that, and measuring the placeholder in `em` means the
 * pictures track the font size for free.
 *
 * When a string has nothing to substitute this delegates straight to [Text], so it is
 * cheap enough to use as the default everywhere content might contain a glyph.
 */
@Composable
fun EmojiText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    /** Height of an inline picture, in multiples of the font size. */
    glyphScale: Float = 1.45f,
) {
    val prepared = remember(text) { prepareInline(text) }
    if (prepared == null) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    val inline = prepared.second.associateWith { glyph ->
        InlineTextContent(
            placeholder = Placeholder(
                width = glyphScale.em,
                height = glyphScale.em,
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
            ),
        ) {
            ArtPicture(glyph, Modifier.fillMaxSize())
        }
    }

    Text(
        text = prepared.first,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        inlineContent = inline,
    )
}

/**
 * Builds the annotated string and the set of glyph ids it references, or null when the
 * text contains nothing we have artwork for — the signal to use a plain [Text].
 */
private fun prepareInline(text: String): Pair<AnnotatedString, Set<String>>? {
    if (text.isEmpty()) return null
    val ids = LinkedHashSet<String>()
    val annotated = buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val cluster = clusterAt(text, i)
            val key = normaliseGlyph(cluster)
            if (key in ART_IMAGES || key in ART_ICONS || key in ART_DRAWN) {
                ids.add(cluster)
                appendInlineContent(cluster, cluster)
                i += cluster.length
            } else {
                val cp = text.codePointAt(i)
                val width = Character.charCount(cp)
                append(text, i, i + width)
                i += width
            }
        }
    }
    return if (ids.isEmpty()) null else annotated to ids
}

/** The longest joined cluster starting at [start] — mirrors [splitGlyphs]' rules. */
private fun clusterAt(text: String, start: Int): String {
    val sb = StringBuilder()
    var i = start
    while (i < text.length) {
        val cp = text.codePointAt(i)
        val width = Character.charCount(cp)
        val attaches = cp == ZWJ ||
            cp == 0xFE0F || cp == 0xFE0E ||
            cp == 0x20E3 ||
            cp in 0x1F3FB..0x1F3FF
        val continuing = sb.isNotEmpty() &&
            (attaches || sb.codePointBefore(sb.length) == ZWJ)
        if (sb.isNotEmpty() && !continuing) break
        sb.appendCodePoint(cp)
        i += width
    }
    return sb.toString()
}
