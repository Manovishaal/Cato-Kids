package com.catokids.app.ui.creator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.data.local.ActivityLibrary
import com.catokids.app.data.local.TeacherResourceLibrary
import com.catokids.app.data.model.*
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoAccents
import com.catokids.app.ui.theme.CatoPalette
import kotlinx.coroutines.launch

private enum class ResourceTab(val label: String) { GUIDE("Training guide"), ACTIVITIES("Activity library") }

private fun reviewKey(domain: DevelopmentalDomain, grade: Grade) = "${domain.wire}_${grade.wire}"

/**
 * The teacher training library: grade-specific "how to teach this" briefings across the
 * 16 developmental/curricular domains the program is built on, plus the ready-to-assign
 * activity catalog teachers browse, understand and assign straight to a class. Reuses the
 * same [CreatorUiState] and [AssignToClassDialog] the rest of the creator tools use, so an
 * assign here behaves exactly like assigning anything a teacher built themselves.
 *
 * The training guide is a tap-through mini "lesson" per skill area (why it matters → goals
 * checklist → a one-tip-at-a-time teaching deck → a milestone reveal) rather than a wall of
 * text, so a teacher browsing between periods can actually get through it.
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
    var lessonDomain by remember { mutableStateOf<DevelopmentalDomain?>(null) }
    var reviewedKeys by remember { mutableStateOf(setOf<String>()) }
    var detailActivity by remember { mutableStateOf<LibraryActivity?>(null) }
    var assignTarget by remember { mutableStateOf<AssignRequest?>(null) }

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(
                    title = "Teacher training library",
                    subtitle = "📘 16 skill areas · 1705 ready-to-assign activities",
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
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else CatoPalette.Ink,
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
                    item {
                        val reviewedForGrade = remember(reviewedKeys, grade) {
                            DevelopmentalDomain.entries.count { reviewedKeys.contains(reviewKey(it, grade)) }
                        }
                        GuideProgressBanner(
                            reviewed = reviewedForGrade,
                            total = DevelopmentalDomain.entries.size,
                            grade = grade,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    }
                    items(DevelopmentalDomain.entries.toList(), key = { "dom-${it.wire}" }) { domain ->
                        val resource = remember(domain, grade) { TeacherResourceLibrary.find(domain, grade) }
                        DomainLessonCard(
                            domain = domain,
                            resource = resource,
                            reviewed = reviewedKeys.contains(reviewKey(domain, grade)),
                            onOpen = { lessonDomain = domain },
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
                    // and a cheap filter over the library doesn't need memoizing anyway.
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

    lessonDomain?.let { domain ->
        val resource = remember(domain, grade) { TeacherResourceLibrary.find(domain, grade) }
        DomainLessonOverlay(
            domain = domain,
            grade = grade,
            resource = resource,
            reviewed = reviewedKeys.contains(reviewKey(domain, grade)),
            onMarkReviewed = { reviewedKeys = reviewedKeys + reviewKey(domain, grade) },
            onClose = { lessonDomain = null },
        )
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

// ---------------------------------------------------------------- guide: browse list

@Composable
private fun GuideProgressBanner(reviewed: Int, total: Int, grade: Grade, modifier: Modifier = Modifier) {
    val allDone = total > 0 && reviewed == total
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CatoPalette.PeriwinkleSoft)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiArt(if (allDone) "🏆" else "✨", size = 24.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                if (allDone) "All ${grade.longLabel} skill areas reviewed!" else "$reviewed of $total ${grade.longLabel} skill areas reviewed",
                style = MaterialTheme.typography.labelLarge,
                color = CatoPalette.Ink,
            )
        }
        Spacer(Modifier.height(8.dp))
        CatoProgressBar(
            fraction = if (total > 0) reviewed.toFloat() / total else 0f,
            color = CatoPalette.PeriwinkleDeep,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DomainLessonCard(
    domain: DevelopmentalDomain,
    resource: TeachingResource?,
    reviewed: Boolean,
    onOpen: () -> Unit,
) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(), onClick = onOpen) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CatoAccents[domain.ordinal % CatoAccents.size].copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) { EmojiArt(domain.emoji, size = 24.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(domain.title, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                if (resource != null) {
                    Text(resource.overview, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft, maxLines = 1)
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (reviewed) CatoPalette.SuccessSoft else CatoPalette.Cloud)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    if (reviewed) "✓ Done" else "Start ▸",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (reviewed) CatoPalette.SuccessDeep else CatoPalette.PeriwinkleDeep,
                )
            }
        }
    }
}

// ---------------------------------------------------------------- guide: the lesson itself

private enum class LessonPage { WHY, GOALS, TEACH, LOOK_FOR }

/**
 * A full-screen tap-through mini lesson for one domain+grade briefing, in place of a
 * paragraph dump: swipe or tap Next through four short pages — why it matters, a tappable
 * goals checklist, a one-tip-at-a-time teaching deck, and a milestone reveal — instead of
 * reading a wall of bullet points all at once.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DomainLessonOverlay(
    domain: DevelopmentalDomain,
    grade: Grade,
    resource: TeachingResource?,
    reviewed: Boolean,
    onMarkReviewed: () -> Unit,
    onClose: () -> Unit,
) {
    val accent = CatoAccents[domain.ordinal % CatoAccents.size]
    val pages = LessonPage.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    CatoBackdrop(modifier = Modifier.fillMaxSize(), top = CatoPalette.PeriwinkleSoft) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(40.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Text("✕", style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                }
                Column(Modifier.weight(1f).padding(start = 2.dp)) {
                    Text(domain.title, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink, maxLines = 1)
                    Text("${grade.emoji} ${grade.longLabel}", style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                }
                if (reviewed) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CatoPalette.SuccessSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) { Text("✓ Reviewed", style = MaterialTheme.typography.labelSmall, color = CatoPalette.SuccessDeep) }
                }
                Spacer(Modifier.width(4.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                pages.forEachIndexed { i, _ ->
                    val active = pagerState.currentPage == i
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (active) accent else CatoPalette.Line),
                    )
                }
            }

            if (resource == null) {
                EmptyState(
                    "📭", "No briefing yet",
                    "This skill area doesn't have a training guide entry yet.",
                    Modifier.weight(1f),
                )
            } else {
                val safeResource = resource
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) { page ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        when (pages[page]) {
                            LessonPage.WHY -> WhyItMattersPage(domain, safeResource, accent)
                            LessonPage.GOALS -> GoalsChecklistPage(domain, grade, safeResource, accent)
                            LessonPage.TEACH -> TeachingTipDeckPage(domain, grade, safeResource, accent)
                            LessonPage.LOOK_FOR -> LookForPage(
                                resource = safeResource,
                                accent = accent,
                                onAcknowledge = {
                                    onMarkReviewed()
                                    onClose()
                                },
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    CatoOutlineButton(
                        text = "◀ Back",
                        onClick = { scope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } },
                        color = accent,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(12.dp))
                    if (pagerState.currentPage < pages.lastIndex) {
                        CatoButton(
                            text = "Next ▶",
                            onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            color = accent,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WhyItMattersPage(domain: DevelopmentalDomain, resource: TeachingResource, accent: Color) {
    Spacer(Modifier.height(8.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(accent.copy(alpha = 0.16f))
            .padding(24.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            EmojiArt(domain.emoji, size = 56.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                resource.overview,
                style = MaterialTheme.typography.titleMedium,
                color = CatoPalette.Ink,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(Modifier.height(16.dp))
    InfoBanner(
        "👉",
        "Swipe or tap Next to see what to build toward, how to teach it, and the milestone to watch for.",
        color = CatoPalette.SkySoft,
    )
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun GoalsChecklistPage(domain: DevelopmentalDomain, grade: Grade, resource: TeachingResource, accent: Color) {
    var checked by remember(domain, grade) { mutableStateOf(setOf<Int>()) }
    val allDone = resource.goals.isNotEmpty() && checked.size == resource.goals.size

    Spacer(Modifier.height(8.dp))
    Text("What to build toward", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
    Spacer(Modifier.height(4.dp))
    Text(
        "Tap each goal as you get familiar with it.",
        style = MaterialTheme.typography.bodySmall,
        color = CatoPalette.InkSoft,
    )
    Spacer(Modifier.height(10.dp))
    CatoProgressBar(
        fraction = if (resource.goals.isNotEmpty()) checked.size.toFloat() / resource.goals.size else 0f,
        color = accent,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(6.dp))
    Box(Modifier.fillMaxWidth().height(34.dp), contentAlignment = Alignment.CenterStart) {
        SparkleBurst(trigger = if (allDone) Unit else null, modifier = Modifier.fillMaxSize())
        Text(
            if (allDone) "All noted — nice!" else "${checked.size} of ${resource.goals.size} noted",
            style = MaterialTheme.typography.labelSmall,
            color = CatoPalette.InkSoft,
        )
    }
    Spacer(Modifier.height(10.dp))
    resource.goals.forEachIndexed { i, goal ->
        val isChecked = checked.contains(i)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isChecked) accent.copy(alpha = 0.14f) else CatoPalette.Cloud)
                .clickable { checked = if (isChecked) checked - i else checked + i }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isChecked) accent else Color.White)
                    .border(2.dp, accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (isChecked) Text("✓", color = Color.White, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                goal,
                style = MaterialTheme.typography.bodyMedium,
                color = CatoPalette.Ink,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun TeachingTipDeckPage(domain: DevelopmentalDomain, grade: Grade, resource: TeachingResource, accent: Color) {
    var tipIndex by remember(domain, grade) { mutableStateOf(0) }
    val tips = resource.teachingTips

    Spacer(Modifier.height(8.dp))
    Text("How to teach it", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
    Spacer(Modifier.height(4.dp))
    Text(
        "Work through these one at a time — tap Next tip when you're ready.",
        style = MaterialTheme.typography.bodySmall,
        color = CatoPalette.InkSoft,
    )
    Spacer(Modifier.height(16.dp))

    if (tips.isEmpty()) {
        InfoBanner("🤷", "No teaching tips listed for this skill area yet.", color = CatoPalette.Cloud)
    } else {
        AnimatedContent(
            targetState = tipIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "tip",
        ) { index ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).background(accent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text("Tip ${index + 1} of ${tips.size}", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
                }
                Spacer(Modifier.height(12.dp))
                Text(tips[index], style = MaterialTheme.typography.bodyLarge, color = CatoPalette.Ink)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CatoOutlineButton(
                text = "◀ Prev tip",
                onClick = { tipIndex = (tipIndex - 1 + tips.size) % tips.size },
                color = accent,
                modifier = Modifier.weight(1f),
            )
            CatoButton(
                text = "Next tip ▶",
                onClick = { tipIndex = (tipIndex + 1) % tips.size },
                color = accent,
                modifier = Modifier.weight(1f),
            )
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun LookForPage(resource: TeachingResource, accent: Color, onAcknowledge: () -> Unit) {
    var revealed by remember(resource) { mutableStateOf(false) }

    Spacer(Modifier.height(8.dp))
    Text("Look for", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
    Spacer(Modifier.height(4.dp))
    Text(
        "The one thing to watch for that tells you a child has got this.",
        style = MaterialTheme.typography.bodySmall,
        color = CatoPalette.InkSoft,
    )
    Spacer(Modifier.height(16.dp))

    if (!revealed) {
        CatoCard(
            Modifier.fillMaxWidth(),
            color = accent.copy(alpha = 0.16f),
            onClick = { revealed = true },
        ) {
            Column(
                Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmojiArt("🔍", size = 40.dp)
                Spacer(Modifier.height(10.dp))
                Text("Tap to reveal the milestone", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
            }
        }
    } else {
        Box(
            Modifier
                .fillMaxWidth()
                .hop(trigger = resource)
                .clip(RoundedCornerShape(20.dp))
                .background(CatoPalette.SuccessSoft)
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiArt("👀", size = 30.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    resource.lookFor,
                    style = MaterialTheme.typography.bodyLarge,
                    color = CatoPalette.SuccessDeep,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        CatoButton(
            text = "Got it — I'll watch for this ✅",
            onClick = onAcknowledge,
            color = CatoPalette.Success,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    Spacer(Modifier.height(16.dp))
}

// ---------------------------------------------------------------- activity library

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
