package com.catokids.app.ui.creator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Assignment
import com.catokids.app.data.model.AssignmentType
import com.catokids.app.data.model.ClassRoom
import com.catokids.app.data.model.CustomGame
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun HomeworkComposerScreen(
    classes: List<ClassRoom>,
    games: List<CustomGame>,
    profileId: String?,
    onSave: (Assignment) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var classId by remember(classes) { mutableStateOf(classes.firstOrNull()?.id) }
    var dueOffset by remember { mutableStateOf<Int?>(7) }
    var points by remember { mutableStateOf(15) }
    var attachedGame by remember { mutableStateOf<CustomGame?>(null) }

    val canSave = title.isNotBlank() && classId != null

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "New homework", subtitle = "📝 For your class", onBack = onBack)
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CatoTextField(value = title, onValueChange = { title = it }, label = "Title", leading = "📝")
                    CatoMultilineField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = "Instructions for the child",
                        placeholder = "What should they do at home?",
                    )

                    Text("Class", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    if (classes.isEmpty()) {
                        InfoBanner("🏫", "No classes yet — add one from your dashboard first.")
                    } else {
                        CatoChipRow(
                            options = classes,
                            selected = classes.firstOrNull { it.id == classId } ?: classes.first(),
                            label = { c -> "${c.name} · ${c.grade.label}" },
                            onSelect = { classId = it.id },
                        )
                    }

                    Text("Due", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = listOf(null, 3, 7, 14),
                        selected = dueOffset,
                        label = { d -> if (d == null) "No date" else "In $d days" },
                        onSelect = { dueOffset = it },
                    )

                    CatoStepper(value = points, onValueChange = { points = it }, label = "Coins earned on completion")

                    Text("Attach a game you built (optional)", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    if (games.isEmpty()) {
                        InfoBanner("🎮", "No games in your library yet — build one from the Create hub first.")
                    } else {
                        CatoChipRow(
                            options = listOf<CustomGame?>(null) + games,
                            selected = attachedGame,
                            label = { it?.title ?: "None — written answer instead" },
                            onSelect = { attachedGame = it },
                        )
                    }

                    InfoBanner(
                        "💡",
                        if (attachedGame != null) "Your class plays the game — no written submission needed."
                        else "Your class will write back an answer for you to review.",
                    )

                    Spacer(Modifier.height(8.dp))
                    CatoButton(
                        text = "Assign homework",
                        leading = "✅",
                        enabled = canSave,
                        emphasise = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val cid = classId ?: return@CatoButton
                            onSave(
                                Assignment(
                                    id = "",
                                    classId = cid,
                                    lessonId = null,
                                    assignedBy = profileId,
                                    dueDate = quickDueDate(dueOffset),
                                    note = null,
                                    title = title.trim(),
                                    type = AssignmentType.HOMEWORK,
                                    instructions = instructions.trim(),
                                    pointsReward = points,
                                    requiresSubmission = attachedGame == null,
                                    customGameId = attachedGame?.id,
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}
