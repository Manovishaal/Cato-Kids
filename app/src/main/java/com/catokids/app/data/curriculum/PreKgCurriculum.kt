package com.catokids.app.data.curriculum

import com.catokids.app.data.model.*
import com.catokids.app.data.model.Grade.PREKG
import com.catokids.app.data.model.SubjectId.*

/**
 * Pre-KG — Level 1 of the Cato Kids books:
 *   Letter Land  · Reading and Activities, Level 1
 *   Number Land  · Concepts and Number Fun, Level 1
 *   Know My World· Fun Activities, Level 1
 */
internal object PreKgCurriculum {

    val letterLand: List<Lesson> = lessons(LETTER_LAND, PREKG) {

        lesson(
            id = "strokes", title = "Trace the lines", subtitle = "Get your fingers ready",
            description = "Standing, sleeping and sloping lines — the very first strokes before letters.",
            game = GameType.TRACE, intro = "Follow the dots with your finger!",
            rounds = listOf(
                traceRound("l1", "|",  "Standing line",  "Standing line, top to bottom"),
                traceRound("l2", "—",  "Sleeping line",  "Sleeping line, left to right"),
                traceRound("l3", "/",  "Sloping line",   "Sloping line"),
                traceRound("l4", "C",  "Curve",          "Curvy line"),
                traceRound("l5", "O",  "Round and round","Round like a ball"),
            ),
        )

        lesson(
            id = "abc_meet", title = "Meet A B C", subtitle = "Our first three letters",
            description = "Say the letter, hear its sound, find its picture.",
            game = GameType.LISTEN_PICK, intro = "Tap the picture that starts with the sound you hear.",
            rounds = listOf('A', 'B', 'C').map { c ->
                val li = letter(c)
                val others = ALPHABET.filter { it.letter != c }.shuffled(kotlin.random.Random(c.code.toLong())).take(3)
                pickRound(
                    id = "p_$c",
                    prompt = "Which one starts with ${li.letter} ${li.letter.lowercaseChar()}?",
                    speak = "${li.letter}. ${li.letter} is for ${li.word}",
                    correct = li.word to li.emoji,
                    distractors = others.map { it.word to it.emoji },
                    explanation = "${li.letter} is for ${li.word}.",
                )
            },
        )

        lesson(
            id = "abc_trace", title = "Write A B C", subtitle = "Trace big letters",
            description = "Trace uppercase A, B and C.",
            game = GameType.TRACE, intro = "Trace the letter with your finger.",
            rounds = listOf('A', 'B', 'C').map { c ->
                val li = letter(c)
                traceRound("t_$c", c.toString(), "${c} for ${li.word}", "${c}. ${c} for ${li.word}", li.emoji)
            },
        )

        lesson(
            id = "find_a", title = "Find the letter A", subtitle = "Circle all the A's",
            description = "Tap every A and a you can see.",
            game = GameType.TAP_ALL, intro = "Tap every letter A — big or small!",
            rounds = listOf(
                tapLetterRound("a1", 'A', "Tap all the letter A", seed = 1),
                tapLetterRound("b1", 'B', "Tap all the letter B", seed = 2),
                tapLetterRound("c1", 'C', "Tap all the letter C", seed = 3),
            ),
        )

        lesson(
            id = "def", title = "Meet D E F", subtitle = "Three more friends",
            description = "Sounds and pictures for D, E and F.",
            game = GameType.LISTEN_PICK, intro = "Listen, then tap the right picture.",
            rounds = listOf('D', 'E', 'F').map { c ->
                val li = letter(c)
                val others = ALPHABET.filter { it.letter != c }.shuffled(kotlin.random.Random(c.code + 5L)).take(3)
                pickRound("p_$c", "Which one starts with ${li.letter}?",
                    "${li.letter}. ${li.letter} is for ${li.word}",
                    li.word to li.emoji, others.map { it.word to it.emoji },
                    "${li.letter} is for ${li.word}.")
            },
        )

        lesson(
            id = "big_small", title = "Big and small letters", subtitle = "Match the pairs",
            description = "Every big letter has a small partner.",
            game = GameType.MATCH_PAIRS, intro = "Match each big letter to its small letter.",
            rounds = listOf(
                matchRound("m1", "Match big and small",
                    listOf('A', 'B', 'C', 'D').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
                matchRound("m2", "Match big and small",
                    listOf('E', 'F', 'G', 'H').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
            ),
        )

        lesson(
            id = "ghij", title = "Meet G H I J", subtitle = "Letters and pictures",
            description = "More letters and the words that start with them.",
            game = GameType.LISTEN_PICK, intro = "Tap the picture that matches the sound.",
            rounds = listOf('G', 'H', 'I', 'J').map { c ->
                val li = letter(c)
                val others = ALPHABET.filter { it.letter != c }.shuffled(kotlin.random.Random(c.code + 11L)).take(3)
                pickRound("p_$c", "Find the ${li.letter} picture",
                    "${li.letter}. ${li.letter} is for ${li.word}",
                    li.word to li.emoji, others.map { it.word to it.emoji },
                    "${li.letter} is for ${li.word}.")
            },
        )

        lesson(
            id = "quiz", title = "Letter Land quiz", subtitle = "Show what you know",
            description = "A short quiz on the letters we have met.",
            game = GameType.QUIZ, intro = "Ten questions. Take your time!",
            rounds = listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H').map { c ->
                val li = letter(c)
                val others = ALPHABET.filter { it.letter != c }.shuffled(kotlin.random.Random(c.code + 21L)).take(3)
                pickRound("q_$c", "${li.emoji}  Which letter does this start with?",
                    "Which letter does ${li.word} start with?",
                    li.letter.toString() to li.emoji,
                    others.map { it.letter.toString() to it.emoji },
                    "${li.word} starts with ${li.letter}.")
            },
        )
    }

    val numberLand: List<Lesson> = lessons(NUMBER_LAND, PREKG) {

        lesson(
            id = "count_1_5", title = "Count 1 to 5", subtitle = "How many do you see?",
            description = "Counting small groups of objects.",
            game = GameType.COUNT_TAP, intro = "Count the pictures, then tap the number.",
            rounds = listOf(
                countRound("c1", "🍎", 1, "How many apples?",    listOf(1, 2, 3)),
                countRound("c2", "🐟", 2, "How many fish?",      listOf(1, 2, 3)),
                countRound("c3", "⭐", 3, "How many stars?",     listOf(2, 3, 4)),
                countRound("c4", "🎈", 4, "How many balloons?",  listOf(3, 4, 5)),
                countRound("c5", "🐝", 5, "How many bees?",      listOf(4, 5, 6)),
            ),
        )

        lesson(
            id = "trace_num", title = "Write 1 2 3", subtitle = "Trace the numbers",
            description = "Trace numbers one, two and three.",
            game = GameType.TRACE, intro = "Trace each number with your finger.",
            rounds = listOf(
                traceRound("n1", "1", "One",   "One",   "☝️"),
                traceRound("n2", "2", "Two",   "Two",   "✌️"),
                traceRound("n3", "3", "Three", "Three", "🤟"),
                traceRound("n4", "4", "Four",  "Four",  "🖖"),
                traceRound("n5", "5", "Five",  "Five",  "🖐️"),
            ),
        )

        lesson(
            id = "shapes", title = "Shapes around me", subtitle = "Circle, square, triangle",
            description = "Meet the first four shapes.",
            game = GameType.SHAPE_HUNT, intro = "Tap the shape I ask for.",
            rounds = listOf(
                pickRound("s1", "Tap the circle",   "Circle", "Circle" to "⚪",
                    listOf("Square" to "🟥", "Triangle" to "🔺", "Star" to "⭐")),
                pickRound("s2", "Tap the square",   "Square", "Square" to "🟥",
                    listOf("Circle" to "⚪", "Triangle" to "🔺", "Heart" to "❤️")),
                pickRound("s3", "Tap the triangle", "Triangle", "Triangle" to "🔺",
                    listOf("Circle" to "🔵", "Square" to "🟩", "Star" to "⭐")),
                pickRound("s4", "Tap the star",     "Star", "Star" to "⭐",
                    listOf("Circle" to "🔵", "Square" to "🟪", "Heart" to "💚")),
            ),
        )

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
            id = "colors", title = "Colours", subtitle = "Red, blue, yellow, green",
            description = "Name and find the four first colours.",
            game = GameType.SHAPE_HUNT, intro = "Tap the colour you hear.",
            rounds = listOf(
                pickRound("c1", "Tap the red one",    "Red",    "Red" to "🔴",
                    listOf("Blue" to "🔵", "Green" to "🟢", "Yellow" to "🟡")),
                pickRound("c2", "Tap the blue one",   "Blue",   "Blue" to "🔵",
                    listOf("Red" to "🔴", "Green" to "🟢", "Purple" to "🟣")),
                pickRound("c3", "Tap the yellow one", "Yellow", "Yellow" to "🟡",
                    listOf("Blue" to "🔵", "Orange" to "🟠", "Green" to "🟢")),
                pickRound("c4", "Tap the green one",  "Green",  "Green" to "🟢",
                    listOf("Red" to "🔴", "Yellow" to "🟡", "Blue" to "🔵")),
            ),
        )

        lesson(
            id = "match_qty", title = "Number and amount", subtitle = "Match them up",
            description = "Match each number to the right number of pictures.",
            game = GameType.MATCH_PAIRS, intro = "Match the number to the group.",
            rounds = listOf(
                matchRound("m1", "Match number to amount", listOf(
                    MatchPair("1", "🍓", "🍓"),
                    MatchPair("2", "🍓🍓", "🍓"),
                    MatchPair("3", "🍓🍓🍓", "🍓"),
                    MatchPair("4", "🍓🍓🍓🍓", "🍓"),
                )),
            ),
        )

        lesson(
            id = "quiz", title = "Number Land quiz", subtitle = "Counting check-up",
            description = "A short quiz on numbers, shapes and colours.",
            game = GameType.QUIZ, intro = "Let's see what you remember!",
            rounds = listOf(
                pickRound("q1", "🍎🍎🍎  How many apples?", "How many apples?",
                    "3" to "", listOf("1" to "", "2" to "", "5" to ""), "Count them: one, two, three."),
                pickRound("q2", "Which one is a circle?", "Which one is a circle?",
                    "Circle" to "⚪", listOf("Square" to "🟥", "Triangle" to "🔺", "Star" to "⭐")),
                pickRound("q3", "Which is the biggest?", "Which is the biggest?",
                    "Elephant" to "🐘", listOf("Ant" to "🐜", "Cat" to "🐱", "Bee" to "🐝")),
                pickRound("q4", "Which one is red?", "Which one is red?",
                    "Red" to "🔴", listOf("Blue" to "🔵", "Green" to "🟢", "Yellow" to "🟡")),
                pickRound("q5", "🐝🐝  How many bees?", "How many bees?",
                    "2" to "", listOf("1" to "", "3" to "", "4" to "")),
            ),
        )
    }

