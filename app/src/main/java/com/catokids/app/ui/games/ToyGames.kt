package com.catokids.app.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catokids.app.data.model.GameRound
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.jelly
import com.catokids.app.ui.theme.CatoPalette
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberNodes
import io.github.sceneview.utils.worldToView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Pre-KG toy games: little worlds to play with, not questions to answer.
 *
 * Rules for every game here, because the players are 3 and 4 years old:
 *  - No reading needed. The voice says everything, in two or three tiny words.
 *  - No "wrong" screen. A wrong move just does something silly and lets you try again.
 *  - Big things to drag, tap and rub. Something fun happens every time.
 *
 * Each game calls [onDone] once the child has finished playing; the host then cheers
 * and moves on by itself.
 */
object ToyGames {
    const val MONSTER = "prekg_letter_land_meet_a_d"
    const val BASKET = "prekg_number_land_count_1_5"
    const val BATH = "prekg_know_my_world_keep_clean"

    val lessonIds = setOf(MONSTER, BASKET, BATH)

    /** Little label under the lesson title, in kid words. */
    fun subtitle(lessonId: String): String = when (lessonId) {
        MONSTER -> "😋 Feed the monster"
        BASKET -> "🧺 Pick and count"
        BATH -> "🧼 Bath time"
        else -> ""
    }

    private val numberWords = listOf("Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten")
    fun numberWord(n: Int) = numberWords.getOrElse(n) { n.toString() }
}

/** Says [text] once, a moment after the round appears (after the host's own prompt). */
@Composable
internal fun SayOnStart(key: Any, text: String, onSay: (String) -> Unit, delayMs: Long = 700) {
    LaunchedEffect(key) { delay(delayMs); onSay(text) }
}

// =================================================================== Feed the letter monster

/**
 * A big fuzzy monster with a letter on its tummy is hungry. Drag (or tap) a picture to
 * its mouth. Right one: CHOMP, burp, hearts. Wrong one: "No no!" and it spits it back.
 */
