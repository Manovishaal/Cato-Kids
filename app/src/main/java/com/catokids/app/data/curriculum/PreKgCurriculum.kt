package com.catokids.app.data.curriculum

import com.catokids.app.data.model.*
import com.catokids.app.data.model.Grade.PREKG
import com.catokids.app.data.model.SubjectId.*

/**
 * Pre-KG — Level 1 of the Cato Kids books, following the Canva workbooks:
 *   Letter Land  · PreKG English — A to Z, six words a letter, "I have learnt so much" checks
 *   Number Land  · PreKG Maths   — comparing, numbers 0 to 20, colours, shapes, odd one out
 *   Know My World· PreKG EVS     — me, family, school, food, trees, animals, birds, seasons,
 *                                  keeping clean and good manners
 */
internal object PreKgCurriculum {

    private val TEEN_NAMES = listOf("Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
        "Sixteen", "Seventeen", "Eighteen", "Nineteen", "Twenty")

    /** Two picture words per letter, taken from the "I like reading 'Aa' words" pages. */
    private data class BookWord(val word: String, val emoji: String)

    private val BOOK_WORDS: Map<Char, Pair<BookWord, BookWord>> = mapOf(
        'A' to (BookWord("Apple", "🍎") to BookWord("Ant", "🐜")),
        'B' to (BookWord("Ball", "⚽") to BookWord("Butterfly", "🦋")),
        'C' to (BookWord("Cat", "🐱") to BookWord("Carrot", "🥕")),
        'D' to (BookWord("Dog", "🐶") to BookWord("Duck", "🦆")),
        'E' to (BookWord("Egg", "🥚") to BookWord("Elephant", "🐘")),
        'F' to (BookWord("Fish", "🐟") to BookWord("Frog", "🐸")),
        'G' to (BookWord("Grapes", "🍇") to BookWord("Goat", "🐐")),
        'H' to (BookWord("Hat", "🎩") to BookWord("Hen", "🐔")),
        'I' to (BookWord("Ice cream", "🍦") to BookWord("Island", "🏝️")),
        'J' to (BookWord("Jacket", "🧥") to BookWord("Jeep", "🚙")),
        'K' to (BookWord("Key", "🔑") to BookWord("Kite", "🪁")),
        'L' to (BookWord("Lion", "🦁") to BookWord("Lemon", "🍋")),
        'M' to (BookWord("Moon", "🌙") to BookWord("Monkey", "🐵")),
        'N' to (BookWord("Nose", "👃") to BookWord("Nest", "🪺")),
        'O' to (BookWord("Orange", "🍊") to BookWord("Owl", "🦉")),
        'P' to (BookWord("Pig", "🐷") to BookWord("Parrot", "🦜")),
        'Q' to (BookWord("Queen", "👸") to BookWord("Question mark", "❓")),
        'R' to (BookWord("Rabbit", "🐰") to BookWord("Rainbow", "🌈")),
        'S' to (BookWord("Sun", "☀️") to BookWord("Sheep", "🐑")),
        'T' to (BookWord("Tree", "🌳") to BookWord("Tomato", "🍅")),
        'U' to (BookWord("Umbrella", "☂️") to BookWord("Underwear", "🩲")),
        // Volcano first: the van picture looks like a bus, which confuses the B round.
        'V' to (BookWord("Volcano", "🌋") to BookWord("Van", "🚐")),
        'W' to (BookWord("Watch", "⌚") to BookWord("Watermelon", "🍉")),
        'X' to (BookWord("X-ray", "🩻") to BookWord("X-ray", "🩻")),
        'Y' to (BookWord("Yak", "🐂") to BookWord("Yo-yo", "🪀")),
        'Z' to (BookWord("Zebra", "🦓") to BookWord("Zero", "0️⃣")),
    )

    private fun first(c: Char) = BOOK_WORDS.getValue(c).first
    private fun second(c: Char) = BOOK_WORDS.getValue(c).second

    /** The book checks progress after D, H, L, P, T and Z, so the letters come in these groups. */
    private val LETTER_GROUPS: List<List<Char>> = listOf(
        "ABCD", "EFGH", "IJKL", "MNOP", "QRST", "UVWXYZ",
    ).map { it.toList() }

    /** "Which one starts with A?" — the right picture plus three from other letters. */
    private fun startsWithRound(id: String, c: Char, seed: Long): GameRound {
        val word = first(c)
        val others = BOOK_WORDS.keys.filter { it != c }
            .shuffled(kotlin.random.Random(c.code + seed)).take(3)
            .map { first(it).let { w -> w.word to w.emoji } }
        return pickRound(
            id = id,
            prompt = "Which one starts with $c ${c.lowercaseChar()}?",
            speak = "$c. $c is for ${word.word}",
            correct = word.word to word.emoji,
            distractors = others,
            explanation = "$c is for ${word.word}.",
        )
    }

