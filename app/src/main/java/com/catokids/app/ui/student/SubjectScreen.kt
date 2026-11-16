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
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Lesson
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.Radius
import com.catokids.app.ui.theme.Space
import com.catokids.app.ui.theme.Touch

@Composable
fun SubjectScreen(
    subject: SubjectId,
    lessons: List<Lesson>,
    state: StudentUiState,
    onOpenLesson: (Lesson) -> Unit,
    onBack: () -> Unit,
) {
    val colours = subjectColours(subject)
    val tint = colours.main
    val summary = state.bySubject[subject]
    val playful = state.grade == Grade.PREKG

    SubjectBackdrop(playful, tint) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(Space.xxl))
                CatoTopBar(
                    title = subject.title,
                    subtitle = state.grade.longLabel,
                    onBack = onBack,
                )
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Row(Modifier.padding(Space.lg), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt(subject.emoji, size = 52.dp)
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
                val nextId = lessons.firstOrNull { !state.isDone(it.id) && !state.isLocked(it) }?.id
                val stars = state.starsFor(lesson.id)
                val done = state.isDone(lesson.id)
                val locked = state.isLocked(lesson)
                PopIn(delayMillis = stagger(index, step = 45)) {
                    LessonRow(
                        index = index + 1,
                        lesson = lesson,
                        stars = stars,
                        done = done,
                        locked = locked,
                        tint = tint,
                        playful = playful,
                        // Only the next lesson to play moves; the rest stay still so it stands out.
                        isNext = lesson.id == nextId,
                        deep = colours.deep,
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
private fun SubjectBackdrop(playful: Boolean, tint: Color, content: @Composable BoxScope.() -> Unit) {
    // Pre-KG lesson covers play their cartoon (the cat in "Meet A B C D" etc.) where one exists.
    if (playful) PreloadCartoons()
    androidx.compose.runtime.CompositionLocalProvider(LocalCartoons provides playful) {
        if (playful) PlaygroundBackdrop(content = content)
        else CatoBackdrop(top = tint.copy(alpha = 0.28f), content = content)
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
    deep: Color = tint,
    playful: Boolean = false,
    isNext: Boolean = false,
) {
    CatoCard(
        modifier = modifier,
        color = if (locked) CatoPalette.Cloud else Color.White,
        onClick = onClick,
    ) {
        Row(Modifier.heightIn(min = Touch.kid + 8.dp).padding(Space.md), verticalAlignment = Alignment.CenterVertically) {
            if (playful && !locked) {
                // Pre-KG: the lesson's own picture, rocking on a splash of colour,
                // with the lesson number (or a tick) as a little badge.
                Box(Modifier.size(64.dp)) {
                    PaintSplat(
                        (if (done) CatoPalette.SuccessSoft else PlayColours[index % PlayColours.size]).copy(alpha = 0.5f),
                        Modifier.fillMaxSize(),
                        seed = index,
                    )
                    EmojiArt(
                        lessonCover(lesson),
                        size = 46.dp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .then(if (isNext) Modifier.sway(degrees = 8f, periodMillis = 1500) else Modifier),
                    )
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (done) CatoPalette.SuccessDeep else deep),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (done) "✓" else "$index",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                }
            } else Box(
                Modifier
                    .size(64.dp)
                    .clip(Radius.control)
                    .background(if (locked) CatoPalette.Line.copy(alpha = 0.5f) else if (done) CatoPalette.SuccessSoft.copy(alpha = 0.5f) else tint.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                if (locked) ArtPicture("🔒", Modifier.size(28.dp))
                else LessonCover(lesson, deep, 46.dp)
            }
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (locked) CatoPalette.InkSoft else CatoPalette.Ink,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                EmojiText(
                    if (playful) com.catokids.app.ui.games.toyFor(lesson).subtitle
                    else "${lesson.gameType.title} · ${lesson.roundCount} rounds",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                    maxLines = 1,
                )
                if (stars > 0) StarRow(stars, size = 16.dp, animate = false, modifier = Modifier.padding(top = Space.xs))
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    Modifier
                        .clip(Radius.chip)
                        .background(if (locked) CatoPalette.Line else if (done) CatoPalette.SuccessDeep else deep)
                        .padding(horizontal = Space.md, vertical = Space.sm),
                ) {
                    Text(
                        if (locked) "Later" else if (done) "Again" else "Play",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (locked) CatoPalette.InkSoft else Color.White,
                    )
                }
            }
        }
    }
}
