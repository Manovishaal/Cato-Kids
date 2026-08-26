package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.local.ActivityLibrary
import com.catokids.app.data.local.TeacherResourceLibrary
import com.catokids.app.data.model.*
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private enum class ResourceTab(val label: String) { GUIDE("Training guide"), ACTIVITIES("Activity library") }

/**
 * The teacher training library: grade-specific "how to teach this" briefings across the
 * 16 developmental/curricular domains the program is built on, plus the 80-activity
 * catalog teachers browse, understand and assign straight to a class. Reuses the same
 * [CreatorUiState] and [AssignToClassDialog] the rest of the creator tools use, so an
 * assign here behaves exactly like assigning anything a teacher built themselves.
 */
@Composable
fun TeacherResourcesScreen(
    state: CreatorUiState,
    onAssign: (Assignment) -> Unit,
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

    var tab by remember { mutableStateOf(ResourceTab.GUIDE) }
    var grade by remember { mutableStateOf(state.profile?.grade ?: Grade.PREKG) }
    var domainFilter by remember { mutableStateOf<DevelopmentalDomain?>(null) }
    var expandedDomain by remember { mutableStateOf<DevelopmentalDomain?>(null) }
    var detailActivity by remember { mutableStateOf<LibraryActivity?>(null) }
    var assignTarget by remember { mutableStateOf<AssignRequest?>(null) }

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(
                    title = "Teacher training library",
                    subtitle = "📘 16 skill areas · 80 ready-to-assign activities",
                    onBack = onBack,
                )
            }

            item { Spacer(Modifier.height(10.dp)) }
            item {
                Row(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ResourceTab.entries.forEach { t ->
                        val selected = tab == t
                        CatoCard(
                            modifier = Modifier.weight(1f),
                            color = if (selected) CatoPalette.Periwinkle else CatoPalette.Cloud,
                            onClick = { tab = t },
                        ) {
                            Text(
                                t.label,
                                modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) androidx.compose.ui.graphics.Color.White else CatoPalette.Ink,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(14.dp)) }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Text("Class", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                    Spacer(Modifier.height(8.dp))
                    CatoChipRow(
                        options = Grade.entries.toList(),
                        selected = grade,
                        label = { it.longLabel },
                        emoji = { it.emoji },
                        onSelect = { grade = it },
                    )
                }
            }
            item {
                InfoBanner(
                    "💡",
                    "Content below is written specifically for ${grade.longLabel} — switch class above to see how it changes.",
                    Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = CatoPalette.SkySoft,
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            when (tab) {
                ResourceTab.GUIDE -> {
                    items(DevelopmentalDomain.entries.toList(), key = { "dom-${it.wire}" }) { domain ->
                        val resource = remember(domain, grade) { TeacherResourceLibrary.find(domain, grade) }
                        DomainResourceRow(
                            domain = domain,
                            resource = resource,
                            expanded = expandedDomain == domain,
                            onToggle = { expandedDomain = if (expandedDomain == domain) null else domain },
                        )
                    }
                }
                ResourceTab.ACTIVITIES -> {
                    item {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                            Text("Skill area", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                            Spacer(Modifier.height(8.dp))
                            CatoChipRow(
                                options = listOf(null) + DevelopmentalDomain.entries.toList(),
                                selected = domainFilter,
                                label = { it?.title ?: "All" },
                                emoji = { it?.emoji ?: "🗂️" },
                                onSelect = { domainFilter = it },
                            )
                        }
                    }
                    // Plain expression, not `remember` — this LazyColumn content block isn't a
                    // @Composable context (only the item { } / items { } lambdas inside it are),
                    // and a cheap filter over 80 items doesn't need memoizing anyway.
                    val activities = ActivityLibrary.forGrade(grade).let { list ->
                        domainFilter?.let { d -> list.filter { it.domain == d } } ?: list
                    }
                    if (activities.isEmpty()) {
                        item {
                            EmptyState(
                                "🗂️", "No activities for this pick",
                                "Try a different class or skill area.",
                                Modifier.padding(horizontal = 20.dp),
                            )
                        }
                    }
                    items(activities, key = { "act-${it.id}" }) { activity ->
                        LibraryActivityRow(activity = activity, onOpen = { detailActivity = activity })
                    }
                }
            }
        }
    }

    detailActivity?.let { activity ->
        LibraryActivityDetailDialog(
            activity = activity,
            onAssign = {
                assignTarget = AssignRequest(
                    title = activity.title,
                    type = AssignmentType.ACTIVITY,
                    pointsReward = activity.pointsReward,
                    instructions = "${activity.instructions}\n\nMaterials: ${activity.materials.ifEmpty { listOf("None needed") }.joinToString(", ")}",
                )
                detailActivity = null
            },
            onDismiss = { detailActivity = null },
        )
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
private fun DomainResourceRow(
    domain: DevelopmentalDomain,
    resource: TeachingResource?,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), onClick = onToggle) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiArt(domain.emoji, size = 26.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(domain.title, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    if (resource != null && !expanded) {
                        Text(resource.overview, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft, maxLines = 1)
                    }
                }
                Text(if (expanded) "▲" else "▼", color = CatoPalette.InkSoft)
            }
            if (expanded && resource != null) {
                Spacer(Modifier.height(10.dp))
                Text(resource.overview, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
                Spacer(Modifier.height(10.dp))
                ResourceSubList("What to build toward", resource.goals)
                Spacer(Modifier.height(8.dp))
                ResourceSubList("How to teach it", resource.teachingTips)
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CatoPalette.SuccessSoft)
                        .padding(10.dp),
                ) {
                    Text(
                        "👀 Look for: ${resource.lookFor}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CatoPalette.SuccessDeep,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceSubList(title: String, items: List<String>) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
    Spacer(Modifier.height(4.dp))
    items.forEach { line ->
        Row(Modifier.padding(vertical = 2.dp)) {
            Text("• ", style = MaterialTheme.typography.bodySmall, color = CatoPalette.Periwinkle)
            Text(line, style = MaterialTheme.typography.bodySmall, color = CatoPalette.Ink)
        }
    }
}

@Composable
private fun LibraryActivityRow(activity: LibraryActivity, onOpen: () -> Unit) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), onClick = onOpen) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiArt(activity.activityType.emoji, size = 26.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.title, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink, maxLines = 1)
                Text(
                    "${activity.domain.title} · ${activity.durationMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(CatoPalette.AmberSoft)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text("🪙 ${activity.pointsReward}", style = MaterialTheme.typography.labelSmall, color = CatoPalette.Ink)
            }
        }
    }
}

@Composable
private fun LibraryActivityDetailDialog(
    activity: LibraryActivity,
    onAssign: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(activity.title) },
        text = {
            Column {
                Text(
                    "${activity.domain.emoji} ${activity.domain.title} · ${activity.grade.longLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CatoPalette.PeriwinkleDeep,
                )
                Spacer(Modifier.height(10.dp))
                Text("Objective", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                Text(activity.objective, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
                Spacer(Modifier.height(10.dp))
                Text("How to run it", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                Text(activity.instructions, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
                if (activity.materials.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("Materials", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                    Text(activity.materials.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "⏱️ ${activity.durationMinutes} min · 🪙 ${activity.pointsReward} coins on completion",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
            }
        },
        confirmButton = { CatoButton(text = "Assign to class", onClick = onAssign) },
        dismissButton = { CatoOutlineButton(text = "Close", onClick = onDismiss) },
    )
}