@Composable
fun LetterMonsterGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSay: (String) -> Unit,
    onTap: () -> Unit,
    /** Off for non-letter lessons: the monster just wants "the mango" the voice asks for. */
    letterMode: Boolean = true,
    tint: Color? = null,
    onMiss: () -> Unit = {},
) {
    val letter = round.prompt.substringAfter("with ").trim().firstOrNull()?.uppercaseChar() ?: 'A'
    val foods = remember(round.id) {
        round.options.shuffled(kotlin.random.Random(round.id.hashCode().toLong() + 13))
    }
    // The opening line is spoken by the game (see toySpeech), so the 🔊 button can repeat it.

    val bodyColour = tint ?: listOf(CatoPalette.Coral, CatoPalette.Teal, CatoPalette.Violet, CatoPalette.Amber,
        CatoPalette.Periwinkle, CatoPalette.BrandPink)[(letter - 'A').coerceAtLeast(0) % 6]
    val scope = rememberCoroutineScope()
    val mouth = remember(round.id) { Animatable(0.15f) }          // 0 shut … 1 wide open
    val shake = remember(round.id) { Animatable(0f) }
    val hop = remember(round.id) { Animatable(0f) }
    var hearts by remember(round.id) { mutableStateOf(0) }
    var eaten by remember(round.id) { mutableStateOf<Int?>(null) }
    var busy by remember(round.id) { mutableStateOf(false) }
    var lookAt by remember(round.id) { mutableStateOf<Offset?>(null) }

    // Monster gently breathes all the time.
    val breathe by rememberInfiniteTransition(label = "breathe").animateFloat(
        0.97f, 1.03f, infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b",
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }
        val monsterCenter = Offset(w / 2f, h * 0.36f)
        val radius = minOf(w * 0.30f, h * 0.22f)
        val itemPx = with(density) { 84.dp.toPx() }
        val homes = List(foods.size) { i -> Offset(w * (0.14f + i * 0.24f), h * 0.80f) }
        val positions = remember(round.id) { foods.indices.map { Animatable(homes[it], Offset.VectorConverter) } }

        fun feed(i: Int) {
            if (busy || eaten != null) return
            busy = true
            scope.launch {
                onTap()
                val food = foods[i]
                lookAt = monsterCenter
                launch { mouth.animateTo(1f, tween(200)) }
                // Hop into the mouth.
                positions[i].animateTo(monsterCenter + Offset(0f, radius * 0.25f), tween(380, easing = FastOutSlowInEasing))
                if (food.correct) {
                    eaten = i
                    repeat(3) { mouth.animateTo(0.05f, tween(110)); mouth.animateTo(0.7f, tween(110)) }
                    mouth.animateTo(0.2f, tween(150))
                    onSay(if (letterMode) "Yum yum! $letter for ${food.label}!" else "Yum yum! ${food.label}!")
                    hearts++
                    hop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
                    hop.animateTo(0f, spring(dampingRatio = 0.5f))
                    delay(1300)
                    onDone(true)
                } else {
                    onSay(if (letterMode) "No no! Not ${food.label}!" else "No no! Not that!")
                    onMiss()
                    mouth.animateTo(0.1f, tween(120))
                    launch {
                        repeat(3) { shake.animateTo(1f, tween(70)); shake.animateTo(-1f, tween(70)) }
                        shake.animateTo(0f, tween(70))
                    }
                    positions[i].animateTo(homes[i], spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
                    mouth.animateTo(0.15f, tween(200))
                    busy = false
                    lookAt = null
                }
            }
        }

        if (!letterMode) {
            Text(
                round.prompt, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = CatoPalette.Ink,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            )
        }

        // ---- the monster
        Box(
            Modifier
                .offset { IntOffset((monsterCenter.x - radius * 1.3f).roundToInt(), (monsterCenter.y - radius * 1.3f).roundToInt()) }
                .size(with(density) { (radius * 2.6f).toDp() })
                .graphicsLayer {
                    scaleX = breathe; scaleY = breathe
                    translationX = shake.value * 18f
                    translationY = -hop.value * radius * 0.35f
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = radius
                // Fuzzy fur: a ring of bumps around the body.
                repeat(22) { k ->
                    val a = (2 * PI * k / 22).toFloat()
                    drawCircle(bodyColour, r * 0.22f, c + Offset(cos(a) * r * 0.95f, sin(a) * r * 0.95f))
                }
                drawCircle(bodyColour, r, c)
                // Two little horns.
                drawCircle(CatoPalette.Gold, r * 0.16f, c + Offset(-r * 0.55f, -r * 1.05f))
                drawCircle(CatoPalette.Gold, r * 0.16f, c + Offset(r * 0.55f, -r * 1.05f))
                // Eyes that follow what you're holding.
                val target = lookAt
                listOf(-0.36f, 0.36f).forEach { ex ->
                    val eye = c + Offset(ex * r, -r * 0.42f)
                    drawCircle(Color.White, r * 0.24f, eye)
                    val dir = if (target != null) {
                        val local = target - (monsterCenter - c) - eye
                        val d = hypot(local.x, local.y).coerceAtLeast(1f)
                        Offset(local.x / d, local.y / d) * (r * 0.09f)
                    } else Offset(0f, r * 0.04f)
                    drawCircle(CatoPalette.Ink, r * 0.11f, eye + dir)
                    drawCircle(Color.White, r * 0.035f, eye + dir + Offset(-r * 0.03f, -r * 0.04f))
                }
                // Big mouth.
                val mh = r * (0.08f + 0.5f * mouth.value)
                val mouthTop = c + Offset(-r * 0.45f, r * 0.0f)
                drawOval(Color(0xFF5B1A2E), topLeft = mouthTop, size = Size(r * 0.9f, mh))
                if (mouth.value > 0.25f) {
                    drawOval(Color(0xFFE57691), topLeft = mouthTop + Offset(r * 0.2f, mh * 0.55f), size = Size(r * 0.5f, mh * 0.4f))
                }
                // Two teeth.
                drawRect(Color.White, topLeft = mouthTop + Offset(r * 0.2f, 0f), size = Size(r * 0.13f, r * 0.12f))
                drawRect(Color.White, topLeft = mouthTop + Offset(r * 0.57f, 0f), size = Size(r * 0.13f, r * 0.12f))
            }
            // The letter on its tummy.
            if (letterMode) Text(
                "$letter ${letter.lowercaseChar()}",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.offset(y = with(density) { (radius * 0.72f).toDp() }),
            )
            if (hearts > 0) HeartsPop(hearts)
        }

        // ---- the food, waiting at the bottom
        foods.forEachIndexed { i, food ->
            if (eaten == i) return@forEachIndexed
            var touched by remember(round.id) { mutableStateOf(0) }
            Box(
                Modifier
                    .offset { IntOffset((positions[i].value.x - itemPx / 2).roundToInt(), (positions[i].value.y - itemPx / 2).roundToInt()) }
                    .size(with(density) { itemPx.toDp() })
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(4.dp, bodyColour.copy(alpha = 0.5f), CircleShape)
                    .jelly(trigger = if (touched > 0) touched else null)
                    .pointerInput(round.id, i) {
                        detectTapGestures(onTap = { touched++; feed(i) })
                    }
                    .pointerInput(round.id, i, "drag") {
                        detectDragGestures(
                            onDragStart = {
                                if (!busy) { touched++; onSay(food.label); scope.launch { mouth.animateTo(0.55f, tween(200)) } }
                            },
                            onDrag = { change, delta ->
                                if (busy) return@detectDragGestures
                                change.consume()
                                scope.launch { positions[i].snapTo(positions[i].value + delta) }
                                lookAt = positions[i].value
                            },
                            onDragEnd = {
                                if (busy) return@detectDragGestures
                                val d = (positions[i].value - monsterCenter).getDistance()
                                if (d < radius * 1.25f) feed(i) else scope.launch {
                                    lookAt = null
                                    mouth.animateTo(0.15f, tween(200))
                                    positions[i].animateTo(homes[i], spring(dampingRatio = 0.5f))
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (food.emoji.isNotBlank()) EmojiArt(food.emoji, size = 58.dp)
                else Text(food.label, fontSize = if (food.label.length <= 3) 34.sp else 16.sp,
                    fontWeight = FontWeight.Black, color = CatoPalette.Ink)
            }
        }
    }
}

/** A few hearts that float up from the monster after a yummy bite. */
@Composable
private fun HeartsPop(trigger: Int) {
    val p = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { p.animateTo(1f, tween(1200, easing = LinearEasing)) }
    if (p.value >= 1f) return
    Box(Modifier.fillMaxSize()) {
        listOf(-60f, 0f, 60f).forEachIndexed { i, dx ->
            EmojiArt(
                "❤️", size = 34.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = dx + sin(p.value * 8f + i) * 12f
                        translationY = -p.value * 260f - i * 20f
                        alpha = 1f - p.value
                    },
            )
        }
    }
}

// =================================================================== Fill the basket

/**
 * "Pick 3!" — tap the fruit on the tree and it drops into the basket. Each one is counted
 * out loud and fills a dot at the top. When the basket has enough, it's full and happy.
 */
@Composable
fun FillBasketGame(
    round: GameRound,
    onDone: (Boolean) -> Unit,
    onSay: (String) -> Unit,
    onTap: () -> Unit,
) {
    val target = round.count
    val emoji = round.emoji
    val name = basketName(round)
    // The opening line is spoken by the game (see toySpeech), so the 🔊 button can repeat it.

    val kind = when (emoji.filter { it != '️' }) {
        "🐟" -> Place.POND
        "🥕" -> Place.SOIL
        "🍓", "🍇" -> Place.BUSH
        "🍎", "🍊", "🍐", "🥭", "🍌" -> if (target + 2 <= 8) Place.TREE else Place.MEADOW
        else -> Place.MEADOW
    }
    val total = (target + 2).coerceAtMost(22)
    var picked by remember(round.id) { mutableStateOf(0) }
    var done by remember(round.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        // "Pick" + big number + picture, and one dot per thing to pick.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pick", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = CatoPalette.Ink)
            Spacer(Modifier.width(10.dp))
            Text("$target", fontSize = 44.sp, fontWeight = FontWeight.Black, color = CatoPalette.Coral)
            Spacer(Modifier.width(8.dp))
            EmojiArt(emoji, size = 42.dp)
        }
        Spacer(Modifier.height(6.dp))
        // Up to 10 dots a row, smaller when there are lots.
        val dot = if (target <= 5) 40.dp else 28.dp
        for (row in 0 until (target + 9) / 10) Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
            for (k in row * 10 until minOf(target, row * 10 + 10)) {
                val filled = k < picked
                Box(
                    Modifier
                        .size(dot)
                        .clip(CircleShape)
                        .background(if (filled) CatoPalette.SuccessSoft else Color.White)
                        .border(3.dp, if (filled) CatoPalette.SuccessDeep else CatoPalette.Line, CircleShape)
                        .jelly(trigger = if (filled) k else null),
                    contentAlignment = Alignment.Center,
                ) {
                    if (filled) Text("${k + 1}", fontSize = if (target <= 5) 20.sp else 13.sp, fontWeight = FontWeight.Bold, color = CatoPalette.SuccessDeep)
                }
            }
        }

        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val basket = Offset(w / 2f, h * 0.83f)
            val itemPx = with(density) { (if (total > 12) 54.dp else 64.dp).toPx() }
            val spots = remember(round.id, w, h) { placeSpots(kind, total, w, h) }
            val flight = remember(round.id) { List(total) { Animatable(0f) } }
            val gone = remember(round.id) { mutableStateListOf<Int>() }

            PlaceBackdrop(kind, Modifier.fillMaxSize())

            // The basket, with what's been picked peeking over the rim.
            var basketBounce by remember(round.id) { mutableStateOf(0) }
            Box(
                Modifier
                    .offset { IntOffset((basket.x - with(density) { 75.dp.toPx() }).roundToInt(), (basket.y - with(density) { 75.dp.toPx() }).roundToInt()) }
                    .size(150.dp)
                    .jelly(trigger = if (basketBounce > 0) basketBounce else null),
                contentAlignment = Alignment.Center,
            ) {
                Row(Modifier.offset(y = (-24).dp), horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                    repeat(picked) { EmojiArt(emoji, size = 34.dp) }
                }
                EmojiArt("🧺", size = 130.dp)
                if (done) EmojiArt("😊", size = 40.dp, modifier = Modifier.offset(y = 30.dp))
            }

            spots.forEachIndexed { i, spot ->
                if (i in gone) return@forEachIndexed
                val p = flight[i].value
                // Arc from the spot down into the basket.
                val pos = Offset(
                    spot.x + (basket.x - spot.x) * p,
                    spot.y + (basket.y - 40f - spot.y) * p - sin(p * PI.toFloat()) * h * 0.12f,
                )
                var wobble by remember(round.id, i) { mutableStateOf(0) }
                Box(
                    Modifier
                        .offset { IntOffset((pos.x - itemPx / 2).roundToInt(), (pos.y - itemPx / 2).roundToInt()) }
                        .size(with(density) { itemPx.toDp() })
                        .graphicsLayer { val s = 1f - p * 0.4f; scaleX = s; scaleY = s; rotationZ = p * 200f }
                        .jelly(trigger = if (wobble > 0) wobble else null)
                        .pointerInput(round.id, i) {
                            detectTapGestures(onTap = {
                                if (done || flight[i].value > 0f) { wobble++; return@detectTapGestures }
                                onTap()
                                scope.launch {
                                    flight[i].animateTo(1f, tween(650, easing = FastOutSlowInEasing))
                                    gone += i
                                    picked++
                                    basketBounce++
                                    onSay(ToyGames.numberWord(picked) + "!")
                                    if (picked >= target) {
                                        done = true
                                        delay(700)
                                        onSay("${ToyGames.numberWord(target)} $name! Yay!")
                                        delay(1500)
                                        onDone(true)
                                    }
                                }
                            })
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    EmojiArt(emoji, size = 54.dp)
                }
            }
        }
    }
}

private enum class Place { TREE, BUSH, POND, SOIL, MEADOW }

private fun placeSpots(kind: Place, n: Int, w: Float, h: Float): List<Offset> {
    // Beyond eight pieces a line or a ring gets too tight for small fingers: use the grid.
    if (n > 8 && kind != Place.MEADOW) return placeSpots(Place.MEADOW, n, w, h)
    val r = kotlin.random.Random(n * 31 + kind.ordinal)
    return when (kind) {
        Place.TREE -> {
            // Spread over the leafy top of the tree.
            val c = Offset(w / 2f, h * 0.30f)
            List(n) { k ->
                val a = (2 * PI * k / n).toFloat() + 0.4f
                val d = if (k % 2 == 0) 0.62f else 0.32f
                c + Offset(cos(a) * w * 0.30f * d * 1.4f, sin(a) * h * 0.17f * d * 1.4f)
            }
        }
        Place.BUSH -> List(n) { k -> Offset(w * (0.18f + 0.64f * k / (n - 1).coerceAtLeast(1)), h * (0.42f + (k % 2) * 0.07f)) }
        Place.POND -> List(n) { k -> Offset(w * (0.2f + 0.6f * k / (n - 1).coerceAtLeast(1)), h * (0.36f + (if (k % 2 == 0) -0.05f else 0.05f) + r.nextFloat() * 0.02f)) }
        Place.SOIL -> List(n) { k -> Offset(w * (0.14f + 0.72f * k / (n - 1).coerceAtLeast(1)), h * 0.46f) }
        Place.MEADOW -> {
            // A loose grid over the grass, nudged so it doesn't look like a table.
            val cols = if (n <= 9) 3 else if (n <= 16) 4 else 5
            val rows = (n + cols - 1) / cols
            List(n) { k ->
                val c = k % cols; val rr = k / cols
                Offset(
                    w * (0.08f + 0.84f * (c + 0.5f) / cols) + (r.nextFloat() - 0.5f) * w * 0.03f,
                    h * (0.04f + (if (rows >= 5) 0.68f else 0.58f) * (rr + 0.5f) / rows) + (r.nextFloat() - 0.5f) * h * 0.02f,
                )
            }
        }
    }
}

@Composable
private fun PlaceBackdrop(kind: Place, modifier: Modifier) {
    val sway by rememberInfiniteTransition(label = "sway").animateFloat(
        -1f, 1f, infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s",
    )
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val leaf = Color(0xFF7BC96F); val leafDark = Color(0xFF5DAE53)
        when (kind) {
            Place.TREE -> {
                drawRoundRect(Color(0xFF9C6B3F), topLeft = Offset(w * 0.45f, h * 0.38f), size = Size(w * 0.1f, h * 0.36f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f))
                val c = Offset(w / 2f + sway * 4f, h * 0.30f)
                listOf(Offset(0f, 0f) to 0.30f, Offset(-0.22f, 0.04f) to 0.2f, Offset(0.22f, 0.04f) to 0.2f,
                    Offset(-0.12f, -0.1f) to 0.2f, Offset(0.12f, -0.1f) to 0.2f).forEachIndexed { k, (o, rr) ->
                    drawCircle(if (k == 0) leafDark else leaf, w * rr, c + Offset(o.x * w, o.y * h))
                }
            }
            Place.BUSH -> listOf(0.25f, 0.5f, 0.75f).forEach { x ->
                drawCircle(leaf, w * 0.2f, Offset(w * x, h * 0.48f))
            }
            Place.POND -> {
                drawOval(Color(0xFF7FC8F8), topLeft = Offset(w * 0.06f, h * 0.2f), size = Size(w * 0.88f, h * 0.34f))
                drawOval(Color(0xFFA8DCFB), topLeft = Offset(w * 0.2f + sway * 8f, h * 0.27f), size = Size(w * 0.3f, h * 0.05f))
                drawOval(Color(0xFFA8DCFB), topLeft = Offset(w * 0.52f - sway * 8f, h * 0.42f), size = Size(w * 0.25f, h * 0.04f))
            }
            Place.MEADOW -> repeat(14) { k ->
                val x = w * ((k * 0.137f) % 1f); val y = h * (0.12f + ((k * 0.29f) % 0.55f))
                drawCircle(leaf.copy(alpha = 0.35f), w * 0.035f, Offset(x, y))
            }
            Place.SOIL -> {
                drawRoundRect(Color(0xFFB07D4F), topLeft = Offset(w * 0.04f, h * 0.44f), size = Size(w * 0.92f, h * 0.18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.05f))
                repeat(10) { k -> drawCircle(leaf, w * 0.025f, Offset(w * (0.08f + k * 0.093f), h * 0.44f)) }
            }
        }
    }
}

