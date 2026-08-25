package com.catokids.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.StudentSummary
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun StudentReportScreen(
    student: StudentSummary?,
    breakdown: Map<SubjectId, Int>,
    onBack: () -> Unit,
) {
    if (student == null) {
        CatoBackdrop {
            Column(Modifier.fillMaxSize()) {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "Report", onBack = onBack)
                EmptyState("🔍", "Student not found", "This child may have been moved to another class.")
            }
        }
        return
    }

    CatoBackdrop {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "Report card", subtitle = student.profile.fullName, onBack = onBack)
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(student.profile.initials, size = 62.dp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(student.profile.fullName, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                            Text(
                                "${student.profile.grade?.longLabel ?: "—"} · last active ${student.lastActiveLabel.lowercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CatoPalette.InkSoft,
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatPill("⭐", "${student.stars}", "stars")
                                StatPill("🔥", "${student.profile.streakDays}", "day streak", color = CatoPalette.CoralSoft)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DonutStat(student.completionPercent, "Lessons done", color = CatoPalette.Teal)
                    DonutStat(student.averageScore, "Average score", color = CatoPalette.Coral)
                }
            }

            item {
                CatoCard(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("By subject", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Spacer(Modifier.height(14.dp))
                        breakdown.forEach { (subject, percent) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                EmojiArt(subject.emoji, size = 26.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(subject.title, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                                    Spacer(Modifier.height(6.dp))
                                    CatoProgressBar(
                                        percent / 100f,
                                        Modifier.fillMaxWidth(),
                                        color = when (subject) {
                                            SubjectId.LETTER_LAND   -> CatoPalette.Coral
                                            SubjectId.NUMBER_LAND   -> CatoPalette.Teal
                                            SubjectId.KNOW_MY_WORLD -> CatoPalette.Periwinkle
                                        },
                                        height = 9.dp,
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text("$percent%", style = MaterialTheme.typography.titleSmall, color = CatoPalette.InkSoft)
                            }
                            Spacer(Modifier.height(14.dp))
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
                        Text("What to work on next", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                        Spacer(Modifier.height(10.dp))
                        val weakest = breakdown.minByOrNull { it.value }
                        val strongest = breakdown.maxByOrNull { it.value }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CatoPalette.AmberSoft)
                                .padding(14.dp),
                        ) {
                            Text(
                                buildString {
                                    if (strongest != null) {
                                        append("${student.profile.firstName} is strongest in ${strongest.key.title} (${strongest.value}%). ")
                                    }
                                    if (weakest != null) {
                                        append("Spend the next few sessions on ${weakest.key.title} — currently ${weakest.value}%.")
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = CatoPalette.Ink,
                            )
                        }
                    }
                }
            }
        }
    }
}
