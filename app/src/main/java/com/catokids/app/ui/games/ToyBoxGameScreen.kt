package com.catokids.app.ui.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette
import kotlinx.coroutines.delay

@Composable
fun ToyBoxGameScreen(
    state: ToyBoxUiState,
    onStart: () -> Unit,
    onChoose: (Int) -> Unit,
    onWrongFeedbackShown: () -> Unit,
    onNextRound: () -> Unit,
    onOpenChest: () -> Unit,
    onLaunchRocket: () -> Unit,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
) {
    CatoBackdrop(top = CatoPalette.AmberSoft) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(38.dp))
            CatoTopBar(title = "Cato's Toy Box", onBack = onExit) {
                if (state.coinsEarned > 0) {
                    Row(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CatoPalette.AmberSoft)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmojiArt("🪙", size = 18.dp)
                        Spacer(Modifier.width(4.dp))
                        Text("+${state.coinsEarned}", style = MaterialTheme.typography.labelMedium, color = CatoPalette.Ink)
                    }
                }
            }

            if (state.phase == ToyBoxPhase.ROUND) {
                CatoProgressBar(
                    fraction = state.roundIndex.toFloat() / state.totalRounds.coerceAtLeast(1),
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    height = 8.dp,
                )
                Spacer(Modifier.height(10.dp))
            }

            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.phase,
                    transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(180)) },
                    label = "toybox-phase",
                ) { phase ->
                    when (phase) {
                        ToyBoxPhase.INTRO -> IntroStep(onStart)
                        ToyBoxPhase.ROUND -> RoundStep(
                            state = state,
                            onChoose = onChoose,
                            onWrongFeedbackShown = onWrongFeedbackShown,
                            onNextRound = onNextRound,
                        )
                        ToyBoxPhase.CHEST -> ChestStep(state = state, onOpenChest = onOpenChest, onLaunchRocket = onLaunchRocket)
                        ToyBoxPhase.ROCKET -> RocketStep(state = state, onPlayAgain = onPlayAgain, onExit = onExit)
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroStep(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PopIn {
            CatoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(CatoPalette.CoralSoft),
                    ) {
                        Model3DView(
                            modelAsset = "cato",
                            background = CatoPalette.CoralSoft,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Hi, I'm Cato!", style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Spin my toys, match their shapes, and let's open my treasure chest together!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CatoPalette.InkSoft,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))
                    CatoButton(text = "Let's play!", leading = "🧸", onClick = onStart, emphasise = true, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun RoundStep(
    state: ToyBoxUiState,
    onChoose: (Int) -> Unit,
    onWrongFeedbackShown: () -> Unit,
    onNextRound: () -> Unit,
) {
    val round = state.round ?: return
    LaunchedEffect(state.feedback) {
        if (state.feedback == ToyAnswerFeedback.WRONG) {
            delay(850)
            onWrongFeedbackShown()
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        CatoCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    round.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = CatoPalette.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CatoPalette.SkySoft),
                ) {
                    Model3DView(
                        modelAsset = round.modelAsset,
                        spotlightPart = round.spotlightPart,
                        background = CatoPalette.SkySoft,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            round.choices.chunked(2).forEachIndexed { rowIndex, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEachIndexed { colIndex, choice ->
                        val index = rowIndex * 2 + colIndex
                        ToyChoiceTile(
                            choice = choice,
                            state = when {
                                state.feedback == ToyAnswerFeedback.CORRECT && index == round.correctIndex -> ToyTileState.CORRECT
                                state.feedback == ToyAnswerFeedback.WRONG && index == state.selectedIndex -> ToyTileState.WRONG
                                else -> ToyTileState.IDLE
                            },
                            enabled = state.feedback != ToyAnswerFeedback.CORRECT,
                            modifier = Modifier.weight(1f),
                            onClick = { onChoose(index) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (state.feedback == ToyAnswerFeedback.CORRECT) {
            PopIn {
                CatoButton(
                    text = if (state.roundIndex >= state.totalRounds - 1) "Open the chest!" else "Next toy",
                    leading = "➡️",
                    onClick = onNextRound,
                    color = CatoPalette.SuccessDeep,
                    emphasise = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private enum class ToyTileState { IDLE, CORRECT, WRONG }

@Composable
private fun ToyChoiceTile(
    choice: ToyChoice,
    state: ToyTileState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = when (state) {
        ToyTileState.CORRECT -> CatoPalette.SuccessSoft
        ToyTileState.WRONG -> CatoPalette.ErrorSoft
        ToyTileState.IDLE -> Color.White
    }
    Column(
        modifier
            .wiggle(trigger = if (state == ToyTileState.WRONG) choice else null)
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (choice.emoji != null) {
            EmojiArt(choice.emoji, size = 34.dp)
        } else {
            Text(
                choice.label,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = CatoPalette.CoralDeep,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(choice.label, style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
    }
}

@Composable
private fun ChestStep(state: ToyBoxUiState, onOpenChest: () -> Unit, onLaunchRocket: () -> Unit) {
    val lidOpen = remember { Animatable(0f) }
    LaunchedEffect(state.chestOpened) {
        if (state.chestOpened) lidOpen.animateTo(1f, tween(1300, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (state.chestOpened) "You found the treasure!" else "All sorted! Tap the chest to open it.",
            style = MaterialTheme.typography.headlineSmall,
            color = CatoPalette.Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CatoPalette.AmberSoft)
                    .pulse(enabled = !state.chestOpened),
            ) {
                Model3DView(
                    modelAsset = "chest",
                    background = CatoPalette.AmberSoft,
                    lidOpenFraction = lidOpen.value,
                    autoRotate = state.chestOpened,
                    onTap = if (!state.chestOpened) onOpenChest else null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            SparkleBurst(trigger = if (state.chestOpened) "chest" else null, modifier = Modifier.size(280.dp))
        }
        Spacer(Modifier.height(18.dp))
        if (state.chestOpened) {
            AnimatedCounter(value = state.coinsEarned, suffix = " coins", style = MaterialTheme.typography.displaySmall, color = CatoPalette.CoralDeep)
            Spacer(Modifier.height(16.dp))
            CatoButton(text = "Blast off!", leading = "🚀", onClick = onLaunchRocket, emphasise = true)
        }
    }
}

@Composable
private fun RocketStep(state: ToyBoxUiState, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    val liftoff = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(450)
        liftoff.animateTo(1f, tween(1700, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(220.dp)
                    .graphicsLayer {
                        translationY = -liftoff.value * 420f
                        alpha = 1f - liftoff.value * 0.35f
                    }
                    .clip(RoundedCornerShape(28.dp))
                    .background(CatoPalette.SkySoft),
            ) {
                Model3DView(
                    modelAsset = "rocket",
                    background = CatoPalette.SkySoft,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            SparkleBurst(trigger = "launch", modifier = Modifier.size(300.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Great job, toy sorter!", style = MaterialTheme.typography.headlineSmall, color = CatoPalette.Ink)
        Spacer(Modifier.height(6.dp))
        Text(
            "You earned ${state.coinsEarned} coins today 🪙",
            style = MaterialTheme.typography.bodyMedium,
            color = CatoPalette.InkSoft,
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CatoOutlineButton(text = "Play again", leading = "🔁", onClick = onPlayAgain)
            CatoButton(text = "Back to games", leading = "🏠", onClick = onExit, color = CatoPalette.SuccessDeep)
        }
    }
}