    val knowMyWorld: List<Lesson> = lessons(KNOW_MY_WORLD, PREKG) {

        lesson(
            id = "my_body", title = "My body", subtitle = "Parts I can point to",
            description = "Eyes, ears, nose, hands and feet.",
            game = GameType.LISTEN_PICK, intro = "Tap the body part you hear.",
            rounds = listOf(
                pickRound("b1", "Tap the eye",   "Eye",   "Eye"  to "👁️", listOf("Ear" to "👂", "Nose" to "👃", "Hand" to "✋")),
                pickRound("b2", "Tap the ear",   "Ear",   "Ear"  to "👂", listOf("Eye" to "👁️", "Foot" to "🦶", "Nose" to "👃")),
                pickRound("b3", "Tap the nose",  "Nose",  "Nose" to "👃", listOf("Mouth" to "👄", "Hand" to "✋", "Ear" to "👂")),
                pickRound("b4", "Tap the hand",  "Hand",  "Hand" to "✋", listOf("Foot" to "🦶", "Eye" to "👁️", "Nose" to "👃")),
            ),
        )

        lesson(
            id = "my_family", title = "My family", subtitle = "People who love me",
            description = "Mother, father, sister, brother, grandparents.",
            game = GameType.LISTEN_PICK, intro = "Who is it? Tap the picture.",
            rounds = listOf(
                pickRound("f1", "Who is the baby?",    "Baby",        "Baby" to "👶",
                    listOf("Mother" to "👩", "Father" to "👨", "Grandma" to "👵")),
                pickRound("f2", "Who is the mother?",  "Mother",      "Mother" to "👩",
                    listOf("Baby" to "👶", "Father" to "👨", "Grandpa" to "👴")),
                pickRound("f3", "Who is the grandpa?", "Grandfather", "Grandpa" to "👴",
                    listOf("Brother" to "👦", "Sister" to "👧", "Mother" to "👩")),
            ),
        )

        lesson(
            id = "animals", title = "Animals I know", subtitle = "Farm and wild",
            description = "Sort animals into farm and wild.",
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
            ),
        )

