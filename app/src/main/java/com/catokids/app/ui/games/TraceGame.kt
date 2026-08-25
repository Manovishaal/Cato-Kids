package com.catokids.app.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catokids.app.data.model.GameRound
import com.catokids.app.ui.components.CatoButton
import com.catokids.app.ui.components.CatoOutlineButton
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.EmojiText
import com.catokids.app.ui.components.pulse
import com.catokids.app.ui.theme.CatoPalette

/**
 * Finger tracing over a large ghost glyph. We measure how much of the canvas the
 * child covered rather than doing strict path matching — at this age the point is
 * the motor practice, not pixel accuracy.
 */
@Composable
fun TraceGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
) {
    val strokes = remember(round.id) { mutableStateListOf<MutableList<Offset>>() }
    var current by remember(round.id) { mutableStateOf<MutableList<Offset>?>(null) }
    var covered by remember(round.id) { mutableStateOf(0) }
    var tick by remember(round.id) { mutableStateOf(0) }
    val visited = remember(round.id) { mutableSetOf<Long>() }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiText(
            round.prompt,
            style = MaterialTheme.typography.headlineSmall,
            color = CatoPalette.Ink,
        )
        if (round.emoji.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            EmojiArt(round.emoji, size = 50.dp)
        }
        Spacer(Modifier.height(14.dp))

        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            // Ghost glyph behind the drawing surface
            Text(
                round.glyph,
                style = TextStyle(fontSize = 190.sp, fontWeight = FontWeight.Black),
                color = CatoPalette.CoralSoft,
                modifier = Modifier.pulse(enabled = covered == 0, maxScale = 1.04f, periodMillis = 1500),
            )

            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(round.id) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                current = mutableListOf(offset)
                                strokes.add(current!!)
                            },
                            onDrag = { change, _ ->
                                current?.add(change.position)
                                tick++
                                val cell = ((change.position.x / 24).toInt().toLong() shl 32) or
                                    (change.position.y / 24).toInt().toLong()
                                if (visited.add(cell)) covered = visited.size
                                change.consume()
                            },
                            onDragEnd = { current = null },
                        )
                    },
            ) {
                @Suppress("UNUSED_EXPRESSION") tick   // read so drags redraw
                strokes.forEach { pts ->
                    if (pts.size > 1) {
                        val path = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            pts.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(
                            path,
                            color = CatoPalette.CoralDeep,
                            style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            when {
                covered == 0 -> "Put your finger on the letter and trace it"
                covered < 18 -> "Keep going…"
                else -> "Beautiful tracing!"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = CatoPalette.InkSoft,
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            CatoOutlineButton(
                text = "Clear",
                leading = "🧽",
                onClick = { strokes.clear(); visited.clear(); covered = 0; tick++ },
                modifier = Modifier.weight(1f),
            )
            CatoButton(
                text = "Done",
                leading = "✅",
                enabled = covered >= 10,
                onClick = { onDone(covered >= 18) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        CatoOutlineButton(
            text = "Hear it again",
            leading = "🔊",
            onClick = onSpeak,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
    }
}
