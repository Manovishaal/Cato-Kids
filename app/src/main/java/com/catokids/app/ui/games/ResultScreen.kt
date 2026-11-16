package com.catokids.app.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.catokids.app.data.model.Craft
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import kotlin.math.absoluteValue
import kotlin.random.Random

@Composable
fun GameResultScreen(
    lessonTitle: String,
    score: Int,
    correct: Int,
    total: Int,
    stars: Int,
    seconds: Int,
    onPlayAgain: () -> Unit,
    onKeepLearning: () -> Unit,
    crafts: List<Craft> = emptyList(),
    /** Pre-KG: cartoon playground, reward shower and animated craft cards. */
    playful: Boolean = false,
) {
    CompositionLocalProvider(LocalCartoons provides playful) {
    ResultBackdrop(playful, stars) {
        if (stars >= 2) Confetti(Modifier.fillMaxSize())
        if (playful && stars >= 1) ConfettiBurst(iterations = 2)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (playful) {
                // Pre-KG: a trophy for a good finish, the dancing bear for "keep trying".
                CartoonAnimation(
                    if (stars >= 2) Cartoons.trophy else Cartoons.celebrations[1],
                    Modifier.size(132.dp),
                )
            } else {
                GooseCharacter(
                    modifier = Modifier.size(124.dp),
                    cheering = stars >= 2,
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                when {
                    stars == 3 -> "Perfect!"
                    stars == 2 -> "Great job!"
                    stars == 1 -> "Good try!"
                    else       -> "Let's practise"
                },
                style = MaterialTheme.typography.displaySmall,
                color = CatoPalette.Ink,
            )
            Text(
                lessonTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = CatoPalette.InkSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            // The sparkles may spill past the stars; only the stars take up room.
            Box(Modifier.height(64.dp), contentAlignment = Alignment.Center) {
                SparkleBurst(
                    trigger = stars.takeIf { it >= 2 },
                    modifier = Modifier.requiredSize(220.dp),
                )
                StarRow(stars, size = 50.dp)
            }
            Spacer(Modifier.height(20.dp))

            // What to do next comes straight after the stars, so it is on screen without scrolling.
            CatoButton(
                text = "Keep learning",
                leading = "📚",
                onClick = onKeepLearning,
                emphasise = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            CatoOutlineButton(
                text = "Play again",
                leading = "🔁",
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(20.dp))

            CatoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        // Percentages mean nothing to a 3-year-old; Pre-KG shows what they won.
                        if (!playful) CountingCell("🎯", score, "%", "Score")
                        ScoreCell("✅", "$correct/$total", if (playful) "First try" else "Correct")
                        CountingCell("⏱️", seconds, "s", "Time")
                        CountingCell("🪙", stars * 2 + correct, "", "Coins")
                    }
                }
            }

            if (crafts.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                if (playful) {
                    // Pre-KG: a child painting at an easel and real snipping scissors.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        CartoonAnimation(Cartoons.scissors, Modifier.size(48.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Craft time!",
                            style = MaterialTheme.typography.titleLarge,
                            color = CatoPalette.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        // This animation carries its own white background, so frame it as a little picture card.
                        CartoonAnimation(
                            Cartoons.painting,
                            Modifier
                                .height(90.dp)
                                .width(135.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        )
                    }
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    // The scissors snip back and forth to say "time to make something".
                    EmojiArt("✂️", size = 32.dp, modifier = Modifier.sway(degrees = 14f, periodMillis = 420))
                    Spacer(Modifier.width(8.dp))
                    Text("Craft time!", style = MaterialTheme.typography.titleLarge, color = CatoPalette.Ink)
                    Spacer(Modifier.width(8.dp))
                    EmojiArt("🖍️", size = 26.dp, modifier = Modifier.floaty(amplitude = 4.dp, periodMillis = 1400))
                }
                Text(
                    "Make these from your book with a grown-up.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.InkSoft,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                crafts.forEachIndexed { i, craft ->
                    PopIn(delayMillis = 300 + stagger(i, step = 120, max = 900)) {
                        CraftCard(craft, i)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

/** One workbook craft: picture, name, what to gather and the steps, readable by a parent. */
@Composable
private fun CraftCard(craft: Craft, index: Int) {
    val colour = PlayColours[index % PlayColours.size]
    var taps by remember { mutableStateOf(0) }
    CatoCard(
        Modifier
            .fillMaxWidth()
            .border(3.dp, colour, RoundedCornerShape(24.dp)),
        onClick = { taps++ },
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // The craft's picture sits on a slowly turning paint splat and rocks
                // gently; tapping the card makes it squash and pop.
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    PaintSplat(colour.copy(alpha = 0.55f), Modifier.fillMaxSize(), seed = index + 3)
                    EmojiArt(
                        craft.emoji,
                        size = 42.dp,
                        modifier = Modifier
                            .sway(degrees = 7f, periodMillis = 1500 + index * 120, phase = index)
                            .jelly(trigger = if (taps > 0) taps else null),
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    if (craft.letter.isNotBlank()) {
                        Text(
                            "${craft.letter} ${craft.letter.lowercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = CatoPalette.TealDeep,
                        )
                    }
                    Text(craft.title, style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                }
            }
            if (craft.materials.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "You need: " + craft.materials.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.InkSoft,
                )
            }
            Spacer(Modifier.height(8.dp))
            craft.steps.forEachIndexed { i, step ->
                Text(
                    "${i + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.Ink,
                )
            }
        }
    }
}

@Composable
private fun ResultBackdrop(playful: Boolean, stars: Int, content: @Composable BoxScope.() -> Unit) {
    if (playful) PlaygroundBackdrop(content = content)
    else CatoBackdrop(
        top = if (stars >= 2) CatoPalette.SuccessSoft.copy(alpha = 0.5f) else CatoPalette.AmberSoft,
        content = content,
    )
}

/** Same shape as [ScoreCell], but the number rolls up to its value. */
@Composable
private fun CountingCell(emoji: String, value: Int, suffix: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EmojiArt(emoji, size = 26.dp)
        Spacer(Modifier.height(4.dp))
        AnimatedCounter(
            value = value,
            style = MaterialTheme.typography.titleMedium,
            color = CatoPalette.Ink,
            suffix = suffix,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
    }
}

@Composable
private fun ScoreCell(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        EmojiArt(emoji, size = 26.dp)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
        Text(label, style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
    }
}

@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val pieces = remember {
        List(46) {
            ConfettiPiece(
                x = Random.nextFloat(),
                delay = Random.nextFloat(),
                size = 6f + Random.nextFloat() * 10f,
                color = listOf(
                    CatoPalette.Coral, CatoPalette.Amber, CatoPalette.Teal,
                    CatoPalette.Periwinkle, CatoPalette.Violet, CatoPalette.Gold,
                ).random(),
                spin = Random.nextFloat() * 4f - 2f,
            )
        }
    }
    val transition = rememberInfiniteTransition(label = "confetti")
    val t by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
        label = "fall",
    )

    Canvas(modifier) {
        pieces.forEach { p ->
            val progress = ((t + p.delay) % 1f)
            val y = progress * (size.height + 80f) - 40f
            val sway = kotlin.math.sin((progress * 8f + p.delay * 6f).toDouble()).toFloat() * 22f
            drawRect(
                color = p.color.copy(alpha = (1f - progress).coerceIn(0.15f, 1f)),
                topLeft = Offset(p.x * size.width + sway, y),
                size = Size(p.size, p.size * (1.4f + p.spin.absoluteValue * 0.2f)),
            )
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val delay: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
)
