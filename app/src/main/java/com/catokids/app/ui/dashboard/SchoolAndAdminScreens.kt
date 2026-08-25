package com.catokids.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Role
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun SchoolHomeScreen(
    state: DashboardUiState,
    onOpenClass: (String) -> Unit,
    onOpenStudent: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCreator: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.VioletSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 52.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EmojiText("🏫 School dashboard", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Text(
                            state.school?.name ?: "Cato Kids",
                            style = MaterialTheme.typography.bodySmall,
                            color = CatoPalette.InkSoft,
                        )
                    }
                    Avatar(state.profile?.initials ?: "S", modifier = Modifier.clickable { onOpenProfile() })
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    color = CatoPalette.Violet,
                    onClick = onOpenCreator,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt("🛠️", size = 30.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Create for your school",
                                style = MaterialTheme.typography.titleSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                            Text(
                                "Homework, activities, courses & games",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                            )
                        }
                        Text("▶", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            state.school?.name ?: "Your school",
                            style = MaterialTheme.typography.headlineSmall,
                            color = CatoPalette.Ink,
                        )
                        Text(
                            listOfNotNull(state.school?.code, state.school?.city).joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = CatoPalette.InkSoft,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard("👧", "${state.students.size}", "Students", Modifier.weight(1f), CatoPalette.VioletSoft)
                            MetricCard("🏫", "${state.classes.size}", "Classes", Modifier.weight(1f), CatoPalette.PeriwinkleSoft)
                            MetricCard("🟢", "${state.activeToday}", "Active today", Modifier.weight(1f), CatoPalette.TealSoft)
                        }
                    }
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Completion by class level", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Spacer(Modifier.height(14.dp))
                        MiniBarChart(
                            values = Grade.entries.map { g ->
                                val group = state.students.filter { it.profile.grade == g }
                                g.label to if (group.isEmpty()) 0 else group.sumOf { it.completionPercent } / group.size
                            },
                            barColor = CatoPalette.Violet,
                        )
                    }
                }
            }

            item { SectionHeader("Classes", Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) }
            items(state.classes) { c ->
                ClassCard(
                    classRoom = c,
                    students = state.students.count { it.profile.grade == c.grade },
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenClass(c.id) },
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
            item { SectionHeader("Top performers", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            items(state.topPerformers) { s ->
                StudentRow(
                    student = s,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenStudent(s.profile.id) },
                )
            }
        }
    }
}

@Composable
fun AdminHomeScreen(
    state: DashboardUiState,
    onOpenStudent: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCreator: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.AmberSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 52.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        EmojiText("🛠️ Administrator", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Text("Platform overview", style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                    }
                    Avatar(state.profile?.initials ?: "A", modifier = Modifier.clickable { onOpenProfile() })
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth(),
                    color = CatoPalette.Amber,
                    onClick = onOpenCreator,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt("🛠️", size = 30.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Create for your school",
                                style = MaterialTheme.typography.titleSmall,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                            Text(
                                "Homework, activities, courses & games",
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                            )
                        }
                        Text("▶", color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }

            item { SectionHeader("People", Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) }
            item {
                Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Role.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { role ->
                                MetricCard(
                                    emoji = role.emoji,
                                    value = "${state.counts[role] ?: 0}",
                                    label = role.label,
                                    modifier = Modifier.weight(1f),
                                    tint = CatoPalette.AmberSoft,
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("Content library", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Grade.entries.forEach { grade ->
                            EmojiText(
                                "${grade.emoji}  ${grade.longLabel}",
                                style = MaterialTheme.typography.titleSmall,
                                color = CatoPalette.Ink,
                            )
                            Spacer(Modifier.height(8.dp))
                            SubjectId.entries.forEach { subject ->
                                val n = CatoCurriculum.countFor(grade, subject)
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    EmojiArt(subject.emoji, size = 22.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        subject.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CatoPalette.InkSoft,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Box(
                                        Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CatoPalette.Cloud)
                                            .padding(horizontal = 10.dp, vertical = 4.dp),
                                    ) {
                                        Text("$n lessons", style = MaterialTheme.typography.labelSmall, color = CatoPalette.Ink)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        InfoBanner(
                            emoji = "📦",
                            text = "${CatoCurriculum.all.size} lessons across 3 levels and 3 books are bundled with the app and mirrored to Supabase.",
                            color = CatoPalette.SkySoft,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("Recent students", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            items(state.students.take(8)) { s ->
                StudentRow(
                    student = s,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenStudent(s.profile.id) },
                )
            }
        }
    }
}
