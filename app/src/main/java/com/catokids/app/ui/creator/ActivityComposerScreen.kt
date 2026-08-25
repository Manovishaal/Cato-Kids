package com.catokids.app.ui.creator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Activity
import com.catokids.app.data.model.ActivityType
import com.catokids.app.data.model.Grade
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun ActivityComposerScreen(
    profileId: String?,
    schoolId: String?,
    onSave: (Activity) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ActivityType.CREATIVE) }
    var grade by remember { mutableStateOf<Grade?>(null) }
    var points by remember { mutableStateOf(15) }

    val canSave = title.isNotBlank() && instructions.isNotBlank()

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "New activity", subtitle = "🎨 A screen-free task", onBack = onBack)
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CatoTextField(value = title, onValueChange = { title = it }, label = "Title", leading = "🎨")
                    CatoMultilineField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = "What should they do?",
                        placeholder = "Cut, colour and staple a paper crown…",
                        minLines = 4,
                    )

                    Text("Kind of activity", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = ActivityType.entries,
                        selected = type,
                        label = { it.label },
                        emoji = { it.emoji },
                        onSelect = { type = it },
                    )

                    Text("Grade (optional)", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = listOf<Grade?>(null) + Grade.entries,
                        selected = grade,
                        label = { it?.label ?: "Any grade" },
                        onSelect = { grade = it },
                    )

                    CatoStepper(value = points, onValueChange = { points = it }, label = "Coins earned on completion")

                    InfoBanner("📸", "Children will write back what they did so you can review it.")

                    Spacer(Modifier.height(8.dp))
                    CatoButton(
                        text = "Save to my library",
                        leading = "✅",
                        enabled = canSave,
                        emphasise = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSave(
                                Activity(
                                    id = "",
                                    createdBy = profileId,
                                    schoolId = schoolId,
                                    title = title.trim(),
                                    instructions = instructions.trim(),
                                    activityType = type,
                                    grade = grade,
                                    pointsReward = points,
                                )
                            )
                        },
                    )
                    InfoBanner("👉", "Saved activities show up in the Create hub, where you can assign them to a class.", color = CatoPalette.SkySoft)
                }
            }
        }
    }
}
