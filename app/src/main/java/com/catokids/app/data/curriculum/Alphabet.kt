package com.catokids.app.data.curriculum

/** Letter → key word → picture, matching the Letter Land workbooks. */
internal data class LetterInfo(val letter: Char, val word: String, val emoji: String)

internal val ALPHABET = listOf(
    LetterInfo('A', "Apple",  "🍎"), LetterInfo('B', "Ball",   "⚽"),
    LetterInfo('C', "Cat",    "🐱"), LetterInfo('D', "Dog",    "🐶"),
    LetterInfo('E', "Egg",    "🥚"), LetterInfo('F', "Fish",   "🐟"),
    LetterInfo('G', "Grapes", "🍇"), LetterInfo('H', "Hat",    "🎩"),
    LetterInfo('I', "Ice",    "🍦"), LetterInfo('J', "Jet",    "✈️"),
    LetterInfo('K', "Key",    "🔑"), LetterInfo('L', "Lion",   "🦁"),
    LetterInfo('M', "Moon",   "🌙"), LetterInfo('N', "Nose",   "👃"),
    LetterInfo('O', "Orange", "🍊"), LetterInfo('P', "Pig",    "🐷"),
    LetterInfo('Q', "Queen",  "👸"), LetterInfo('R', "Rabbit", "🐰"),
    LetterInfo('S', "Sun",    "☀️"), LetterInfo('T', "Tree",   "🌳"),
    LetterInfo('U', "Umbrella","☂️"), LetterInfo('V', "Violin","🎻"),
    LetterInfo('W', "Watch",  "⌚"), LetterInfo('X', "Box",    "📦"),
    LetterInfo('Y', "Yak",    "🐂"), LetterInfo('Z', "Zebra",  "🦓"),
)

internal fun letter(c: Char): LetterInfo = ALPHABET.first { it.letter == c.uppercaseChar() }

/** A mixed pool of letters that always contains [target] a few times. */
internal fun letterPool(target: Char, hits: Int = 4, size: Int = 12, seed: Int = 0): List<String> {
    val rnd = kotlin.random.Random((target.code * 7919 + seed).toLong())
    val others = ALPHABET.map { it.letter }.filter { it != target.uppercaseChar() }
    val pool = mutableListOf<String>()
    repeat(hits) { pool += if (it % 2 == 0) target.uppercaseChar().toString() else target.lowercaseChar().toString() }
    while (pool.size < size) {
        val c = others[rnd.nextInt(others.size)]
        pool += if (rnd.nextBoolean()) c.toString() else c.lowercaseChar().toString()
    }
    // Deterministic shuffle so every child sees the same board.
    return pool.shuffled(kotlin.random.Random((target.code + seed).toLong()))
}

/** Marks both cases of the target letter as correct. */
internal fun tapLetterRound(id: String, target: Char, prompt: String, seed: Int = 0) =
    com.catokids.app.data.model.GameRound(
        id = id,
        target = target.uppercaseChar().toString(),
        prompt = prompt,
        speak = prompt,
        options = letterPool(target, seed = seed).map {
            com.catokids.app.data.model.Option(
                label = it,
                correct = it.equals(target.toString(), ignoreCase = true),
            )
        },
    )
