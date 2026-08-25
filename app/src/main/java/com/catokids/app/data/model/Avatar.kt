package com.catokids.app.data.model

import kotlinx.serialization.Serializable

/** Which slot on the character an item fills. */
enum class ItemCategory(val label: String) {
    SKIN("Skin tone"),
    HAIR_STYLE("Hair style"),
    HAIR_COLOR("Hair colour"),
    OUTFIT("Outfit"),
    ACCESSORY_HEAD("Headwear"),
    ACCESSORY_FACE("Face"),
    ACCESSORY_HAND("Holding"),
    PET("Pet"),
    EFFECT("Tap effect"),
    BACKGROUND("Backdrop"),
}

enum class Rarity(val label: String) {
    COMMON("Common"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary"),
}

/** One purchasable (or free) piece of the character creator / shop catalogue. */
data class ShopItem(
    val key: String,
    val category: ItemCategory,
    val name: String,
    val emoji: String,
    val price: Int = 0,
    val rarity: Rarity = Rarity.COMMON,
) {
    val isFree: Boolean get() = price <= 0
}

/**
 * A student's whole look, saved as one small JSON blob. Every field is an item
 * `key` from [com.catokids.app.data.local.ShopCatalog] — free slots always hold
 * a valid key, optional slots may be null ("nothing equipped here").
 */
@Serializable
data class AvatarConfig(
    val skinTone: String = "peach",
    val hairStyle: String = "short",
    val hairColor: String = "brown",
    val outfit: String = "coral",
    val accessoryHead: String? = null,
    val accessoryFace: String? = null,
    val accessoryHand: String? = null,
    val pet: String? = null,
    val tapEffect: String = "none",
    val background: String = "sky",
)

/** What plays when a student with an equipped effect taps the screen. */
enum class TapEffectType {
    NONE, SPARKLE, STARS, CONFETTI, HEARTS, RAINBOW, BUBBLES;

    companion object {
        fun fromKey(key: String?): TapEffectType = when (key) {
            "sparkle"  -> SPARKLE
            "stars"    -> STARS
            "confetti" -> CONFETTI
            "hearts"   -> HEARTS
            "rainbow"  -> RAINBOW
            "bubbles"  -> BUBBLES
            else       -> NONE
        }
    }
}
