package com.catokids.app.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.GameRound
import com.catokids.app.ui.components.CatoButton
import com.catokids.app.ui.components.CatoOutlineButton
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.EmojiText
import com.catokids.app.ui.components.hop
import com.catokids.app.ui.theme.CatoPalette

/** Build the word by tapping letters in order. */
@Composable
fun JumbledWordGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
) {
    val target = remember(round.id) { round.target.uppercase() }
    val tiles = remember(round.id) {
        target.toList().shuffled(kotlin.random.Random(round.id.hashCode().toLong() + 7))
            .mapIndexed { i, c -> i to c }
    }
    val used = remember(round.id) { mutableStateListOf<Int>() }
    val built = used.joinToString("") { tiles[it].second.toString() }
    var attempts by remember(round.id) { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiArt(round.emoji, size = 64.dp)
        Spacer(Modifier.height(6.dp))
        EmojiText(
            round.prompt,
            style = MaterialTheme.typography.titleMedium,
            color = CatoPalette.InkSoft,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        CatoOutlineButton(text = "Hear the word", leading = "🔊", onClick = onSpeak)
        Spacer(Modifier.height(24.dp))

        // answer slots
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            target.indices.forEach { i ->
                val ch = built.getOrNull(i)
                Box(
                    Modifier
                        .size(58.dp)
                        .hop(trigger = ch, height = 7.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (ch != null) CatoPalette.CoralTint else Color.White)
                        .border(3.dp, if (ch != null) CatoPalette.Coral else CatoPalette.Line, RoundedCornerShape(16.dp))
                        .clickable(enabled = used.isNotEmpty() && i == built.length - 1) {
                            used.removeAt(used.lastIndex); onTap()
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ch?.toString() ?: "", style = MaterialTheme.typography.displaySmall, color = CatoPalette.Ink)
                }
            }
        }

        Spacer(Modifier.height(34.dp))
        Text("Tap the letters in order", style = MaterialTheme.typography.bodyMedium, color = CatoPalette.InkSoft)
        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            tiles.forEach { (idx, ch) ->
                val isUsed = idx in used
                val scale by animateFloatAsState(if (isUsed) 0.85f else 1f, spring(), label = "tile$idx")
                Box(
                    Modifier
                        .size(58.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isUsed) CatoPalette.Cloud else CatoPalette.AmberLight)
                        .clickable(enabled = !isUsed) { used.add(idx); onTap() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (isUsed) "" else ch.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = CatoPalette.Ink,
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CatoOutlineButton(
                text = "Clear",
                leading = "↩️",
                onClick = { used.clear(); onTap() },
                modifier = Modifier.weight(1f),
            )
            CatoButton(
                text = "Check",
                leading = "✅",
                enabled = built.length == target.length,
                onClick = { attempts++; onDone(built == target) },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}
