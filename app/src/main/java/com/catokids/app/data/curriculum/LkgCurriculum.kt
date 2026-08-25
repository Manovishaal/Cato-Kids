package com.catokids.app.data.curriculum

import com.catokids.app.data.model.*
import com.catokids.app.data.model.Grade.LKG
import com.catokids.app.data.model.SubjectId.*

/**
 * LKG — Level 2 of the Cato Kids books:
 *   Letter Land  · Reading & Activities + Written, Level 2
 *   Number Land  · Concepts and Number Fun, Level 2
 *   Know My World· Fun Activities, Level 2
 *
 * The Letter Land Written L2 workbook introduces letters in stroke-family order
 * (L I T J F V W X Z C O D A G Q E N M H U Y …) rather than alphabetically —
 * that order is preserved here.
 */
internal object LkgCurriculum {

    /** Stroke-family order taken straight from the Written Level 2 workbook. */
    private val WRITTEN_ORDER = listOf('L','I','T','J','F','V','W','X','Z','C','O','D','A','G','Q','E','N','M','H','U','Y','R','B','P','K','S')

    val letterLand: List<Lesson> = lessons(LETTER_LAND, LKG) {

        WRITTEN_ORDER.chunked(5).forEachIndexed { index, group ->
            val label = group.joinToString(" ")
            lesson(
                id = "write_${index + 1}", title = "Write $label", subtitle = "Trace and find",
                description = "Trace each letter, then circle every one you can find.",
                game = GameType.TRACE, intro = "Trace the letter, big and small.",
                rounds = group.flatMap { c ->
                    val li = letter(c)
                    listOf(
                        traceRound("t_${c}u", c.toString(), "Big $c", "$c. $c for ${li.word}", li.emoji),
                        traceRound("t_${c}l", c.lowercaseChar().toString(), "Small ${c.lowercaseChar()}", "$c", li.emoji),
                    )
                },
            )

            lesson(
                id = "find_${index + 1}", title = "Circle all $label", subtitle = "Sharp eyes needed",
                description = "Tap every one of these letters — big or small.",
                game = GameType.TAP_ALL, intro = "Tap every matching letter you can see.",
                rounds = group.mapIndexed { i, c -> tapLetterRound("f_$c", c, "Tap all the letter $c", seed = index * 10 + i) },
            )
        }

        lesson(
            id = "beginning_sounds", title = "Beginning sounds", subtitle = "What does it start with?",
            description = "Listen to the word and pick the letter it starts with.",
            game = GameType.LISTEN_PICK, intro = "Which letter does the word begin with?",
            rounds = ALPHABET.filter { it.letter in listOf('S','M','T','P','B','N','R','D') }.map { li ->
                val others = ALPHABET.filter { it.letter != li.letter }
                    .shuffled(kotlin.random.Random(li.letter.code + 31L)).take(3)
                pickRound("bs_${li.letter}", "${li.emoji}  ${li.word} starts with…",
                    "${li.word} starts with…",
                    li.letter.toString() to "", others.map { it.letter.toString() to "" },
                    "${li.word} starts with ${li.letter}.")
            },
        )

        lesson(
            id = "case_match", title = "Big and small letters", subtitle = "Find the partners",
            description = "Every capital letter has a lowercase partner.",
            game = GameType.MATCH_PAIRS, intro = "Match each big letter to its small letter.",
            rounds = listOf(
                matchRound("m1", "Match the pairs", listOf('L','I','T','J').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
                matchRound("m2", "Match the pairs", listOf('F','V','W','X').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
                matchRound("m3", "Match the pairs", listOf('C','O','D','A').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
                matchRound("m4", "Match the pairs", listOf('G','Q','E','N').map { MatchPair(it.toString(), it.lowercaseChar().toString(), letter(it).emoji) }),
            ),
        )

        lesson(
            id = "vowels", title = "The five vowels", subtitle = "A E I O U",
            description = "Vowels are special letters that live inside every word.",
            game = GameType.TAP_ALL, intro = "Tap all the vowels you can find.",
            rounds = listOf('A','E','I','O','U').mapIndexed { i, c ->
                tapLetterRound("v_$c", c, "Tap all the letter $c", seed = 60 + i)
            },
        )

        lesson(
            id = "cvc_intro", title = "My first words", subtitle = "Three-letter words",
            description = "Build simple words like cat, sun and bed.",
            game = GameType.JUMBLED_WORD, intro = "Put the letters in the right order.",
            rounds = listOf(
                wordRound("w1", "cat", "🐱", "A furry pet that says meow"),
                wordRound("w2", "sun", "☀️", "It shines in the sky"),
                wordRound("w3", "bed", "🛏️", "We sleep on it"),
                wordRound("w4", "pig", "🐷", "A pink farm animal"),
                wordRound("w5", "bus", "🚌", "It takes us to school"),
                wordRound("w6", "hat", "🎩", "We wear it on our head"),
            ),
        )

        lesson(
            id = "quiz", title = "Letter Land quiz", subtitle = "Level 2 check-up",
            description = "Letters, sounds and first words.",
            game = GameType.QUIZ, intro = "Eight questions. You've got this!",
            rounds = listOf(
                pickRound("q1", "🦁  Which letter does this start with?", "Which letter does lion start with?",
                    "L" to "", listOf("I" to "", "T" to "", "J" to ""), "Lion starts with L."),
                pickRound("q2", "Which is the small letter for T?", "Which is the small letter for T?",
                    "t" to "", listOf("f" to "", "l" to "", "i" to "")),
                pickRound("q3", "🍎  Which letter does this start with?", "Which letter does apple start with?",
                    "A" to "", listOf("E" to "", "O" to "", "U" to "")),
                pickRound("q4", "Which one is a vowel?", "Which one is a vowel?",
                    "E" to "", listOf("B" to "", "K" to "", "M" to ""), "A, E, I, O and U are vowels."),
                pickRound("q5", "☀️  What is this word?", "What is this word?",
                    "SUN" to "", listOf("SIT" to "", "SAT" to "", "SON" to "")),
                pickRound("q6", "🐷  Which letter does this start with?", "Which letter does pig start with?",
                    "P" to "", listOf("B" to "", "D" to "", "G" to "")),
            ),
        )
    }

    val numberLand: List<Lesson> = lessons(NUMBER_LAND, LKG) {

        lesson(
            id = "write_1_10", title = "Write 1 to 10", subtitle = "Number writing",
            description = "Trace each number from one to ten.",
            game = GameType.TRACE, intro = "Trace the number, then say it out loud.",
            rounds = (1..10).map { n ->
                traceRound("n$n", n.toString(), numberWord(n), numberWord(n), "🔢")
            },
        )

        lesson(
            id = "count_6_10", title = "Count 6 to 10", subtitle = "Bigger groups",
            description = "Count groups of six to ten objects.",
            game = GameType.COUNT_TAP, intro = "Count carefully, then tap the number.",
            rounds = listOf(
                countRound("c6",  "🍬", 6,  "How many sweets?",   listOf(5, 6, 7)),
                countRound("c7",  "🐤", 7,  "How many chicks?",   listOf(6, 7, 8)),
                countRound("c8",  "🌻", 8,  "How many flowers?",  listOf(7, 8, 9)),
                countRound("c9",  "🚗", 9,  "How many cars?",     listOf(8, 9, 10)),
                countRound("c10", "🎈", 10, "How many balloons?", listOf(9, 10, 11)),
            ),
        )

        lesson(
            id = "before_after", title = "Before and after", subtitle = "Number neighbours",
            description = "Which number comes before, and which comes after?",
            game = GameType.QUIZ, intro = "Think about the number line.",
            rounds = listOf(
                pickRound("ba1", "Which number comes after 4?",  "Which number comes after four?",  "5" to "", listOf("3" to "", "6" to "", "9" to "")),
                pickRound("ba2", "Which number comes before 7?", "Which number comes before seven?","6" to "", listOf("8" to "", "5" to "", "7" to "")),
                pickRound("ba3", "Which number is between 2 and 4?", "Which number is between two and four?", "3" to "", listOf("1" to "", "5" to "", "6" to "")),
                pickRound("ba4", "Which number comes after 9?",  "Which number comes after nine?",  "10" to "", listOf("8" to "", "11" to "", "7" to "")),
            ),
        )

        lesson(
            id = "more_less", title = "More and less", subtitle = "Which group is bigger?",
            description = "Compare two groups and choose the bigger one.",
            game = GameType.QUIZ, intro = "Which side has more?",
            rounds = listOf(
                pickRound("ml1", "Which has MORE?", "Which has more?", "🍎🍎🍎🍎" to "", listOf("🍎🍎" to "", "🍎" to "", "🍎🍎🍎" to "")),
                pickRound("ml2", "Which has LESS?", "Which has less?", "⭐" to "", listOf("⭐⭐⭐" to "", "⭐⭐⭐⭐⭐" to "", "⭐⭐" to "")),
                pickRound("ml3", "Which has MORE?", "Which has more?", "🐟🐟🐟🐟🐟" to "", listOf("🐟🐟" to "", "🐟🐟🐟" to "", "🐟" to "")),
            ),
        )

        lesson(
            id = "shapes2", title = "More shapes", subtitle = "Rectangle, oval, diamond",
            description = "Meet three more shapes and find them.",
            game = GameType.SHAPE_HUNT, intro = "Tap the shape you hear.",
            rounds = listOf(
                pickRound("s1", "Tap the rectangle", "Rectangle", "Rectangle" to "▭", listOf("Square" to "🟥", "Circle" to "⚪", "Triangle" to "🔺")),
                pickRound("s2", "Tap the diamond",   "Diamond",   "Diamond"   to "🔷", listOf("Circle" to "🔵", "Square" to "🟨", "Star" to "⭐")),
                pickRound("s3", "Tap the heart",     "Heart",     "Heart"     to "❤️", listOf("Star" to "⭐", "Circle" to "🔵", "Triangle" to "🔺")),
                pickRound("s4", "Tap the oval",      "Oval",      "Oval"      to "⬭", listOf("Square" to "🟩", "Diamond" to "🔷", "Star" to "🌟")),
            ),
        )

        lesson(
            id = "patterns", title = "What comes next?", subtitle = "Patterns",
            description = "Finish the pattern.",
            game = GameType.QUIZ, intro = "Look at the pattern and choose what comes next.",
            rounds = listOf(
                pickRound("p1", "🔴 🔵 🔴 🔵 ❓", "What comes next?", "🔴" to "", listOf("🔵" to "", "🟢" to "", "🟡" to "")),
                pickRound("p2", "⭐ ⭐ ❤️ ⭐ ⭐ ❓", "What comes next?", "❤️" to "", listOf("⭐" to "", "🔺" to "", "🔵" to "")),
                pickRound("p3", "🍎 🍌 🍎 🍌 ❓", "What comes next?", "🍎" to "", listOf("🍌" to "", "🍇" to "", "🍊" to "")),
                pickRound("p4", "🔺 🔵 🔵 🔺 🔵 🔵 ❓", "What comes next?", "🔺" to "", listOf("🔵" to "", "🟩" to "", "⭐" to "")),
            ),
        )

        lesson(
            id = "add_intro", title = "Adding is fun", subtitle = "Put them together",
            description = "Simple addition up to ten.",
            game = GameType.QUIZ, intro = "Count both groups together.",
            rounds = listOf(
                pickRound("a1", "🍎🍎 + 🍎 = ?", "Two plus one",   "3" to "", listOf("2" to "", "4" to "", "5" to ""), "2 + 1 = 3"),
                pickRound("a2", "⭐⭐ + ⭐⭐ = ?", "Two plus two",  "4" to "", listOf("3" to "", "5" to "", "6" to ""), "2 + 2 = 4"),
                pickRound("a3", "🐟🐟🐟 + 🐟🐟 = ?", "Three plus two", "5" to "", listOf("4" to "", "6" to "", "7" to ""), "3 + 2 = 5"),
                pickRound("a4", "🎈 + 🎈🎈🎈 = ?", "One plus three", "4" to "", listOf("2" to "", "3" to "", "5" to ""), "1 + 3 = 4"),
            ),
        )

        lesson(
            id = "quiz", title = "Number Land quiz", subtitle = "Level 2 check-up",
            description = "Counting, patterns and first addition.",
            game = GameType.QUIZ, intro = "Nine questions to finish the book!",
            rounds = listOf(
                pickRound("q1", "🌻🌻🌻🌻🌻🌻  How many?", "How many flowers?", "6" to "", listOf("5" to "", "7" to "", "8" to "")),
                pickRound("q2", "What comes after 8?",  "What comes after eight?", "9" to "", listOf("7" to "", "10" to "", "6" to "")),
                pickRound("q3", "🍬🍬 + 🍬🍬🍬 = ?", "Two plus three", "5" to "", listOf("4" to "", "6" to "", "3" to "")),
                pickRound("q4", "Which shape has 4 equal sides?", "Which shape has four equal sides?", "Square" to "🟥", listOf("Circle" to "⚪", "Triangle" to "🔺", "Oval" to "⬭")),
                pickRound("q5", "🔵 🟡 🔵 🟡 ❓", "What comes next?", "🔵" to "", listOf("🟡" to "", "🔴" to "", "🟢" to "")),
            ),
        )
    }

    val knowMyWorld: List<Lesson> = lessons(KNOW_MY_WORLD, LKG) {

        lesson(
            id = "living", title = "Living and non-living", subtitle = "Alive or not?",
            description = "Living things grow, eat and move.",
            game = GameType.SORT_BUCKETS, intro = "Sort each picture into Living or Non-living.",
            rounds = listOf(
                sortRound("l1", "Living or non-living?", listOf("Living", "Non-living"), listOf(
                    BucketItem("Dog",   "🐶", "Living"),
                    BucketItem("Chair", "🪑", "Non-living"),
                    BucketItem("Tree",  "🌳", "Living"),
                    BucketItem("Car",   "🚗", "Non-living"),
                    BucketItem("Bird",  "🐦", "Living"),
                    BucketItem("Book",  "📕", "Non-living"),
                )),
            ),
        )

        lesson(
            id = "weather", title = "Weather and seasons", subtitle = "What is it like outside?",
            description = "Sunny, rainy, cloudy, windy and snowy days.",
            game = GameType.LISTEN_PICK, intro = "Tap the weather you hear.",
            rounds = listOf(
                pickRound("w1", "Tap the sunny day",  "Sunny",  "Sunny"  to "☀️", listOf("Rainy" to "🌧️", "Snowy" to "❄️", "Cloudy" to "☁️")),
                pickRound("w2", "Tap the rainy day",  "Rainy",  "Rainy"  to "🌧️", listOf("Sunny" to "☀️", "Windy" to "🌬️", "Snowy" to "❄️")),
                pickRound("w3", "Tap the snowy day",  "Snowy",  "Snowy"  to "❄️", listOf("Sunny" to "☀️", "Rainy" to "🌧️", "Cloudy" to "☁️")),
                pickRound("w4", "What do we use in the rain?", "What do we use in the rain?", "Umbrella" to "☂️", listOf("Sunglasses" to "🕶️", "Fan" to "🪭", "Kite" to "🪁")),
            ),
        )

        lesson(
            id = "transport", title = "How we travel", subtitle = "Land, water and air",
            description = "Sort vehicles by where they travel.",
            game = GameType.SORT_BUCKETS, intro = "Where does each one go?",
            rounds = listOf(
                sortRound("t1", "Land, water or air?", listOf("Land", "Water", "Air"), listOf(
                    BucketItem("Bus",       "🚌", "Land"),
                    BucketItem("Boat",      "⛵", "Water"),
                    BucketItem("Plane",     "✈️", "Air"),
                    BucketItem("Car",       "🚗", "Land"),
                    BucketItem("Ship",      "🚢", "Water"),
                    BucketItem("Helicopter","🚁", "Air"),
                )),
            ),
        )

        lesson(
            id = "helpers", title = "Community helpers", subtitle = "People who help us",
            description = "Doctor, teacher, farmer, police officer and firefighter.",
            game = GameType.LISTEN_PICK, intro = "Who helps us do this?",
            rounds = listOf(
                pickRound("h1", "Who helps us when we are sick?", "Who helps us when we are sick?", "Doctor" to "👩‍⚕️", listOf("Farmer" to "👨‍🌾", "Pilot" to "👨‍✈️", "Chef" to "👩‍🍳")),
                pickRound("h2", "Who grows our food?",            "Who grows our food?",            "Farmer" to "👨‍🌾", listOf("Doctor" to "👩‍⚕️", "Teacher" to "👩‍🏫", "Police" to "👮")),
                pickRound("h3", "Who puts out fires?",            "Who puts out fires?",            "Firefighter" to "👨‍🚒", listOf("Chef" to "👩‍🍳", "Farmer" to "👨‍🌾", "Doctor" to "👩‍⚕️")),
                pickRound("h4", "Who teaches us at school?",      "Who teaches us at school?",      "Teacher" to "👩‍🏫", listOf("Pilot" to "👨‍✈️", "Police" to "👮", "Chef" to "👩‍🍳")),
            ),
        )

        lesson(
            id = "animal_homes", title = "Animal homes", subtitle = "Where do they live?",
            description = "Match each animal to its home.",
            game = GameType.MATCH_PAIRS, intro = "Match the animal to where it lives.",
            rounds = listOf(
                matchRound("ah1", "Match animal to home", listOf(
                    MatchPair("Bird", "Nest",   "🐦"),
                    MatchPair("Bee",  "Hive",   "🐝"),
                    MatchPair("Dog",  "Kennel", "🐶"),
                    MatchPair("Fish", "Water",  "🐟"),
                )),
            ),
        )

        lesson(
            id = "quiz", title = "Know My World quiz", subtitle = "Level 2 check-up",
            description = "Living things, weather, travel and helpers.",
            game = GameType.QUIZ, intro = "Six questions to finish.",
            rounds = listOf(
                pickRound("q1", "Which one is living?", "Which one is living?", "Tree" to "🌳", listOf("Chair" to "🪑", "Ball" to "⚽", "Cup" to "🥤")),
                pickRound("q2", "Which travels in the air?", "Which travels in the air?", "Plane" to "✈️", listOf("Bus" to "🚌", "Boat" to "⛵", "Bike" to "🚲")),
                pickRound("q3", "Where does a bird live?", "Where does a bird live?", "Nest" to "🪺", listOf("Hive" to "🍯", "Kennel" to "🏠", "Pond" to "💧")),
                pickRound("q4", "What do we wear when it rains?", "What do we wear when it rains?", "Raincoat" to "🧥", listOf("Sunglasses" to "🕶️", "Cap" to "🧢", "Slippers" to "🩴")),
            ),
        )
    }

    val all: List<Lesson> get() = letterLand + numberLand + knowMyWorld
}

internal fun numberWord(n: Int): String = when (n) {
    1 -> "One"; 2 -> "Two"; 3 -> "Three"; 4 -> "Four"; 5 -> "Five"
    6 -> "Six"; 7 -> "Seven"; 8 -> "Eight"; 9 -> "Nine"; 10 -> "Ten"
    else -> n.toString()
}