    /**
     * Lessons follow the PreKG English workbook page order: the letters A–Z in the book's
     * review groups, with the first tracing pages ("Let's trace the line") after L, then the
     * alphabet check and the end-of-book quiz.
     */
    val letterLand: List<Lesson> = lessons(LETTER_LAND, PREKG) {

        LETTER_GROUPS.forEachIndexed { g, group ->
            val range = "${group.first()}–${group.last()}"
            val spaced = group.joinToString(" ")
            val key = "${group.first()}_${group.last()}".lowercase()

            lesson(
                id = "meet_$key", title = "Meet $spaced", subtitle = "I like reading these words",
                description = "Hear each letter and find the picture that starts with it.",
                game = GameType.LISTEN_PICK, intro = if (g == 0) "Feed the monster!" else "Tap the picture that starts with the sound you hear.",
                rounds = group.map { c -> startsWithRound("p_$c", c, seed = 5L + g) },
                crafts = PreKgCrafts.forLetters(group),
            )

            lesson(
                id = "write_$key", title = "Write $spaced", subtitle = "Trace big letters",
                description = "Trace the big letters $range.",
                game = GameType.TRACE, intro = "Trace the letter with your finger.",
                rounds = group.map { c ->
                    val w = first(c)
                    traceRound("t_$c", c.toString(), "$c for ${w.word}", "$c. $c for ${w.word}", w.emoji)
                },
            )

            lesson(
                id = "learnt_$key", title = "I have learnt $range", subtitle = "Match the picture to its letter",
                description = "Which letter does each picture start with?",
                game = GameType.MATCH_PAIRS, intro = "Tap a picture, then the letter it starts with.",
                rounds = listOf(
                    matchRound("m1", "Match the picture to its letter",
                        group.map { c -> MatchPair(second(c).word, "$c ${c.lowercaseChar()}", second(c).emoji) }),
                ),
            )

            if (g == 0) {
                lesson(
                    id = "find_a", title = "Find the letters", subtitle = "Circle them all",
                    description = "Tap every A, B, C and D you can see — big or small.",
                    game = GameType.TAP_ALL, intro = "Tap every letter I ask for — big or small!",
                    rounds = group.mapIndexed { i, c ->
                        tapLetterRound("f_$c", c, "Tap all the letter $c", seed = i + 1)
                    },
                )
            }

            // The book's first tracing pages ("Let's trace the line and match the
            // pictures") come straight after the I–L review.
            if (g == 2) {
                lesson(
                    id = "strokes", title = "Trace the lines", subtitle = "Let's trace the line",
                    description = "Standing, sleeping and sloping lines — the first strokes before writing.",
                    game = GameType.TRACE, intro = "Follow the dots with your finger!",
                    rounds = listOf(
                        traceRound("l1", "|",  "Standing line",  "Standing line, top to bottom"),
                        traceRound("l2", "—",  "Sleeping line",  "Sleeping line, left to right"),
                        traceRound("l3", "/",  "Sloping line",   "Sloping line"),
                        traceRound("l4", "C",  "Curve",          "Curvy line"),
                        traceRound("l5", "O",  "Round and round","Round like a ball"),
                    ),
                )
            }
        }

        lesson(
            id = "big_small", title = "Big and small letters", subtitle = "Do I know all my alphabets?",
            description = "Every big letter has a small partner — all the way from A to Z.",
            game = GameType.MATCH_PAIRS, intro = "Match each big letter to its small letter.",
            rounds = LETTER_GROUPS.mapIndexed { i, group ->
                matchRound("m${i + 1}", "Match big and small",
                    group.map { MatchPair(it.toString(), it.lowercaseChar().toString(), first(it).emoji) })
            },
        )

        lesson(
            id = "quiz", title = "Letter Land quiz", subtitle = "Congratulations — you did it!",
            description = "A short quiz on the whole alphabet.",
            game = GameType.QUIZ, intro = "Ten questions. Take your time!",
            // Feed-the-monster rounds across the whole alphabet.
            rounds = listOf('A', 'C', 'F', 'H', 'K', 'M', 'P', 'R', 'T', 'Z').map { c -> startsWithRound("q_$c", c, seed = 21L) },
        )
    }

