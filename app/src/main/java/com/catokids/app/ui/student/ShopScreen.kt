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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.catokids.app.data.local.ShopCatalog
import com.catokids.app.data.model.ItemCategory
import com.catokids.app.data.model.Rarity
import com.catokids.app.data.model.ShopItem
import com.catokids.app.ui.components.*
import com.catokids.app.ui.theme.CatoPalette

private val SHOP_CATEGORIES = listOf(
    ItemCategory.OUTFIT, ItemCategory.ACCESSORY_HEAD, ItemCategory.ACCESSORY_FACE,
    ItemCategory.ACCESSORY_HAND, ItemCategory.PET, ItemCategory.EFFECT, ItemCategory.BACKGROUND,
    ItemCategory.HAIR_COLOR,
)

private fun Rarity.color(): Color = when (this) {
    Rarity.COMMON -> CatoPalette.InkSoft
    Rarity.RARE -> CatoPalette.PeriwinkleDeep
    Rarity.EPIC -> CatoPalette.VioletDeep
    Rarity.LEGENDARY -> CatoPalette.CoralDeep
}

@Composable
fun ShopScreen(
    state: AvatarUiState,
    onBuy: (ShopItem) -> Unit,
    onMessageShown: () -> Unit,
    onBack: () -> Unit,
) {
    var category by remember { mutableStateOf(ItemCategory.EFFECT) }

    CatoBackdrop(top = CatoPalette.AmberSoft) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 50.dp)) {
            item {
                Spacer(Modifier.height(40.dp))
                CatoTopBar(title = "Coin shop", subtitle = "🪙 ${state.profile?.coins ?: 0} coins to spend", onBack = onBack)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(SHOP_CATEGORIES) { c ->
                        val selected = c == category
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) CatoPalette.Amber else CatoPalette.Cloud)
                                .clickable { category = c }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                c.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) Color.White else CatoPalette.Ink,
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }

            val shopItems = ShopCatalog.forCategory(category).filter { !it.isFree }
            if (shopItems.isEmpty()) {
                item { InfoBanner("✨", "Everything here is free — pick it in the character creator!") }
            }
            items(shopItems, key = { it.key }) { item ->
                ShopRow(item = item, owned = state.owns(item), affordable = (state.profile?.coins ?: 0) >= item.price, onBuy = { onBuy(item) })
            }
        }

        state.message?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(2200)
                onMessageShown()
            }
            Box(Modifier.fillMaxSize().padding(bottom = 40.dp), contentAlignment = Alignment.BottomCenter) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CatoPalette.Ink)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Text(msg, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ShopRow(item: ShopItem, owned: Boolean, affordable: Boolean, onBuy: () -> Unit) {
    CatoCard(Modifier.padding(horizontal = 20.dp, vertical = 5.dp).fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            EmojiArt(item.emoji, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleSmall, color = CatoPalette.Ink)
                Text(item.rarity.label, style = MaterialTheme.typography.labelSmall, color = item.rarity.color())
            }
            if (owned) {
                Box(Modifier.clip(RoundedCornerShape(12.dp)).background(CatoPalette.SuccessSoft).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Owned", style = MaterialTheme.typography.labelSmall, color = CatoPalette.SuccessDeep)
                }
            } else {
                CatoButton(
                    text = "${item.price} 🪙",
                    onClick = onBuy,
                    enabled = affordable,
                    color = CatoPalette.Amber,
                )
            }
        }
    }
}
