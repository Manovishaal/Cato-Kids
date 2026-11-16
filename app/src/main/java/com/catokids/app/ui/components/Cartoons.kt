package com.catokids.app.ui.components

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.LottieCompositionFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.catokids.app.R

/**
 * Real 2D cartoon animations for Pre-KG, played with Airbnb's Lottie (Apache 2.0).
 *
 * Every file in res/raw/cartoon_*.json is a free animation from LottieFiles under the
 * Lottie Simple License (free for commercial apps, no attribution required). The source
 * page of each one is listed in THIRD_PARTY_NOTICES.md so it can be traced and replaced.
 *
 * Screens opt in by providing [LocalCartoons] = true. While it is on, [EmojiArt] plays the
 * cartoon for any picture that has one (🐱 walks, 🐝 buzzes) and falls back to the still
 * artwork for everything else, so a lesson never shows a blank tile.
 */
val LocalCartoons = staticCompositionLocalOf { false }

/** Lesson pictures that have a cartoon. Keys are glyphs with variation selectors removed. */
internal val CARTOON_FOR_GLYPH: Map<String, Int> = mapOf(
    "🐱" to R.raw.cartoon_cat,
    "🐶" to R.raw.cartoon_dog,
    "🐝" to R.raw.cartoon_bee,
    "🦋" to R.raw.cartoon_butterfly,
    "🐘" to R.raw.cartoon_elephant,
    "🦁" to R.raw.cartoon_lion,
    "🦒" to R.raw.cartoon_giraffe,
    "🐸" to R.raw.cartoon_frog,
    "🦉" to R.raw.cartoon_owl,
    "🦜" to R.raw.cartoon_parrot,
    "🦆" to R.raw.cartoon_duck,
    "🐵" to R.raw.cartoon_monkey,
    "☀" to R.raw.cartoon_sun,
)

/**
 * Width ÷ height for the cartoons that aren't square, so a wide scene (the cat chasing its
 * ball) gets a wider frame instead of being shrunk to fit a square.
 */
internal val CARTOON_ASPECT: Map<Int, Float> = mapOf(
    R.raw.cartoon_cat to 2.0f,
    R.raw.cartoon_duck to 1.5f,
)

/** Animations that aren't lesson pictures: greetings, celebrations and craft time. */
object Cartoons {
    val hello = R.raw.cartoon_hello_bird
    val confetti = R.raw.cartoon_confetti
    val trophy = R.raw.cartoon_trophy
    val painting = R.raw.cartoon_painting
    val scissors = R.raw.cartoon_scissors

    /** Rotated through on right answers so the cheer keeps changing. */
    val celebrations = listOf(R.raw.cartoon_dancing_star, R.raw.cartoon_dancing_bear, R.raw.cartoon_jumping_girl)
}

/** Plays one cartoon. Loops forever unless [iterations] says otherwise. */
@Composable
fun CartoonAnimation(
    @RawRes res: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1f,
    /** Crop for full-screen effects like confetti, whose canvas is landscape. */
    contentScale: ContentScale = ContentScale.Fit,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(res))
    val progress by animateLottieCompositionAsState(composition, iterations = iterations, speed = speed)
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier,
        contentScale = contentScale,
    )
}

/**
 * The confetti cartoon is a wide 16:9 scene. Stretching it over a tall phone screen blows
 * the streamers up into giant ribbons, so it plays at its own proportions in two bands —
 * one near the top, one through the middle — which covers the screen at the right scale.
 */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier, iterations: Int = 1) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        repeat(2) {
            CartoonAnimation(
                Cartoons.confetti,
                Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                iterations = iterations,
            )
        }
    }
}

/**
 * Loads every cartoon into Lottie's shared cache in the background. Call it when a Pre-KG
 * screen opens, so the cheering character and confetti appear the instant a child answers
 * instead of popping in a moment later while their file is read for the first time.
 */
@Composable
fun PreloadCartoons() {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (CARTOON_FOR_GLYPH.values + Cartoons.celebrations +
            listOf(Cartoons.hello, Cartoons.confetti, Cartoons.trophy, Cartoons.painting, Cartoons.scissors))
            .distinct()
            .forEach { LottieCompositionFactory.fromRawRes(context, it) }
    }
}

/** The cartoon for a picture, if there is one. */
internal fun cartoonFor(glyph: String): Int? =
    CARTOON_FOR_GLYPH[glyph.filter { it != '️' && it != '︎' }]