    val numberLand: List<Lesson> = lessons(NUMBER_LAND, PREKG) {

        lesson(
            id = "big_small_size", title = "Big and small", subtitle = "Sorting by size",
            description = "Put the big things and the small things in the right basket.",
            game = GameType.SORT_BUCKETS, intro = "Drag each picture into Big or Small.",
            rounds = listOf(
                sortRound("s1", "Big or small?", listOf("Big", "Small"), listOf(
                    BucketItem("Elephant", "🐘", "Big"),
                    BucketItem("Ant",      "🐜", "Small"),
                    BucketItem("Bus",      "🚌", "Big"),
                    BucketItem("Key",      "🔑", "Small"),
                    BucketItem("Whale",    "🐳", "Big"),
                    BucketItem("Ladybird", "🐞", "Small"),
                )),
            ),
        )

        lesson(
            id = "tall_short", title = "Tall and short", subtitle = "Look up, look down",
            description = "Some things are tall and some are short.",
            game = GameType.SORT_BUCKETS, intro = "Drag each picture into Tall or Short.",
            rounds = listOf(
                sortRound("t1", "Tall or short?", listOf("Tall", "Short"), listOf(
                    BucketItem("Giraffe",  "🦒", "Tall"),
                    BucketItem("Mushroom", "🍄", "Short"),
                    BucketItem("Tree",     "🌳", "Tall"),
                    BucketItem("Duck",     "🦆", "Short"),
                    BucketItem("Building", "🏢", "Tall"),
                    BucketItem("Cat",      "🐱", "Short"),
                )),
            ),
        )

        lesson(
            id = "heavy_light", title = "Heavy and light", subtitle = "Can you lift it?",
            description = "Some things are heavy to lift and some are light as a feather.",
            game = GameType.SORT_BUCKETS, intro = "Drag each picture into Heavy or Light.",
            rounds = listOf(
                sortRound("h1", "Heavy or light?", listOf("Heavy", "Light"), listOf(
                    BucketItem("Elephant", "🐘", "Heavy"),
                    BucketItem("Feather",  "🪶", "Light"),
                    BucketItem("Truck",    "🚚", "Heavy"),
                    BucketItem("Leaf",     "🍃", "Light"),
                    BucketItem("Rock",     "🪨", "Heavy"),
                    BucketItem("Balloon",  "🎈", "Light"),
                )),
            ),
        )

        lesson(
            id = "more_less", title = "More and less", subtitle = "Which group has more?",
            description = "Compare two groups and choose.",
            game = GameType.QUIZ, intro = "Look carefully at each group.",
            rounds = listOf(
                pickRound("ml1", "Which has MORE?", "Which has more?", "🍎🍎🍎" to "", listOf("🍎" to "", "🍎🍎" to "")),
                pickRound("ml2", "Which has LESS?", "Which has less?", "⭐" to "", listOf("⭐⭐⭐" to "", "⭐⭐" to "")),
                pickRound("ml3", "Which has MORE?", "Which has more?", "🐟🐟🐟🐟" to "", listOf("🐟🐟" to "", "🐟" to "")),
                pickRound("ml4", "Which has LESS?", "Which has less?", "🎈🎈" to "", listOf("🎈🎈🎈🎈" to "", "🎈🎈🎈" to "")),
            ),
        )

        lesson(
            id = "count_1_5", title = "Count 1 to 5", subtitle = "I am 1, I am 2 …",
            description = "Counting small groups of objects.",
            game = GameType.COUNT_TAP, intro = "Pick and count!",
            rounds = listOf(
                // Foods the 3D "feed the animal" game has models for: pig, cat, bunny,
                // monkey and elephant eat them.
                countRound("c1", "🍎", 1, "How many apples?",       listOf(1, 2, 3)),
                countRound("c2", "🐟", 2, "How many fish?",         listOf(1, 2, 3)),
                countRound("c3", "🥕", 3, "How many carrots?",      listOf(2, 3, 4)),
                countRound("c4", "🍌", 4, "How many bananas?",      listOf(3, 4, 5)),
                countRound("c5", "🍓", 5, "How many strawberries?", listOf(4, 5, 6)),
            ),
        )

        lesson(
            id = "count_6_10", title = "Count 6 to 10", subtitle = "Bigger groups",
            description = "Counting groups of six to ten.",
            game = GameType.COUNT_TAP, intro = "Count carefully, then tap the number.",
            rounds = listOf(
                countRound("c6",  "🍓", 6,  "How many strawberries?", listOf(5, 6, 7)),
                countRound("c7",  "🐞", 7,  "How many ladybirds?",    listOf(6, 7, 8)),
                countRound("c8",  "🌸", 8,  "How many flowers?",      listOf(7, 8, 9)),
                countRound("c9",  "🐤", 9,  "How many chicks?",       listOf(8, 9, 10)),
                countRound("c10", "⚽", 10, "How many balls?",        listOf(8, 9, 10)),
            ),
        )

        lesson(
            id = "colors", title = "I know my colours", subtitle = "Red, yellow, blue",
            description = "Name and find the first colours.",
            game = GameType.SHAPE_HUNT, intro = "Tap the colour you hear.",
            rounds = listOf(
                pickRound("c1", "Tap the red one",    "Red",    "Red" to "🔴",
                    listOf("Blue" to "🔵", "Green" to "🟢", "Yellow" to "🟡")),
                pickRound("c2", "Tap the yellow one", "Yellow", "Yellow" to "🟡",
                    listOf("Blue" to "🔵", "Orange" to "🟠", "Green" to "🟢")),
                pickRound("c3", "Tap the blue one",   "Blue",   "Blue" to "🔵",
                    listOf("Red" to "🔴", "Green" to "🟢", "Purple" to "🟣")),
                pickRound("c4", "Tap the green one",  "Green",  "Green" to "🟢",
                    listOf("Red" to "🔴", "Yellow" to "🟡", "Blue" to "🔵")),
            ),
        )

        lesson(
            id = "shapes", title = "What is my name?", subtitle = "Square, rectangle, triangle, circle",
            description = "Meet the four shapes from the book.",
            game = GameType.SHAPE_HUNT, intro = "Tap the shape I ask for.",
            rounds = listOf(
                pickRound("s1", "Tap the circle",    "Circle",    "Circle" to "⚪",
                    listOf("Square" to "🟥", "Triangle" to "🔺", "Rectangle" to "▭")),
                pickRound("s2", "Tap the square",    "Square",    "Square" to "🟥",
                    listOf("Circle" to "⚪", "Triangle" to "🔺", "Rectangle" to "▭")),
                pickRound("s3", "Tap the triangle",  "Triangle",  "Triangle" to "🔺",
                    listOf("Circle" to "🔵", "Square" to "🟩", "Rectangle" to "▭")),
                pickRound("s4", "Tap the rectangle", "Rectangle", "Rectangle" to "▭",
                    listOf("Circle" to "🔵", "Square" to "🟪", "Triangle" to "🔺")),
            ),
        )

        lesson(
            id = "match_qty", title = "Number and amount", subtitle = "Match the correct number",
            description = "Match each number to the right number of pictures.",
            game = GameType.MATCH_PAIRS, intro = "Match the number to the group.",
            rounds = listOf(
                matchRound("m1", "Match number to amount", listOf(
                    MatchPair("1", "🍓", "🍓"),
                    MatchPair("2", "🍓🍓", "🍓"),
                    MatchPair("3", "🍓🍓🍓", "🍓"),
                    MatchPair("4", "🍓🍓🍓🍓", "🍓"),
                )),
                matchRound("m2", "Match number to amount", listOf(
                    MatchPair("5", "⭐⭐⭐⭐⭐", "⭐"),
                    MatchPair("6", "⭐⭐⭐⭐⭐⭐", "⭐"),
                    MatchPair("2", "⭐⭐", "⭐"),
                    MatchPair("4", "⭐⭐⭐⭐", "⭐"),
                )),
            ),
        )

        lesson(
            id = "count_11_15", title = "Count 11 to 15", subtitle = "I am 11 … I am 15",
            description = "Counting bigger groups, from eleven to fifteen.",
            game = GameType.COUNT_TAP, intro = "Count slowly, one by one.",
            rounds = listOf(
                countRound("c11", "🍎", 11, "How many apples?",   listOf(10, 11, 12)),
                countRound("c12", "⭐", 12, "How many stars?",    listOf(11, 12, 13)),
                countRound("c13", "🐟", 13, "How many fish?",     listOf(12, 13, 14)),
                countRound("c14", "🎈", 14, "How many balloons?", listOf(13, 14, 15)),
                countRound("c15", "🐝", 15, "How many bees?",     listOf(14, 15, 16)),
            ),
        )

        lesson(
            id = "odd_one_out", title = "Pick the odd one out!", subtitle = "Pop the odd one!",
            description = "Three things belong together — find the one that does not.",
            game = GameType.QUIZ, intro = "Look at all four. Which one is different?",
            rounds = listOf(
                pickRound("o1", "🍎 🍎 🍌 🍎", "Pop the odd one!", "🍌" to "", listOf("🍎" to "")),
                pickRound("o2", "🐶 🐱 🚗 🐰", "Pop the odd one!", "🚗" to "",
                    listOf("🐶" to "", "🐱" to "", "🐰" to "")),
                pickRound("o3", "🔺 🔺 🔺 ⚪", "Pop the odd one!", "⚪" to "", listOf("🔺" to "")),
                pickRound("o4", "🍇 🍊 🥕 🍓", "Pop the odd one!", "🥕" to "",
                    listOf("🍇" to "", "🍊" to "", "🍓" to "")),
            ),
        )

        lesson(
            id = "count_16_20", title = "Count 16 to 20", subtitle = "I am 16 … I am 20",
            description = "Counting the biggest groups, from sixteen to twenty.",
            game = GameType.COUNT_TAP, intro = "Count slowly, one by one.",
            rounds = listOf(
                countRound("c16", "🌸", 16, "How many flowers?",   listOf(15, 16, 17)),
                // Three rounds, not five: each one is 16+ taps for a small child.
                countRound("c18", "🍓", 18, "How many strawberries?", listOf(17, 18, 19)),
                countRound("c20", "⚽", 20, "How many balls?",     listOf(18, 19, 20)),
            ),
        )

        lesson(
            id = "trace_num", title = "Write 1 to 5", subtitle = "Trace the numbers",
            description = "Trace numbers one to five.",
            game = GameType.TRACE, intro = "Trace each number with your finger.",
            rounds = listOf(
                traceRound("n1", "1", "One",   "One",   "🍎"),
                traceRound("n2", "2", "Two",   "Two",   "🍎🍎"),
                traceRound("n3", "3", "Three", "Three", "🍎🍎🍎"),
                traceRound("n4", "4", "Four",  "Four",  "🍎🍎🍎🍎"),
                traceRound("n5", "5", "Five",  "Five",  "🍎🍎🍎🍎🍎"),
            ),
        )

        lesson(
            id = "trace_6_10", title = "Write 6 to 10", subtitle = "And zero too",
            description = "Trace six, seven, eight, nine, zero and ten.",
            game = GameType.TRACE, intro = "Trace each number with your finger.",
            rounds = listOf(
                traceRound("n6",  "6",  "Six",   "Six"),
                traceRound("n7",  "7",  "Seven", "Seven"),
                traceRound("n8",  "8",  "Eight", "Eight"),
                traceRound("n9",  "9",  "Nine",  "Nine"),
                traceRound("n0",  "0",  "Zero",  "Zero. Nothing at all"),
                traceRound("n10", "10", "Ten",   "Ten"),
            ),
        )

        lesson(
            id = "trace_11_20", title = "Write 11 to 15", subtitle = "Let's trace the numbers",
            description = "Trace the numbers eleven to fifteen.",
            game = GameType.TRACE, intro = "Trace both digits.",
            rounds = (11..15).map { n -> traceRound("n$n", n.toString(), TEEN_NAMES[n - 11], TEEN_NAMES[n - 11]) },
        )

        // Split from "Write 11 to 20" so a session stays short enough for a 3-year-old.
        lesson(
            id = "trace_16_20", title = "Write 16 to 20", subtitle = "Let's trace the numbers",
            description = "Trace the numbers sixteen to twenty.",
            game = GameType.TRACE, intro = "Trace both digits.",
            rounds = (16..20).map { n -> traceRound("n$n", n.toString(), TEEN_NAMES[n - 11], TEEN_NAMES[n - 11]) },
        )

        lesson(
            id = "quiz", title = "Number Land quiz", subtitle = "I am an expert in my numbers!",
            description = "A short quiz on comparing, counting, shapes and colours.",
            game = GameType.QUIZ, intro = "Let's see what you remember!",
            rounds = listOf(
                pickRound("q1", "🍎🍎🍎  How many apples?", "How many apples?",
                    "3" to "", listOf("1" to "", "2" to "", "5" to ""), "Count them: one, two, three."),
                pickRound("q2", "Find the rectangle!", "Find the rectangle!",
                    "Rectangle" to "▭", listOf("Square" to "🟥", "Triangle" to "🔺", "Circle" to "⚪")),
                pickRound("q3", "Who is tall?", "Who is tall?",
                    "Giraffe" to "🦒", listOf("Cat" to "🐱", "Duck" to "🦆", "Mushroom" to "🍄")),
                pickRound("q4", "Which is light?", "Which is light?",
                    "Feather" to "🪶", listOf("Rock" to "🪨", "Truck" to "🚚", "Elephant" to "🐘")),
                pickRound("q5", "Find yellow!", "Find yellow!",
                    "Yellow" to "🟡", listOf("Blue" to "🔵", "Red" to "🔴", "Green" to "🟢")),
                pickRound("q6", "After 9?", "After nine?",
                    "10" to "", listOf("8" to "", "6" to "", "11" to ""), "After nine comes ten."),
                pickRound("q7", "🐝🐝🐝🐝🐝🐝🐝  How many bees?", "How many bees?",
                    "7" to "", listOf("5" to "", "6" to "", "8" to "")),
            ),
        )
    }

