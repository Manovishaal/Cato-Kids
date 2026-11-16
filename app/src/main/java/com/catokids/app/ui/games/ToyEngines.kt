package com.catokids.app.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catokids.app.data.model.GameRound
import com.catokids.app.data.model.GameType
import com.catokids.app.data.model.Lesson
import com.catokids.app.ui.components.EmojiArt
import com.catokids.app.ui.components.EmojiText
import com.catokids.app.ui.components.SparkleBurst
import com.catokids.app.ui.components.jelly
import com.catokids.app.ui.theme.CatoPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Every Pre-KG lesson is a toy. This picks the toy for a lesson and hands it the round.
 *
 *  Feed the monster · Pick and count · Bath time (in ToyGames.kt)
 *  Light it up · Pop the bubbles · Put it home · Peekaboo · Hungry bear (below)
 */
/** Each toy has one picture and one action word, the same everywhere it appears. */
enum class Toy(val subtitle: String, val cue: String) {
    MONSTER("😋 Feed the monster", "Drag"), FEED("😋 Feed me", "Drag"), BASKET("🧺 Pick and count", "Tap"),
    BATH("🧼 Bath time", "Rub"), LIGHT("✨ Light it up", "Rub"), POP("🫧 Pop the bubbles", "Tap"),
    HOME("🏠 Put it home", "Drag"), PEEK("🙈 Peekaboo", "Tap"), BEAR("🐻 Hungry bear", "Tap"),
}

private fun short(lesson: Lesson) = lesson.id.substringAfter("prekg_").substringAfter("_land_").substringAfter("my_world_")

fun toyFor(lesson: Lesson): Toy {
    val id = short(lesson)
    return when {
        lesson.id == ToyGames.BATH -> Toy.BATH
        id.startsWith("meet_") -> Toy.MONSTER
        lesson.gameType == GameType.TRACE -> Toy.LIGHT
        lesson.gameType == GameType.TAP_ALL -> Toy.POP
        lesson.gameType == GameType.COUNT_TAP -> Toy.BASKET
        lesson.gameType == GameType.MATCH_PAIRS || lesson.gameType == GameType.SORT_BUCKETS -> Toy.HOME
        id == "more_less" -> Toy.BEAR
        id == "odd_one_out" || lesson.gameType == GameType.SHAPE_HUNT || id == "my_body" -> Toy.POP
        id in setOf("find_me", "birds", "my_family", "seasons") -> Toy.PEEK
        lesson.subject.wire.contains("letter") && id == "quiz" -> Toy.MONSTER
        else -> Toy.FEED
    }
}

/**
 * The one short line a toy says when a round starts — and again whenever the 🔊 button is
 * tapped. Kept tiny: a 3-year-old hears "Feed me A!", never the answer.
 */
fun toySpeech(lesson: Lesson, round: GameRound): String {
    val target = round.options.firstOrNull { it.correct }
    return when (toyFor(lesson)) {
        Toy.MONSTER -> "I'm hungry! Feed me ${round.prompt.substringAfter("with ").trim().firstOrNull()?.uppercaseChar() ?: 'A'}!"
        Toy.BASKET -> basketLine(round)
        Toy.BATH -> "Oh no! Muddy ${Models3D.animalName(round.emoji)}! Rub rub!"
        Toy.LIGHT -> "Rub the dots! ${round.prompt.substringBefore(" for ")}!"
        Toy.POP -> when {
            lesson.gameType == GameType.TAP_ALL -> "Pop ${target?.label?.uppercase() ?: ""}!"
            short(lesson) == "odd_one_out" -> "Pop the odd one!"
            else -> "Pop the ${(round.speak ?: target?.label ?: "").lowercase()}!"
        }
        Toy.HOME -> "Take them home!"
        Toy.BEAR -> if (round.prompt.contains("MORE", true)) "I want more!" else "I want less!"
        Toy.FEED, Toy.PEEK -> round.speak ?: round.prompt
    }
}

