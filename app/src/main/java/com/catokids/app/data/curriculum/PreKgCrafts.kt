package com.catokids.app.data.curriculum

import com.catokids.app.data.model.Craft

/**
 * The hands-on pages of the PreKG workbooks, one craft per letter in Letter Land plus the
 * cut-and-stick pages of Know My World. Wording follows the "Note :-" line on each page.
 *
 * Children see these on the result screen of the matching lesson; teachers see the same
 * list in the Activity Library (Art & Craft · Pre-KG) so they can assign them as homework.
 */
internal object PreKgCrafts {

    private fun craft(letter: Char, title: String, emoji: String, materials: List<String>, vararg steps: String) =
        Craft(
            id = "prekg_craft_${letter.lowercaseChar()}",
            title = title,
            emoji = emoji,
            letter = letter.toString(),
            materials = materials,
            steps = steps.toList(),
        )

    /** One craft per letter, A to Z — the page that follows each "I like reading" page. */
    val letters: Map<Char, Craft> = listOf(
        craft('A', "Spongy Apple", "🍎", listOf("Apple picture", "Sponge", "Red paint"),
            "Dip a small sponge in red paint.",
            "Dab the sponge all over the apple until it is red."),
        craft('B', "Bindi art", "🎈", listOf("Balloon picture", "Bindis"),
            "Peel off one bindi at a time.",
            "Stick the bindis on the balloon to decorate it."),
        craft('C', "Printed carrot", "🥕", listOf("Carrot picture", "A piece of carrot", "Orange paint"),
            "Ask a grown-up to cut a small piece of carrot.",
            "Dip the cut side in orange paint.",
            "Press it on the carrot picture to make prints."),
        craft('D', "Dotted dolphin", "🐬", listOf("Dolphin picture", "Cotton swabs", "Paint"),
            "Dip a cotton swab in paint.",
            "Make dots all over the dolphin."),
        craft('E', "Sticky envelope", "✉️", listOf("Coloured paper", "Glue"),
            "Fold a piece of coloured paper into an envelope with a grown-up.",
            "Glue it onto the page."),
        craft('F', "Finger print fish", "🐟", listOf("Fish picture", "Paint"),
            "Dip your finger in paint.",
            "Press finger prints all over the fish to decorate it."),
        craft('G', "Grippy gate", "🪵", listOf("Gate picture", "Popsicle sticks", "Glue"),
            "Put glue on the gate picture.",
            "Stick popsicle sticks on it to make a gate."),
        craft('H', "Happy hand", "✋", listOf("Paper", "Your favourite paint colour"),
            "Paint your palm with your favourite colour.",
            "Press your hand on the page to make a hand print."),
        craft('I', "I love ice cream", "🍦", listOf("Ice cream cone picture", "3 small balloons", "3 paint colours"),
            "Blow up three small balloons with a grown-up.",
            "Dip each balloon in a different colour.",
            "Press them on top of the cone to make ice cream scoops."),
        craft('J', "Jello jelly", "🖍️", listOf("Jelly picture", "Red crayon"),
            "Colour the jelly red with a red crayon."),
        craft('K', "Crown the King", "👑", listOf("King picture", "Glitter", "Glue"),
            "Put glue on the king's crown.",
            "Sprinkle glitter on the glue to decorate it."),
        craft('L', "Lovely leaves", "🍃", listOf("Leaves from the garden", "Paint", "Paper"),
            "Pick a few leaves from your garden.",
            "Paint one side of each leaf.",
            "Press the leaves on paper to make prints."),
        craft('M', "Matching mat", "🧺", listOf("Coloured paper strips", "Glue"),
            "Ask a grown-up to cut strips of coloured paper.",
            "Weave the strips over and under to make a mat.",
            "Stick the mat on the picture."),
        craft('N', "Nutty nuts", "🥜", listOf("Different nutshells", "Paint", "Paper"),
            "Dip different nutshells in paint.",
            "Press them on paper to print their shapes."),
        craft('O', "Oval orange", "🍊", listOf("Orange picture", "Oval sponge", "Orange paint"),
            "Dip the oval sponge in orange paint.",
            "Colour the orange with the sponge."),
        craft('P', "Pretty parrot", "🦜", listOf("Parrot picture", "Green crayon or paint"),
            "Colour the parrot green."),
        craft('Q', "Quicky quarters", "🔴", listOf("Circle with four quarters", "Red, blue, yellow and green crayons"),
            "Colour the four quarters of the circle red, blue, yellow and green."),
        craft('R', "Rainbow raindrops", "🌈", listOf("Raindrop picture", "Colour stones", "Glue"),
            "Put a little glue on each raindrop.",
            "Stick the colour stones on the raindrops."),
        craft('S', "Sparkly stars", "⭐", listOf("Star picture", "Silver glitter", "Glue"),
            "Spread glue on the star.",
            "Sprinkle silver glitter over it."),
        craft('T', "Timeless trees", "🌳", listOf("Pictures of trees", "Glue"),
            "Find pictures of your favourite trees.",
            "Stick them in your book."),
        craft('U', "U and me ukulele", "🎸", listOf("Ukulele picture", "Crayons", "Wool or string", "Glue"),
            "Colour the ukulele.",
            "Stick pieces of wool or string on it as strings."),
        craft('V', "We and vegetables", "🥗", listOf("Vegetables", "A grown-up", "A photo"),
            "Make a vegetable salad with a grown-up.",
            "Take a photo of it.",
            "Stick the photo in your book."),
        craft('W', "Wonderful watermelon", "🍉", listOf("Paper plate", "Red paint", "Glue"),
            "Ask a grown-up to cut a paper plate in half.",
            "Colour the half plate red.",
            "Stick it on the green rim in your book."),
        craft('X', "X and x", "✏️", listOf("Pencil or crayon"),
            "Trace the big X and the small x."),
        craft('Y', "Yo-yo yarn", "🧶", listOf("Yarn", "Paint", "Paper"),
            "Dip a piece of yarn in paint.",
            "Lay it on the paper and drag it downwards to make art."),
        craft('Z', "Zipzap zebra", "🦓", listOf("Zebra picture", "Black yarn", "Glue"),
            "Put glue on the zebra's stripes.",
            "Stick black yarn on the stripes."),
    ).associateBy { it.letter.single() }

