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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.GameType
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

    CatoBackdrop(top = CatoPalette.CoralTint) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(38.dp))
            CatoTopBar(
                title = lesson.title,
                subtitle = "${lesson.gameType.emoji} ${lesson.gameType.title}",
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
                height = 8.dp,
            )
            Spacer(Modifier.height(14.dp))

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
                    when (lesson.gameType) {
                        GameType.TRACE        -> TraceGame(shown, onSubmit, onSpeak)
                        GameType.TAP_ALL      -> TapAllGame(shown, onSubmit, onSpeak, onTap)
                        GameType.COUNT_TAP    -> CountTapGame(shown, onSubmit, onSpeak, onTap)
                        GameType.MATCH_PAIRS  -> MatchPairsGame(shown, onSubmit, onSpeak, onTap)
                        GameType.SORT_BUCKETS -> SortBucketsGame(shown, onSubmit, onSpeak, onTap)
                        GameType.JUMBLED_WORD -> JumbledWordGame(shown, onSubmit, onSpeak, onTap)
                        GameType.LISTEN_PICK  -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = true)
                        GameType.SHAPE_HUNT   -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = true)
                        GameType.QUIZ         -> PickOneGame(shown, onSubmit, onSpeak, showSpeaker = false)
                    }
                }

                FeedbackOverlay(
                    visible = state.feedback != Feedback.NONE,
                    correct = state.feedback == Feedback.CORRECT,
                    explanation = state.explanation,
                    isLast = state.roundIndex >= state.totalRounds - 1,
                    onNext = onNext,
                    onRetry = onRetry,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
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
            GooseCharacter(
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