/** The one place the game screen asks for a Pre-KG round to be played. */
@Composable
fun ToyRound(
    lesson: Lesson,
    round: GameRound,
    scene3D: Scene3DContext?,
    onDone: (Boolean) -> Unit,
    onSay: (String) -> Unit,
    onTap: () -> Unit,
    onMiss: () -> Unit = {},
) {
    val id = short(lesson)
    when (toyFor(lesson)) {
        Toy.MONSTER -> LetterMonsterGame(round, onDone, onSay, onTap, onMiss = onMiss)
        Toy.FEED -> LetterMonsterGame(round, onDone, onSay, onTap, letterMode = false,
            tint = listOf(CatoPalette.Teal, CatoPalette.Violet, CatoPalette.Periwinkle, CatoPalette.BrandPink, CatoPalette.Amber)[(id.hashCode() and 0xff) % 5], onMiss = onMiss)
        Toy.BASKET -> FillBasketGame(round, onDone, onSay, onTap)
        Toy.BATH -> if (scene3D != null && Models3D.hasAnimal(round.emoji)) BathTimeGame(round, scene3D, onDone, onSay, onTap)
            else LetterMonsterGame(round, onDone, onSay, onTap, letterMode = false, onMiss = onMiss)
        Toy.LIGHT -> LightUpGame(round, onDone, onSay, onTap)
        Toy.POP -> PopGame(round, lesson.gameType, odd = id == "odd_one_out", onDone, onSay, onTap, onMiss)
        Toy.HOME -> HomesGame(round, onDone, onSay, onTap, onMiss)
        Toy.PEEK -> PeekabooGame(round, when (id) { "my_family" -> Peek.HOUSE; "seasons" -> Peek.CLOUD; else -> Peek.BUSH }, onDone, onSay, onTap, onMiss)
        Toy.BEAR -> HungryBearGame(round, onDone, onSay, onTap, onMiss)
    }
}

/** A clock in seconds that ticks every frame while the toy is on screen. */
@Composable
private fun rememberClock(key: Any): State<Float> {
    val t = remember(key) { mutableFloatStateOf(0f) }
    LaunchedEffect(key) {
        val start = withFrameNanos { it }
        while (isActive) withFrameNanos { t.floatValue = (it - start) / 1_000_000_000f }
    }
    return t
}

/** Big friendly words at the top of a toy: "Pop A!", "More?" */
@Composable
private fun ToyTitle(text: String) {
    EmojiText(
        text,
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
        color = CatoPalette.Ink, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
    )
}

/** Picture if there is one, else the word or letter, big. */
@Composable
private fun Thing(label: String, emoji: String, size: Int = 54) {
    if (emoji.isNotBlank() && label.length > 2) EmojiArt(emoji, size = size.dp)
    else EmojiText(
        label,
        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black, fontSize = if (label.length <= 2) (size * 0.62f).sp else (size * 0.3f).sp),
        color = CatoPalette.Ink, textAlign = TextAlign.Center,
    )
}

// =================================================================== Light it up

/**
 * The letter (or number, or line) is made of sleeping fireflies. Rub or tap along it and
 * each one wakes up glowing. When they're all awake the whole thing shines gold.
 */
@Composable
fun LightUpGame(round: GameRound, onDone: (Boolean) -> Unit, onSay: (String) -> Unit, onTap: () -> Unit, onMiss: () -> Unit = {}) {
    val glyph = round.glyph.ifBlank { "A" }
    val scope = rememberCoroutineScope()
    var done by remember(round.id) { mutableStateOf(false) }
    var lit by remember(round.id) { mutableStateOf(setOf<Int>()) }
    var sparkle by remember(round.id) { mutableStateOf<Int?>(null) }
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(
        0.55f, 1f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "g",
    )

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ToyTitle(if (done) "Wow! ✨" else "Rub the dots!")
        }
        if (round.emoji.isNotBlank()) EmojiArt(round.emoji, size = 52.dp, modifier = Modifier.jelly(trigger = sparkle))

        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val built = remember(round.id, w, h) { glyphDots(glyph, w, h) }
            val path = built.first
            val dots = built.second
            val reach = w * 0.08f

            fun touch(p: Offset) {
                if (done) return
                val near = dots.indices.filter { it !in lit && (dots[it] - p).getDistance() < reach }
                if (near.isEmpty()) return
                lit = lit + near
                if (lit.size % 3 == 0) onTap()
                if (lit.size >= (dots.size * 0.9f).toInt().coerceAtLeast(1)) {
                    done = true
                    lit = dots.indices.toSet()
                    sparkle = 1
                    scope.launch {
                        onSay("${round.prompt.substringBefore(" for ")}! Yay!")
                        delay(1600)
                        onDone(true)
                    }
                }
            }

            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(round.id) { detectTapGestures { touch(it) } }
                    .pointerInput(round.id, "drag") {
                        detectDragGestures(onDragStart = { touch(it) }) { change, _ -> change.consume(); touch(change.position) }
                    },
            ) {
                // The shape itself: faint while sleeping, gold when done.
                if (path != null) drawIntoCanvas { c ->
                    val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        color = (if (done) CatoPalette.Gold else CatoPalette.Line).toArgb()
                        style = android.graphics.Paint.Style.FILL
                    }
                    c.nativeCanvas.drawPath(path, p)
                }
                dots.forEachIndexed { i, d ->
                    if (i in lit) {
                        drawCircle(CatoPalette.Gold.copy(alpha = 0.35f * glow), reach * 0.55f, d)
                        drawCircle(Color(0xFFFFE27A), reach * 0.24f, d)
                        drawCircle(Color.White, reach * 0.09f, d)
                    } else {
                        drawCircle(Color.White, reach * 0.22f, d)
                        drawCircle(CatoPalette.InkSoft.copy(alpha = 0.5f), reach * 0.22f, d, style = Stroke(5f))
                    }
                }
            }
            SparkleBurst(trigger = sparkle, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(12.dp))
    }
}