    fun forLetters(group: List<Char>): List<Craft> = group.mapNotNull { letters[it] }

    // ---------- Know My World (PreKG EVS) ----------

    val familyTree = Craft("prekg_craft_family_tree", "This is my family tree", "🌳",
        materials = listOf("Family photos or drawings", "Glue"),
        steps = listOf(
            "Draw or find small photos of your family.",
            "Stick them on the family tree page.",
            "Say each person's name aloud.",
        ))

    val vegetableGarden = Craft("prekg_craft_veg_garden", "Vegetable print garden", "🥔",
        materials = listOf("Vegetables cut by a grown-up (potato, carrot, okra)", "Paint", "Paper"),
        steps = listOf(
            "Ask a grown-up to cut a few vegetables in half.",
            "Dip the cut side in paint.",
            "Print them on paper to make a garden.",
        ))

    val farmAnimals = Craft("prekg_craft_farm_animals", "My farm animals", "🐄",
        materials = listOf("Pictures of farm animals", "Glue"),
        steps = listOf(
            "Find pictures of animals you see on a farm.",
            "Stick them in your book and name each one.",
        ))

    val colourBird = Craft("prekg_craft_colour_bird", "Let's colour the bird", "🦜",
        materials = listOf("Bird picture", "Crayons"),
        steps = listOf("Colour the bird with your favourite colours."))

    val seasonsBoxes = Craft("prekg_craft_seasons", "Cut and stick the seasons", "🍂",
        materials = listOf("Season pictures", "Child-safe scissors", "Glue"),
        steps = listOf(
            "Cut out the season pictures with a grown-up.",
            "Stick each one in the summer, winter, spring or autumn box.",
        ))

    val evs: List<Craft> = listOf(familyTree, vegetableGarden, farmAnimals, colourBird, seasonsBoxes)

    val all: List<Craft> get() = letters.values.toList() + evs
}
