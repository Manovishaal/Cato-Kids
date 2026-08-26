package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.*
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun CreatorHubScreen(
    state: CreatorUiState,
    onCreateHomework: () -> Unit,
    onCreateActivity: () -> Unit,
    onCreateCourse: () -> Unit,
    onCreateGame: () -> Unit,
    onOpenResources: () -> Unit,
    onOpenSubmissions: (String) -> Unit,
    onAssign: (Assignment) -> Unit,
    onDeleteGame: (String) -> Unit,
    onDeleteCourse: (String) -> Unit,
    onDeleteActivity: (String) -> Unit,
    onDeleteAssignment: (String) -> Unit,
    onTogglePublishGame: (CustomGame) -> Unit,
    onTogglePublishCourse: (ExtraCourse) -> Unit,
    onTogglePublishActivity: (Activity) -> Unit,
    onBack: () -> Unit,
) {
    if (!state.allowed && !state.loading) {
        CatoBackdrop {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                EmptyState("🔒", "Teachers & staff only", "This tool is for teachers, schools and administrators.") {
                    CatoButton(text = "Back", onClick = onBack)
                }
            }
        }
        return
    }

    var assignTarget by remember { mutableStateOf<AssignRequest?>(null) }

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "Create for your class", subtitle = "📚 Homework · activities · games", onBack = onBack)
            }

            item { Spacer(Modifier.height(8.dp)) }
            item {
                CatoCard(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    color = CatoPalette.Periwinkle,
                    onClick = onOpenResources,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt("📘", size = 30.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Teacher training library",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                            )
                            Text(
                                "Content & tips for 16 skill areas, per class · 80 ready-to-assign activities",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                        Text("▶", color = Color.White)
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        CreateTile("📝", "Homework", Modifier.weight(1f), onCreateHomework)
                        CreateTile("🎨", "Activity", Modifier.weight(1f), onCreateActivity)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        CreateTile("🌟", "Extra course", Modifier.weight(1f), onCreateCourse)
                        CreateTile("🎮", "Build a game", Modifier.weight(1f), onCreateGame)
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
            item { SectionHeader("My games", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            if (state.games.isEmpty()) {
                item { InfoBanner("🎮", "Nothing built yet — try \"Build a game\" above.", Modifier.padding(horizontal = 20.dp)) }
            }
            items(state.games, key = { "g-${it.id}" }) { game ->
                LibraryRow(
                    emoji = game.gameType.emoji,
                    title = game.title,
                    subtitle = "${game.gameType.title} · ${game.content.rounds.size} rounds",
                    published = game.published,
                    onAssign = { assignTarget = AssignRequest(game.title, AssignmentType.HOMEWORK, customGameId = game.id, pointsReward = 10) },
                    onTogglePublish = { onTogglePublishGame(game) },
                    onDelete = { onDeleteGame(game.id) },
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("My extra courses", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            if (state.courses.isEmpty()) {
                item { InfoBanner("🌟", "No electives yet — bundle a few lessons together.", Modifier.padding(horizontal = 20.dp)) }
            }
            items(state.courses, key = { "c-${it.id}" }) { course ->
                LibraryRow(
                    emoji = course.coverEmoji,
                    title = course.title,
                    subtitle = "${course.lessonIds.size} lessons",
                    published = course.published,
                    onAssign = { assignTarget = AssignRequest(course.title, AssignmentType.COURSE, courseId = course.id, pointsReward = 0) },
                    onTogglePublish = { onTogglePublishCourse(course) },
                    onDelete = { onDeleteCourse(course.id) },
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("My activities", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            if (state.activities.isEmpty()) {
                item { InfoBanner("🎨", "No offline activities yet — crafts, outdoor tasks, anything screen-free.", Modifier.padding(horizontal = 20.dp)) }
            }
            items(state.activities, key = { "a-${it.id}" }) { activity ->
                LibraryRow(
                    emoji = activity.activityType.emoji,
                    title = activity.title,
                    subtitle = activity.activityType.label,
                    published = activity.published,
                    onAssign = {
                        assignTarget = AssignRequest(
                            activity.title, AssignmentType.ACTIVITY,
                            activityId = activity.id, pointsReward = activity.pointsReward,
                            instructions = activity.instructions,
                        )
                    },
                    onTogglePublish = { onTogglePublishActivity(activity) },
                    onDelete = { onDeleteActivity(activity.id) },
                )
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("Assigned to a class", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            if (state.assignedWork.isEmpty()) {
                item { EmptyState("📭", "Nothing assigned yet", "Build something above, then assign it to a class.") }
            }
            items(state.assignedWork, key = { "w-${it.id}" }) { work ->
                val className = state.classes.firstOrNull { it.id == work.classId }?.name ?: "Class"
                AssignedWorkRow(
                    work = work,
                    className = className,
                    onReview = { onOpenSubmissions(work.id) },
                    onDelete = { onDeleteAssignment(work.id) },
                )
            }
        }
    }

    assignTarget?.let { request ->
        AssignToClassDialog(
            request = request,
            classes = state.classes,
            profileId = state.profile?.id,
            onConfirm = { assignment -> onAssign(assignment); assignTarget = null },
            onDismiss = { assignTarget = null },
        )
    }
}

@Composable
private fun CreateTile(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CatoCard(modifier = modifier, onClick = onClick, color = Color.White) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CatoPalette.PeriwinkleSoft),
                contentAlignment = Alignment.Center,
            ) { EmojiArt(emoji, size = 30.dp) }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
        }
    }
}

@Composable
private fun LibraryRow(
    emoji: String,
    title: String,
    subtitle: String,
    published: Boolean,
    onAssign: () -> Unit,
    onTogglePublish: () -> Unit,
    onDelete: () -> Unit,
) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiArt(emoji, size = 26.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink, maxLines = 1)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                }
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (published) CatoPalette.SuccessSoft else CatoPalette.Cloud)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        if (published) "Live" else "Hidden",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (published) CatoPalette.SuccessDeep else CatoPalette.InkSoft,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CatoOutlineButton(text = "Assign", onClick = onAssign, modifier = Modifier.weight(1f))
                CatoOutlineButton(
                    text = if (published) "Hide" else "Publish",
                    onClick = onTogglePublish,
                    color = CatoPalette.Amber,
                    modifier = Modifier.weight(1f),
                )
                CatoOutlineButton(text = "Delete", onClick = onDelete, color = CatoPalette.Error, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AssignedWorkRow(
    work: Assignment,
    className: String,
    onReview: () -> Unit,
    onDelete: () -> Unit,
) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiArt(work.type.emoji, size = 26.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(work.displayTitle, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink, maxLines = 1)
                Text(
                    "$className${work.dueDate?.let { " · due $it" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
            }
            if (work.requiresSubmission) {
                CatoOutlineButton(text = "Review", onClick = onReview)
                Spacer(Modifier.width(6.dp))
            }
            IconButton(onClick = onDelete) {
                Text("✕", color = CatoPalette.Error)
            }
        }
    }
}
