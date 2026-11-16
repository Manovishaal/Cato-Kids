package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Lesson
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.Radius
import com.catokids.app.ui.theme.Space
import com.catokids.app.ui.theme.Touch
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun StudentHomeScreen(
    state: StudentUiState,
    onOpenSubject: (SubjectId) -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRewards: () -> Unit,
    onChangeGrade: (Grade) -> Unit,
    onOpenAssignments: () -> Unit,
    onOpenCharacter: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenToyBox: () -> Unit,
) {
    val profile = state.profile

    CatoBackdrop(top = CatoPalette.CoralSoft) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = Space.xxxl, bottom = Space.xxxl),
        ) {
            // ---- greeting: who is playing, and their coins ----
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Space.gutter, vertical = Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(
                        profile?.initials ?: "?",
                        size = 52.dp,
                        modifier = Modifier.clip(CircleShape).clickable(onClickLabel = "My profile") { onOpenProfile() },
                    )
                    Spacer(Modifier.width(Space.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Hi, ${profile?.firstName ?: "friend"}!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CatoPalette.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            state.grade.longLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = CatoPalette.InkSoft,
                            maxLines = 1,
                        )
                    }
                    CoinChip(profile?.coins ?: 0, onClick = onOpenRewards)
                }
            }

            // ---- the one thing to do next ----
            item {
                val lesson = state.continueLesson ?: state.nextLesson
                if (lesson != null) {
                    PopIn(delayMillis = 60) {
                        ContinueCard(
                            lesson = lesson,
                            isResume = state.continueLesson != null,
                            onClick = { onOpenLesson(lesson) },
                            modifier = Modifier.padding(horizontal = Space.gutter, vertical = Space.md),
                        )
                    }
                }
            }

            // ---- class switcher (Explore only: a signed-in child's class is set by a grown-up) ----
            if (state.demo) item {
                Column(Modifier.padding(horizontal = Space.gutter, vertical = Space.sm)) {
                    SectionHeader("Pick a class")
                    Spacer(Modifier.height(Space.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        Grade.entries.forEach { g ->
                            com.catokids.app.ui.auth.GradeChip(
                                grade = g,
                                selected = state.grade == g,
                                color = CatoPalette.CoralDeep,
                                modifier = Modifier.weight(1f),
                                onClick = { onChangeGrade(g) },
                            )
                        }
                    }
                }
            }

            // ---- the three workbooks ----
            item {
                SectionHeader("My books", Modifier.padding(horizontal = Space.gutter).padding(top = Space.lg, bottom = Space.sm))
            }
            itemsIndexed(SubjectId.entries.toList()) { index, subject ->
                val summary = state.bySubject[subject]
                PopIn(delayMillis = 120 + stagger(index, step = 70)) {
                    SubjectCard(
                        subject = subject,
                        completed = summary?.completedLessons ?: 0,
                        total = summary?.totalLessons ?: 0,
                        fraction = summary?.fraction ?: 0f,
                        stars = summary?.stars ?: 0,
                        onClick = { onOpenSubject(subject) },
                        modifier = Modifier
                            .padding(horizontal = Space.gutter, vertical = Space.sm - Space.xxs)
                            .fillMaxWidth(),
                    )
                }
            }

            // ---- shortcuts: a 2x2 grid, so nothing hides off the edge of a small phone ----
            item {
                Column(
                    Modifier.padding(horizontal = Space.gutter).padding(top = Space.xl),
                    verticalArrangement = Arrangement.spacedBy(Space.md),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        QuickAccessTile("✉️", "Homework", CatoPalette.PeriwinkleSoft, onOpenAssignments, Modifier.weight(1f))
                        QuickAccessTile("🤸", "My character", CatoPalette.CoralSoft, onOpenCharacter, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                        QuickAccessTile("👑", "Coin shop", CatoPalette.AmberSoft, onOpenShop, Modifier.weight(1f))
                        QuickAccessTile("🧸", "Toy box", CatoPalette.TealSoft, onOpenToyBox, Modifier.weight(1f))
                    }
                }
            }

            // ---- how it's going ----
            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = Space.gutter)
                        .padding(top = Space.xl)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(Space.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("My progress", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${state.summary.completedLessons} of ${state.summary.totalLessons}",
                                style = MaterialTheme.typography.titleSmall,
                                color = CatoPalette.CoralDeep,
                            )
                        }
                        Spacer(Modifier.height(Space.md))
                        CatoProgressBar(state.summary.fraction, Modifier.fillMaxWidth(), color = CatoPalette.Coral)
                        Spacer(Modifier.height(Space.md))
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            StatPill("⭐", "${state.summary.stars}", "stars", Modifier.weight(1f))
                            StatPill("🎯", "${state.summary.averageScore}%", "score", Modifier.weight(1f), CatoPalette.TealSoft)
                            StatPill("⏱️", "${state.summary.minutesPlayed}m", "played", Modifier.weight(1f), CatoPalette.PeriwinkleSoft)
                        }
                    }
                }
            }

            // ---- more games ----
            item {
                SectionHeader("Games for you", Modifier.padding(horizontal = Space.gutter).padding(top = Space.xl, bottom = Space.sm))
            }
            item {
                val picks = remember(state.grade, state.progress) {
                    // Same lock rule as the book pages: only lessons the child can open now.
                    CatoCurriculum.forGrade(state.grade).filterNot { state.isLocked(it) }.distinctBy { it.gameType }.distinctBy { lessonCover(it) }.take(8)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Space.gutter),
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                ) {
                    items(picks) { lesson ->
                        GameTile(lesson = lesson, stars = state.starsFor(lesson.id)) { onOpenLesson(lesson) }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTile(emoji: String, label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .heightIn(min = Touch.kid)
            .clip(Radius.control)
            .background(color)
            .clickable(onClickLabel = label) { onClick() }
            .padding(horizontal = Space.md, vertical = Space.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiArt(emoji, size = 40.dp)
        Spacer(Modifier.width(Space.sm))
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = CatoPalette.Ink,
            maxLines = 2,
        )
    }
}

@Composable
private fun CoinChip(coins: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .hop(trigger = coins.takeIf { it > 0 })
            .heightIn(min = 44.dp)
            .clip(CircleShape)
            .background(CatoPalette.AmberSoft)
            .clickable(onClickLabel = "My rewards") { onClick() }
            .padding(horizontal = Space.md, vertical = Space.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiArt("🪙", size = 24.dp)
        Spacer(Modifier.width(Space.xs + Space.xxs))
        Text("$coins", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
    }
}

@Composable
private fun ContinueCard(
    lesson: Lesson,
    isResume: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colours = subjectColours(lesson.subject)
    Column(
        modifier
            .fillMaxWidth()
            .clip(Radius.sheet)
            .background(Brush.verticalGradient(listOf(CatoPalette.AmberLight, CatoPalette.Amber)))
            .clickable(onClickLabel = if (isResume) "Resume" else "Play") { onClick() }
            .padding(Space.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isResume) "Keep going" else "Next up",
                    style = MaterialTheme.typography.labelLarge,
                    color = CatoPalette.Ink.copy(alpha = 0.72f),
                )
                Spacer(Modifier.height(Space.xs))
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = CatoPalette.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    lesson.subject.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.Ink.copy(alpha = 0.72f),
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(Space.md))
            Box(
                Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt(lessonCover(lesson), size = 70.dp, modifier = Modifier.sway(degrees = 5f, periodMillis = 2200))
            }
        }
        Spacer(Modifier.height(Space.lg))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = Touch.min)
                .clip(Radius.control)
                .background(colours.deep),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Rounded.PlayArrow, null, Modifier.size(32.dp), tint = Color.White)
            Spacer(Modifier.width(Space.sm))
            Text(
                if (isResume) "Resume" else "Play",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectId,
    completed: Int,
    total: Int,
    fraction: Float,
    stars: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colours = subjectColours(subject)
    CatoCard(modifier = modifier, onClick = onClick) {
        Row(
            Modifier
                .heightIn(min = 96.dp)
                .padding(Space.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(Radius.control)
                    .background(colours.soft),
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt(subject.emoji, size = 56.dp)
            }
            Spacer(Modifier.width(Space.md))
            Column(Modifier.weight(1f)) {
                Text(subject.title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink, maxLines = 1)
                Text(
                    subject.blurb,
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(Space.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CatoProgressBar(fraction, Modifier.weight(1f), color = colours.main, height = 10.dp)
                    Spacer(Modifier.width(Space.sm))
                    Text("$completed/$total", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
                }
            }
        }
    }
}

@Composable
private fun GameTile(lesson: Lesson, stars: Int, onClick: () -> Unit) {
    val colours = subjectColours(lesson.subject)
    Column(
        Modifier
            .width(140.dp)
            .clip(Radius.card)
            .background(colours.soft)
            .clickable(onClickLabel = lesson.title) { onClick() }
            .padding(Space.md),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(Radius.control)
                .background(Color.White.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            LessonCover(lesson, subjectColours(lesson.subject).deep, 64.dp)
        }
        Spacer(Modifier.height(Space.sm))
        Text(
            lesson.title,
            style = MaterialTheme.typography.titleSmall,
            color = CatoPalette.Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            minLines = 2,
        )
        Spacer(Modifier.height(Space.xs))
        StarRow(stars, size = 16.dp, animate = false)
    }
}
