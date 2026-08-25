package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun AssignmentsScreen(
    state: AssignmentsUiState,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.CoralSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "My homework", subtitle = "📝 Set by your teacher", onBack = onBack)
            }

            if (state.loading) {
                item { LoadingBlock() }
            } else if (state.items.isEmpty()) {
                item { EmptyState("🌤️", "Nothing right now", "When your teacher sets homework or an activity, it shows up here.") }
            }

            if (state.pending.isNotEmpty()) {
                item { SectionHeader("To do", Modifier.padding(horizontal = 20.dp)) }
                item { Spacer(Modifier.height(10.dp)) }
                items(state.pending, key = { it.assignment.id }) { item ->
                    AssignmentRow(item, onClick = { onOpen(item.assignment.id) })
                }
            }

            if (state.done.isNotEmpty()) {
                item { Spacer(Modifier.height(20.dp)) }
                item { SectionHeader("Done", Modifier.padding(horizontal = 20.dp)) }
                item { Spacer(Modifier.height(10.dp)) }
                items(state.done, key = { it.assignment.id }) { item ->
                    AssignmentRow(item, onClick = { onOpen(item.assignment.id) })
                }
            }
        }
    }
}

@Composable
private fun AssignmentRow(item: StudentAssignmentStatus, onClick: () -> Unit) {
    val a = item.assignment
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 6.dp).fillMaxWidth(), onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiArt(a.type.emoji, size = 30.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(a.displayTitle, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink, maxLines = 1)
                Text(
                    listOfNotNull(a.type.label, a.dueDate?.let { "due $it" }, if (a.pointsReward > 0) "${a.pointsReward} coins" else null)
                        .joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
            }
            StatusBadge(item)
        }
    }
}

@Composable
private fun StatusBadge(item: StudentAssignmentStatus) {
    val (text, color) = when {
        item.isReviewed -> "✅ Reviewed" to CatoPalette.SuccessSoft
        item.isDone -> "📬 Done" to CatoPalette.AmberSoft
        else -> "To do" to CatoPalette.Cloud
    }
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(color).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = CatoPalette.Ink)
    }
}
