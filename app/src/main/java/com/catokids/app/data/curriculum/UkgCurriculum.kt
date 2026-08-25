package com.catokids.app.data.curriculum

import com.catokids.app.data.model.*
import com.catokids.app.data.model.Grade.UKG
import com.catokids.app.data.model.SubjectId.*

/**
 * UKG — Level 3 of the Cato Kids books:
 *   Letter Land  · Reading & Activities + Written, Level 3
 *   Number Land  · Concepts and Number Fun, Level 3
 *   Know My World· Fun Activities, Level 3
 */
internal object UkgCurriculum {

    val letterLand: List<Lesson> = lessons(LETTER_LAND, UKG) {

        lesson(
            id = "cvc_a", title = "Words with 'a'", subtitle = "cat · bat · map",
            description = "Short 'a' words — read them and build them.",
            game = GameType.JUMBLED_WORD, intro = "Drag the letters into the right order.",
            rounds = listOf(
                wordRound("w1", "cat", "🐱", "It says meow"),
                wordRound("w2", "bat", "🦇", "It flies at night"),
                wordRound("w3", "map", "🗺️", "It shows the way"),
                wordRound("w4", "van", "🚐", "A big car for carrying things"),
                wordRound("w5", "hand", "✋", "You have two of these"),
            ),
        )

        lesson(
            id = "cvc_e_i", title = "Words with 'e' and 'i'", subtitle = "bed · pig · fish",
            description = "Short 'e' and short 'i' words.",
            game = GameType.JUMBLED_WORD, intro = "Build each word.",
            rounds = listOf(
                wordRound("w1", "bed",  "🛏️", "We sleep on it"),
                wordRound("w2", "pen",  "🖊️", "We write with it"),
                wordRound("w3", "pig",  "🐷", "A pink farm animal"),
                wordRound("w4", "fish", "🐟", "It swims in water"),
                wordRound("w5", "ship", "🚢", "It sails on the sea"),
            ),
        )

        lesson(
            id = "cvc_o_u", title = "Words with 'o' and 'u'", subtitle = "dog · sun · bus",
            description = "Short 'o' and short 'u' words.",
            game = GameType.JUMBLED_WORD, intro = "Build each word.",
            rounds = listOf(
                wordRound("w1", "dog",  "🐶", "It says woof"),
                wordRound("w2", "box",  "📦", "We keep things in it"),
                wordRound("w3", "sun",  "☀️", "It gives us light"),
                wordRound("w4", "bus",  "🚌", "It takes us to school"),
                wordRound("w5", "duck", "🦆", "It says quack"),
            ),
        )

        lesson(
            id = "sight_words", title = "Sight words", subtitle = "Words we just know",
            description = "The, and, is, you, my, we — read them without sounding out.",
            game = GameType.LISTEN_PICK, intro = "Tap the word you hear.",
            rounds = listOf(
                pickRound("s1", "Tap the word: THE",  "The",  "the" to "", listOf("she" to "", "he" to "", "then" to "")),
                pickRound("s2", "Tap the word: AND",  "And",  "and" to "", listOf("end" to "", "an" to "", "sand" to "")),
                pickRound("s3", "Tap the word: YOU",  "You",  "you" to "", listOf("your" to "", "yes" to "", "yet" to "")),
                pickRound("s4", "Tap the word: MY",   "My",   "my"  to "", listOf("me" to "", "may" to "", "many" to "")),
                pickRound("s5", "Tap the word: WE",   "We",   "we"  to "", listOf("me" to "", "be" to "", "wet" to "")),
                pickRound("s6", "Tap the word: IS",   "Is",   "is"  to "", listOf("it" to "", "in" to "", "if" to "")),
            ),
        )

        lesson(
            id = "rhyme", title = "Rhyming words", subtitle = "Words that sound alike",
            description = "Find the word that rhymes.",
            game = GameType.QUIZ, intro = "Which word rhymes with the first one?",
            rounds = listOf(
                pickRound("r1", "Which rhymes with CAT?", "Which rhymes with cat?", "hat" to "🎩", listOf("dog" to "🐶", "sun" to "☀️", "cup" to "🥤"), "Cat and hat both end with -at."),
                pickRound("r2", "Which rhymes with DOG?", "Which rhymes with dog?", "log" to "🪵", listOf("cat" to "🐱", "pen" to "🖊️", "bus" to "🚌")),
                pickRound("r3", "Which rhymes with STAR?", "Which rhymes with star?", "car" to "🚗", listOf("sun" to "☀️", "moon" to "🌙", "tree" to "🌳")),
                pickRound("r4", "Which rhymes with BEE?", "Which rhymes with bee?", "tree" to "🌳", listOf("bed" to "🛏️", "book" to "📕", "ball" to "⚽")),
            ),
        )

        lesson(
            id = "blends", title = "Two letters, one sound", subtitle = "sh · ch · th",
            description = "Digraphs: two letters that make one new sound.",
            game = GameType.LISTEN_PICK, intro = "Which sound do you hear at the start?",
            rounds = listOf(
                pickRound("b1", "🚢  Ship starts with…",  "Ship starts with",  "sh" to "", listOf("ch" to "", "th" to "", "s" to ""), "Ship starts with the 'sh' sound."),
                pickRound("b2", "🪑  Chair starts with…", "Chair starts with", "ch" to "", listOf("sh" to "", "th" to "", "c" to "")),
                pickRound("b3", "👍  Thumb starts with…", "Thumb starts with", "th" to "", listOf("t" to "", "sh" to "", "ch" to "")),
                pickRound("b4", "🐑  Sheep starts with…", "Sheep starts with", "sh" to "", listOf("s" to "", "ch" to "", "th" to "")),
            ),
        )

        lesson(
            id = "write_words", title = "Write the word", subtitle = "Careful handwriting",
            description = "Trace whole words letter by letter.",
            game = GameType.TRACE, intro = "Trace each letter to write the word.",
            rounds = listOf(
                traceRound("t1", "C", "cat", "cat", "🐱"),
                traceRound("t2", "a", "cat", "cat", "🐱"),
                traceRound("t3", "t", "cat", "cat", "🐱"),
                traceRound("t4", "S", "sun", "sun", "☀️"),
                traceRound("t5", "u", "sun", "sun", "☀️"),
                traceRound("t6", "n", "sun", "sun", "☀️"),
            ),
        )

        lesson(
            id = "quiz", title = "Letter Land quiz", subtitle = "Level 3 check-up",
            description = "Reading, rhyming and word building.",
            game = GameType.QUIZ, intro = "Ten questions — the big one!",
            rounds = listOf(
                pickRound("q1", "🐱  What is this word?", "What is this word?", "CAT" to "", listOf("COT" to "", "CUT" to "", "CAP" to "")),
                pickRound("q2", "Which rhymes with SUN?", "Which rhymes with sun?", "fun" to "", listOf("sit" to "", "sad" to "", "sea" to "")),
                pickRound("q3", "How many vowels are there?", "How many vowels are there?", "5" to "", listOf("3" to "", "7" to "", "26" to ""), "A, E, I, O and U — five vowels."),
                pickRound("q4", "🚢  Ship starts with…", "Ship starts with", "sh" to "", listOf("s" to "", "ch" to "", "th" to "")),
                pickRound("q5", "Which is a sight word?", "Which is a sight word?", "the" to "", listOf("tha" to "", "teh" to "", "hte" to "")),
                pickRound("q6", "🦆  What is this word?", "What is this word?", "DUCK" to "", listOf("DOCK" to "", "DECK" to "", "DARK" to "")),
            ),
        )
    }

