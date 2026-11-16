package com.catokids.app

import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.ui.components.ART_DRAWN
import com.catokids.app.ui.components.ART_ICONS
import com.catokids.app.ui.components.ART_IMAGES
import com.catokids.app.ui.components.hasArt
import com.catokids.app.ui.components.splitGlyphs
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * No emoji font ever reaches a child's screen: every picture key the app uses must resolve
 * to a bundled illustration, a Material icon or a shape the app draws.
 */
class ArtCoverageTest {

    /** Arrows, ticks and bullets are typography, not pictures. */
    private val typography = setOf("✓", "✔", "✕", "✗", "→", "←", "↔", "▶", "◀", "▸", "●", "○", "−", "•", "·", "…", "↩", "➡", "⃣")

    private fun isPictureCodePoint(cp: Int) =
        cp in 0x1F000..0x1FAFF || cp in 0x2600..0x27BF || cp in 0x2B00..0x2BFF ||
            cp in 0x2300..0x23FF || cp in 0x2190..0x21FF || cp in 0x25A0..0x25FF

    private fun pictureKeys(text: String): List<String> =
        splitGlyphs(text).filter { g -> isPictureCodePoint(g.codePointAt(0)) }
            .map { it.replace("️", "") }
            .filter { it !in typography }

    @Test
    fun `every picture in the source code has artwork`() {
        val src = File("src/main/java")
        val missing = sortedMapOf<String, MutableSet<String>>()
        src.walkTopDown().filter { it.extension == "kt" && it.name != "ArtCatalog.kt" }.forEach { f ->
            f.readLines().forEach { line ->
                // Only string literals: comments may mention a glyph without drawing it.
                Regex("\"([^\"\\\\]|\\\\.)*\"").findAll(line.substringBefore("//")).forEach { lit ->
                    pictureKeys(lit.value).filterNot { hasArt(it) }.forEach {
                        missing.getOrPut(it) { mutableSetOf() }.add(f.name)
                    }
                }
            }
        }
        assertTrue("Pictures with no artwork: $missing", missing.isEmpty())
    }

    @Test
    fun `every picture in the curriculum has artwork`() {
        val missing = mutableSetOf<String>()
        CatoCurriculum.all.forEach { lesson ->
            val texts = buildList {
                add(lesson.title); add(lesson.subtitle)
                lesson.content.crafts.forEach { add(it.emoji) }
                lesson.content.rounds.forEach { r ->
                    add(r.prompt); add(r.emoji)
                    r.options.forEach { add(it.label); add(it.emoji) }
                    r.items.forEach { add(it.label); add(it.emoji) }
                    r.pairs.forEach { add(it.left); add(it.right); add(it.emoji) }
                }
            }
            texts.flatMap { pictureKeys(it) }.filterNot { hasArt(it) }.forEach { missing += "${lesson.id}:$it" }
        }
        assertTrue("Curriculum pictures with no artwork: $missing", missing.isEmpty())
    }

    @Test
    fun `art catalog is not empty and has no overlaps`() {
        assertTrue(ART_IMAGES.size > 150)
        val overlap = ART_IMAGES.keys.intersect(ART_ICONS.keys) + ART_IMAGES.keys.intersect(ART_DRAWN.keys)
        assertTrue("Keys mapped twice: $overlap", overlap.isEmpty())
    }
}
