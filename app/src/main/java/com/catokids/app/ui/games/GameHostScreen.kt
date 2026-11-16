package com.catokids.app.ui.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.GameType
import com.catokids.app.data.model.Grade
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

@Composable
fun GameHostScreen(
    state: GameUiState,
    onSubmit: (Boolean) -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onSpeak: () -> Unit,
    onTap: () -> Unit,
    onExit: () -> Unit,
    /** Speaks any text — the 3D games say each picture's name when it's tapped. */
    onSay: (String) -> Unit = {},
    /** A wrong move inside a toy game (counted for reports, never shown to the child). */
    onMiss: () -> Unit = {},
) {
    val lesson = state.lesson
    val round = state.round

    if (state.loading) {
        CatoBackdrop {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                LoadingBlock(label = "Getting your game ready…")
            }
        }
        return
    }

    if (lesson == null || round == null) {
        CatoBackdrop {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                EmptyState("🤔", "Lesson not found", "Let's go back and pick another one.") {
                    CatoButton(text = "Back", onClick = onExit)
                }
            }
        }
        return
    }

    // Pre-KG gets the cartoon playground, the goose reading out the task, wobbly
    // pictures and a reward shower. Older grades keep the calmer look.
    val playful = lesson.grade == Grade.PREKG

    // Pre-KG prototype lessons play as real 3D games. One Filament engine per lesson,
    // shared by every round, so turning the page doesn't rebuild the 3D world.
    // Every Pre-KG lesson is a toy game.
    val toy = playful
    val is3D = toy && lesson.id in Models3D.lessons3D
    val scene3D = if (is3D) {
        val engine = io.github.sceneview.rememberEngine()
        val modelLoader = io.github.sceneview.rememberModelLoader(engine)
        val materialLoader = io.github.sceneview.rememberMaterialLoader(engine)
        remember(engine) { Scene3DContext(engine, modelLoader, materialLoader) }
    } else null

    if (playful) PreloadCartoons()
    CompositionLocalProvider(LocalCartoons provides playful) {
    GameBackdrop(playful) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(28.dp))
            CatoTopBar(
                title = lesson.title,
                subtitle = if (toy) toyFor(lesson).let { "${it.subtitle} · ${it.cue}" } else "${lesson.gameType.emoji} ${lesson.gameType.title}",
                onBack = onExit,
            ) {
                Text(
                    "${state.roundIndex + 1}/${state.totalRounds}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CatoPalette.InkSoft,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }
            CatoProgressBar(
                fraction = (state.roundIndex).toFloat() / state.totalRounds.coerceAtLeast(1),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                height = 10.dp,
                color = CatoPalette.CoralDeep,
            )
            Spacer(Modifier.height(10.dp))

            if (playful && lesson.content.intro.isNotBlank()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    MascotBubble(
                        // Toy games: the owl just names the toy, in kid words.
                        text = if (toy) toyFor(lesson).subtitle.substringAfter(' ') + "!" else lesson.content.intro,
                        cheering = state.feedback == Feedback.CORRECT,
                        modifier = Modifier.weight(1f),
                    )
                    if (toy) {
                        // A big "say it again" button, so a child who can't read is never stuck.
                        Box(
                            Modifier
                                .size(64.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(CatoPalette.AmberLight)
                                .clickable(onClickLabel = "Say it again", onClick = onSpeak),
                            contentAlignment = Alignment.Center,
                        ) { EmojiArt("🔊", size = 38.dp) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Box(Modifier.weight(1f)) {
                // The outgoing round leaves to the left as the next one arrives from the
                // right, so finishing a question feels like turning a page.
                AnimatedContent(
                    targetState = state.roundIndex,
                    transitionSpec = {
                        (fadeIn(tween(280, delayMillis = 90)) +
                            slideInHorizontally(tween(360, delayMillis = 90)) { it / 4 }) togetherWith
                            (fadeOut(tween(160)) + slideOutHorizontally(tween(260)) { -it / 4 })
                    },
                    label = "round",
                ) { index ->
                    val shown = lesson.content.rounds.getOrNull(index) ?: round
                    if (toy) ToyRound(lesson, shown, scene3D, onSubmit, onSay, onTap, onMiss)
                    else when (lesson.gameType) {
                        GameType.TRACE        -> TraceGame(shown, onSubmit, onSpeak)
                        GameType.TAP_ALL      -> TapAllGame(shown, onSubmit, onSpeak, onTap)
                        GameType.COUNT_TAP    -> CountTapGame(shown, onSubmit, onSpeak, onTap, playful)
                        GameType.MATCH_PAIRS  -> MatchPairsGame(shown, onSubmit, onSpeak, onTap)
                        GameType.SORT_BUCKETS -> SortBucketsGame(shown, onSubmit, onSpeak, onTap)
                        GameType.JUMBLED_WORD -> JumbledWordGame(shown, onSubmit, onSpeak, onTap)
                        GameType.LISTEN_PICK  -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = true, playful = playful)
                        GameType.SHAPE_HUNT   -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = true, playful = playful)
                        GameType.QUIZ         -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = false, playful = playful)
                    }
                }

                // Toy games never show a right/wrong sheet: they cheer and move on by themselves.
                if (toy) {
                    if (state.feedback != Feedback.NONE) {
                        LaunchedEffect(state.roundIndex) { kotlinx.coroutines.delay(1600); onNext() }
                    }
                } else FeedbackOverlay(
                    visible = state.feedback != Feedback.NONE,
                    correct = state.feedback == Feedback.CORRECT,
                    explanation = state.explanation,
                    isLast = state.roundIndex >= state.totalRounds - 1,
                    onNext = onNext,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )

                // Drawn last so the confetti bursts over the feedback sheet. It has no
                // touch handling, so the Next button underneath still works.
                if (playful && state.feedback == Feedback.CORRECT) {
                    key(state.roundIndex, state.correct) {
                        ConfettiBurst()
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun GameBackdrop(playful: Boolean, content: @Composable BoxScope.() -> Unit) {
    if (playful) PlaygroundBackdrop(content = content)
    else CatoBackdrop(top = CatoPalette.CoralTint, content = content)
}

/**
 * Kept in its own composable deliberately: inside a Box that sits in a Column, both
 * BoxScope and ColumnScope are implicit receivers, and Kotlin picks the ColumnScope
 * overload of AnimatedVisibility — which then can't be called. With no receiver in
 * scope here, the plain overload resolves cleanly and the caller passes the alignment in.
 */
@Composable
private fun FeedbackOverlay(
    visible: Boolean,
    correct: Boolean,
    explanation: String?,
    isLast: Boolean,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier,
    ) {
        FeedbackSheet(
            correct = correct,
            explanation = explanation,
            isLast = isLast,
            onNext = onNext,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun FeedbackSheet(
    correct: Boolean,
    explanation: String?,
    isLast: Boolean,
    onNext: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
            .background(if (correct) CatoPalette.SuccessSoft else CatoPalette.ErrorSoft)
            .padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center) {
            SparkleBurst(
                trigger = if (correct) explanation.orEmpty() + "ok" else null,
                modifier = Modifier.size(170.dp),
            )
            if (correct && LocalCartoons.current) {
                // Pre-KG: a dancing star, bear or jumping girl cheers instead of the goose.
                val pick = Cartoons.celebrations[(explanation.orEmpty().hashCode() and 0x7fffffff) % Cartoons.celebrations.size]
                CartoonAnimation(pick, Modifier.size(150.dp))
            } else GooseCharacter(
                modifier = Modifier
                    .size(94.dp)
                    // She jumps for a right answer and shakes her head for a wrong one.
                    .hop(trigger = if (correct) explanation.orEmpty() + "yes" else null)
                    .wiggle(trigger = if (correct) null else explanation.orEmpty() + "no"),
                cheering = correct,
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (correct) "Correct answer!" else "Not quite…",
            style = MaterialTheme.typography.headlineSmall,
            color = if (correct) CatoPalette.SuccessDeep else CatoPalette.Error,
        )
        if (!explanation.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(14.dp),
            ) {
                Text(
                    explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CatoPalette.Ink,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!correct) {
                CatoOutlineButton(
                    text = "Try again",
                    leading = "🔁",
                    onClick = onRetry,
                    color = CatoPalette.Error,
                    modifier = Modifier.weight(1f),
                )
            }
            CatoButton(
                text = if (isLast) "See my score" else "Next",
                leading = if (isLast) "🏁" else "➡️",
                onClick = onNext,
                color = if (correct) CatoPalette.SuccessDeep else CatoPalette.Coral,
                emphasise = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