// =================================================================== Bath time (3D)

/**
 * A muddy 3D animal. Rub it with the soap — each blob of mud shrinks away and bubbles
 * pile up. Then tap the big water drop: splash! The bubbles wash off and the clean
 * animal does a happy dance.
 *
 * The mud and bubbles live inside the 3D scene (a 3D view always draws over app
 * drawing), and each frame we work out where every mud blob is on screen so rubbing
 * with a finger finds it.
 */
@Composable
fun BathTimeGame(
    round: GameRound,
    ctx: Scene3DContext,
    onDone: (Boolean) -> Unit,
    onSay: (String) -> Unit,
    onTap: () -> Unit,
) {
    val animal = Models3D.animalName(round.emoji)
    // The opening line is spoken by the game (see toySpeech), so the 🔊 button can repeat it.

    val node = remember(round.id) {
        ModelNode(
            modelInstance = ctx.modelLoader.createModelInstance(Models3D.animalPath(round.emoji)),
            autoAnimate = false, scaleToUnits = 1.2f, centerOrigin = Position(0f, 0f, 0f),
        ).apply { isEditable = false; isTouchable = false; playOnly("idle") }
    }

    // Mud blobs on the front of the animal (world positions), flattened brown balls.
    val mudAt = remember(round.id) {
        listOf(Position(-0.30f, 0.42f, 0.66f), Position(0.30f, 0.40f, 0.66f), Position(0.0f, 0.12f, 0.72f),
            Position(-0.32f, 0.10f, 0.66f), Position(0.32f, 0.12f, 0.66f), Position(-0.14f, -0.10f, 0.66f),
            Position(0.16f, -0.12f, 0.66f))
    }
    // Mud splats are Kenney's dark chocolate cookie, turned to face us; foam is whipped cream.
    val mud = remember(round.id) {
        mudAt.mapIndexed { k, p ->
            ModelNode(
                modelInstance = ctx.modelLoader.createModelInstance(Models3D.MUD),
                autoAnimate = false, scaleToUnits = 0.28f + (k % 3) * 0.04f, centerOrigin = Position(0f, 0f, 0f),
            ).apply { position = p; rotation = Rotation(x = 90f, z = k * 47f); isEditable = false; isTouchable = false }
        }
    }
    val mudScale = remember(round.id) { mud.map { it.scale } }
    val bubbles = remember(round.id) { mutableListOf<ModelNode>() }
    val bubbleScale = remember(round.id) { mutableListOf<io.github.sceneview.math.Scale>() }
    val childNodes = rememberNodes { add(node); addAll(mud) }
    val camera = rememberCameraNode(ctx.engine) {
        position = Position(0f, 0.15f, 3.6f)
        lookAt(Position(0f, 0f, 0f))
    }

    val dirt = remember(round.id) { FloatArray(mudAt.size) { 1f } }
    val mudScreen = remember(round.id) { Array(mudAt.size) { Offset.Unspecified } }
    var soapAt by remember(round.id) { mutableStateOf<Offset?>(null) }
    var saidRub by remember(round.id) { mutableStateOf(false) }
    var phase by remember(round.id) { mutableStateOf(0) }   // 0 muddy · 1 soapy · 2 splash · 3 clean
    val splash = remember(round.id) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val rnd = remember(round.id) { kotlin.random.Random(round.id.hashCode()) }

    fun addBubble(near: Position) {
        if (bubbles.size >= 45) return
        val b = ModelNode(
            modelInstance = ctx.modelLoader.createModelInstance(Models3D.FOAM),
            autoAnimate = false, scaleToUnits = 0.14f + rnd.nextFloat() * 0.1f, centerOrigin = Position(0f, 0f, 0f),
        ).apply {
            position = Position(near.x + (rnd.nextFloat() - 0.5f) * 0.3f, near.y + (rnd.nextFloat() - 0.5f) * 0.3f, 0.78f)
            rotation = Rotation(x = 70f, y = rnd.nextFloat() * 360f)
            isEditable = false; isTouchable = false
        }
        bubbleScale += b.scale
        bubbles += b
        childNodes += b
    }

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            when (phase) { 0 -> "Rub rub! 🧼"; 1 -> "Splash! 💧"; else -> "Clean! ✨" },
            fontSize = 34.sp, fontWeight = FontWeight.Black, color = CatoPalette.Ink,
        )
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val reach = w * 0.14f

            Scene(
                modifier = Modifier.fillMaxSize(),
                engine = ctx.engine,
                modelLoader = ctx.modelLoader,
                materialLoader = ctx.materialLoader,
                isOpaque = false,
                cameraNode = camera,
                cameraManipulator = null,
                childNodes = childNodes,
                onFrame = { nanos ->
                    val t = (nanos / 1_000_000_000.0 % 10_000).toFloat()
                    // Stay facing us while muddy (the mud isn't glued on); wiggle once clean.
                    node.rotation = Rotation(y = if (phase >= 3) sin(t * 3f) * 20f else 0f)
                    mud.forEachIndexed { k, m ->
                        val d = dirt[k]
                        m.isVisible = d > 0.02f
                        m.scale = mudScale[k] * d
                        val v = camera.worldToView(m.worldPosition)
                        mudScreen[k] = Offset(v.x * w, (1f - v.y) * h)
                    }
                    val left = 1f - splash.value
                    bubbles.forEachIndexed { k, b ->
                        val s = left * (1f + 0.08f * sin(t * 4f + k))
                        b.scale = bubbleScale[k] * s
                        b.isVisible = left > 0.02f
                    }
                },
            )

            // Touch layer: rubbing anywhere over the animal.
            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(round.id) {
                        detectDragGestures(
                            onDragStart = { p ->
                                if (phase == 0) {
                                    soapAt = p
                                    if (!saidRub) { saidRub = true; onSay("Rub rub rub!") }
                                    node.playOnly("gesture-positive")
                                }
                            },
                            onDrag = { change, _ ->
                                if (phase != 0) return@detectDragGestures
                                change.consume()
                                val p = change.position
                                soapAt = p
                                mudScreen.forEachIndexed { k, sp ->
                                    if (sp.isSpecified && dirt[k] > 0f && (sp - p).getDistance() < reach) {
                                        dirt[k] = (dirt[k] - 0.18f).coerceAtLeast(0f)
                                        if (rnd.nextFloat() < 0.35f) addBubble(mudAt[k])
                                    }
                                }
                                if (dirt.all { it <= 0f }) {
                                    phase = 1
                                    soapAt = null
                                    node.playOnly("idle")
                                    onTap()
                                    onSay("Bubbles! Now splash!")
                                }
                            },
                            onDragEnd = { soapAt = null; if (phase == 0) node.playOnly("idle") },
                        )
                    },
            )

            // Water falling during the splash.
            if (splash.value in 0.01f..0.99f) {
                Canvas(Modifier.fillMaxSize()) {
                    repeat(40) { k ->
                        val x = size.width * (0.12f + 0.76f * ((k * 37) % 100) / 100f)
                        val y = ((splash.value * 2.2f + k * 0.13f) % 1f) * size.height
                        drawLine(Color(0xFF4FC3F7), Offset(x, y), Offset(x, y + 34f), strokeWidth = 7f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    }
                }
            }

            // The soap follows your finger.
            soapAt?.let { p ->
                EmojiArt("🧼", size = 70.dp, modifier = Modifier.offset {
                    IntOffset((p.x - with(density) { 35.dp.toPx() }).roundToInt(), (p.y - with(density) { 35.dp.toPx() }).roundToInt())
                })
            }
            if (phase == 0 && soapAt == null) {
                // A soap bar waiting whenever it isn't in a hand, so it's always clear what to do.
                EmojiArt("🧼", size = 80.dp, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).jelly(trigger = 1))
            }
        }

        // The big water button.
        if (phase == 1) {
            Box(
                Modifier
                    .padding(bottom = 40.dp)
                    .size(112.dp)
                    .jelly(trigger = 1)
                    .clip(CircleShape)
                    .background(Color(0xFF4FC3F7))
                    .clickable {
                        phase = 2
                        onTap()
                        scope.launch {
                            onSay("Splash splash!")
                            splash.animateTo(1f, tween(1800, easing = LinearEasing))
                            phase = 3
                            node.playOnly("dance")
                            onSay("Clean $animal! Yay!")
                            delay(1800)
                            onDone(true)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                EmojiArt("💧", size = 80.dp)
            }
        } else Spacer(Modifier.height(138.dp))
    }
}

/** "How many apples?" with 1 to pick -> "apple"; with 3 -> "apples". */
internal fun basketName(round: GameRound): String {
    val plural = round.prompt.substringAfter("many ").substringBefore("?").trim().ifBlank { "ones" }
    val single = when {
        plural.endsWith("ies") -> plural.dropLast(3) + "y"
        plural.endsWith("s") -> plural.dropLast(1)
        else -> plural
    }
    return if (round.count == 1) single else plural
}

internal fun basketLine(round: GameRound) = "Pick ${ToyGames.numberWord(round.count).lowercase()} ${basketName(round)}!"