        lesson(
            id = "fruits_veg", title = "Fruits and vegetables", subtitle = "Good food",
            description = "Sort what we eat into fruits and vegetables.",
            game = GameType.SORT_BUCKETS, intro = "Which basket does it go in?",
            rounds = listOf(
                sortRound("fv1", "Fruit or vegetable?", listOf("Fruit", "Vegetable"), listOf(
                    BucketItem("Apple",  "🍎", "Fruit"),
                    BucketItem("Carrot", "🥕", "Vegetable"),
                    BucketItem("Banana", "🍌", "Fruit"),
                    BucketItem("Tomato", "🍅", "Vegetable"),
                    BucketItem("Grapes", "🍇", "Fruit"),
                    BucketItem("Corn",   "🌽", "Vegetable"),
                )),
            ),
        )

        lesson(
            id = "quiz", title = "Know My World quiz", subtitle = "All about me",
            description = "A short quiz about my body, family and animals.",
            game = GameType.QUIZ, intro = "Five quick questions.",
            rounds = listOf(
                pickRound("q1", "What do we see with?",   "What do we see with?",   "Eyes" to "👁️",
                    listOf("Ears" to "👂", "Nose" to "👃", "Feet" to "🦶"), "We see with our eyes."),
                pickRound("q2", "Which animal lives on a farm?", "Which animal lives on a farm?", "Cow" to "🐄",
                    listOf("Lion" to "🦁", "Tiger" to "🐅", "Monkey" to "🐵")),
                pickRound("q3", "Which one is a fruit?",  "Which one is a fruit?",  "Banana" to "🍌",
                    listOf("Carrot" to "🥕", "Tomato" to "🍅", "Onion" to "🧅")),
                pickRound("q4", "What do we hear with?",  "What do we hear with?",  "Ears" to "👂",
                    listOf("Eyes" to "👁️", "Hands" to "✋", "Nose" to "👃")),
            ),
        )
    }

    val all: List<Lesson> get() = letterLand + numberLand + knowMyWorld
}
