package com.catokids.app.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.catokids.app.ui.theme.CatoPalette

/**
 * Tap-left-then-tap-right matching. Deliberately not drag-and-drop: small fingers
 * are far more reliable at tapping, and a mis-drag is frustrating at this age.
 */
@Composable
fun MatchPairsGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
) {
    val lefts = remember(round.id) { round.pairs.map { it.left } }
    val rights = remember(round.id) { round.pairs.map { it.right }.shuffled(kotlin.random.Random(round.id.hashCode().toLong())) }

    val matched = remember(round.id) { mutableStateListOf<String>() }
    var selectedLeft by remember(round.id) { mutableStateOf<String?>(null) }
    var mistakes by remember(round.id) { mutableStateOf(0) }
    var shakeRight by remember(round.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(matched.size) {
        if (matched.size == lefts.size && lefts.isNotEmpty()) onDone(mistakes <= 1)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        EmojiText(round.prompt, style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap one on the left, then its partner on the right.",
            style = MaterialTheme.typography.bodyMedium,
            color = CatoPalette.InkSoft,
        )
        Spacer(Modifier.height(10.dp))
        CatoOutlineButton(text = "Hear it", leading = "🔊", onClick = onSpeak)
        Spacer(Modifier.height(18.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                lefts.forEach { left ->
                    val pair = round.pairs.first { it.left == left }
                    val isMatched = left in matched
                    MatchTile(
                        text = left,
                        emoji = pair.emoji,
                        selected = selectedLeft == left,
                        matched = isMatched,
                        onClick = { if (!isMatched) { selectedLeft = left; onTap() } },
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rights.forEach { right ->
                    val owner = round.pairs.first { it.right == right }.left
                    val isMatched = owner in matched
                    MatchTile(
                        text = right,
                        emoji = "",
                        selected = false,
                        matched = isMatched,
                        shake = shakeRight == right,
                        onClick = {
                            val l = selectedLeft
                            if (l != null) {
                                onTap()
                                if (round.pairs.first { it.left == l }.right == right) {
                                    matched.add(l)
                                    selectedLeft = null
                                    shakeRight = null
                                } else {
                                    mistakes++
                                    shakeRight = right
                                    selectedLeft = null
                                }
                            }
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Matched ${matched.size} of ${lefts.size}",
            style = MaterialTheme.typography.titleMedium,
            color = CatoPalette.TealDeep,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MatchTile(
    text: String,
    emoji: String,
    selected: Boolean,
    matched: Boolean,
    shake: Boolean = false,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(if (selected) 1.05f else if (shake) 0.95f else 1f, spring(), label = "tile")
    Row(
        Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(if (matched) 0.45f else 1f)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    matched  -> CatoPalette.SuccessSoft
                    selected -> CatoPalette.CoralTint
                    shake    -> CatoPalette.ErrorSoft
                    else     -> Color.White
                }
            )
            .border(
                3.dp,
                if (selected) CatoPalette.Coral else CatoPalette.Line,
                RoundedCornerShape(18.dp),
            )
            .clickable(enabled = !matched) { onClick() }
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji.isNotBlank()) {
            EmojiArt(emoji, size = 26.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            style = if (text.length <= 3) MaterialTheme.typography.headlineSmall
                    else MaterialTheme.typography.titleMedium,
            color = CatoPalette.Ink,
        )
    }
}

/** Sorting into labelled baskets — tap an item, then tap its basket. */
@Composable
fun SortBucketsGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
) {
    val placed = remember(round.id) { mutableStateMapOf<String, String>() }
    var selected by remember(round.id) { mutableStateOf<String?>(null) }
    val remaining = round.items.filter { it.label !in placed.keys }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        EmojiText(round.prompt, style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
        Spacer(Modifier.height(8.dp))
        CatoOutlineButton(text = "Hear it", leading = "🔊", onClick = onSpeak)
        Spacer(Modifier.height(16.dp))

        Text("Pick a picture", style = MaterialTheme.typography.titleMedium, color = CatoPalette.InkSoft)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            remaining.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { item ->
                        val isSelected = selected == item.label
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) CatoPalette.CoralTint else Color.White)
                                .border(3.dp, if (isSelected) CatoPalette.Coral else CatoPalette.Line, RoundedCornerShape(18.dp))
                                .clickable { selected = item.label; onTap() }
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            EmojiArt(item.emoji, size = 40.dp)
                            EmojiText(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = CatoPalette.InkSoft,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            round.buckets.forEach { bucket ->
                val contents = placed.filterValues { it == bucket }.keys
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 140.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(CatoPalette.Cloud)
                        .border(3.dp, if (selected != null) CatoPalette.Teal else Color.Transparent, RoundedCornerShape(22.dp))
                        .clickable(enabled = selected != null) {
                            selected?.let { placed[it] = bucket; selected = null; onTap() }
                        }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmojiArt("🧺", size = 26.dp)
                    Text(bucket, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    Spacer(Modifier.height(6.dp))
                    contents.forEach { label ->
                        val item = round.items.first { it.label == label }
                        EmojiText("${item.emoji} $label", style = MaterialTheme.typography.bodySmall, color = CatoPalette.InkSoft)
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))
        CatoButton(
            text = "Check my baskets",
            leading = "✅",
            enabled = placed.size == round.items.size,
            onClick = {
                val allRight = round.items.all { placed[it.label] == it.bucket }
                onDone(allRight)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}
