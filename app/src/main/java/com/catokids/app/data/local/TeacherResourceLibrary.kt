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
 *
 * Ten domains still carry their original illustrative briefings. The other six (Emotional
 * Skills, Language Development, Science (EVS), Social Skills, Gross Motor, Fine Motor) are
 * grounded in the program's real source curriculum documents — each overview, goal and tip
 * reflects recurring themes actually found across that domain's library activities.
 */
object TeacherResourceLibrary {

    val all: List<TeachingResource> = listOf(

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

        // ---------------- Emotional Skills (real content) ----------------
        TeachingResource(EMOTIONAL_SKILLS, PREKG,
            "Short, adult-led circle activities — sorting, guessing games, a mystery bag — give children a gentle, predictable way to sit with surprise or a wrong guess and name how it felt.",
            listOf("Sit through a short guided activity with adult support", "Point to or say how a surprise or a guess made them feel", "Try again calmly after a small setback, like a toppled tower"),
            listOf("Keep every activity under 10 minutes and narrate feelings out loud as they happen", "Model the reflection question yourself first (\"That surprised me too!\")", "Praise the trying, not just the right guess or the standing tower"),
            "Stays engaged through a short circle activity and can point to or name how it felt when something surprised them."),
        TeachingResource(EMOTIONAL_SKILLS, LKG,
            "Build-test-reflect activities — block towers, bridges, mazes, shadow play — give children a real reason to feel frustration or pride and a routine (a reflection circle) for talking about it.",
            listOf("Keep trying after a structure falls or a first attempt fails", "Say one thing that was \"tricky\" and one that felt good about an activity", "Notice and name a partner's feeling during group work"),
            listOf("Always close with the same two reflection questions: \"what was tricky?\" and \"how did it feel?\"", "Pair a child who gives up quickly with a steady partner during build-and-test tasks", "Celebrate the rebuild after a collapse as loudly as the first success"),
            "Rebuilds or retries after something doesn't work the first time, and can name what felt tricky about it."),
        TeachingResource(EMOTIONAL_SKILLS, UKG,
            "Multi-step group projects — bridges to test, sandwiches to make, stories to co-write — ask children to plan together, handle a shared setback, and reflect on what they'd change next time.",
            listOf("Work through a multi-step group task without giving up when a step goes wrong", "Explain why a plan didn't work and what they'd change", "Support a group member who is frustrated or stuck"),
            listOf("Debrief every group project with \"what would you change if you built it again?\"", "Let a bridge or tower actually fail sometimes — the recovery is the lesson", "Rotate who leads each group so children practice both leading and supporting"),
            "Explains what didn't work in their own words and proposes a change, without needing an adult to point it out."),

        // ---------------- Language Development (real content) ----------------
        TeachingResource(LANGUAGE_DEVELOPMENT, PREKG,
            "Naming games, picture-book exploration and songs with actions build a first bank of words children can point to, repeat and eventually say on their own.",
            listOf("Name familiar objects and pictures on request", "Repeat words and simple song phrases after an adult", "Point to a picture that matches a word they hear"),
            listOf("Narrate everyday routines in short, repeated phrases", "Read the same picture book many times and pause for the child to fill in a word", "Use songs with actions so the movement reinforces the word"),
            "Reliably names 8-10 familiar objects or pictures and repeats short song phrases."),
        TeachingResource(LANGUAGE_DEVELOPMENT, LKG,
            "Listening games, rhyme play and simple show-and-tell push children from single words into short sentences and back-and-forth conversation.",
            listOf("Answer a simple question in a short sentence", "Identify a rhyming word or the first sound in a word", "Retell one or two events from a story just read"),
            listOf("Play rhyming and \"first sound\" games daily in short, playful bursts", "After reading, ask \"what happened first? what happened next?\"", "Expand a child's answer back to them one word longer instead of correcting it"),
            "Answers a \"what happened\" question about a story in a full short sentence."),
        TeachingResource(LANGUAGE_DEVELOPMENT, UKG,
            "Show-and-tell, sentence-building with word cards and collaborative storytelling ask children to organize more language at once — full sentences, descriptions, and stories with a sequence.",
            listOf("Describe an object or picture with several sentences of detail", "Build or complete a full sentence from word or picture cards", "Tell a short story in order, alone or as part of a group"),
            listOf("Hold regular show-and-tell and coach for full sentences, not single words", "Use word-card or picture-sequence games to make sentence order visible", "Let children build a class or group story one sentence at a time"),
            "Tells a short story or describes an item in several connected sentences, in the right order."),

        // ---------------- Science (EVS) (real content) ----------------
        TeachingResource(SCIENCE_EVS, PREKG,
            "Sensory exploration of the child's own body, school, and everyday nature — touching, naming, sorting real fruits, animals and plants — builds the vocabulary science later builds on.",
            listOf("Name themselves, family members and familiar school routines", "Name common fruits, vegetables and animals from real objects or pictures", "Explore a simple sensory material (water, sand, playdough) with guidance"),
            listOf("Always start with the real object or picture before any worksheet", "Let children touch, smell and describe before naming — description comes first", "Keep every activity to one clear sensory question, like \"does it float?\""),
            "Names several fruits, vegetables or animals from real examples and explores a sensory material with interest."),
        TeachingResource(SCIENCE_EVS, LKG,
            "Simple experiments with a predict-then-test structure — sink or float, ice melting, seed growing — and wider animal and plant topic units build early observation and classification.",
            listOf("Predict a simple outcome before testing it", "Sort animals or objects into groups by an observed feature (habitat, living/non-living)", "Describe one step of how a plant or animal grows or changes"),
            listOf("Ask \"what do you think will happen?\" before every experiment, every time", "Use real sorting trays for animal habitat and living/non-living activities", "Revisit a growing seed or plant daily so change becomes visible over time"),
            "Makes a prediction before a simple experiment and describes what actually happened."),
        TeachingResource(SCIENCE_EVS, UKG,
            "Multi-step experiments (volcanoes, cloud in a jar, color mixing) and cross-habitat animal comparisons (arctic vs. desert, wild vs. domestic) push children toward explaining cause and effect, not just observing it.",
            listOf("Explain why an experiment turned out the way it did", "Compare two habitats or animal groups and explain a key difference", "Run a simple experiment with guidance and record the result"),
            listOf("Ask \"why do you think that happened?\" after every result, not just \"what happened?\"", "Use habitat-comparison sorting (hot vs. cold, land vs. water) to build explanatory language", "Let children help set up and record one experiment step themselves"),
            "Explains in their own words why an experiment came out the way it did, using a cause-and-effect sentence."),

        // ---------------- Social Skills (real content) ----------------
        TeachingResource(SOCIAL_SKILLS, PREKG,
            "Simple tastes, sounds and sights from around the world — a snack, a song, a dress-up item — give children their first sense that other people live and celebrate differently than they do.",
            listOf("Try or observe something from another culture without hesitation", "Enjoy music, dance or dress-up from a culture other than their own", "Point to or name one thing that's different about another country's food, clothes or music"),
            listOf("Bring in a real object, snack or piece of music rather than only a picture", "Keep the framing warm and curious — \"look what's different and fun,\" not a test", "Let children touch, taste or wear things, not just watch"),
            "Engages happily with a food, song or dress-up item from another culture and can name what's different about it."),
        TeachingResource(SOCIAL_SKILLS, LKG,
            "Folktales, traditional games and flag or art crafts from named countries start connecting a place to its people, stories and traditions.",
            listOf("Name a country or culture connected to a story, game or craft they just did", "Play a traditional game from another culture with the group", "Recognize a few national flags or symbols"),
            listOf("Always name the country or culture out loud before, during and after the activity", "Teach a traditional playground game exactly as it's played, then let the class play it", "Use a world map or globe alongside every culture activity so place sticks with content"),
            "Names the country a story, game or craft came from and can point to it on a simple map."),
        TeachingResource(SOCIAL_SKILLS, UKG,
            "Festival simulations, language greetings and world map exploration ask children to hold several cultures in mind at once and talk about what's shared and what's different between them.",
            listOf("Explain the significance of a celebration or tradition from another culture", "Greet someone using a word from another language", "Compare a tradition or celebration to something similar in their own life"),
            listOf("Simulate one festival a term with real decorations, food or crafts, and explain its meaning first", "Teach greetings in 2-3 languages and practice them as a daily routine", "Ask \"what's the same as something we do?\" as well as \"what's different?\""),
            "Explains in their own words what a celebration from another culture is for, and compares it to something familiar."),

        // ---------------- Gross Motor (real content) ----------------
        TeachingResource(GROSS_MOTOR, PREKG,
            "Animal walks, simple ball rolling and short obstacle paths build the basic balance and whole-body control everything else depends on.",
            listOf("Imitate a simple animal movement (crawl, waddle, hop) with a steady rhythm", "Roll or gently catch a large ball", "Walk along a taped line or low obstacle with support"),
            listOf("Demonstrate the movement yourself first, then move alongside the child", "Use big, slow, exaggerated animal movements before adding any speed", "Keep obstacle paths short — 3-4 stations — and let children repeat them freely"),
            "Copies a simple animal movement and walks a short line or low obstacle with growing steadiness."),
        TeachingResource(GROSS_MOTOR, LKG,
            "Jumping, hopping, throwing and short relay games build the coordination and rule-following needed for real team play.",
            listOf("Jump or hop with control, landing on balance", "Throw or toss a ball toward a target with some accuracy", "Take a turn in a simple relay or partner game"),
            listOf("Break every new skill (jump, throw, balance) into one clear cue, like \"bend, then jump\"", "Use soft, large balls before smaller ones for throwing and catching practice", "Set up simple 2-3 station obstacle courses that combine skills"),
            "Jumps or hops with controlled landings and takes their turn correctly in a simple relay game."),
        TeachingResource(GROSS_MOTOR, UKG,
            "Multi-station obstacle courses, races and team games with real rules ask children to combine speed, coordination and teamwork under a bit of pressure.",
            listOf("Complete a multi-station obstacle course independently", "Play a race or relay game while following its rules", "Encourage or cheer for teammates during a group physical game"),
            listOf("Run relay races and team games with one rule introduced at a time", "Rotate team membership often so children play with everyone, not just friends", "Praise good sportsmanship and cheering as loudly as winning"),
            "Completes a multi-station obstacle course or relay following the rules, and encourages teammates along the way."),

        // ---------------- Fine Motor (real content) ----------------
        TeachingResource(FINE_MOTOR, PREKG,
            "Squeezing playdough, threading large beads and sorting small objects build the hand strength and pincer grasp that scissors and pencils will need later.",
            listOf("Squeeze, roll and pinch playdough or similar material", "Pick up and place a small object using thumb and forefinger", "Thread a large bead onto a shoelace or pipe cleaner"),
            listOf("Offer playdough or squeeze-and-transfer play daily, even for just a few minutes", "Use large beads and thick laces before anything fiddly", "Let repetition happen — squeezing and threading the same way many times is the skill-building"),
            "Uses a pincer grasp to pick up a small object and threads a large bead with growing control."),
        TeachingResource(FINE_MOTOR, LKG,
            "Cutting practice, lacing cards and tweezer or tongs sorting games build the controlled, purposeful hand movements needed for real tool use.",
            listOf("Cut along a straight or gently curved line with safety scissors", "Sort small objects using tweezers or tongs", "Lace or thread through a series of holes in order"),
            listOf("Do short, frequent cutting-strip practice rather than one long session", "Use tweezer and tongs transfer games to build the grip strength cutting needs", "Offer a choice of two materials so children make small decisions during fine motor play"),
            "Cuts along a line with safety scissors and completes a tweezer- or tongs-sorting task without losing focus."),
        TeachingResource(FINE_MOTOR, UKG,
            "Tracing, hole punching and small-object construction tasks push hand control toward the precision that pre-writing and detailed craft work need.",
            listOf("Trace a shape, letter or name with reasonable accuracy", "Build a small structure from beads, blocks or paper tubes with fine control", "Complete a multi-step fine motor task (like a hole-punch craft) independently"),
            listOf("Practice pre-writing strokes (lines, curves, zigzags) briefly every day before free drawing", "Give real small tools — hole punches, tweezers, droppers — not just crayons", "Let children finish a fine motor project across two sittings if it has several steps"),
            "Traces their name or a simple shape with control and completes a multi-step fine motor task independently."),

    )

    fun forGrade(grade: Grade): List<TeachingResource> = all.filter { it.grade == grade }

    fun forDomain(domain: DevelopmentalDomain): List<TeachingResource> = all.filter { it.domain == domain }

    fun find(domain: DevelopmentalDomain, grade: Grade): TeachingResource? =
        all.firstOrNull { it.domain == domain && it.grade == grade }
}

