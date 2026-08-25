package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private data class Badge(val emoji: String, val title: String, val needs: Int, val what: String)

private val BADGES = listOf(
    Badge("🌱", "First steps",     1,  "Finish your first lesson"),
    Badge("🖐️", "High five",       5,  "Finish 5 lessons"),
    Badge("🔟", "Perfect ten",     10, "Finish 10 lessons"),
    Badge("📚", "Book worm",       20, "Finish 20 lessons"),
    Badge("🏅", "Champion",        35, "Finish 35 lessons"),
    Badge("👑", "Cato royalty",    50, "Finish 50 lessons"),
)

@Composable
fun RewardsScreen(state: StudentUiState, onBack: () -> Unit) {
    val done = state.summary.completedLessons

    CatoBackdrop(top = CatoPalette.AmberSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "My rewards", onBack = onBack)
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GooseCharacter(modifier = Modifier.size(126.dp), cheering = true)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatPill("⭐", "${state.summary.stars}", "stars")
                        StatPill("🪙", "${state.profile?.coins ?: 0}", "coins", color = CatoPalette.CoralSoft)
                        StatPill("✅", "$done", "lessons", color = CatoPalette.TealSoft)
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
            item { SectionHeader("Badges", Modifier.padding(horizontal = 20.dp)) }
            item { Spacer(Modifier.height(10.dp)) }
            itemsIndexed(BADGES) { index, badge ->
                val unlocked = done >= badge.needs
                PopIn(delayMillis = stagger(index, step = 60)) {
                    CatoCard(
                        Modifier
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                            .fillMaxWidth()
                            .alpha(if (unlocked) 1f else 0.55f),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (unlocked) CatoPalette.AmberSoft else CatoPalette.Cloud),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmojiArt(
                                    if (unlocked) badge.emoji else "🔒",
                                    size = 36.dp,
                                    modifier = Modifier.floaty(
                                        amplitude = if (unlocked) 3.dp else 0.dp,
                                        phase = index * 0.8f,
                                    ),
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(badge.title, style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                                Text(badge.what, style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                                if (!unlocked) {
                                    Spacer(Modifier.height(8.dp))
                                    CatoProgressBar(
                                        (done.toFloat() / badge.needs).coerceIn(0f, 1f),
                                        Modifier.fillMaxWidth(),
                                        color = CatoPalette.Amber,
                                        height = 7.dp,
                                    )
                                }
                            }
                            if (!unlocked) {
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "$done/${badge.needs}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CatoPalette.InkSoft,
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
