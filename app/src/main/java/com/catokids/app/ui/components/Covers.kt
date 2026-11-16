package com.catokids.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.catokids.app.data.model.Lesson
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.theme.CatoPalette

/**
 * A picture that says what the lesson is about: its craft, else the first picture in its
 * rounds (the apple in "Meet A B C D", the giraffe in "Tall and short"), else the game's icon.
 */
fun lessonCover(lesson: Lesson): String {
    lesson.content.crafts.firstOrNull()?.emoji?.takeIf { it.isNotBlank() }?.let { return firstGlyph(it) }
    lesson.content.rounds.forEach { r ->
        val pic = r.emoji.takeIf { it.isNotBlank() }
            ?: r.options.firstOrNull { it.correct && it.emoji.isNotBlank() }?.emoji
            ?: r.items.firstOrNull()?.emoji
            ?: r.pairs.firstOrNull { it.emoji.isNotBlank() }?.emoji
        if (!pic.isNullOrBlank()) return firstGlyph(pic)
    }
    return lesson.gameType.emoji
}

/** Each book keeps one colour everywhere it appears. */
data class SubjectColours(val soft: Color, val main: Color, val deep: Color)

fun subjectColours(subject: SubjectId): SubjectColours = when (subject) {
    SubjectId.LETTER_LAND   -> SubjectColours(CatoPalette.CoralSoft, CatoPalette.Coral, CatoPalette.CoralDeep)
    SubjectId.NUMBER_LAND   -> SubjectColours(CatoPalette.TealSoft, CatoPalette.Teal, CatoPalette.TealDeep)
    SubjectId.KNOW_MY_WORLD -> SubjectColours(CatoPalette.PeriwinkleSoft, CatoPalette.Periwinkle, CatoPalette.PeriwinkleDeep)
}

/**
 * The lesson's picture; for letter and number lessons with no picture of their own, the
 * first letter or number they teach, drawn big — far clearer to a child than a UI icon.
 */
@Composable
fun LessonCover(lesson: Lesson, colour: Color, size: Dp) {
    val cover = lessonCover(lesson)
    val symbol = Regex("""\b([A-Z]|\d{1,2})\b""").find(lesson.title)?.value
    if (normaliseGlyph(cover) !in ART_IMAGES && symbol != null) {
        Text(
            symbol,
            color = colour,
            style = MaterialTheme.typography.displayMedium,
            fontSize = (size.value * 0.9f).sp,
            maxLines = 1,
        )
    } else EmojiArt(cover, size = size)
}
