package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Lesson
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun SubjectScreen(
    subject: SubjectId,
    lessons: List<Lesson>,
    state: StudentUiState,
    onOpenLesson: (Lesson) -> Unit,
    onBack: () -> Unit,
) {
    val tint = when (subject) {
        SubjectId.LETTER_LAND   -> CatoPalette.CoralLight
        SubjectId.NUMBER_LAND   -> CatoPalette.Teal
        SubjectId.KNOW_MY_WORLD -> CatoPalette.Periwinkle
    }
    val summary = state.bySubject[subject]

    CatoBackdrop(top = tint.copy(alpha = 0.28f)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(
                    title = subject.title,
                    subtitle = "${subject.bookTitle} · ${state.grade.longLabel}",
                    onBack = onBack,
                )
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt(subject.emoji, size = 40.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${summary?.completedLessons ?: 0} of ${lessons.size} lessons done",
                                style = MaterialTheme.typography.titleMedium,
                                color = CatoPalette.Ink,
                            )
                            Spacer(Modifier.height(8.dp))
                            CatoProgressBar(summary?.fraction ?: 0f, Modifier.fillMaxWidth(), color = tint, height = 10.dp)
                        }
                        Spacer(Modifier.width(10.dp))
                        EmojiText(
                            "⭐ ${summary?.stars ?: 0}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CatoPalette.Amber,
                        )
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MotherGooseLogo(modifier = Modifier.height(60.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "From the ${subject.bookTitle} workbooks",
                        style = MaterialTheme.typography.bodySmall,
                        color = CatoPalette.InkSoft,
                    )
                }
            }

            item {
                SectionHeader("Topics", Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }

            if (lessons.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "📚",
                        title = "Nothing here yet",
                        message = "Lessons for this class are on their way.",
                    )
                }
            }

            itemsIndexed(lessons) { index, lesson ->
                val stars = state.starsFor(lesson.id)
                val done = state.isDone(lesson.id)
                val locked = index > 0 && !state.isDone(lessons[index - 1].id) && !done && index > 1
                PopIn(delayMillis = stagger(index, step = 45)) {
                    LessonRow(
                        index = index + 1,
                        lesson = lesson,
                        stars = stars,
                        done = done,
                        locked = locked,
                        tint = tint,
                        onClick = { if (!locked) onOpenLesson(lesson) },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    index: Int,
    lesson: Lesson,
    stars: Int,
    done: Boolean,
    locked: Boolean,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CatoCard(
        modifier = modifier,
        color = if (locked) CatoPalette.Cloud else Color.White,
        onClick = onClick,
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (done) CatoPalette.SuccessSoft else tint.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                EmojiText(
                    if (locked) "🔒" else if (done) "✓" else "$index",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (done) CatoPalette.SuccessDeep else CatoPalette.Ink,
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (locked) CatoPalette.InkSoft else CatoPalette.Ink,
                )
                EmojiText(
                    "${lesson.gameType.emoji} ${lesson.gameType.title} · ${lesson.roundCount} rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
                if (lesson.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    EmojiText(lesson.subtitle, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                StarRow(stars, size = 16.dp, animate = false)
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (locked) CatoPalette.Line else tint)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        if (locked) "Locked" else if (done) "Play again" else "Play",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (locked) CatoPalette.InkSoft else Color.White,
                    )
                }
            }
        }
    }
}