    val numberLand: List<Lesson> = lessons(NUMBER_LAND, UKG) {

        lesson(
            id = "count_20", title = "Numbers to 20", subtitle = "Teen numbers",
            description = "Count and recognise numbers up to twenty.",
            game = GameType.COUNT_TAP, intro = "Count them all, then tap the number.",
            rounds = listOf(
                countRound("c1", "⭐", 11, "How many stars?",   listOf(10, 11, 12)),
                countRound("c2", "🍬", 13, "How many sweets?",  listOf(12, 13, 14)),
                countRound("c3", "🐞", 15, "How many ladybirds?", listOf(14, 15, 16)),
                countRound("c4", "🎈", 17, "How many balloons?", listOf(16, 17, 18)),
                countRound("c5", "🌸", 20, "How many flowers?", listOf(19, 20, 21)),
            ),
        )

        lesson(
            id = "addition", title = "Addition", subtitle = "Adding to 20",
            description = "Add two numbers together.",
            game = GameType.QUIZ, intro = "Work it out, then tap your answer.",
            rounds = listOf(
                pickRound("a1", "5 + 3 = ?",  "Five plus three",  "8"  to "", listOf("7" to "", "9" to "", "6" to ""),  "5 + 3 = 8"),
                pickRound("a2", "6 + 4 = ?",  "Six plus four",    "10" to "", listOf("9" to "", "11" to "", "8" to ""), "6 + 4 = 10"),
                pickRound("a3", "7 + 5 = ?",  "Seven plus five",  "12" to "", listOf("11" to "", "13" to "", "10" to "")),
                pickRound("a4", "9 + 8 = ?",  "Nine plus eight",  "17" to "", listOf("16" to "", "18" to "", "15" to "")),
                pickRound("a5", "10 + 10 = ?","Ten plus ten",     "20" to "", listOf("15" to "", "18" to "", "21" to "")),
            ),
        )

        lesson(
            id = "subtraction", title = "Subtraction", subtitle = "Taking away",
            description = "Take one number away from another.",
            game = GameType.QUIZ, intro = "How many are left?",
            rounds = listOf(
                pickRound("s1", "5 − 2 = ?",  "Five take away two",   "3" to "", listOf("2" to "", "4" to "", "7" to ""), "5 − 2 = 3"),
                pickRound("s2", "8 − 3 = ?",  "Eight take away three","5" to "", listOf("4" to "", "6" to "", "11" to "")),
                pickRound("s3", "10 − 6 = ?", "Ten take away six",    "4" to "", listOf("3" to "", "5" to "", "16" to "")),
                pickRound("s4", "12 − 5 = ?", "Twelve take away five","7" to "", listOf("6" to "", "8" to "", "17" to "")),
            ),
        )

        lesson(
            id = "skip", title = "Skip counting", subtitle = "2s, 5s and 10s",
            description = "Count in jumps.",
            game = GameType.QUIZ, intro = "What comes next in the jump?",
            rounds = listOf(
                pickRound("k1", "2, 4, 6, 8, ❓",     "Two four six eight",      "10" to "", listOf("9" to "", "11" to "", "12" to ""), "We are counting in 2s."),
                pickRound("k2", "5, 10, 15, ❓",      "Five ten fifteen",        "20" to "", listOf("16" to "", "25" to "", "18" to "")),
                pickRound("k3", "10, 20, 30, ❓",     "Ten twenty thirty",       "40" to "", listOf("31" to "", "35" to "", "50" to "")),
                pickRound("k4", "1, 3, 5, 7, ❓",     "One three five seven",    "9"  to "", listOf("8" to "", "10" to "", "11" to "")),
            ),
        )

        lesson(
            id = "shapes3", title = "Shapes and sides", subtitle = "Counting sides",
            description = "How many sides and corners does each shape have?",
            game = GameType.QUIZ, intro = "Count the sides carefully.",
            rounds = listOf(
                pickRound("s1", "🔺  How many sides?",  "How many sides does a triangle have?", "3" to "", listOf("4" to "", "5" to "", "0" to "")),
                pickRound("s2", "🟥  How many sides?",  "How many sides does a square have?",   "4" to "", listOf("3" to "", "5" to "", "6" to "")),
                pickRound("s3", "⚪  How many corners?", "How many corners does a circle have?", "0" to "", listOf("1" to "", "2" to "", "4" to ""), "A circle has no corners."),
                pickRound("s4", "⬟  How many sides?",   "How many sides does a pentagon have?", "5" to "", listOf("4" to "", "6" to "", "3" to "")),
            ),
        )

        lesson(
            id = "time_money", title = "Time and money", subtitle = "Clocks and coins",
            description = "O'clock times and counting coins.",
            game = GameType.QUIZ, intro = "Look carefully at the clock.",
            rounds = listOf(
                pickRound("t1", "🕒  What time is it?", "What time is it?", "3 o'clock" to "", listOf("2 o'clock" to "", "4 o'clock" to "", "12 o'clock" to "")),
                pickRound("t2", "🕕  What time is it?", "What time is it?", "6 o'clock" to "", listOf("5 o'clock" to "", "7 o'clock" to "", "12 o'clock" to "")),
                pickRound("t3", "How many hours in a day?", "How many hours in a day?", "24" to "", listOf("12" to "", "30" to "", "7" to "")),
                pickRound("t4", "How many days in a week?", "How many days in a week?", "7" to "", listOf("5" to "", "12" to "", "30" to "")),
            ),
        )

        lesson(
            id = "write_teens", title = "Write 11 to 20", subtitle = "Two-digit numbers",
            description = "Trace the teen numbers.",
            game = GameType.TRACE, intro = "Trace both digits.",
            rounds = (11..20).map { n -> traceRound("n$n", n.toString(), "Number $n", n.toString(), "🔢") },
        )

        lesson(
            id = "quiz", title = "Number Land quiz", subtitle = "Level 3 check-up",
            description = "Addition, subtraction, shapes and time.",
            game = GameType.QUIZ, intro = "The big number quiz!",
            rounds = listOf(
                pickRound("q1", "7 + 6 = ?",  "Seven plus six",  "13" to "", listOf("12" to "", "14" to "", "11" to "")),
                pickRound("q2", "15 − 7 = ?", "Fifteen take away seven", "8" to "", listOf("7" to "", "9" to "", "22" to "")),
                pickRound("q3", "5, 10, 15, 20, ❓", "What comes next?", "25" to "", listOf("21" to "", "30" to "", "24" to "")),
                pickRound("q4", "🟥  How many corners?", "How many corners does a square have?", "4" to "", listOf("3" to "", "0" to "", "6" to "")),
                pickRound("q5", "How many days in a week?", "How many days in a week?", "7" to "", listOf("5" to "", "10" to "", "12" to "")),
            ),
        )
    }

