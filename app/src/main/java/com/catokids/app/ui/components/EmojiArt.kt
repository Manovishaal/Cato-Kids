package com.catokids.app.ui.components

import androidx.compose.foundation.Image
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
 * Draws a glyph — or a short run of them — as real artwork instead of letting the
 * device's emoji font decide.
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
 * Anything with no artwork falls back to text at the same visual size, so an unmapped
 * glyph still shows up rather than vanishing.
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
        glyphs.forEach { glyph ->
            val art = EMOJI_ARTWORK[normaliseGlyph(glyph)]
            if (art != null) {
                Image(
                    painter = painterResource(art),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(size),
                )
            } else {
                Text(
                    text = glyph,
                    fontSize = with(LocalDensity.current) { (size * 0.82f).toSp() },
                )
            }
        }
    }
}

/** Variation selectors carry no meaning for artwork; everything else does. */
private fun normaliseGlyph(glyph: String): String =
    glyph.filter { it != '️' && it != '︎' }

/**
 * Splits a string into renderable clusters.
 *
 * A cluster keeps together anything joined by a zero-width joiner (so "woman" + "school"
 * stays one teacher rather than becoming two pictures), plus trailing variation
 * selectors, skin-tone modifiers and keycap marks.
 */
private fun splitGlyphs(text: String): List<String> {
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
            val art = EMOJI_ARTWORK[normaliseGlyph(glyph)]
            if (art != null) {
                Image(
                    painter = painterResource(art),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    Text(
        text = prepared.first,
        modifier = modifier,
        style = style,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
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
            if (EMOJI_ARTWORK.containsKey(key)) {
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
