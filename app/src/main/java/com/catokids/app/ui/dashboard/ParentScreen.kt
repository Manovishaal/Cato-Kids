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
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun ParentHomeScreen(
    state: DashboardUiState,
    onOpenChild: (String) -> Unit,
    onOpenProfile: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.TealSoft) {
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
                        Text("Parent dashboard", style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                    }
                    Avatar(state.profile?.initials ?: "P", modifier = Modifier.clickable { onOpenProfile() })
                }
            }

            item {
                Text(
                    "How are they doing?",
                    style = MaterialTheme.typography.displaySmall,
                    color = CatoPalette.Ink,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(Modifier.height(16.dp))
            }

            item {
                Row(
                    Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricCard("👶", "${state.children.size}", "Children", Modifier.weight(1f), CatoPalette.TealSoft)
                    MetricCard("✅", "${state.averageCompletion}%", "Completion", Modifier.weight(1f), CatoPalette.CoralSoft)
                    MetricCard("⭐", "${state.children.sumOf { it.stars }}", "Stars", Modifier.weight(1f), CatoPalette.AmberSoft)
                }
            }

            item { Spacer(Modifier.height(22.dp)) }
            item { SectionHeader("My children", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }

            items(state.children) { child ->
                StudentRow(
                    student = child,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    onClick = { onOpenChild(child.profile.id) },
                )
            }

            if (state.children.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        emoji = "🔗",
                        title = "No children linked yet",
                        message = "Ask your child's school for their student code to link an account.",
                    )
                }
            }

            item { Spacer(Modifier.height(20.dp)) }
            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader("Tips for grown-ups")
                    Spacer(Modifier.height(10.dp))
                    InfoBanner("⏰", "Fifteen focused minutes a day beats an hour once a week.", color = CatoPalette.TealSoft)
                    Spacer(Modifier.height(8.dp))
                    InfoBanner("🗣️", "Ask them to say the letter sound out loud — it doubles what sticks.", color = CatoPalette.CoralSoft)
                    Spacer(Modifier.height(8.dp))
                    InfoBanner("🎉", "Celebrate the stars, not just the score. Effort is the habit you're building.", color = CatoPalette.AmberSoft)
                }
            }
        }
    }
}
