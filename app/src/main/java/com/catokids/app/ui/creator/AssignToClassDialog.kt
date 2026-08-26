package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.Assignment
import com.catokids.app.data.model.AssignmentType
import com.catokids.app.data.model.ClassRoom
import com.catokids.app.ui.components.CatoButton
import com.catokids.app.ui.components.CatoOutlineButton
import com.catokids.app.ui.theme.CatoPalette
import java.util.Calendar

/**
 * Shared "assign this to a class" flow — used by the creator hub for content a teacher
 * built themselves, and by the teacher training library for a bundled [LibraryActivity]
 * a teacher is assigning as-is. Both just need to describe what they're assigning as an
 * [AssignRequest]; this dialog turns the teacher's pick into a real [Assignment].
 */
data class AssignRequest(
    val title: String,
    val type: AssignmentType,
    val customGameId: String? = null,
    val courseId: String? = null,
    val activityId: String? = null,
    val pointsReward: Int = 10,
    val instructions: String = "",
)

@Composable
fun AssignToClassDialog(
    request: AssignRequest,
    classes: List<ClassRoom>,
    profileId: String?,
    onConfirm: (Assignment) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedClass by remember { mutableStateOf(classes.firstOrNull()?.id) }
    var dueOffset by remember { mutableStateOf<Int?>(7) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign \"${request.title}\"") },
        text = {
            Column {
                Text("Class", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                Spacer(Modifier.height(6.dp))
                if (classes.isEmpty()) {
                    Text("No classes yet.", style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                }
                classes.forEach { c ->
                    PickRow(text = "${c.name} · ${c.grade.label}", selected = selectedClass == c.id) { selectedClass = c.id }
                }
                Spacer(Modifier.height(14.dp))
                Text("Due", style = MaterialTheme.typography.labelLarge, color = CatoPalette.InkSoft)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("No date" to null, "3 days" to 3, "1 week" to 7, "2 weeks" to 14).forEach { (label, days) ->
                        DueChip(label, selected = dueOffset == days) { dueOffset = days }
                    }
                }
            }
        },
        confirmButton = {
            CatoButton(
                text = "Assign",
                enabled = selectedClass != null,
                onClick = {
                    val classId = selectedClass ?: return@CatoButton
                    onConfirm(
                        Assignment(
                            id = "",
                            classId = classId,
                            lessonId = null,
                            assignedBy = profileId,
                            dueDate = quickDueDate(dueOffset),
                            note = null,
                            title = request.title,
                            type = request.type,
                            instructions = request.instructions,
                            pointsReward = request.pointsReward,
                            requiresSubmission = request.type == AssignmentType.ACTIVITY,
                            customGameId = request.customGameId,
                            courseId = request.courseId,
                            activityId = request.activityId,
                        )
                    )
                },
            )
        },
        dismissButton = { CatoOutlineButton(text = "Cancel", onClick = onDismiss) },
    )
}

fun quickDueDate(daysFromNow: Int?): String? {
    if (daysFromNow == null) return null
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, daysFromNow)
    return String.format(
        "%04d-%02d-%02d",
        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
    )
}

@Composable
fun PickRow(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(if (selected) CatoPalette.PeriwinkleSoft else Color(0x00000000))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (selected) "●" else "○", color = CatoPalette.PeriwinkleDeep)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink)
    }
}

@Composable
fun DueChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) CatoPalette.Periwinkle else CatoPalette.Cloud)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (selected) Color.White else CatoPalette.Ink)
    }
}