    val knowMyWorld: List<Lesson> = lessons(KNOW_MY_WORLD, UKG) {

        lesson(
            id = "plants", title = "Parts of a plant", subtitle = "Root, stem, leaf, flower",
            description = "What each part of a plant does.",
            game = GameType.MATCH_PAIRS, intro = "Match each part to its job.",
            rounds = listOf(
                matchRound("p1", "Match part to job", listOf(
                    MatchPair("Root",   "Drinks water",  "🌱"),
                    MatchPair("Stem",   "Holds it up",   "🌿"),
                    MatchPair("Leaf",   "Makes food",    "🍃"),
                    MatchPair("Flower", "Makes seeds",   "🌸"),
                )),
            ),
        )

        lesson(
            id = "young_ones", title = "Animals and their babies", subtitle = "Young ones",
            description = "Match grown-up animals to their babies.",
            game = GameType.MATCH_PAIRS, intro = "Who belongs to whom?",
            rounds = listOf(
                matchRound("y1", "Match animal to baby", listOf(
                    MatchPair("Cow",  "Calf",   "🐄"),
                    MatchPair("Dog",  "Puppy",  "🐶"),
                    MatchPair("Cat",  "Kitten", "🐱"),
                    MatchPair("Hen",  "Chick",  "🐔"),
                )),
                matchRound("y2", "Match animal to baby", listOf(
                    MatchPair("Frog",  "Tadpole", "🐸"),
                    MatchPair("Sheep", "Lamb",    "🐑"),
                    MatchPair("Horse", "Foal",    "🐴"),
                    MatchPair("Duck",  "Duckling","🦆"),
                )),
            ),
        )

        lesson(
            id = "senses", title = "My five senses", subtitle = "See, hear, smell, taste, touch",
            description = "Which sense do we use?",
            game = GameType.SORT_BUCKETS, intro = "Sort each one under the right sense.",
            rounds = listOf(
                sortRound("s1", "Which sense?", listOf("See", "Hear", "Taste"), listOf(
                    BucketItem("Rainbow", "🌈", "See"),
                    BucketItem("Drum",    "🥁", "Hear"),
                    BucketItem("Lemon",   "🍋", "Taste"),
                    BucketItem("Star",    "⭐", "See"),
                    BucketItem("Bell",    "🔔", "Hear"),
                    BucketItem("Honey",   "🍯", "Taste"),
                )),
            ),
        )

        lesson(
            id = "good_habits", title = "Good habits", subtitle = "Staying healthy and safe",
            description = "Choices that keep us well.",
            game = GameType.SORT_BUCKETS, intro = "Is it a good habit or not?",
            rounds = listOf(
                sortRound("g1", "Good habit or bad habit?", listOf("Good", "Not good"), listOf(
                    BucketItem("Brush teeth",  "🪥", "Good"),
                    BucketItem("Wash hands",   "🧼", "Good"),
                    BucketItem("Eat fruit",    "🍎", "Good"),
                    BucketItem("Litter",       "🗑️", "Not good"),
                    BucketItem("Too much TV",  "📺", "Not good"),
                    BucketItem("Waste water",  "🚰", "Not good"),
                )),
            ),
        )

        lesson(
            id = "earth", title = "Our Earth", subtitle = "Land, water and sky",
            description = "Air, water, land and how we care for them.",
            game = GameType.LISTEN_PICK, intro = "Tap the right picture.",
            rounds = listOf(
                pickRound("e1", "Where do fish live?",        "Where do fish live?",        "Water" to "🌊", listOf("Sky" to "☁️", "Desert" to "🏜️", "Mountain" to "⛰️")),
                pickRound("e2", "What do plants need to grow?","What do plants need to grow?","Water" to "💧", listOf("Toys" to "🧸", "Shoes" to "👟", "Books" to "📚")),
                pickRound("e3", "What gives us light in the day?", "What gives us light in the day?", "Sun" to "☀️", listOf("Moon" to "🌙", "Star" to "⭐", "Lamp" to "💡")),
                pickRound("e4", "Which one should we recycle?", "Which one should we recycle?", "Bottle" to "♻️", listOf("Apple core" to "🍎", "Water" to "💧", "Leaf" to "🍃")),
            ),
        )

        lesson(
            id = "safety", title = "Safety first", subtitle = "Rules that keep us safe",
            description = "Road safety and safe choices.",
            game = GameType.QUIZ, intro = "What is the safe thing to do?",
            rounds = listOf(
                pickRound("s1", "🚦  What does red mean?",   "What does the red light mean?", "Stop" to "🛑", listOf("Go" to "✅", "Run" to "🏃", "Wait a bit" to "⏳"), "Red means stop."),
                pickRound("s2", "🚦  What does green mean?", "What does the green light mean?", "Go" to "✅", listOf("Stop" to "🛑", "Sleep" to "😴", "Sit" to "🪑")),
                pickRound("s3", "Where do we cross the road?", "Where do we cross the road?", "Zebra crossing" to "🦓", listOf("Anywhere" to "❓", "On a bend" to "↩️", "Between cars" to "🚗")),
                pickRound("s4", "Who should we hold hands with on the road?", "Who should we hold hands with?", "A grown-up" to "🧑", listOf("Nobody" to "🙅", "A cat" to "🐱", "A toy" to "🧸")),
            ),
        )

        lesson(
            id = "quiz", title = "Know My World quiz", subtitle = "Level 3 check-up",
            description = "Plants, animals, senses, safety and our Earth.",
            game = GameType.QUIZ, intro = "The final quiz of the book!",
            rounds = listOf(
                pickRound("q1", "Which part of a plant drinks water?", "Which part drinks water?", "Root" to "🌱", listOf("Leaf" to "🍃", "Flower" to "🌸", "Stem" to "🌿")),
                pickRound("q2", "A baby cat is called a…", "A baby cat is called", "Kitten" to "🐱", listOf("Puppy" to "🐶", "Calf" to "🐄", "Chick" to "🐔")),
                pickRound("q3", "Which sense do we use for a drum?", "Which sense for a drum?", "Hear" to "👂", listOf("See" to "👁️", "Taste" to "👅", "Smell" to "👃")),
                pickRound("q4", "🚦  Red light means…", "Red light means", "Stop" to "🛑", listOf("Go" to "✅", "Run" to "🏃", "Dance" to "💃")),
                pickRound("q5", "Which is a good habit?", "Which is a good habit?", "Wash hands" to "🧼", listOf("Litter" to "🗑️", "Waste water" to "🚰", "Skip breakfast" to "🍽️")),
            ),
        )
    }

    val all: List<Lesson> get() = letterLand + numberLand + knowMyWorld
}
