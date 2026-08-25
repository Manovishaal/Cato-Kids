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

@Composable
fun StudentHomeScreen(
    state: StudentUiState,
    onOpenSubject: (SubjectId) -> Unit,
    onOpenLesson: (Lesson) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRewards: () -> Unit,
    onChangeGrade: (Grade) -> Unit,
) {
    val profile = state.profile

    CatoBackdrop(top = CatoPalette.CoralSoft) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp),
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 52.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EmojiText(
                            "✋ Hi, ${profile?.firstName ?: "friend"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CatoPalette.Ink,
                        )
                        EmojiText(
                            "${state.grade.emoji} ${state.grade.longLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CatoPalette.InkSoft,
                        )
                    }
                    CoinChip(profile?.coins ?: 0, onClick = onOpenRewards)
                    Spacer(Modifier.width(10.dp))
                    Avatar(profile?.initials ?: "?", modifier = Modifier.clickable { onOpenProfile() })
                }
            }

            item { Spacer(Modifier.height(6.dp)) }

            item {
                PopIn(delayMillis = 40) {
                    Text(
                        "Ready to learn?",
                        style = MaterialTheme.typography.displaySmall,
                        color = CatoPalette.Ink,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            // ---- continue / start card ----
            item {
                val lesson = state.continueLesson ?: state.nextLesson
                if (lesson != null) {
                    PopIn(delayMillis = 110) {
                        ContinueCard(
                            lesson = lesson,
                            isResume = state.continueLesson != null && !state.isDone(lesson.id),
                            onClick = { onOpenLesson(lesson) },
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            // ---- overall progress ----
            item {
                PopIn(delayMillis = 180) {
                    CatoCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("My progress", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "${state.summary.completedLessons}/${state.summary.totalLessons}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CatoPalette.Coral,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            CatoProgressBar(state.summary.fraction, Modifier.fillMaxWidth())
                            Spacer(Modifier.height(14.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatPill("⭐", "${state.summary.stars}", "stars", Modifier.weight(1f))
                                StatPill("🎯", "${state.summary.averageScore}%", "average", Modifier.weight(1f), CatoPalette.TealSoft)
                                StatPill("⏱️", "${state.summary.minutesPlayed}m", "played", Modifier.weight(1f), CatoPalette.PeriwinkleSoft)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(22.dp)) }

            // ---- class switcher ----
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader("My class")
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Grade.entries.forEach { g ->
                            com.catokids.app.ui.auth.GradeChip(
                                grade = g,
                                selected = state.grade == g,
                                color = CatoPalette.Coral,
                                modifier = Modifier.weight(1f),
                                onClick = { onChangeGrade(g) },
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }

            // ---- subjects ----
            item {
                SectionHeader("My books", Modifier.padding(horizontal = 20.dp))
            }
            item { Spacer(Modifier.height(12.dp)) }

            itemsIndexed(SubjectId.entries.toList()) { index, subject ->
                val summary = state.bySubject[subject]
                PopIn(delayMillis = 260 + stagger(index, step = 80)) {
                    SubjectCard(
                        subject = subject,
                        completed = summary?.completedLessons ?: 0,
                        total = summary?.totalLessons ?: 0,
                        fraction = summary?.fraction ?: 0f,
                        onClick = { onOpenSubject(subject) },
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                    )
                }
            }

            item { Spacer(Modifier.height(22.dp)) }

            // ---- recommended games ----
            item { SectionHeader("Games for you", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(12.dp)) }
            item {
                val picks = remember(state.grade) {
                    CatoCurriculum.forGrade(state.grade).distinctBy { it.gameType }.take(8)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
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
private fun CoinChip(coins: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .hop(trigger = coins.takeIf { it > 0 })
            .clip(CircleShape)
            .background(CatoPalette.AmberSoft)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiArt("🪙", size = 22.dp)
        Spacer(Modifier.width(6.dp))
        Text("$coins", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
    }
}

@Composable
private fun ContinueCard(
    lesson: Lesson,
    isResume: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.horizontalGradient(listOf(CatoPalette.AmberLight, CatoPalette.Amber)))
            .clickable { onClick() }
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (isResume) "Continue where you left off" else "Start your next lesson",
                    style = MaterialTheme.typography.labelMedium,
                    color = CatoPalette.Ink.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(6.dp))
                Text(lesson.title, style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
                Text(
                    "${lesson.subject.title} · ${lesson.gameType.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.Ink.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .pulse(maxScale = 1.05f, periodMillis = 1700)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmojiText("▶", color = CatoPalette.CoralDeep)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isResume) "Resume" else "Let's go",
                        style = MaterialTheme.typography.labelMedium,
                        color = CatoPalette.CoralDeep,
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            GooseCharacter(modifier = Modifier.size(96.dp), cheering = true)
        }
    }
}

@Composable
private fun SubjectCard(
    subject: SubjectId,
    completed: Int,
    total: Int,
    fraction: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = when (subject) {
        SubjectId.LETTER_LAND   -> CatoPalette.CoralLight
        SubjectId.NUMBER_LAND   -> CatoPalette.Teal
        SubjectId.KNOW_MY_WORLD -> CatoPalette.Periwinkle
    }
    CatoCard(modifier = modifier, onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(tint.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt(subject.emoji, size = 36.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(subject.title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                Text(subject.blurb, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                Spacer(Modifier.height(10.dp))
                CatoProgressBar(fraction, Modifier.fillMaxWidth(), color = tint, height = 8.dp)
            }
            Spacer(Modifier.width(12.dp))
            Text("$completed/$total", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
        }
    }
}

@Composable
private fun GameTile(lesson: Lesson, stars: Int, onClick: () -> Unit) {
    val tint = when (lesson.subject) {
        SubjectId.LETTER_LAND   -> CatoPalette.CoralSoft
        SubjectId.NUMBER_LAND   -> CatoPalette.TealSoft
        SubjectId.KNOW_MY_WORLD -> CatoPalette.PeriwinkleSoft
    }
    Column(
        Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(tint)
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        EmojiArt(
            lesson.gameType.emoji,
            size = 40.dp,
            modifier = Modifier.floaty(amplitude = 3.dp, phase = lesson.id.hashCode() % 7 * 0.9f),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            lesson.gameType.title,
            style = MaterialTheme.typography.titleSmall,
            color = CatoPalette.Ink,
            maxLines = 1,
        )
        Text(
            lesson.title,
            style = MaterialTheme.typography.bodySmall,
            color = CatoPalette.InkSoft,
            maxLines = 2,
        )
        Spacer(Modifier.height(10.dp))
        StarRow(stars, size = 16.dp, animate = false)
    }
}
