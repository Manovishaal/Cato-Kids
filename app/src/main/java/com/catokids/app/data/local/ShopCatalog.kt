package com.catokids.app.data.local

import com.catokids.app.data.model.ItemCategory
import com.catokids.app.data.model.Rarity
import com.catokids.app.data.model.ShopItem

/**
 * Every option a child can put on their character, bundled with the app like the
 * curriculum is — no network round trip just to open the creator. Skin tones, hair
 * and a starter outfit are free (`price = 0`); accessories, pets, effects and the
 * flashier outfits and backdrops are what gold coins are actually for.
 */
object ShopCatalog {

    val skinTones = listOf(
        ShopItem("peach", ItemCategory.SKIN, "Peach", "🟠"),
        ShopItem("sand",  ItemCategory.SKIN, "Sand",  "🟡"),
        ShopItem("tan",   ItemCategory.SKIN, "Tan",   "🟤"),
        ShopItem("deep",  ItemCategory.SKIN, "Deep",  "⚫"),
    )

    val hairStyles = listOf(
        ShopItem("short",   ItemCategory.HAIR_STYLE, "Short",    "💇"),
        ShopItem("curly",   ItemCategory.HAIR_STYLE, "Curly",    "🌀"),
        ShopItem("pigtails", ItemCategory.HAIR_STYLE, "Pigtails", "🎀"),
        ShopItem("long",    ItemCategory.HAIR_STYLE, "Long",     "💁"),
        ShopItem("bald",    ItemCategory.HAIR_STYLE, "No hair",  "🥚"),
    )

    val hairColors = listOf(
        ShopItem("black",  ItemCategory.HAIR_COLOR, "Black",  "⚫"),
        ShopItem("brown",  ItemCategory.HAIR_COLOR, "Brown",  "🟤"),
        ShopItem("blonde", ItemCategory.HAIR_COLOR, "Blonde", "🟡"),
        ShopItem("red",    ItemCategory.HAIR_COLOR, "Red",    "🔴"),
        ShopItem("blue",   ItemCategory.HAIR_COLOR, "Blue",   "🔵", price = 40, rarity = Rarity.RARE),
        ShopItem("hair_rainbow", ItemCategory.HAIR_COLOR, "Rainbow", "🌈", price = 120, rarity = Rarity.LEGENDARY),
    )

    val outfits = listOf(
        ShopItem("coral",      ItemCategory.OUTFIT, "Coral",       "🧡"),
        ShopItem("teal",       ItemCategory.OUTFIT, "Teal",        "💚"),
        ShopItem("periwinkle", ItemCategory.OUTFIT, "Periwinkle",  "💙"),
        ShopItem("violet",     ItemCategory.OUTFIT, "Violet",      "💜", price = 30, rarity = Rarity.RARE),
        ShopItem("gold",       ItemCategory.OUTFIT, "Gold",        "💛", price = 60, rarity = Rarity.EPIC),
        ShopItem("outfit_rainbow", ItemCategory.OUTFIT, "Rainbow suit", "🌈", price = 150, rarity = Rarity.LEGENDARY),
    )

    val headwear = listOf(
        ShopItem("cap",    ItemCategory.ACCESSORY_HEAD, "Cap",         "🧢"),
        ShopItem("bow",    ItemCategory.ACCESSORY_HEAD, "Bow",         "🎀"),
        ShopItem("crown",  ItemCategory.ACCESSORY_HEAD, "Crown",       "👑", price = 80, rarity = Rarity.EPIC),
        ShopItem("wizard", ItemCategory.ACCESSORY_HEAD, "Wizard hat",  "🧙", price = 100, rarity = Rarity.EPIC),
        ShopItem("halo",   ItemCategory.ACCESSORY_HEAD, "Halo",        "😇", price = 130, rarity = Rarity.LEGENDARY),
    )

    val faceAccessories = listOf(
        ShopItem("glasses",   ItemCategory.ACCESSORY_FACE, "Glasses",   "👓"),
        ShopItem("freckles",  ItemCategory.ACCESSORY_FACE, "Freckles",  "✨"),
        ShopItem("shades",    ItemCategory.ACCESSORY_FACE, "Sunglasses", "🕶️", price = 35, rarity = Rarity.RARE),
        ShopItem("mask",      ItemCategory.ACCESSORY_FACE, "Superhero mask", "🦸", price = 55, rarity = Rarity.EPIC),
    )

    val handAccessories = listOf(
        ShopItem("balloon", ItemCategory.ACCESSORY_HAND, "Balloon", "🎈"),
        ShopItem("book",    ItemCategory.ACCESSORY_HAND, "Book",    "📗"),
        ShopItem("wand",    ItemCategory.ACCESSORY_HAND, "Magic wand", "🪄", price = 70, rarity = Rarity.EPIC),
        ShopItem("shield",  ItemCategory.ACCESSORY_HAND, "Shield",  "🛡️", price = 70, rarity = Rarity.EPIC),
    )

    val pets = listOf(
        ShopItem("cat",    ItemCategory.PET, "Kitten",   "🐱"),
        ShopItem("dog",    ItemCategory.PET, "Puppy",    "🐶"),
        ShopItem("bunny",  ItemCategory.PET, "Bunny",    "🐰"),
        ShopItem("bird",   ItemCategory.PET, "Bluebird", "🐦", price = 45, rarity = Rarity.RARE),
        ShopItem("dragon", ItemCategory.PET, "Dragon",   "🐲", price = 140, rarity = Rarity.LEGENDARY),
        ShopItem("unicorn", ItemCategory.PET, "Unicorn", "🦄", price = 160, rarity = Rarity.LEGENDARY),
    )

    val tapEffects = listOf(
        ShopItem("sparkle",  ItemCategory.EFFECT, "Sparkle trail", "✨"),
        ShopItem("stars",    ItemCategory.EFFECT, "Star pop",      "⭐"),
        ShopItem("bubbles",  ItemCategory.EFFECT, "Bubbles",       "🫧", price = 30, rarity = Rarity.RARE),
        ShopItem("confetti", ItemCategory.EFFECT, "Confetti burst", "🎉", price = 50, rarity = Rarity.EPIC),
        ShopItem("hearts",   ItemCategory.EFFECT, "Heart burst",   "💖", price = 50, rarity = Rarity.EPIC),
        ShopItem("rainbow",  ItemCategory.EFFECT, "Rainbow ripple", "🌈", price = 90, rarity = Rarity.LEGENDARY),
    )

    val backgrounds = listOf(
        ShopItem("sky",     ItemCategory.BACKGROUND, "Sky",     "☁️"),
        ShopItem("meadow",  ItemCategory.BACKGROUND, "Meadow",  "🌼"),
        ShopItem("sunset",  ItemCategory.BACKGROUND, "Sunset",  "🌇", price = 40, rarity = Rarity.RARE),
        ShopItem("space",   ItemCategory.BACKGROUND, "Space",   "🪐", price = 90, rarity = Rarity.EPIC),
    )

    /** Everything, in one list — what the shop and the creator both filter down from. */
    val all: List<ShopItem> =
        skinTones + hairStyles + hairColors + outfits + headwear + faceAccessories +
            handAccessories + pets + tapEffects + backgrounds

    val shopOnly: List<ShopItem> get() = all.filter { !it.isFree }

    fun item(key: String?): ShopItem? = if (key == null) null else all.firstOrNull { it.key == key }

    fun forCategory(category: ItemCategory): List<ShopItem> = all.filter { it.category == category }

    /** Every free item is owned from the start; nothing else is until it's bought. */
    val freeKeys: Set<String> = all.filter { it.isFree }.map { it.key }.toSet()
}