/** The glyph as a big centred outline plus dots spread along it. Lines are drawn as dots only. */
private fun glyphDots(glyph: String, w: Float, h: Float): Pair<android.graphics.Path?, List<Offset>> {
    val cx = w / 2f; val cy = h / 2f; val r = minOf(w, h) * 0.38f
    fun line(a: Offset, b: Offset, n: Int = 9) = List(n) { k -> a + (b - a) * (k / (n - 1f)) }
    when (glyph) {
        "|" -> return null to line(Offset(cx, cy - r), Offset(cx, cy + r))
        "—", "-" -> return null to line(Offset(cx - r, cy), Offset(cx + r, cy))
        "/" -> return null to line(Offset(cx + r * 0.6f, cy - r), Offset(cx - r * 0.6f, cy + r))
    }
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textSize = h * 0.62f
    }
    // Shrink wide glyphs like "10" to fit the width.
    val width = paint.measureText(glyph)
    if (width > w * 0.8f) paint.textSize *= (w * 0.8f) / width
    val path = android.graphics.Path()
    paint.getTextPath(glyph, 0, glyph.length, 0f, 0f, path)
    val b = android.graphics.RectF(); path.computeBounds(b, true)
    path.offset(cx - b.centerX(), cy - b.centerY())
    val dots = mutableListOf<Offset>()
    val pm = android.graphics.PathMeasure(path, false)
    val spacing = minOf(w, h) * 0.075f
    val pos = FloatArray(2)
    do {
        val len = pm.length
        val n = (len / spacing).toInt().coerceAtLeast(3)
        for (k in 0 until n) { pm.getPosTan(len * k / n, pos, null); dots += Offset(pos[0], pos[1]) }
    } while (pm.nextContour() && dots.size < 70)
    return path to dots
}

// =================================================================== Pop the bubbles

/**
 * Bubbles float up with letters or pictures inside. Pop only the ones the voice asks for —
 * the others just bonk and keep floating. When every right one is popped: "All done!"
 */
