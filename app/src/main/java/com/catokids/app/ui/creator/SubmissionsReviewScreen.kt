package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.AssignmentSubmission
import com.catokids.app.data.model.SubmissionStatus
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun SubmissionsReviewScreen(
    assignmentTitle: String,
    submissions: List<AssignmentSubmission>,
    loading: Boolean,
    onReview: (AssignmentSubmission, SubmissionStatus, Int?, String) -> Unit,
    onBack: () -> Unit,
) {
    var reviewing by remember { mutableStateOf<AssignmentSubmission?>(null) }

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = assignmentTitle, subtitle = "📬 ${submissions.size} handed in", onBack = onBack)
            }
            if (loading) {
                item { LoadingBlock() }
            } else if (submissions.isEmpty()) {
                item { EmptyState("📭", "Nothing handed in yet", "You'll see submissions here as your class completes this.") }
            }
            items(submissions, key = { it.id }) { s ->
                CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth(), onClick = { reviewing = s }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                s.answerText?.ifBlank { "(no written answer)" } ?: "(completed)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CatoPalette.Ink,
                                maxLines = 3,
                                modifier = Modifier.weight(1f),
                            )
                            StatusChip(s.status)
                        }
                        if (s.score != null || !s.teacherFeedback.isNullOrBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                listOfNotNull(
                                    s.score?.let { "Score: $it" },
                                    s.teacherFeedback?.ifBlank { null },
                                ).joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = CatoPalette.InkSoft,
                            )
                        }
                    }
                }
            }
        }
    }

    reviewing?.let { submission ->
        ReviewDialog(
            submission = submission,
            onConfirm = { status, score, feedback -> onReview(submission, status, score, feedback); reviewing = null },
            onDismiss = { reviewing = null },
        )
    }
}

@Composable
private fun StatusChip(status: SubmissionStatus) {
    val color = when (status) {
        SubmissionStatus.SUBMITTED -> CatoPalette.AmberSoft
        SubmissionStatus.REVIEWED -> CatoPalette.SuccessSoft
        SubmissionStatus.NEEDS_REVISION -> CatoPalette.ErrorSoft
    }
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(color).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(status.label, style = MaterialTheme.typography.labelSmall, color = CatoPalette.Ink)
    }
}

@Composable
private fun ReviewDialog(
    submission: AssignmentSubmission,
    onConfirm: (SubmissionStatus, Int?, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var score by remember { mutableStateOf((submission.score ?: 100).toString()) }
    var feedback by remember { mutableStateOf(submission.teacherFeedback.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Review this submission") },
        text = {
            Column {
                Text(
                    submission.answerText?.ifBlank { "No written answer." } ?: "No written answer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.Ink,
                )
                Spacer(Modifier.height(14.dp))
                CatoTextField(
                    value = score, onValueChange = { score = it.filter(Char::isDigit) },
                    label = "Score (0-100)", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                )
                Spacer(Modifier.height(10.dp))
                CatoMultilineField(value = feedback, onValueChange = { feedback = it }, label = "Feedback for the child", minLines = 2)
            }
        },
        confirmButton = {
            CatoButton(
                text = "Mark reviewed",
                onClick = { onConfirm(SubmissionStatus.REVIEWED, score.toIntOrNull()?.coerceIn(0, 100), feedback) },
            )
        },
        dismissButton = {
            CatoOutlineButton(
                text = "Ask to redo",
                color = CatoPalette.Error,
                onClick = { onConfirm(SubmissionStatus.NEEDS_REVISION, score.toIntOrNull(), feedback) },
            )
        },
    )
}
