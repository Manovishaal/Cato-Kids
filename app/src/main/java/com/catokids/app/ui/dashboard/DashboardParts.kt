package com.catokids.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.ClassRoom
import com.catokids.app.data.model.StudentSummary
import com.catokids.app.ui.components.Avatar
import com.catokids.app.ui.components.CatoCard
import com.catokids.app.ui.components.CatoProgressBar
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.PopIn
import com.catokids.app.ui.theme.CatoPalette
import com.catokids.app.ui.theme.LocalRoleColors

@Composable
fun MetricCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalRoleColors.current.soft,
) {
    PopIn(modifier = modifier, delayMillis = 70, rise = 10.dp) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(tint)
                .padding(16.dp),
        ) {
            EmojiArt(emoji, size = 26.dp)
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = CatoPalette.Ink)
            Text(label, style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
        }
    }
}

@Composable
fun StudentRow(
    student: StudentSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CatoCard(modifier = modifier, onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(student.profile.initials, size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(student.profile.fullName, style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                Text(
                    "${student.profile.grade?.label ?: "—"} · ${student.lessonsCompleted}/${student.totalLessons} lessons · ${student.lastActiveLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
                Spacer(Modifier.height(8.dp))
                CatoProgressBar(student.completionPercent / 100f, Modifier.fillMaxWidth(), height = 7.dp)
            }
            Spacer(Modifier.width(12.dp))
            ScoreBadge(student.averageScore)
        }
    }
}

@Composable
fun ScoreBadge(score: Int) {
    val color = when {
        score >= 85 -> CatoPalette.Success
        score >= 70 -> CatoPalette.Amber
        score > 0   -> CatoPalette.Error
        else        -> CatoPalette.Line
    }
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (score == 0) "—" else "$score",
            style = MaterialTheme.typography.titleSmall,
            color = color,
        )
    }
}

@Composable
fun ClassCard(
    classRoom: ClassRoom,
    students: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalRoleColors.current
    CatoCard(modifier = modifier, onClick = onClick) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.soft),
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt(classRoom.grade.emoji, size = 30.dp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(classRoom.name, style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                Text(
                    "${classRoom.grade.longLabel} · $students students",
                    style = MaterialTheme.typography.bodySmall,
                    color = CatoPalette.InkSoft,
                )
            }
            Text("›", style = MaterialTheme.typography.headlineSmall, color = CatoPalette.InkSoft)
        }
    }
}

/** Simple bar chart — no charting dependency, no external assets. */
@Composable
fun MiniBarChart(
    values: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = CatoPalette.Periwinkle,
) {
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            if (values.isEmpty()) return@Canvas
            val max = (values.maxOf { it.second }).coerceAtLeast(1)
            val slot = size.width / values.size
            val barW = slot * 0.5f
            values.forEachIndexed { i, (_, v) ->
                val h = (v.toFloat() / max) * (size.height - 12f)
                drawRoundRect(
                    color = barColor.copy(alpha = 0.25f),
                    topLeft = Offset(i * slot + (slot - barW) / 2f, 0f),
                    size = Size(barW, size.height - 12f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f),
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(i * slot + (slot - barW) / 2f, size.height - 12f - h),
                    size = Size(barW, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW / 2f),
                )
            }
            drawLine(
                CatoPalette.Line,
                Offset(0f, size.height - 6f),
                Offset(size.width, size.height - 6f),
                strokeWidth = 2f,
                cap = StrokeCap.Round,
            )
        }
        Row(Modifier.fillMaxWidth()) {
            values.forEach { (label, _) ->
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = CatoPalette.InkSoft,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun DonutStat(
    percent: Int,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = CatoPalette.Teal,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(104.dp)) {
                val stroke = Stroke(width = 18f, cap = StrokeCap.Round)
                drawArc(
                    color = CatoPalette.Cloud, startAngle = -90f, sweepAngle = 360f,
                    useCenter = false, style = stroke,
                    topLeft = Offset(9f, 9f),
                    size = Size(size.width - 18f, size.height - 18f),
                )
                drawArc(
                    color = color, startAngle = -90f, sweepAngle = 360f * (percent / 100f),
                    useCenter = false, style = stroke,
                    topLeft = Offset(9f, 9f),
                    size = Size(size.width - 18f, size.height - 18f),
                )
            }
            Text("$percent%", style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
    }
}
