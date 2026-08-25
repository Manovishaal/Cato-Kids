package com.catokids.app.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
) {
    CatoBackdrop(top = if (stars >= 2) CatoPalette.SuccessSoft.copy(alpha = 0.5f) else CatoPalette.AmberSoft) {
        if (stars >= 2) Confetti(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GooseCharacter(
                modifier = Modifier.size(150.dp),
                cheering = stars >= 2,
            )
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
            Spacer(Modifier.height(18.dp))
            Box(contentAlignment = Alignment.Center) {
                SparkleBurst(
                    trigger = stars.takeIf { it >= 2 },
                    modifier = Modifier.size(240.dp),
                )
                StarRow(stars, size = 46.dp)
            }
            Spacer(Modifier.height(22.dp))

            CatoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CountingCell("🎯", score, "%", "Score")
                        ScoreCell("✅", "$correct/$total", "Correct")
                        CountingCell("⏱️", seconds, "s", "Time")
                        CountingCell("🪙", correct * 2, "", "Coins")
                    }
                    Spacer(Modifier.height(16.dp))
                    CatoProgressBar(if (total == 0) 0f else correct.toFloat() / total, Modifier.fillMaxWidth())
                }
            }

            Spacer(Modifier.height(26.dp))
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
        }
    }
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
