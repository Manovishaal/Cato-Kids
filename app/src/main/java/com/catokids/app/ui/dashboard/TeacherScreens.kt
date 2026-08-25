package com.catokids.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.StudentSummary
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun TeacherHomeScreen(
    state: DashboardUiState,
    onOpenClass: (String) -> Unit,
    onOpenStudent: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCreator: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
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
                        EmojiText(
                            "👋 Hi, ${state.profile?.firstName ?: "there"}",
                            style = MaterialTheme.typography.titleMedium,
                            color = CatoPalette.Ink,
                        )
                        Text(
                            state.school?.name ?: "Cato Kids",
                            style = MaterialTheme.typography.bodySmall,
                            color = CatoPalette.InkSoft,
                        )
                    }
                    Avatar(state.profile?.initials ?: "T", modifier = Modifier.clickable { onOpenProfile() })
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    color = CatoPalette.Periwinkle,
                    onClick = onOpenCreator,
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiArt("🛠️", size = 30.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Create for your class",
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
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text(
                    "Students' reports",
                    style = MaterialTheme.typography.displaySmall,
                    color = CatoPalette.Ink,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(14.dp))
            }

            item {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard("👧", "${state.students.size}", "Students", Modifier.weight(1f))
                    MetricCard("✅", "${state.averageCompletion}%", "Completion", Modifier.weight(1f), CatoPalette.TealSoft)
                    MetricCard("🎯", "${state.averageScore}%", "Avg score", Modifier.weight(1f), CatoPalette.AmberSoft)
                }
            }

            item { Spacer(Modifier.height(20.dp)) }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Progress by class", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Spacer(Modifier.height(14.dp))
                        MiniBarChart(
                            values = Grade.entries.map { g ->
                                val group = state.students.filter { it.profile.grade == g }
                                g.label to if (group.isEmpty()) 0 else group.sumOf { it.completionPercent } / group.size
                            },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(22.dp)) }

            item { SectionHeader("My classes", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
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

            if (state.needsHelp.isNotEmpty()) {
                item { Spacer(Modifier.height(20.dp)) }
                item {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        SectionHeader("Needs a hand")
                        Spacer(Modifier.height(6.dp))
                        InfoBanner(
                            "💡",
                            "These children scored under 70%. A short one-to-one on their weakest subject usually fixes it.",
                            color = CatoPalette.AmberSoft,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
                items(state.needsHelp) { s ->
                    StudentRow(
                        student = s,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .fillMaxWidth(),
                        onClick = { onOpenStudent(s.profile.id) },
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item { SectionHeader("All students", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            items(state.students) { s ->
                StudentRow(
                    student = s,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenStudent(s.profile.id) },
                )
            }

            if (state.students.isEmpty() && !state.loading) {
                item {
                    EmptyState("🪺", "No students yet", "Add children to a class and their progress will appear here.")
                }
            }
        }
    }
}

@Composable
fun ClassDetailScreen(
    className: String,
    students: List<StudentSummary>,
    onOpenStudent: (String) -> Unit,
    onBack: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = className, subtitle = "${students.size} students", onBack = onBack)
            }
            item {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard("✅", "${if (students.isEmpty()) 0 else students.sumOf { it.completionPercent } / students.size}%", "Completion", Modifier.weight(1f))
                    MetricCard("⭐", "${students.sumOf { it.stars }}", "Stars earned", Modifier.weight(1f), CatoPalette.AmberSoft)
                }
            }
            item { SectionHeader("Ranking", Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) }
            items(students.sortedByDescending { it.averageScore }) { s ->
                StudentRow(
                    student = s,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenStudent(s.profile.id) },
                )
            }
            if (students.isEmpty()) {
                item { EmptyState("👀", "Empty class", "No children have been added to this class yet.") }
            }
        }
    }
}