    val knowMyWorld: List<Lesson> = lessons(KNOW_MY_WORLD, PREKG) {

        lesson(
            id = "my_family", title = "This is my family", subtitle = "People who love me",
            description = "Mother, father, sister, brother and grandparents.",
            game = GameType.LISTEN_PICK, intro = "Who is it? Tap the picture.",
            rounds = listOf(
                pickRound("f1", "Who is the baby?",    "Baby",        "Baby" to "👶",
                    listOf("Mother" to "👩", "Father" to "👨", "Grandma" to "👵")),
                pickRound("f2", "Who is the mother?",  "Mother",      "Mother" to "👩",
                    listOf("Baby" to "👶", "Father" to "👨", "Grandpa" to "👴")),
                pickRound("f3", "Who is the father?",  "Father",      "Father" to "👨",
                    listOf("Sister" to "👧", "Mother" to "👩", "Baby" to "👶")),
                pickRound("f4", "Who is the sister?",  "Sister",      "Sister" to "👧",
                    listOf("Brother" to "👦", "Grandma" to "👵", "Father" to "👨")),
                pickRound("f5", "Who is the brother?", "Brother",     "Brother" to "👦",
                    listOf("Sister" to "👧", "Grandpa" to "👴", "Mother" to "👩")),
                pickRound("f6", "Who is the grandpa?", "Grandfather", "Grandpa" to "👴",
                    listOf("Brother" to "👦", "Sister" to "👧", "Mother" to "👩")),
            ),
            crafts = listOf(PreKgCrafts.familyTree),
        )

        lesson(
            id = "my_school", title = "My school", subtitle = "Where I learn and play",
            description = "Teachers, classrooms, books and the school bus.",
            game = GameType.LISTEN_PICK, intro = "Listen and tap the right picture.",
            rounds = listOf(
                pickRound("s1", "Where do we learn?", "Where do we learn?", "School" to "🏫",
                    listOf("House" to "🏠", "Hospital" to "🏥", "Tent" to "⛺")),
                pickRound("s2", "Who teaches us?", "Who teaches us?", "Teacher" to "👩‍🏫",
                    listOf("Doctor" to "👩‍⚕️", "Cook" to "👩‍🍳", "Farmer" to "👨‍🌾")),
                pickRound("s3", "What do we read?", "What do we read?", "Book" to "📕",
                    listOf("Ball" to "⚽", "Cup" to "☕", "Shoe" to "👟")),
                pickRound("s4", "What do we write with?", "What do we write with?", "Pencil" to "✏️",
                    listOf("Spoon" to "🥄", "Key" to "🔑", "Comb" to "🪮")),
                pickRound("s5", "Ride to school!", "Ride to school!",
                    "School bus" to "🚌", listOf("Rocket" to "🚀", "Ship" to "🚢", "Aeroplane" to "✈️")),
            ),
        )

        lesson(
            id = "my_body", title = "Parts of my body", subtitle = "Parts I can point to",
            description = "Eyes, ears, nose, mouth, hands, legs and feet.",
            game = GameType.LISTEN_PICK, intro = "Tap the body part you hear.",
            rounds = listOf(
                pickRound("b1", "Tap the eye",   "Eye",   "Eye"   to "👁️", listOf("Ear" to "👂", "Nose" to "👃", "Hand" to "✋")),
                pickRound("b2", "Tap the ear",   "Ear",   "Ear"   to "👂", listOf("Eye" to "👁️", "Foot" to "🦶", "Nose" to "👃")),
                pickRound("b3", "Tap the nose",  "Nose",  "Nose"  to "👃", listOf("Mouth" to "👄", "Hand" to "✋", "Ear" to "👂")),
                pickRound("b4", "Tap the mouth", "Mouth", "Mouth" to "👄", listOf("Eye" to "👁️", "Leg" to "🦵", "Nose" to "👃")),
                pickRound("b5", "Tap the hand",  "Hand",  "Hand"  to "✋", listOf("Foot" to "🦶", "Eye" to "👁️", "Nose" to "👃")),
                pickRound("b6", "Tap the leg",   "Leg",   "Leg"   to "🦵", listOf("Hand" to "✋", "Ear" to "👂", "Mouth" to "👄")),
            ),
        )

        lesson(
            id = "fruits", title = "Fruits", subtitle = "Sweet and juicy",
            description = "Apple, grapes, orange, pineapple, watermelon, mango and pear.",
            game = GameType.LISTEN_PICK, intro = "Tap the fruit you hear.",
            rounds = listOf(
                pickRound("fr1", "Tap the apple",      "Apple",      "Apple" to "🍎",
                    listOf("Mango" to "🥭", "Pear" to "🍐", "Grapes" to "🍇")),
                pickRound("fr2", "Tap the grapes",     "Grapes",     "Grapes" to "🍇",
                    listOf("Orange" to "🍊", "Apple" to "🍎", "Pineapple" to "🍍")),
                pickRound("fr3", "Tap the pineapple",  "Pineapple",  "Pineapple" to "🍍",
                    listOf("Watermelon" to "🍉", "Mango" to "🥭","Pear" to "🍐")),
                pickRound("fr4", "Tap the watermelon", "Watermelon", "Watermelon" to "🍉",
                    listOf("Apple" to "🍎", "Orange" to "🍊", "Grapes" to "🍇")),
                pickRound("fr5", "Tap the mango",      "Mango",      "Mango" to "🥭",
                    listOf("Pear" to "🍐", "Pineapple" to "🍍", "Orange" to "🍊")),
                pickRound("fr6", "Tap the pear",       "Pear",       "Pear" to "🍐",
                    listOf("Apple" to "🍎", "Watermelon" to "🍉", "Mango" to "🥭")),
            ),
        )

        lesson(
            id = "vegetables", title = "Vegetables", subtitle = "Make me strong",
            description = "Onion, potato, tomato, carrot, garlic and cabbage.",
            game = GameType.LISTEN_PICK, intro = "Tap the vegetable you hear.",
            rounds = listOf(
                pickRound("v1", "Tap the onion",   "Onion",   "Onion" to "🧅",
                    listOf("Potato" to "🥔", "Garlic" to "🧄", "Tomato" to "🍅")),
                pickRound("v2", "Tap the potato",  "Potato",  "Potato" to "🥔",
                    listOf("Carrot" to "🥕", "Onion" to "🧅", "Cabbage" to "🥬")),
                pickRound("v3", "Tap the tomato",  "Tomato",  "Tomato" to "🍅",
                    listOf("Garlic" to "🧄", "Potato" to "🥔", "Carrot" to "🥕")),
                pickRound("v4", "Tap the carrot",  "Carrot",  "Carrot" to "🥕",
                    listOf("Onion" to "🧅", "Cabbage" to "🥬", "Tomato" to "🍅")),
                pickRound("v5", "Tap the garlic",  "Garlic",  "Garlic" to "🧄",
                    listOf("Onion" to "🧅", "Potato" to "🥔", "Carrot" to "🥕")),
                pickRound("v6", "Tap the cabbage", "Cabbage", "Cabbage" to "🥬",
                    listOf("Tomato" to "🍅", "Garlic" to "🧄", "Potato" to "🥔")),
            ),
            crafts = listOf(PreKgCrafts.vegetableGarden),
        )

        lesson(
            id = "fruits_veg", title = "Fruits and vegetables", subtitle = "Good food",
            description = "Sort what we eat into fruits and vegetables.",
            game = GameType.SORT_BUCKETS, intro = "Which basket does it go in?",
            rounds = listOf(
                sortRound("fv1", "Fruit or vegetable?", listOf("Fruit", "Vegetable"), listOf(
                    BucketItem("Apple",  "🍎", "Fruit"),
                    BucketItem("Carrot", "🥕", "Vegetable"),
                    BucketItem("Mango",  "🥭", "Fruit"),
                    BucketItem("Potato", "🥔", "Vegetable"),
                    BucketItem("Grapes", "🍇", "Fruit"),
                    BucketItem("Onion",  "🧅", "Vegetable"),
                )),
            ),
        )

        lesson(
            id = "trees", title = "Trees", subtitle = "Big, green and kind",
            description = "Trees give us fruit, shade and fresh air.",
            game = GameType.LISTEN_PICK, intro = "Listen and tap the right picture.",
            rounds = listOf(
                pickRound("t1", "Find the tree!", "Find the tree!", "Tree" to "🌳",
                    listOf("Flower" to "🌸", "Mushroom" to "🍄", "Leaf" to "🍃")),
                pickRound("t2", "On the coconut tree?", "On the coconut tree?",
                    "Coconut" to "🥥", listOf("Carrot" to "🥕", "Onion" to "🧅", "Grapes" to "🍇")),
                pickRound("t3", "On the mango tree?", "On the mango tree?",
                    "Mango" to "🥭", listOf("Potato" to "🥔", "Egg" to "🥚", "Garlic" to "🧄")),
                pickRound("t4", "On the banana tree?", "On the banana tree?",
                    "Banana" to "🍌", listOf("Cabbage" to "🥬", "Tomato" to "🍅", "Onion" to "🧅")),
            ),
        )

        lesson(
            id = "animals", title = "Domestic and wild animals", subtitle = "Farm and forest",
            description = "Sort animals into the ones on the farm and the ones in the wild.",
            game = GameType.SORT_BUCKETS, intro = "Drag each animal to where it lives.",
            rounds = listOf(
                sortRound("a1", "Farm or wild?", listOf("Farm", "Wild"), listOf(
                    BucketItem("Cow",      "🐄", "Farm"),
                    BucketItem("Lion",     "🦁", "Wild"),
                    BucketItem("Hen",      "🐔", "Farm"),
                    BucketItem("Elephant", "🐘", "Wild"),
                    BucketItem("Goat",     "🐐", "Farm"),
                    BucketItem("Tiger",    "🐅", "Wild"),
                )),
                sortRound("a2", "Farm or wild?", listOf("Farm", "Wild"), listOf(
                    BucketItem("Sheep",  "🐑", "Farm"),
                    BucketItem("Monkey", "🐵", "Wild"),
                    BucketItem("Horse",  "🐴", "Farm"),
                    BucketItem("Zebra",  "🦓", "Wild"),
                    BucketItem("Pig",    "🐷", "Farm"),
                    BucketItem("Bear",   "🐻", "Wild"),
                )),
            ),
            crafts = listOf(PreKgCrafts.farmAnimals),
        )

        lesson(
            id = "find_me", title = "Find me if you can", subtitle = "Animal riddles",
            description = "Guess the wild animal from a clue.",
            game = GameType.LISTEN_PICK, intro = "Listen to the clue, then tap the animal.",
            rounds = listOf(
                pickRound("r1", "Long nose, big ears!", "I have a long nose and big ears. Who am I?",
                    "Elephant" to "🐘", listOf("Lion" to "🦁", "Zebra" to "🦓", "Giraffe" to "🦒")),
                pickRound("r2", "Big fluffy hair!", "I have a beautiful mane. Who am I?",
                    "Lion" to "🦁", listOf("Elephant" to "🐘", "Leopard" to "🐆", "Rhino" to "🦏")),
                pickRound("r3", "Black and white stripes!", "I have stripes on me. Who am I?",
                    "Zebra" to "🦓", listOf("Giraffe" to "🦒", "Elephant" to "🐘", "Rhino" to "🦏")),
                pickRound("r4", "Long, long neck!", "I have a very long neck. Who am I?",
                    "Giraffe" to "🦒", listOf("Lion" to "🦁", "Leopard" to "🐆", "Zebra" to "🦓")),
                pickRound("r5", "Spots all over!", "I have spots on my body. Who am I?",
                    "Leopard" to "🐆", listOf("Zebra" to "🦓", "Elephant" to "🐘", "Lion" to "🦁")),
                pickRound("r6", "A horn on my nose!", "I have a horn on my face. Who am I?",
                    "Rhino" to "🦏", listOf("Giraffe" to "🦒", "Leopard" to "🐆", "Elephant" to "🐘")),
            ),
        )

        lesson(
            id = "birds", title = "Birds", subtitle = "Flying friends",
            description = "Parrot, peacock, owl, duck, hen and eagle.",
            game = GameType.LISTEN_PICK, intro = "Tap the bird you hear.",
            rounds = listOf(
                pickRound("bd1", "Tap the parrot",  "Parrot",  "Parrot" to "🦜",
                    listOf("Owl" to "🦉", "Hen" to "🐔", "Eagle" to "🦅")),
                pickRound("bd2", "Tap the peacock", "Peacock", "Peacock" to "🦚",
                    listOf("Duck" to "🦆", "Parrot" to "🦜", "Owl" to "🦉")),
                pickRound("bd3", "Tap the owl",     "Owl",     "Owl" to "🦉",
                    listOf("Eagle" to "🦅", "Peacock" to "🦚", "Hen" to "🐔")),
                pickRound("bd4", "Tap the duck",    "Duck",    "Duck" to "🦆",
                    listOf("Hen" to "🐔", "Parrot" to "🦜", "Eagle" to "🦅")),
                pickRound("bd5", "Tap the eagle",   "Eagle",   "Eagle" to "🦅",
                    listOf("Owl" to "🦉", "Duck" to "🦆", "Peacock" to "🦚")),
            ),
            crafts = listOf(PreKgCrafts.colourBird),
        )

        lesson(
            id = "seasons", title = "Seasons", subtitle = "Summer, winter, spring, autumn",
            description = "What is the weather like in each season?",
            game = GameType.LISTEN_PICK, intro = "Listen, then tap the season.",
            rounds = listOf(
                pickRound("se1", "Hot and sunny?", "Hot and sunny?",
                    "Summer" to "☀️", listOf("Winter" to "❄️", "Spring" to "🌸", "Autumn" to "🍂")),
                pickRound("se2", "Very cold?", "Very cold?",
                    "Winter" to "❄️", listOf("Summer" to "☀️", "Spring" to "🌸", "Autumn" to "🍂")),
                pickRound("se3", "Flowers come out?", "Flowers come out?",
                    "Spring" to "🌸", listOf("Winter" to "❄️", "Summer" to "☀️", "Autumn" to "🍂")),
                pickRound("se4", "Leaves fall down?", "Leaves fall down?",
                    "Autumn" to "🍂", listOf("Spring" to "🌸", "Summer" to "☀️", "Winter" to "❄️")),
            ),
            crafts = listOf(PreKgCrafts.seasonsBoxes),
        )

        lesson(
            id = "keep_clean", title = "Bath time", subtitle = "Keeping myself clean",
            description = "Wash the muddy animals with soap and water.",
            game = GameType.LISTEN_PICK, intro = "Wash them clean!",
            // Played as the 3D bath game: each round is one muddy animal. The soap
            // question is only the fallback if 3D can't run.
            rounds = listOf("🐷" to "pig", "🐶" to "dog", "🐱" to "cat", "🐰" to "bunny").mapIndexed { i, (pic, name) ->
                pickRound("k${i + 1}", "Wash the $name!", "Muddy $name! Wash it!",
                    "Soap" to "🧼", listOf("Stone" to "🪨", "Book" to "📕", "Ball" to "⚽"),
                    "Soap makes it clean!").copy(emoji = pic)
            },
            crafts = emptyList(),
        )

        lesson(
            id = "manners", title = "Good manners", subtitle = "Try it — never do it",
            description = "Be kind, share, wait for your turn — and never push or tell lies.",
            game = GameType.SORT_BUCKETS, intro = "Is it a good manner or not?",
            rounds = listOf(
                // Pictures and tiny words: the voice reads each one when it's touched.
                sortRound("gm1", "Kind or not kind?", listOf("Kind", "Not kind"), listOf(
                    BucketItem("Say hello",    "👋", "Kind"),
                    BucketItem("Hit",          "👊", "Not kind"),
                    BucketItem("Hug a friend", "🫂", "Kind"),
                    BucketItem("Shout",        "🗣️", "Not kind"),
                    BucketItem("Share",        "🧸", "Kind"),
                    BucketItem("Fib",          "🤥", "Not kind"),
                )),
            ),
        )

        lesson(
            id = "quiz", title = "Know My World quiz", subtitle = "I know the world around me!",
            description = "A short quiz about me, my food, animals and seasons.",
            game = GameType.QUIZ, intro = "Six quick questions.",
            rounds = listOf(
                pickRound("q1", "What do we see with?",   "What do we see with?",   "Eyes" to "👁️",
                    listOf("Ears" to "👂", "Nose" to "👃", "Feet" to "🦶"), "We see with our eyes."),
                pickRound("q2", "Who lives on a farm?", "Who lives on a farm?", "Cow" to "🐄",
                    listOf("Lion" to "🦁", "Tiger" to "🐅", "Monkey" to "🐵")),
                pickRound("q3", "Find a fruit!",  "Find a fruit!",  "Mango" to "🥭",
                    listOf("Carrot" to "🥕", "Potato" to "🥔", "Onion" to "🧅")),
                pickRound("q4", "Find a bird!",   "Find a bird!",   "Parrot" to "🦜",
                    listOf("Cat" to "🐱", "Frog" to "🐸", "Fish" to "🐟")),
                pickRound("q5", "Snowy and cold?",  "Snowy and cold?",  "Winter" to "❄️",
                    listOf("Summer" to "☀️", "Spring" to "🌸", "Autumn" to "🍂")),
                pickRound("q6", "Which is kind?", "Which is kind?", "Hug a friend" to "🫂",
                    listOf("Hit" to "👊", "Fib" to "🤥", "Shout" to "🗣️")),
            ),
        )
    }

    val all: List<Lesson> get() = letterLand + numberLand + knowMyWorld
}