@Composable
fun PopGame(round: GameRound, type: GameType, odd: Boolean, onDone: (Boolean) -> Unit, onSay: (String) -> Unit, onTap: () -> Unit, onMiss: () -> Unit = {}) {
    data class Bubble(val label: String, val emoji: String, val target: Boolean)
    val target = round.options.firstOrNull { it.correct }
    val bubbles = remember(round.id) {
        val r = kotlin.random.Random(round.id.hashCode())
        when {
            type == GameType.TAP_ALL -> round.options.map { Bubble(it.label, "", it.correct) }
            odd -> round.prompt.split(" ").filter { it.isNotBlank() }.map { Bubble(it, it, it == target?.label) }
            else -> (List(3) { Bubble(target!!.label, target.emoji, true) } +
                round.options.filter { !it.correct }.flatMap { o -> List(2) { Bubble(o.label, o.emoji, false) } }).shuffled(r)
        }
    }
    val goal = bubbles.count { it.target }
    val title = when {
        type == GameType.TAP_ALL -> "Pop ${target?.label?.uppercase() ?: ""} ${target?.label?.lowercase() ?: ""}!"
        odd -> "Pop the odd one!"
        else -> "Pop ${target?.emoji?.ifBlank { target.label } ?: ""}!"
    }
    // The opening line is spoken by the game (see toySpeech), so the 🔊 button can repeat it.

    val clock by rememberClock(round.id)
    val popped = remember(round.id) { mutableStateMapOf<Int, Float>() }      // index -> time popped
    val bonked = remember(round.id) { mutableStateMapOf<Int, Float>() }
    var finished by remember(round.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ToyTitle(title)
        Text("${popped.size} / $goal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = CatoPalette.TealDeep)
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val size = with(density) { (if (odd) 110.dp else 92.dp).toPx() }
            val n = bubbles.size

            fun posOf(i: Int, t: Float): Offset {
                val lane = ((i * 0.618f) % 1f)
                val period = 9f + (i % 4)
                val phase = i.toFloat() / n
                val y = h + size - (((t / period) + phase) % 1f) * (h + size * 2)
                val x = size / 2 + lane * (w - size) + sin(t * 1.3f + i) * 14f
                return Offset(x, y)
            }

            Box(
                Modifier.fillMaxSize().pointerInput(round.id) {
                    detectTapGestures { p ->
                        if (finished) return@detectTapGestures
                        val i = (0 until n).filter { it !in popped }
                            .minByOrNull { (posOf(it, clock) - p).getDistance() }
                            ?.takeIf { (posOf(it, clock) - p).getDistance() < size * 0.7f } ?: return@detectTapGestures
                        if (bubbles[i].target) {
                            popped[i] = clock; onTap()
                            onSay(ToyGames.numberWord(popped.size) + "!")
                            if (popped.size >= goal) {
                                finished = true
                                scope.launch { delay(700); onSay("All done! Yay!"); delay(1300); onDone(true) }
                            }
                        } else {
                            bonked[i] = clock
                            onSay("Not that one!"); onMiss()
                        }
                    }
                },
            ) {
                for (i in 0 until n) {
                    val b = bubbles[i]
                    val pAt = popped[i]
                    val p = posOf(i, if (pAt != null) pAt else clock)
                    val burst = if (pAt != null) ((clock - pAt) / 0.35f).coerceIn(0f, 1f) else 0f
                    if (burst >= 1f) continue
                    val shake = bonked[i]?.let { if (clock - it < 0.5f) sin((clock - it) * 50f) * 14f else 0f } ?: 0f
                    Box(
                        Modifier
                            .offset { IntOffset((p.x - size / 2 + shake).roundToInt(), (p.y - size / 2).roundToInt()) }
                            .size(with(density) { size.toDp() })
                            .graphicsLayer { val s = 1f + burst * 0.6f; scaleX = s; scaleY = s; alpha = 1f - burst }
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.55f))
                            .border(4.dp, Color(0xFF8FD3FF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Thing(b.label, b.emoji, size = if (odd) 64 else 50)
                    }
                }
            }
        }
    }
}

// =================================================================== Put it home

/**
 * Two to four homes at the top (big house, little house; the B mailbox, the C mailbox…)
 * and things at the bottom. Drag each thing into its home. The wrong home bounces it back.
 */
