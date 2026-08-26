package com.catokids.app.data.local

import com.catokids.app.data.model.DevelopmentalDomain
import com.catokids.app.data.model.DevelopmentalDomain.*
import com.catokids.app.data.model.Grade
import com.catokids.app.data.model.Grade.*
import com.catokids.app.data.model.TeachingResource

/**
 * The teacher training library: one grade-specific "how to teach this" briefing per
 * developmental domain per grade (16 domains × 3 grades = 48 briefings). Bundled with
 * the app like the curriculum and shop catalog are — no network round trip to open it.
 *
 * These are deliberately *not* the same text reused across grades: what "cognitive
 * development" means to teach a Pre-KG three-year-old is a different briefing than what
 * it means for a UKG five-year-old, and every entry below reflects that.
 */
object TeacherResourceLibrary {

    val all: List<TeachingResource> = listOf(

        // ---------------- Language Development ----------------
        TeachingResource(LANGUAGE_DEVELOPMENT, PREKG,
            "Talk-rich routines build vocabulary before grammar arrives.",
            listOf("Name everyday objects and actions", "Imitate simple words and sounds", "Point to what they want while an adult narrates it"),
            listOf("Narrate what the child is doing in short sentences", "Repeat new words often across the day", "Sing simple rhymes and pause for them to fill in the last word"),
            "Uses single words or two-word phrases to ask for things."),
        TeachingResource(LANGUAGE_DEVELOPMENT, LKG,
            "Children start combining words into short sentences and asking questions.",
            listOf("Build 3-4 word sentences", "Ask simple who/what/where questions", "Follow two-step spoken instructions"),
            listOf("Expand a child's sentence back to them one word longer", "Ask open \"what happened next\" questions during play", "Model correct grammar without correcting harshly"),
            "Speaks in short sentences and is understood by unfamiliar adults."),
        TeachingResource(LANGUAGE_DEVELOPMENT, UKG,
            "Vocabulary and sentence structure grow fast; storytelling and reasoning aloud begin.",
            listOf("Use full sentences with connectors like \"because\" and \"and then\"", "Retell an event in order", "Ask \"why\" questions and listen to the answer"),
            listOf("Hold daily show-and-tell time", "Ask children to explain a game's rules in their own words", "Introduce one new \"big\" word a day and use it naturally"),
            "Retells a short story or event with a beginning, middle and end."),

        // ---------------- Cognitive Development ----------------
        TeachingResource(COGNITIVE_DEVELOPMENT, PREKG,
            "Thinking is sensory and concrete — children learn by touching, stacking and sorting.",
            listOf("Match identical objects", "Sort by one property (color or size)", "Find a hidden object after watching it get hidden"),
            listOf("Use real objects before pictures", "Let them repeat the same puzzle many times", "Narrate cause and effect as it happens (\"you pushed it, it fell\")"),
            "Completes a 3-4 piece puzzle or simple shape sorter independently."),
        TeachingResource(COGNITIVE_DEVELOPMENT, LKG,
            "Children start comparing, sequencing and holding two ideas in mind at once.",
            listOf("Sort by two properties at once", "Sequence 3 pictures into story order", "Notice what's \"different\" in a set"),
            listOf("Ask \"how did you know?\" after every sort", "Use size-ordering games, biggest to smallest", "Play simple memory-matching games"),
            "Explains the rule they used to sort or order something."),
        TeachingResource(COGNITIVE_DEVELOPMENT, UKG,
            "Early logical reasoning, patterns and simple planning emerge.",
            listOf("Continue and create AB and ABC patterns", "Solve simple \"what comes next\" problems", "Plan two steps ahead in a game"),
            listOf("Ask children to predict before revealing an answer", "Introduce pattern blocks and beads", "Play simple strategy games like tic-tac-toe"),
            "Predicts the next item in a pattern and explains why."),

        // ---------------- Early Practical Life ----------------
        TeachingResource(EARLY_PRACTICAL_LIFE, PREKG,
            "Independence starts with dressing, tidying and simple self-care.",
            listOf("Pour without spilling using a small jug", "Put toys back in a labeled bin", "Attempt to put on their own shoes"),
            listOf("Use small, child-sized tools and pitchers", "Break every task into slow, visible steps", "Praise the attempt, not just the result"),
            "Completes a one-step self-care task, like washing hands, with minimal help."),
        TeachingResource(EARLY_PRACTICAL_LIFE, LKG,
            "Children take on short multi-step routines and simple responsibilities.",
            listOf("Follow a 3-step tidy-up routine", "Button a large button or zip a jacket", "Set a table with the right number of items"),
            listOf("Give one small classroom job per week and rotate it", "Use picture checklists for routines", "Let small mistakes happen and calmly help them fix it"),
            "Completes a 2-3 step routine (wash, dry, put away) without reminders."),
        TeachingResource(EARLY_PRACTICAL_LIFE, UKG,
            "Growing independence and care for shared spaces and belongings.",
            listOf("Pack their own bag using a checklist", "Care for a classroom plant or pet on a schedule", "Help a peer with a practical task"),
            listOf("Assign a weekly \"classroom helper\" role", "Teach how to calmly fix a small mistake, like wiping a spill", "Link chores to real outcomes (\"the plant needs water or it wilts\")"),
            "Takes initiative on a classroom responsibility without being asked."),

        // ---------------- Fine Motor ----------------
        TeachingResource(FINE_MOTOR, PREKG,
            "Hand strength and the pincer grasp are still forming.",
            listOf("Pick up small objects with thumb and forefinger", "Scribble with a fat crayon", "Stack 4-6 blocks"),
            listOf("Offer playdough squeezing and rolling daily", "Use chunky crayons and short drawing sessions", "Thread large beads onto a shoelace"),
            "Uses a pincer grasp to pick up a small object like a bead."),
        TeachingResource(FINE_MOTOR, LKG,
            "Children refine grip and start making controlled, purposeful marks.",
            listOf("Trace straight and curved lines", "Use child-safe scissors to cut along a line", "Copy a circle or cross shape"),
            listOf("Practice tracing before free drawing", "Do weekly \"cutting strips\" with safety scissors", "Use tweezer or tongs games to build grip strength"),
            "Holds a crayon or pencil with a tripod-ish grip and controls direction."),
        TeachingResource(FINE_MOTOR, UKG,
            "Precision and pencil control approach what's needed for writing.",
            listOf("Copy their name", "Color within a boundary most of the time", "Cut out a simple shape neatly"),
            listOf("Use dot-to-dot and maze worksheets", "Practice pre-writing strokes daily: lines, curves, zigzags", "Give real tools — child scissors, tongs, droppers — for fine motor play"),
            "Writes some recognizable letters of their own name unprompted."),

        // ---------------- Gross Motor ----------------
        TeachingResource(GROSS_MOTOR, PREKG,
            "Balance and big-muscle movement are just becoming reliable.",
            listOf("Walk without support and start to run", "Climb a low step with help", "Kick a large stationary ball"),
            listOf("Give open floor space for crawling, climbing and rolling", "Use music for simple stop-and-go movement games", "Hold hands for balance practice on a line"),
            "Walks steadily and attempts to run without falling often."),
        TeachingResource(GROSS_MOTOR, LKG,
            "Coordination improves — jumping, climbing and throwing become purposeful.",
            listOf("Jump with both feet off the ground", "Catch a large soft ball with two hands", "Walk on a line heel-to-toe"),
            listOf("Set up simple obstacle courses: crawl, jump, balance", "Play catch with a soft, large ball first", "Use hopscotch-style floor games"),
            "Jumps forward with both feet and lands with control."),
        TeachingResource(GROSS_MOTOR, UKG,
            "Coordination, speed and rule-based movement games become possible.",
            listOf("Skip or gallop", "Throw a ball toward a target", "Play simple team games with basic rules"),
            listOf("Introduce relay races and follow-the-leader with varied movements", "Teach one game rule at a time before adding more", "Encourage skipping practice in short daily bursts"),
            "Combines movement and rules, like stopping on a signal during a game."),

        // ---------------- Social Skills ----------------
        TeachingResource(SOCIAL_SKILLS, PREKG,
            "Play is mostly parallel — alongside others rather than fully with them.",
            listOf("Play near peers without conflict", "Imitate a peer's action in play", "Hand over a toy when asked by an adult"),
            listOf("Model sharing language out loud (\"my turn, your turn\")", "Keep group activities very short, 2-3 minutes", "Supervise closely and step in before frustration peaks"),
            "Plays contentedly near other children for a few minutes."),
        TeachingResource(SOCIAL_SKILLS, LKG,
            "Children begin true cooperative play and simple turn-taking.",
            listOf("Take turns in a 2-person game", "Join an ongoing group activity", "Use words instead of grabbing when there's a conflict"),
            listOf("Use visual turn-taking tools like a timer or a \"talking object\"", "Coach through conflicts with simple scripts (\"can I have a turn when you're done?\")", "Praise cooperative moments specifically and immediately"),
            "Waits for a turn without adult prompting most of the time."),
        TeachingResource(SOCIAL_SKILLS, UKG,
            "Group games with rules, friendship preferences and a basic sense of fairness emerge.",
            listOf("Follow rules in a small group game", "Include a peer who's left out", "Negotiate a simple disagreement with words"),
            listOf("Run small-group games of 3-5 children with clear, simple rules", "Hold a short circle time for children to raise and solve a shared problem", "Recognize and name kind or fair behavior when it happens"),
            "Resolves a small disagreement with a peer using words, without adult help."),

        // ---------------- Emotional Skills ----------------
        TeachingResource(EMOTIONAL_SKILLS, PREKG,
            "Emotions are big and fast; children need co-regulation from adults.",
            listOf("Point to or name \"happy\" and \"sad\" faces", "Calm down with an adult's help within a few minutes", "Seek comfort when upset"),
            listOf("Name the child's feeling out loud before problem-solving (\"you're sad the toy broke\")", "Keep a predictable daily routine to reduce anxiety", "Offer a comfort object or quiet corner"),
            "Shows a wide range of emotions and settles with adult support."),
        TeachingResource(EMOTIONAL_SKILLS, LKG,
            "Children start naming their own feelings and trying simple calming strategies.",
            listOf("Name their own emotion in the moment", "Try one calming strategy, like a deep breath, with a reminder", "Recognize a peer's emotion from their face"),
            listOf("Teach one simple calming tool (\"smell the flower, blow the candle\" breathing) and practice it while calm", "Use an emotions chart for daily check-ins", "Read a story and pause to ask \"how does this character feel?\""),
            "Names their own feeling using a word, not just crying or acting out."),
        TeachingResource(EMOTIONAL_SKILLS, UKG,
            "Self-regulation and empathy strengthen; children can reflect after the moment has passed.",
            listOf("Use a calming strategy independently when upset", "Describe why they feel a certain way", "Notice and respond kindly to a peer's distress"),
            listOf("Debrief after a conflict once everyone is calm, not during", "Give children a \"feelings toolbox\" of 2-3 strategies to choose from", "Celebrate effort at self-control, not just the outcome"),
            "Uses a learned calming strategy without being told to."),

        // ---------------- Art & Craft ----------------
        TeachingResource(ART_AND_CRAFT, PREKG,
            "Process matters far more than the result — exploration is the goal.",
            listOf("Explore paint, crayons and playdough freely", "Make marks with different tools", "Tear and crumple paper for texture play"),
            listOf("Cover the table and let mess happen", "Offer big paper and big movements", "Never ask \"what is it?\" — ask \"tell me about it\" instead"),
            "Engages with art materials for several minutes with visible enjoyment."),
        TeachingResource(ART_AND_CRAFT, LKG,
            "Children start planning simple creations and using tools with more control.",
            listOf("Use glue and scissors for a simple collage", "Draw a recognizable shape, like a sun or house", "Mix two colors and notice the change"),
            listOf("Offer a simple 2-3 step craft with a visible model", "Introduce color-mixing as a mini science experiment", "Give a choice between two materials to build decision-making"),
            "Completes a simple 2-step craft (cut, then glue) with minimal help."),
        TeachingResource(ART_AND_CRAFT, UKG,
            "Craft becomes intentional — children plan, follow steps and add personal detail.",
            listOf("Follow a 3-4 step craft sequence", "Draw a picture that tells a story", "Use craft to represent something they learned"),
            listOf("Connect crafts to the week's story or science topic", "Let children explain their finished piece to the class", "Introduce simple planning: \"what will you make before you start?\""),
            "Describes their finished artwork and the steps they used to make it."),

        // ---------------- English ----------------
        TeachingResource(ENGLISH, PREKG,
            "Letter and sound awareness starts through play, songs and picture books.",
            listOf("Recognize letters in their own name", "Enjoy rhymes and repeated phrases", "Point to pictures matching a spoken word"),
            listOf("Sing the alphabet with actions", "Point to letters on signs during everyday routines", "Read the same favorite book repeatedly — repetition builds recognition"),
            "Recognizes 3-5 letters, especially from their own name."),
        TeachingResource(ENGLISH, LKG,
            "Letter-sound links form and children begin blending simple sounds.",
            listOf("Name most uppercase and lowercase letters", "Produce the sound for common letters", "Blend two sounds together, like \"c-a\""),
            listOf("Use letter-sound songs and picture cards together", "Practice one letter sound a day with an object hunt", "Keep sessions short — 5 to 10 minutes — and playful"),
            "Says the correct sound for at least half the alphabet."),
        TeachingResource(ENGLISH, UKG,
            "Children move from letter sounds to reading and building simple words.",
            listOf("Blend 3 sounds into a CVC word", "Read a handful of sight words", "Write their name and copy simple words"),
            listOf("Use magnetic or physical letters to build words hands-on", "Practice sight words in short, frequent bursts rather than long drills", "Encourage sounding it out rather than giving the answer immediately"),
            "Reads a simple CVC word, like \"cat\" or \"sun\", independently."),

        // ---------------- Math ----------------
        TeachingResource(MATH, PREKG,
            "Number sense starts with counting objects, not digits.",
            listOf("Count up to 5 objects, touching each one", "Recognize \"more\" and \"less\" in a visual comparison", "Match a group of objects to the right count"),
            listOf("Count everything out loud during the day — steps, snacks, toys", "Use real objects before number symbols", "Keep counting fun with songs and finger rhymes"),
            "Counts up to 5 objects correctly, one touch per object."),
        TeachingResource(MATH, LKG,
            "Children link number names to written numerals and count further.",
            listOf("Count to 10-20 by rote and count objects up to 10 accurately", "Recognize written numerals 1-10", "Compare two small groups and say which has more"),
            listOf("Use number lines and hopping games", "Match numeral cards to dot cards", "Practice one-to-one correspondence with snack-time counting"),
            "Matches a written numeral to the correct quantity of objects up to 10."),
        TeachingResource(MATH, UKG,
            "Early operations, shapes and simple measurement enter the picture.",
            listOf("Add and subtract within 10 using objects", "Recognize and name basic 2D shapes", "Compare length, size or weight using simple words"),
            listOf("Use fingers, counters or a number line for every addition problem", "Go on a \"shape hunt\" around the classroom", "Introduce simple word problems tied to real snack or toy counts"),
            "Solves a simple addition or subtraction story problem within 10, using objects or fingers."),

        // ---------------- Science (EVS) ----------------
        TeachingResource(SCIENCE_EVS, PREKG,
            "Curiosity about the immediate world — weather, animals, plants — drives early science.",
            listOf("Name common animals and the sounds they make", "Notice weather changes, like sunny or rainy", "Explore water, sand or textures with their hands"),
            listOf("Take a short \"look and touch\" nature walk weekly", "Ask \"what do you notice?\" instead of giving facts", "Use real fruit, leaves and toys, not just pictures"),
            "Names 3-5 animals or plants from their everyday environment."),
        TeachingResource(SCIENCE_EVS, LKG,
            "Children start asking \"why\" about the natural world and making simple observations.",
            listOf("Describe a simple life cycle, like seed to plant, in order", "Sort living vs non-living things", "Observe and describe what they see in a simple experiment"),
            listOf("Grow a real seed in a cup and check on it together daily", "Sort a mixed basket of toys and natural objects into \"living/non-living\"", "Ask a prediction question before every experiment"),
            "Describes at least one step of a simple life cycle correctly."),
        TeachingResource(SCIENCE_EVS, UKG,
            "Simple cause-and-effect experiments and community/environment awareness develop.",
            listOf("Predict and test a simple cause-effect question, like sink or float", "Explain why we recycle or care for plants", "Identify their body parts and basic senses"),
            listOf("Run one hands-on experiment a week and record the result together as a class", "Connect science to everyday habits like washing hands or recycling", "Ask \"what do you think will happen?\" before every demonstration"),
            "Makes a prediction before an experiment and compares it to what actually happened."),

        // ---------------- STEAM ----------------
        TeachingResource(STEAM, PREKG,
            "STEAM at this age is stacking, dumping and noticing what happens — informal exploration.",
            listOf("Build a simple tower and knock it down", "Notice that some objects float and others sink", "Use blocks to represent something real, like \"this is my house\""),
            listOf("Offer open-ended materials — blocks, boxes, water — with no \"right\" outcome", "Narrate what happens as they experiment", "Let repetition happen; building and knocking down is the learning"),
            "Stays engaged in a hands-on building or exploring activity for several minutes."),
        TeachingResource(STEAM, LKG,
            "Children begin simple design-and-test thinking with adult scaffolding.",
            listOf("Build something to solve a simple challenge, like a bridge for a toy car", "Use a tool — a ramp, a magnet — to test an idea", "Describe what they built in simple terms"),
            listOf("Pose one simple challenge and offer a small set of materials", "Ask \"what could we try instead?\" when something doesn't work", "Celebrate the trying, not just success"),
            "Attempts a second try after a first attempt doesn't work."),
        TeachingResource(STEAM, UKG,
            "Early engineering-design thinking — plan, build, test, improve — becomes possible.",
            listOf("Plan a simple structure before building it", "Test and improve a design based on what happened", "Explain the steps of what they built and why"),
            listOf("Use a simple \"plan, build, test, fix\" cycle out loud for every STEAM challenge", "Introduce basic tools like ramps, pulleys and magnets with adult supervision", "Let children present their build to the class"),
            "Changes their design after testing it, based on what they observed."),

        // ---------------- Stories ----------------
        TeachingResource(STORIES, PREKG,
            "Stories build listening stamina and a love of books through repetition and pictures.",
            listOf("Sit and listen to a short story with pictures", "Point to characters or objects when asked", "Fill in a repeated phrase from a favorite book"),
            listOf("Read the same short book many times — children love repetition", "Use big, expressive voices for characters", "Let them turn the pages and \"read\" the pictures back to you"),
            "Sits through a short picture book and points to familiar characters."),
        TeachingResource(STORIES, LKG,
            "Children begin following a simple plot and predicting what happens next.",
            listOf("Answer \"who\" and \"what\" questions about a story", "Predict what might happen next", "Act out a simple story with props or puppets"),
            listOf("Pause mid-story and ask \"what do you think happens next?\"", "Use puppets or simple props to retell favorite stories", "Ask one simple recall question after reading"),
            "Answers a simple \"who\" or \"what\" question about a story just read."),
        TeachingResource(STORIES, UKG,
            "Children grasp story structure and can retell events in the right order.",
            listOf("Retell a story with a beginning, middle and end", "Identify how a character feels and why", "Compare two stories or characters"),
            listOf("Use a simple story map — first, then, last — after reading", "Ask \"how do you think the character felt, and why?\"", "Encourage children to invent their own short story orally"),
            "Retells a familiar story in the correct order with minimal prompting."),

        // ---------------- Comprehension ----------------
        TeachingResource(COMPREHENSION, PREKG,
            "Comprehension starts with matching words to pictures and objects in the moment.",
            listOf("Point to a named object or picture", "Follow a simple one-step spoken instruction", "Recognize familiar routines from a spoken cue, like \"time to eat\""),
            listOf("Pair every instruction with a gesture at first", "Let children point rather than answer in words if they're not talking much yet", "Keep instructions to one step"),
            "Follows a simple one-step instruction without a gesture prompt."),
        TeachingResource(COMPREHENSION, LKG,
            "Children begin answering direct questions about what they just heard or saw.",
            listOf("Follow a two-step spoken instruction", "Answer a direct \"what\" question about a short story or event", "Sort objects or pictures by a spoken category"),
            listOf("Give instructions in order and check understanding by asking them to repeat it back", "Ask direct comprehension questions right after a story, not much later", "Use \"show me\" tasks to check understanding without requiring speech"),
            "Follows a two-step instruction in the correct order."),
        TeachingResource(COMPREHENSION, UKG,
            "Inferential thinking begins — children can go beyond what was said or shown.",
            listOf("Answer \"why\" and \"how\" questions that require inference", "Follow a three-step instruction", "Summarize what a short passage was mainly about"),
            listOf("Ask \"why do you think that happened?\" instead of only \"what happened?\"", "Build instructions up to three steps gradually", "Ask children to summarize in one sentence what a story was about"),
            "Answers a \"why\" question about a story that isn't directly stated in the text."),

        // ---------------- CVC Words ----------------
        TeachingResource(CVC_WORDS, PREKG,
            "Too early for blending — this stage is about individual letter sounds only.",
            listOf("Recognize a few individual letters by sight", "Produce the sound of 2-3 common letters", "Notice that words are made of separate sounds through clapping games"),
            listOf("Clap out syllables in the child's own name", "Play \"I spy something that starts with...\" using sounds", "Keep this purely oral and playful — no worksheets yet"),
            "Produces the correct sound for at least one or two letters."),
        TeachingResource(CVC_WORDS, LKG,
            "Children start blending two sounds and get ready for full CVC blending.",
            listOf("Blend two sounds together, like \"a-t\" to \"at\"", "Identify the first sound in a spoken word", "Match a letter to its sound reliably for common letters"),
            listOf("Use letter tiles to physically slide two sounds together", "Play \"what's the first sound?\" games with everyday objects", "Practice a small set of 3-letter word families before mixing them"),
            "Blends two given sounds into a short chunk like \"at\" or \"an.\""),
        TeachingResource(CVC_WORDS, UKG,
            "Full CVC blending — consonant-vowel-consonant — becomes a core reading skill.",
            listOf("Blend three sounds into a full CVC word, like cat, dog, sun", "Read a set of 10-15 CVC words fluently", "Spell a simple CVC word by sounding it out"),
            listOf("Use word families, like cat, hat, mat, to build blending speed", "Practice with physical letter tiles before moving to paper", "Celebrate every successful blend immediately to build confidence"),
            "Reads 5 or more different CVC words independently."),

        // ---------------- Two-letter Words ----------------
        TeachingResource(TWO_LETTER_WORDS, PREKG,
            "Not yet developmentally appropriate for reading — focus stays on oral language and letter play.",
            listOf("Recognize that some words are short and some are long by sound", "Enjoy simple word games with an adult", "Identify the first letter of their own name"),
            listOf("Keep this entirely oral: clapping and sound games, not text", "Point out short familiar words like \"no\" and \"go\" in daily life", "Celebrate any letter recognition warmly"),
            "Notices and enjoys simple sound-based word games."),
        TeachingResource(TWO_LETTER_WORDS, LKG,
            "Children begin sight-reading a small set of very short, high-frequency two-letter words.",
            listOf("Recognize \"no,\" \"go,\" \"up,\" \"in,\" \"on,\" \"it\" by sight", "Write one or two of these words with support", "Use these words correctly in a simple spoken sentence"),
            listOf("Put these words on cards and turn recognition into a quick daily game", "Point them out wherever they appear — signs, books", "Keep the set very small at first, 3-4 words, not all at once"),
            "Reads 3 or more common two-letter sight words on sight."),
        TeachingResource(TWO_LETTER_WORDS, UKG,
            "Two-letter words become fully automatic sight words, freeing attention for full sentences.",
            listOf("Read all common two-letter sight words instantly", "Use them correctly while reading a full simple sentence", "Spell them from memory"),
            listOf("Mix sight words into simple sentence-building practice", "Time a quick, light and encouraging daily flash-card round for fluency", "Have children build short sentences using two-letter words plus a CVC word"),
            "Reads a full simple sentence containing two-letter sight words without sounding each one out."),
    )

    fun forGrade(grade: Grade): List<TeachingResource> = all.filter { it.grade == grade }

    fun forDomain(domain: DevelopmentalDomain): List<TeachingResource> = all.filter { it.domain == domain }

    fun find(domain: DevelopmentalDomain, grade: Grade): TeachingResource? =
        all.firstOrNull { it.domain == domain && it.grade == grade }
}
