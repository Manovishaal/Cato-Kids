package com.catokids.app.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.catokids.app.data.local.ShopCatalog
import com.catokids.app.data.model.AvatarConfig
import com.catokids.app.data.model.ItemCategory
import com.catokids.app.data.model.ShopItem
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private val CREATOR_CATEGORIES = listOf(
    ItemCategory.SKIN, ItemCategory.HAIR_STYLE, ItemCategory.HAIR_COLOR, ItemCategory.OUTFIT,
    ItemCategory.ACCESSORY_HEAD, ItemCategory.ACCESSORY_FACE, ItemCategory.ACCESSORY_HAND,
    ItemCategory.PET, ItemCategory.EFFECT, ItemCategory.BACKGROUND,
)

private fun AvatarConfig.equippedKey(category: ItemCategory): String? = when (category) {
    ItemCategory.SKIN -> skinTone
    ItemCategory.HAIR_STYLE -> hairStyle
    ItemCategory.HAIR_COLOR -> hairColor
    ItemCategory.OUTFIT -> outfit
    ItemCategory.ACCESSORY_HEAD -> accessoryHead
    ItemCategory.ACCESSORY_FACE -> accessoryFace
    ItemCategory.ACCESSORY_HAND -> accessoryHand
    ItemCategory.PET -> pet
    ItemCategory.EFFECT -> tapEffect
    ItemCategory.BACKGROUND -> background
}

/** True for a slot the child can clear back to "nothing" — headwear, face, hand, pet. */
private fun ItemCategory.isOptional() = this in setOf(
    ItemCategory.ACCESSORY_HEAD, ItemCategory.ACCESSORY_FACE, ItemCategory.ACCESSORY_HAND, ItemCategory.PET,
)

@Composable
fun CharacterCreatorScreen(
    state: AvatarUiState,
    onEquip: (ShopItem) -> Unit,
    onUnequip: (ItemCategory) -> Unit,
    onOpenShop: () -> Unit,
    onBack: () -> Unit,
) {
    var category by remember { mutableStateOf(ItemCategory.HAIR_STYLE) }

    CatoBackdrop(top = CatoPalette.CoralSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 50.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "My character", subtitle = "🧑‍🎨 Make it yours", onBack = onBack)
            }
            item {
                Column(Modifier.padding(horizontal = 40.dp).fillMaxWidth()) {
                    CharacterAvatar(config = state.config, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        StatPill("🪙", "${state.profile?.coins ?: 0}", "coins")
                        Spacer(Modifier.width(10.dp))
                        CatoOutlineButton(text = "Shop for more", leading = "🛍️", onClick = onOpenShop)
                    }
                }
            }
            item { Spacer(Modifier.height(18.dp)) }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(CREATOR_CATEGORIES) { c ->
                        val selected = c == category
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) CatoPalette.Coral else CatoPalette.Cloud)
                                .clickable { category = c }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                c.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) androidx.compose.ui.graphics.Color.White else CatoPalette.Ink,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }

            val options = ShopCatalog.forCategory(category)
            val equippedKey = state.config.equippedKey(category)

            if (category.isOptional()) {
                item {
                    Box(Modifier.padding(horizontal = 20.dp)) {
                        ItemTile(
                            item = null,
                            selected = equippedKey == null,
                            owned = true,
                            onClick = { onUnequip(category) },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            items(options.chunked(2), key = { it.first().key }) { row ->
                Row(
                    Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    row.forEach { item ->
                        Box(Modifier.weight(1f)) {
                            ItemTile(
                                item = item,
                                selected = equippedKey == item.key,
                                owned = state.owns(item),
                                onClick = { if (state.owns(item)) onEquip(item) else onOpenShop() },
                            )
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ItemTile(item: ShopItem?, selected: Boolean, owned: Boolean, onClick: () -> Unit) {
    CatoCard(
        Modifier.fillMaxWidth(),
        color = if (selected) CatoPalette.CoralSoft else androidx.compose.ui.graphics.Color.White,
        onClick = onClick,
    ) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            EmojiArt(item?.emoji ?: "🚫", size = 32.dp)
            Spacer(Modifier.height(6.dp))
            Text(
                item?.name ?: "None",
                style = MaterialTheme.typography.labelMedium,
                color = CatoPalette.Ink,
                maxLines = 1,
            )
            if (item != null && !owned) {
                Spacer(Modifier.height(4.dp))
                Text("🔒 ${item.price}", style = MaterialTheme.typography.labelSmall, color = CatoPalette.InkSoft)
            } else if (selected) {
                Spacer(Modifier.height(4.dp))
                Text("Equipped", style = MaterialTheme.typography.labelSmall, color = CatoPalette.CoralDeep)
            }
        }
    }
}
