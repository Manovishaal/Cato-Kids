package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.curriculum.CatoCurriculum
import com.catokids.app.data.model.ExtraCourse
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.SubjectId
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private val COVER_EMOJIS = listOf("📘", "🌟", "🦁", "🚀", "🎵", "🧩", "🏆", "🌈")

@Composable
fun ExtraCourseComposerScreen(
    profileId: String?,
    schoolId: String?,
    onSave: (ExtraCourse) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var coverEmoji by remember { mutableStateOf(COVER_EMOJIS.first()) }
    var grade by remember { mutableStateOf(Grade.LKG) }
    var subject by remember { mutableStateOf(SubjectId.LETTER_LAND) }
    val selectedLessonIds = remember { mutableStateListOf<String>() }

    val lessons = remember(grade, subject) { CatoCurriculum.forGradeAndSubject(grade, subject) }
    LaunchedEffect(grade, subject) { selectedLessonIds.clear() }

    val canSave = title.isNotBlank() && selectedLessonIds.isNotEmpty()

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "New extra course", subtitle = "🌟 An elective bundle", onBack = onBack)
            }
            item {
                Column(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CatoTextField(value = title, onValueChange = { title = it }, label = "Course title", leading = coverEmoji)
                    CatoMultilineField(
                        value = description, onValueChange = { description = it },
                        label = "Short description", minLines = 2,
                    )

                    Text("Cover", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = COVER_EMOJIS, selected = coverEmoji,
                        label = { it }, onSelect = { coverEmoji = it },
                    )

                    Text("Grade", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = Grade.entries, selected = grade,
                        label = { it.label }, emoji = { it.emoji }, onSelect = { grade = it },
                    )

                    Text("Book", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = SubjectId.entries, selected = subject,
                        label = { it.title }, emoji = { it.emoji }, onSelect = { subject = it },
                    )

                    Text(
                        "Pick lessons (${selectedLessonIds.size} chosen)",
                        style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink,
                    )
                }
            }
            items(lessons, key = { it.id }) { lesson ->
                val checked = lesson.id in selectedLessonIds
                Row(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (checked) CatoPalette.PeriwinkleSoft else CatoPalette.Cloud)
                        .clickable {
                            if (checked) selectedLessonIds.remove(lesson.id) else selectedLessonIds.add(lesson.id)
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EmojiArt(lesson.gameType.emoji, size = 22.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(lesson.title, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink, modifier = Modifier.weight(1f))
                    Text(if (checked) "✓" else "", color = CatoPalette.PeriwinkleDeep, style = MaterialTheme.typography.titleMedium)
                }
            }
            if (lessons.isEmpty()) {
                item { EmptyState("📭", "No lessons here yet", "Pick a different book or grade.") }
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Spacer(Modifier.height(10.dp))
                    CatoButton(
                        text = "Save course",
                        leading = "✅",
                        enabled = canSave,
                        emphasise = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSave(
                                ExtraCourse(
                                    id = "",
                                    createdBy = profileId,
                                    schoolId = schoolId,
                                    title = title.trim(),
                                    description = description.trim(),
                                    coverEmoji = coverEmoji,
                                    subject = subject,
                                    grade = grade,
                                    lessonIds = selectedLessonIds.toList(),
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}
