package com.catokids.app.ui.creator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.catokids.app.data.model.*
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private data class OptionDraft(val label: String, val emoji: String, val correct: Boolean)
private data class PairDraft(val left: String, val right: String, val emoji: String)
private data class ItemDraft(val label: String, val emoji: String, val bucket: String)

/**
 * The teacher-facing game authoring tool. One screen, not nine: the fields that show
 * depend on [GameType], but every game engine already reads from the same [GameRound]
 * shape, so this is really just a form over that shape rather than nine bespoke editors.
 */
@Composable
fun GameBuilderScreen(
    profileId: String?,
    schoolId: String?,
    onSave: (CustomGame) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var gameType by remember { mutableStateOf(GameType.QUIZ) }
    var subject by remember { mutableStateOf(SubjectId.LETTER_LAND) }
    var grade by remember { mutableStateOf(Grade.LKG) }
    val rounds = remember { mutableStateListOf<GameRound>() }

    // ---- the round currently being composed ----
    var prompt by remember { mutableStateOf("") }
    var speak by remember { mutableStateOf("") }
    var glyph by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var count by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf<OptionDraft>() }
    val pairs = remember { mutableStateListOf<PairDraft>() }
    var bucketA by remember { mutableStateOf("") }
    var bucketB by remember { mutableStateOf("") }
    val bucketItems = remember { mutableStateListOf<ItemDraft>() }

    fun clearDraft() {
        prompt = ""; speak = ""; glyph = ""; target = ""; emoji = ""; count = ""; explanation = ""
        options.clear(); pairs.clear(); bucketItems.clear()
    }

    fun addRound() {
        val round = when (gameType) {
            GameType.TRACE -> GameRound(
                id = "r${rounds.size + 1}", glyph = glyph, prompt = prompt.ifBlank { "Trace it!" },
                speak = speak.ifBlank { glyph }, emoji = emoji,
            )
            GameType.JUMBLED_WORD -> GameRound(
                id = "r${rounds.size + 1}", target = target.uppercase(), emoji = emoji,
                prompt = prompt.ifBlank { "Build the word" }, speak = target,
            )
            GameType.MATCH_PAIRS -> GameRound(
                id = "r${rounds.size + 1}", prompt = prompt.ifBlank { "Match them up!" }, speak = prompt,
                pairs = pairs.map { MatchPair(it.left, it.right, it.emoji) },
            )
            GameType.SORT_BUCKETS -> GameRound(
                id = "r${rounds.size + 1}", prompt = prompt.ifBlank { "Sort it out!" }, speak = prompt,
                buckets = listOfNotNull(bucketA.ifBlank { null }, bucketB.ifBlank { null }),
                items = bucketItems.map { BucketItem(it.label, it.emoji, it.bucket) },
            )
            GameType.COUNT_TAP -> {
                val n = count.toIntOrNull() ?: 0
                GameRound(
                    id = "r${rounds.size + 1}", emoji = emoji, count = n, target = n.toString(),
                    prompt = prompt.ifBlank { "Count and tap!" }, speak = prompt,
                    options = options.map { Option(it.label, it.emoji, it.correct) },
                )
            }
            GameType.TAP_ALL -> GameRound(
                id = "r${rounds.size + 1}",
                target = options.firstOrNull { it.correct }?.label ?: target,
                prompt = prompt.ifBlank { "Find them all!" }, speak = speak.ifBlank { null },
                options = options.map { Option(it.label, it.emoji, it.correct) },
            )
            GameType.QUIZ, GameType.LISTEN_PICK, GameType.SHAPE_HUNT -> GameRound(
                id = "r${rounds.size + 1}",
                target = options.firstOrNull { it.correct }?.label.orEmpty(),
                prompt = prompt, speak = speak.ifBlank { prompt },
                explanation = explanation.ifBlank { null },
                options = options.map { Option(it.label, it.emoji, it.correct) },
            )
        }
        rounds.add(round)
        clearDraft()
    }

    val roundReady = when (gameType) {
        GameType.TRACE -> glyph.isNotBlank()
        GameType.JUMBLED_WORD -> target.isNotBlank()
        GameType.MATCH_PAIRS -> pairs.size >= 2
        GameType.SORT_BUCKETS -> bucketA.isNotBlank() && bucketB.isNotBlank() && bucketItems.isNotEmpty()
        GameType.COUNT_TAP -> count.toIntOrNull() != null && options.any { it.correct }
        else -> prompt.isNotBlank() && options.any { it.correct } && options.size >= 2
    }
    val canSave = title.isNotBlank() && rounds.isNotEmpty()

    CatoBackdrop(top = CatoPalette.PeriwinkleSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "Build a game", subtitle = "🎮 Your own rounds, your own engine", onBack = onBack)
            }
            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CatoTextField(value = title, onValueChange = { title = it }, label = "Game title", leading = gameType.emoji)
                    CatoMultilineField(value = description, onValueChange = { description = it }, label = "Short description", minLines = 2)

                    Text("Engine", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = GameType.entries, selected = gameType,
                        label = { it.title }, emoji = { it.emoji },
                        onSelect = { if (it != gameType) { gameType = it; rounds.clear(); clearDraft() } },
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Grade", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                            Spacer(Modifier.height(8.dp))
                            CatoChipRow(options = Grade.entries, selected = grade, label = { it.label }, onSelect = { grade = it })
                        }
                    }
                    Text("Book", style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                    CatoChipRow(
                        options = SubjectId.entries, selected = subject,
                        label = { it.title }, emoji = { it.emoji }, onSelect = { subject = it },
                    )

                    HorizontalDivider()
                    Text("Round ${rounds.size + 1}", style = MaterialTheme.typography.titleMedium, color = CatoPalette.Ink)
                }
            }

            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (gameType) {
                        GameType.TRACE -> {
                            CatoTextField(value = glyph, onValueChange = { glyph = it }, label = "Letter or word to trace")
                            CatoTextField(value = prompt, onValueChange = { prompt = it }, label = "Prompt shown to the child")
                            CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "Picture emoji (optional)")
                        }
                        GameType.JUMBLED_WORD -> {
                            CatoTextField(value = target, onValueChange = { target = it }, label = "Word to build")
                            CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "Picture emoji")
                            CatoTextField(value = prompt, onValueChange = { prompt = it }, label = "Hint")
                        }
                        GameType.MATCH_PAIRS -> PairEditorSection(pairs)
                        GameType.SORT_BUCKETS -> {
                            CatoTextField(value = bucketA, onValueChange = { bucketA = it }, label = "Basket A name")
                            CatoTextField(value = bucketB, onValueChange = { bucketB = it }, label = "Basket B name")
                            BucketItemEditorSection(bucketItems, listOfNotNull(bucketA.ifBlank { null }, bucketB.ifBlank { null }))
                        }
                        GameType.COUNT_TAP -> {
                            CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "What to count (emoji)")
                            CatoTextField(value = count, onValueChange = { count = it.filter(Char::isDigit) }, label = "How many", keyboardType = KeyboardType.Number)
                            CatoTextField(value = prompt, onValueChange = { prompt = it }, label = "Prompt")
                            Text("Answer choices — mark the right one", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
                            OptionEditorSection(options, labelHint = "Number")
                        }
                        GameType.TAP_ALL -> {
                            CatoTextField(value = prompt, onValueChange = { prompt = it }, label = "Prompt, e.g. \"Tap every letter A\"")
                            Text("Choices — mark every correct one", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
                            OptionEditorSection(options, labelHint = "Choice")
                        }
                        GameType.QUIZ, GameType.LISTEN_PICK, GameType.SHAPE_HUNT -> {
                            CatoTextField(value = prompt, onValueChange = { prompt = it }, label = "Question")
                            CatoTextField(value = explanation, onValueChange = { explanation = it }, label = "Explanation shown after answering (optional)")
                            Text("Answer choices — mark the right one", style = MaterialTheme.typography.labelMedium, color = CatoPalette.InkSoft)
                            OptionEditorSection(options, labelHint = "Choice")
                        }
                    }

                    CatoOutlineButton(
                        text = "Add this round",
                        leading = "➕",
                        onClick = { addRound() },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (!roundReady) {
                        Text(
                            "Fill in the round above to add it.",
                            style = MaterialTheme.typography.labelSmall,
                            color = CatoPalette.InkSoft,
                        )
                    }
                }
            }

            if (rounds.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)) }
                item { SectionHeader("Rounds so far (${rounds.size})", Modifier.padding(horizontal = 20.dp)) }
                item { Spacer(Modifier.height(8.dp)) }
                items(rounds, key = { it.id }) { round ->
                    Row(
                        Modifier
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CatoPalette.Cloud)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            round.prompt.ifBlank { round.glyph.ifBlank { round.target } },
                            style = MaterialTheme.typography.bodyMedium,
                            color = CatoPalette.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "✕",
                            color = CatoPalette.Error,
                            modifier = Modifier.clickable { rounds.remove(round) },
                        )
                    }
                }
            }

            item {
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Spacer(Modifier.height(16.dp))
                    CatoButton(
                        text = "Save game (${rounds.size} rounds)",
                        leading = "✅",
                        enabled = canSave,
                        emphasise = true,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSave(
                                CustomGame(
                                    id = "",
                                    createdBy = profileId,
                                    schoolId = schoolId,
                                    title = title.trim(),
                                    description = description.trim(),
                                    subject = subject,
                                    grade = grade,
                                    gameType = gameType,
                                    content = LessonContent(rounds = rounds.toList()),
                                )
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionEditorSection(options: MutableList<OptionDraft>, labelHint: String) {
    var label by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var correct by remember { mutableStateOf(false) }

    options.forEachIndexed { index, opt ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (opt.correct) CatoPalette.SuccessSoft else CatoPalette.Cloud)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (opt.emoji.isNotBlank()) { EmojiArt(opt.emoji, size = 20.dp); Spacer(Modifier.width(8.dp)) }
            Text(opt.label, style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink, modifier = Modifier.weight(1f))
            if (opt.correct) Text("✓ correct", style = MaterialTheme.typography.labelSmall, color = CatoPalette.SuccessDeep)
            Spacer(Modifier.width(8.dp))
            Text("✕", color = CatoPalette.Error, modifier = Modifier.clickable { options.removeAt(index) })
        }
        Spacer(Modifier.height(6.dp))
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CatoTextField(value = label, onValueChange = { label = it }, label = labelHint, modifier = Modifier.weight(2f))
        CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "Emoji", modifier = Modifier.weight(1f))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = correct, onCheckedChange = { correct = it })
        Text("This is a correct answer", style = MaterialTheme.typography.bodySmall, color = CatoPalette.Ink)
    }
    CatoOutlineButton(
        text = "Add choice",
        onClick = {
            if (label.isNotBlank()) {
                options.add(OptionDraft(label.trim(), emoji.trim(), correct))
                label = ""; emoji = ""; correct = false
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PairEditorSection(pairs: MutableList<PairDraft>) {
    var left by remember { mutableStateOf("") }
    var right by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }

    pairs.forEachIndexed { index, p ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CatoPalette.Cloud)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (p.emoji.isNotBlank()) { EmojiArt(p.emoji, size = 20.dp); Spacer(Modifier.width(8.dp)) }
            Text("${p.left}  ↔  ${p.right}", style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink, modifier = Modifier.weight(1f))
            Text("✕", color = CatoPalette.Error, modifier = Modifier.clickable { pairs.removeAt(index) })
        }
        Spacer(Modifier.height(6.dp))
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CatoTextField(value = left, onValueChange = { left = it }, label = "Left", modifier = Modifier.weight(1f))
        CatoTextField(value = right, onValueChange = { right = it }, label = "Right", modifier = Modifier.weight(1f))
    }
    CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "Emoji (optional)")
    CatoOutlineButton(
        text = "Add pair",
        onClick = {
            if (left.isNotBlank() && right.isNotBlank()) {
                pairs.add(PairDraft(left.trim(), right.trim(), emoji.trim()))
                left = ""; right = ""; emoji = ""
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BucketItemEditorSection(items: MutableList<ItemDraft>, buckets: List<String>) {
    var label by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    var bucket by remember(buckets) { mutableStateOf(buckets.firstOrNull().orEmpty()) }

    items.forEachIndexed { index, it2 ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CatoPalette.Cloud)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (it2.emoji.isNotBlank()) { EmojiArt(it2.emoji, size = 20.dp); Spacer(Modifier.width(8.dp)) }
            Text("${it2.label} → ${it2.bucket}", style = MaterialTheme.typography.bodyMedium, color = CatoPalette.Ink, modifier = Modifier.weight(1f))
            Text("✕", color = CatoPalette.Error, modifier = Modifier.clickable { items.removeAt(index) })
        }
        Spacer(Modifier.height(6.dp))
    }

    if (buckets.isEmpty()) {
        Text("Name both baskets above first.", style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CatoTextField(value = label, onValueChange = { label = it }, label = "Item", modifier = Modifier.weight(2f))
        CatoTextField(value = emoji, onValueChange = { emoji = it }, label = "Emoji", modifier = Modifier.weight(1f))
    }
    CatoChipRow(options = buckets, selected = bucket, label = { it }, onSelect = { bucket = it })
    CatoOutlineButton(
        text = "Add item",
        onClick = {
            if (label.isNotBlank() && bucket.isNotBlank()) {
                items.add(ItemDraft(label.trim(), emoji.trim(), bucket))
                label = ""; emoji = ""
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
}
