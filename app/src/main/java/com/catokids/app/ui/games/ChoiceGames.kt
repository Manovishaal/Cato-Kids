package com.catokids.app.ui.games

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.catokids.app.data.model.Option
import com.catokids.app.ui.components.bounceOnPress
import com.catokids.app.ui.components.CatoButton
import com.catokids.app.ui.components.CatoOutlineButton
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.EmojiText
import com.catokids.app.ui.components.floaty
import com.catokids.app.ui.components.hop
import com.catokids.app.ui.components.PopIn
import com.catokids.app.ui.components.stagger
import com.catokids.app.ui.theme.CatoPalette

/**
 * One picker used by Listen-and-pick, Shape hunt and Quiz — the interaction is the
 * same (choose the one right answer), only the framing changes.
 */
@Composable
fun PickOneGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    showSpeaker: Boolean = true,
) {
    var chosen by remember(round.id) { mutableStateOf<Int?>(null) }
    val options = remember(round.id) { round.options }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiText(
            round.prompt,
            style = MaterialTheme.typography.headlineSmall,
            color = CatoPalette.Ink,
            textAlign = TextAlign.Center,
        )
        if (showSpeaker) {
            Spacer(Modifier.height(10.dp))
            CatoOutlineButton(text = "Hear it", leading = "🔊", onClick = onSpeak)
        }
        Spacer(Modifier.height(20.dp))

        val cols = if (options.any { it.label.length > 6 }) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            options.chunked(cols).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { opt ->
                        val index = options.indexOf(opt)
                        PopIn(
                            modifier = Modifier.weight(1f),
                            delayMillis = stagger(index, step = 70),
                            resetKey = round.id,
                        ) {
                            OptionCard(
                                option = opt,
                                selected = chosen == index,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { chosen = index },
                            )
                        }
                    }
                    if (rowItems.size < cols) Spacer(Modifier.weight((cols - rowItems.size).toFloat()))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        CatoButton(
            text = "Check my answer",
            leading = "✅",
            enabled = chosen != null,
            onClick = { onDone(options.getOrNull(chosen ?: -1)?.correct == true) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OptionCard(
    option: Option,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow),
        label = "opt",
    )
    val border by animateColorAsState(if (selected) CatoPalette.Coral else CatoPalette.Line, label = "border")
    Column(
        modifier
            .scale(scale)
            .bounceOnPress(interaction)
            .heightIn(min = 108.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) CatoPalette.CoralTint else Color.White)
            .border(3.dp, border, RoundedCornerShape(22.dp))
            .clickable(interactionSource = interaction, indication = LocalIndication.current) { onClick() }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (option.emoji.isNotBlank()) {
            EmojiArt(option.emoji, size = 50.dp)
            Spacer(Modifier.height(6.dp))
        }
        EmojiText(
            option.label,
            style = if (option.label.length <= 3) MaterialTheme.typography.displaySmall
                    else MaterialTheme.typography.titleMedium,
            color = CatoPalette.Ink,
            textAlign = TextAlign.Center,
        )
    }
}

/** "Circle all the letter L" — tap every matching tile on the board. */
@Composable
fun TapAllGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
) {
    val picked = remember(round.id) { mutableStateListOf<Int>() }
    val total = remember(round.id) { round.options.count { it.correct } }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiText(round.prompt, style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            "Found ${picked.count { round.options[it].correct }} of $total",
            style = MaterialTheme.typography.bodyMedium,
            color = CatoPalette.InkSoft,
        )
        Spacer(Modifier.height(10.dp))
        CatoOutlineButton(text = "Hear it", leading = "🔊", onClick = onSpeak)
        Spacer(Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(round.options.size) { i ->
                val opt = round.options[i]
                val isPicked = i in picked
                val bg = when {
                    isPicked && opt.correct -> CatoPalette.SuccessSoft
                    isPicked                -> CatoPalette.ErrorSoft
                    else                    -> Color.White
                }
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .hop(trigger = if (isPicked) i else null, height = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .border(2.dp, if (isPicked) Color.Transparent else CatoPalette.Line, RoundedCornerShape(18.dp))
                        .clickable(enabled = !isPicked) { picked.add(i); onTap() },
                    contentAlignment = Alignment.Center,
                ) {
                    EmojiText(opt.label, style = MaterialTheme.typography.displaySmall, color = CatoPalette.Ink)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        CatoButton(
            text = "I found them all",
            leading = "🎉",
            enabled = picked.isNotEmpty(),
            onClick = {
                val hits = picked.count { round.options[it].correct }
                val misses = picked.count { !round.options[it].correct }
                onDone(hits >= total && misses == 0)
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
    }
}

/** Count the pictures, then tap the matching number. */
@Composable
fun CountTapGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
) {
    var chosen by remember(round.id) { mutableStateOf<String?>(null) }
    var countedTo by remember(round.id) { mutableStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmojiText(round.prompt, style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        CatoOutlineButton(text = "Hear it", leading = "🔊", onClick = onSpeak)
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            FlowRowSimple(items = round.count) { i ->
                val tapped = i < countedTo
                val objectScale by animateFloatAsState(
                    targetValue = if (tapped) 0.78f else 1f,
                    animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessLow),
                    label = "count$i",
                )
                EmojiArt(
                    round.emoji,
                    size = 40.dp,
                    modifier = Modifier
                        .padding(6.dp)
                        .scale(objectScale)
                        .floaty(
                            amplitude = if (tapped) 0.dp else 2.dp,
                            periodMillis = 2200,
                            phase = i * 0.7f,
                        )
                        .clickable {
                            if (i == countedTo) { countedTo++; onTap() }
                        },
                )
            }
        }
        if (countedTo > 0) {
            Spacer(Modifier.height(8.dp))
            Text("You counted $countedTo", style = MaterialTheme.typography.titleMedium, color = CatoPalette.TealDeep)
        }

        Spacer(Modifier.height(20.dp))
        Text("How many?", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            round.options.forEach { opt ->
                val selected = chosen == opt.label
                Box(
                    Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selected) CatoPalette.Teal else Color.White)
                        .border(3.dp, if (selected) CatoPalette.Teal else CatoPalette.Line, RoundedCornerShape(22.dp))
                        .clickable { chosen = opt.label },
                    contentAlignment = Alignment.Center,
                ) {
                    EmojiText(
                        opt.label,
                        style = MaterialTheme.typography.displaySmall,
                        color = if (selected) Color.White else CatoPalette.Ink,
                    )
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        CatoButton(
            text = "Check my answer",
            leading = "✅",
            enabled = chosen != null,
            onClick = { onDone(round.options.firstOrNull { it.label == chosen }?.correct == true) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** A minimal wrapping row — avoids depending on the experimental FlowRow API. */
@Composable
private fun FlowRowSimple(items: Int, perRow: Int = 5, content: @Composable (Int) -> Unit) {
    Column {
        (0 until items).chunked(perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                row.forEach { content(it) }
            }
        }
    }
}