@Composable
fun HomesGame(round: GameRound, onDone: (Boolean) -> Unit, onSay: (String) -> Unit, onTap: () -> Unit, onMiss: () -> Unit = {}) {
    data class Home(val key: String, val label: String, val icon: String)
    data class Thing2(val label: String, val emoji: String, val home: String)
    val homes = remember(round.id) {
        if (round.buckets.isNotEmpty()) round.buckets.map { Home(it, it, homeIcon(it)) }
        else round.pairs.map { Home(it.right, it.right, "") }.shuffled(kotlin.random.Random(round.id.hashCode()))
    }
    val things = remember(round.id) {
        (if (round.items.isNotEmpty()) round.items.map { Thing2(it.label, it.emoji, it.bucket) }
        else round.pairs.map { Thing2(it.left, it.emoji, it.right) }).shuffled(kotlin.random.Random(round.id.hashCode() + 3))
    }
    val placed = remember(round.id) { mutableStateMapOf<Int, String>() }
    val homeBounce = remember(round.id) { mutableStateMapOf<String, Int>() }
    var busy by remember(round.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ToyTitle("Take them home! 🏠")
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            // Up to 3 homes in a row; more go on two rows so each stays big enough to drop into.
            val pad = w * 0.03f
            val cols = if (homes.size <= 3) homes.size else (homes.size + 1) / 2
            val rows = (homes.size + cols - 1) / cols
            val homeW = (w - pad * (cols + 1)) / cols
            val itemPx = with(density) { 76.dp.toPx() }
            val perRow = if (things.size <= 4) things.size else (things.size + 1) / 2
            val thingRows = (things.size + perRow - 1) / perRow
            // Pieces sit along the bottom with a full piece of room between rows, so they never
            // overlap on a small phone; the homes take whatever height is left above them.
            val rowGap = itemPx * 1.12f
            val bottomInset = with(density) { 28.dp.toPx() }   // clear of the gesture bar
            val lastRowY = h - itemPx * 0.62f - bottomInset
            val firstRowY = lastRowY - (thingRows - 1) * rowGap
            val homesBottom = firstRowY - itemPx * 0.75f
            val homeH = ((homesBottom - h * 0.03f - pad * (rows - 1)) / rows)
                .coerceAtMost(if (rows == 1) h * 0.36f else h * 0.24f)
            val homeRect = homes.indices.map { i -> Offset(pad + (i % cols) * (homeW + pad), h * 0.03f + (i / cols) * (homeH + pad)) }
            val starts = things.indices.map { k ->
                val row = k / perRow; val col = k % perRow
                val inRow = if (row == 0) perRow else things.size - perRow
                Offset(w * (col + 0.5f) / inRow, firstRowY + row * rowGap)
            }
            val pos = remember(round.id, w, h) { things.indices.map { Animatable(starts[it], Offset.VectorConverter) } }

            fun homeAt(p: Offset) = homes.indices.firstOrNull { i ->
                val o = homeRect[i]; p.x in o.x..(o.x + homeW) && p.y in o.y..(o.y + homeH + itemPx * 0.4f)
            }

            fun drop(k: Int) {
                val hi = homeAt(pos[k].value)
                val t = things[k]
                if (hi == null) { scope.launch { pos[k].animateTo(starts[k], spring(dampingRatio = 0.5f)) }; return }
                val home = homes[hi]
                busy = true
                scope.launch {
                    if (home.key == t.home) {
                        placed[k] = home.key
                        homeBounce[home.key] = (homeBounce[home.key] ?: 0) + 1
                        onTap()
                        onSay(if (home.label.length <= 3 && home.label.first().isLetter()) "${home.label.first().uppercase()} for ${t.label}!" else "${t.label}! Yes!")
                        if (placed.size == things.size) { delay(900); onSay("All home! Yay!"); delay(1300); onDone(true) }
                    } else {
                        onSay("Not this home!"); onMiss()
                        pos[k].animateTo(starts[k], spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
                    }
                    busy = false
                }
            }

            // Homes.
            homes.forEachIndexed { i, home ->
                val inside = placed.filterValues { it == home.key }.keys.map { things[it] }
                Column(
                    Modifier
                        .offset { IntOffset(homeRect[i].x.roundToInt(), homeRect[i].y.roundToInt()) }
                        .size(with(density) { homeW.toDp() }, with(density) { homeH.toDp() })
                        .jelly(trigger = homeBounce[home.key])
                        .clip(RoundedCornerShape(28.dp))
                        .background(listOf(Color(0xFFFFE9C7), Color(0xFFDFF4F2), Color(0xFFEDE7FF), Color(0xFFFFDDD4))[i % 4])
                        .border(4.dp, listOf(CatoPalette.Amber, CatoPalette.Teal, CatoPalette.Violet, CatoPalette.Coral)[i % 4], RoundedCornerShape(28.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (home.icon.isNotBlank()) EmojiArt(home.icon, size = if (rows == 1) 54.dp else 36.dp)
                    EmojiText(
                        home.label,
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black, fontSize = if (home.label.length <= 3) (if (rows == 1) 34.sp else 28.sp) else 18.sp),
                        color = CatoPalette.Ink, textAlign = TextAlign.Center,
                    )
                    // What's already home, small.
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        inside.take(4).forEach { Thing(it.label, it.emoji, size = 28) }
                    }
                }
            }

            // Things to drag.
            things.forEachIndexed { k, t ->
                if (k in placed) return@forEachIndexed
                var touched by remember(round.id, k) { mutableStateOf(0) }
                Box(
                    Modifier
                        .offset { IntOffset((pos[k].value.x - itemPx / 2).roundToInt(), (pos[k].value.y - itemPx / 2).roundToInt()) }
                        .size(with(density) { itemPx.toDp() })
                        .jelly(trigger = if (touched > 0) touched else null)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, CatoPalette.Line, CircleShape)
                        .pointerInput(round.id, k) { detectTapGestures { touched++; onSay(t.label) } }
                        .pointerInput(round.id, k, "drag") {
                            detectDragGestures(
                                onDragStart = { if (!busy) { touched++; onSay(t.label) } },
                                onDrag = { c, d -> if (!busy) { c.consume(); scope.launch { pos[k].snapTo(pos[k].value + d) } } },
                                onDragEnd = { if (!busy) drop(k) },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) { Thing(t.label, t.emoji, size = 50) }
            }
        }
    }
}

/** Pictures for the word-named homes, so a child who can't read still knows which is which. */
private fun homeIcon(name: String): String = when (name.lowercase()) {
    "big" -> "🐘"; "small" -> "🐜"; "tall" -> "🦒"; "short" -> "🐢"; "heavy" -> "🪨"; "light" -> "🪶"
    "farm" -> "🚜"; "wild" -> "🌳"; "fruit" -> "🧺"; "vegetable" -> "🥕"; "kind" -> "😊"; "not kind" -> "😢"
    else -> ""
}

// =================================================================== Peekaboo

enum class Peek { BUSH, HOUSE, CLOUD }

/**
 * Friends are hiding behind bushes (or house windows, or clouds). The voice says who to
 * find. Tap a hiding place: whoever's there pops up and says hello. Found the right one?
 * It jumps and cheers. If not, it giggles and hides again.
 */
@Composable
fun PeekabooGame(round: GameRound, place: Peek, onDone: (Boolean) -> Unit, onSay: (String) -> Unit, onTap: () -> Unit, onMiss: () -> Unit = {}) {
    val spots = remember(round.id) { round.options.shuffled(kotlin.random.Random(round.id.hashCode() + 5)) }
    val up = remember(round.id) { spots.map { Animatable(0f) } }
    var found by remember(round.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ToyTitle(round.prompt)
        Spacer(Modifier.height(10.dp))
        val cols = 2
        for (row in spots.indices.chunked(cols)) {
            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (i in row) {
                    val o = spots[i]
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(round.id, i) {
                                detectTapGestures {
                                    if (found || up[i].isRunning) return@detectTapGestures
                                    onTap()
                                    scope.launch {
                                        up[i].animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
                                        if (o.correct) {
                                            found = true
                                            onSay("Peekaboo! ${o.label}!")
                                            delay(1700); onDone(true)
                                        } else {
                                            onSay("${o.label}! Hee hee!"); onMiss()
                                            delay(1100)
                                            up[i].animateTo(0f, tween(300))
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        // The friend, rising out from behind its hiding place.
                        Column(
                            Modifier.padding(bottom = 40.dp).graphicsLayer {
                                translationY = (1f - up[i].value) * 160f
                                alpha = up[i].value.coerceIn(0f, 1f)
                            },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Thing(o.label, o.emoji, size = 74)
                            Text(o.label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CatoPalette.Ink)
                        }
                        HidingPlace(place, i, Modifier.fillMaxWidth().height(130.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun HidingPlace(place: Peek, seed: Int, modifier: Modifier) {
    val sway by rememberInfiniteTransition(label = "hide").animateFloat(
        -1f, 1f, infiniteRepeatable(tween(1600 + seed * 200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "s",
    )
    Canvas(modifier) {
        val w = size.width; val h = size.height
        when (place) {
            Peek.BUSH -> {
                val g = Color(0xFF6FC36A); val d = Color(0xFF55A752)
                listOf(0.25f to 0.32f, 0.5f to 0.4f, 0.75f to 0.32f).forEachIndexed { k, (x, r) ->
                    drawCircle(if (k == 1) d else g, w * r * 0.6f, Offset(w * x + sway * 4f, h * 0.62f))
                }
                drawRect(g, topLeft = Offset(w * 0.08f, h * 0.62f), size = Size(w * 0.84f, h * 0.38f))
            }
            Peek.HOUSE -> {
                drawRoundRect(Color(0xFFF7C59F), topLeft = Offset(w * 0.12f, h * 0.15f), size = Size(w * 0.76f, h * 0.85f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f))
                drawRect(Color(0xFF9AD0F5), topLeft = Offset(w * 0.25f, h * 0.3f), size = Size(w * 0.5f, h * 0.4f))
                drawRect(Color(0xFFE57691), topLeft = Offset(w * 0.25f, h * 0.3f), size = Size(w * 0.23f + sway * 3f, h * 0.4f))
                drawRect(Color(0xFFE57691), topLeft = Offset(w * 0.52f - sway * 3f, h * 0.3f), size = Size(w * 0.23f, h * 0.4f))
            }
            Peek.CLOUD -> {
                val c = Color.White
                listOf(0.3f to 0.25f, 0.5f to 0.33f, 0.7f to 0.25f).forEach { (x, r) ->
                    drawCircle(c, w * r * 0.7f, Offset(w * x + sway * 5f, h * 0.6f))
                }
                drawRect(c, topLeft = Offset(w * 0.15f, h * 0.6f), size = Size(w * 0.7f, h * 0.3f))
            }
        }
    }
}

// =================================================================== Hungry bear

/**
 * "Give me MORE!" A bear sits at the top; plates of food at the bottom. Tap a plate and the
 * bear trots over. The right plate: munch munch, hearts. The wrong one: "Hmm, no!" and back.
 */
@Composable
fun HungryBearGame(round: GameRound, onDone: (Boolean) -> Unit, onSay: (String) -> Unit, onTap: () -> Unit, onMiss: () -> Unit = {}) {
    val more = round.prompt.contains("MORE", ignoreCase = true)
    val plates = remember(round.id) { round.options.shuffled(kotlin.random.Random(round.id.hashCode() + 9)) }
    val bearX = remember(round.id) { Animatable(0.5f) }
    val bearY = remember(round.id) { Animatable(0f) }
    val shake = remember(round.id) { Animatable(0f) }
    var eaten by remember(round.id) { mutableStateOf<Int?>(null) }
    var busy by remember(round.id) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // The opening line is spoken by the game (see toySpeech), so the 🔊 button can repeat it.

    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        ToyTitle(if (more) "I want MORE! 🍪" else "I want LESS! 🤏")
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val density = LocalDensity.current
            val w = with(density) { maxWidth.toPx() }
            val h = with(density) { maxHeight.toPx() }
            val plateY = h * 0.72f
            val plateX = plates.indices.map { (it + 0.5f) / plates.size }

            // The bear.
            EmojiArt(
                if (eaten != null) "😋" else "🐻", size = 120.dp,
                modifier = Modifier.offset {
                    IntOffset(
                        (bearX.value * w - with(density) { 60.dp.toPx() } + shake.value * 20f).roundToInt(),
                        (h * 0.05f + bearY.value * (plateY - h * 0.38f)).roundToInt(),
                    )
                },
            )

            plates.forEachIndexed { i, o ->
                Box(
                    Modifier
                        .offset { IntOffset((plateX[i] * w - w / plates.size / 2).roundToInt(), (plateY - with(density) { 60.dp.toPx() }).roundToInt()) }
                        .size(with(density) { (w / plates.size).toDp() }, 120.dp)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(60.dp))
                        .background(Color.White)
                        .border(5.dp, CatoPalette.Line, RoundedCornerShape(60.dp))
                        .pointerInput(round.id, i) {
                            detectTapGestures {
                                if (busy || eaten != null) return@detectTapGestures
                                busy = true; onTap()
                                scope.launch {
                                    launch { bearX.animateTo(plateX[i], tween(500)) }
                                    bearY.animateTo(1f, tween(500))
                                    if (o.correct) {
                                        eaten = i
                                        onSay(if (more) "Yum! More!" else "Yes! Less!")
                                        delay(1700); onDone(true)
                                    } else {
                                        onSay("Hmm, no!"); onMiss()
                                        repeat(3) { shake.animateTo(1f, tween(70)); shake.animateTo(-1f, tween(70)) }
                                        shake.animateTo(0f, tween(70))
                                        launch { bearX.animateTo(0.5f, tween(450)) }
                                        bearY.animateTo(0f, tween(450))
                                        busy = false
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (eaten != i) EmojiText(o.label, style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                    else EmojiArt("💕", size = 44.dp)
                }
            }
        }
    }
}
