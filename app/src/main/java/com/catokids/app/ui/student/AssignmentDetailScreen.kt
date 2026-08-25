package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.AssignmentType
import com.catokids.app.data.model.SubmissionStatus
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun AssignmentDetailScreen(
    item: StudentAssignmentStatus?,
    onPlayGame: (String) -> Unit,
    onOpenLesson: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onBack: () -> Unit,
) {
    if (item == null) {
        CatoBackdrop(top = CatoPalette.CoralSoft) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                EmptyState("🤔", "Not found", "Let's go back and pick another one.") {
                    CatoButton(text = "Back", onClick = onBack)
                }
            }
        }
        return
    }

    val a = item.assignment
    val gameId = a.customGameId
    val course = item.course
    var answer by remember(a.id) { mutableStateOf(item.submission?.answerText.orEmpty()) }

    CatoBackdrop(top = CatoPalette.CoralSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = a.displayTitle, subtitle = "${a.type.emoji} ${a.type.label}", onBack = onBack)
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (a.instructions.isNotBlank()) {
                        CatoCard(Modifier.fillMaxWidth()) {
                            Text(a.instructions, style = MaterialTheme.typography.bodyLarge, color = CatoPalette.Ink, modifier = Modifier.padding(18.dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (a.dueDate != null) StatPill("📅", a.dueDate, "due", Modifier.weight(1f))
                        if (a.pointsReward > 0) StatPill("🪙", "${a.pointsReward}", "coins", Modifier.weight(1f), CatoPalette.AmberSoft)
                    }

                    when {
                        gameId != null -> {
                            if (item.gameCompleted) {
                                InfoBanner("🎉", "You've already played this one — nice work!", color = CatoPalette.SuccessSoft)
                            }
                            CatoButton(
                                text = if (item.gameCompleted) "Play again" else "Play the game",
                                leading = "🎮",
                                emphasise = !item.gameCompleted,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onPlayGame(gameId) },
                            )
                        }

                        a.type == AssignmentType.COURSE && course != null -> {
                            Text("Lessons in this course", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                        }

                        else -> {
                            val submission = item.submission
                            if (submission != null) {
                                Text("What you handed in", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                                CatoCard(Modifier.fillMaxWidth()) {
                                    Text(
                                        submission.answerText?.ifBlank { "(no written answer)" } ?: "(no written answer)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CatoPalette.Ink,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                                when (submission.status) {
                                    SubmissionStatus.REVIEWED -> InfoBanner(
                                        "✅",
                                        listOfNotNull(
                                            submission.score?.let { "Score: $it" },
                                            submission.teacherFeedback?.ifBlank { null },
                                        ).ifEmpty { listOf("Reviewed by your teacher.") }.joinToString(" · "),
                                        color = CatoPalette.SuccessSoft,
                                    )
                                    SubmissionStatus.NEEDS_REVISION -> {
                                        InfoBanner(
                                            "🔁",
                                            submission.teacherFeedback?.ifBlank { "Your teacher would like you to try again." }
                                                ?: "Your teacher would like you to try again.",
                                            color = CatoPalette.ErrorSoft,
                                        )
                                        CatoMultilineField(value = answer, onValueChange = { answer = it }, label = "Try again")
                                        CatoButton(
                                            text = "Resubmit", leading = "📬", emphasise = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            onClick = { onSubmit(answer) },
                                        )
                                    }
                                    SubmissionStatus.SUBMITTED -> InfoBanner(
                                        "📬", "Handed in — your teacher hasn't looked at it yet.", color = CatoPalette.AmberSoft,
                                    )
                                }
                            } else {
                                CatoMultilineField(
                                    value = answer, onValueChange = { answer = it },
                                    label = "Your answer", placeholder = "Tell me what you did!",
                                    minLines = 4,
                                )
                                CatoButton(
                                    text = "Hand it in", leading = "📬", emphasise = true,
                                    enabled = answer.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onSubmit(answer) },
                                )
                            }
                        }
                    }
                }
            }

            if (a.type == AssignmentType.COURSE && course != null) {
                items(course.lessonIds, key = { it }) { lessonId ->
                    val lesson = CatoCurriculum.lesson(lessonId)
                    Row(
                        Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CatoPalette.Cloud)
                            .clickable { onOpenLesson(lessonId) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmojiArt(lesson?.gameType?.emoji ?: "📖", size = 24.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            lesson?.title ?: lessonId,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CatoPalette.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text("▶", color = CatoPalette.CoralDeep)
                    }
                }
            }
        }
    }
}
