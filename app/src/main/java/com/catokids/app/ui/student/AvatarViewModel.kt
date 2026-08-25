package com.catokids.app.ui.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.catokids.app.core.AppContainer
import com.catokids.app.core.CatoResult
import com.catokids.app.data.model.AvatarConfig
import com.catokids.app.data.model.ItemCategory
import com.catokids.app.data.model.Profile
import com.catokids.app.data.model.ShopItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AvatarUiState(
    val profile: Profile? = null,
    val config: AvatarConfig = AvatarConfig(),
    val owned: Set<String> = emptySet(),
    val loading: Boolean = true,
    val message: String? = null,
) {
    fun owns(item: ShopItem) = item.isFree || item.key in owned
}

/** Backs both the character creator and the coin shop — they share one look and one wallet. */
class AvatarViewModel(private val container: AppContainer) : ViewModel() {

    private val _state = MutableStateFlow(AvatarUiState())
    val state: StateFlow<AvatarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            container.auth.profile.collect { profile ->
                if (profile == null) {
                    _state.value = AvatarUiState(loading = false)
                } else {
                    _state.value = _state.value.copy(profile = profile, loading = true)
                    container.avatar.sync(profile.id)
                    _state.value = _state.value.copy(
                        config = container.avatar.snapshot(profile.id),
                        owned = container.avatar.ownedKeys(profile.id),
                        loading = false,
                    )
                }
            }
        }
    }

    /** Equips an item — free ones equip instantly, shop items must already be owned. */
    fun equip(item: ShopItem) {
        val profile = _state.value.profile ?: return
        if (!_state.value.owns(item)) return
        val current = _state.value.config
        val updated = when (item.category) {
            ItemCategory.SKIN -> current.copy(skinTone = item.key)
            ItemCategory.HAIR_STYLE -> current.copy(hairStyle = item.key)
            ItemCategory.HAIR_COLOR -> current.copy(hairColor = item.key)
            ItemCategory.OUTFIT -> current.copy(outfit = item.key)
            ItemCategory.ACCESSORY_HEAD -> current.copy(accessoryHead = if (current.accessoryHead == item.key) null else item.key)
            ItemCategory.ACCESSORY_FACE -> current.copy(accessoryFace = if (current.accessoryFace == item.key) null else item.key)
            ItemCategory.ACCESSORY_HAND -> current.copy(accessoryHand = if (current.accessoryHand == item.key) null else item.key)
            ItemCategory.PET -> current.copy(pet = if (current.pet == item.key) null else item.key)
            ItemCategory.EFFECT -> current.copy(tapEffect = item.key)
            ItemCategory.BACKGROUND -> current.copy(background = item.key)
        }
        _state.value = _state.value.copy(config = updated)
        viewModelScope.launch { container.avatar.save(profile.id, updated) }
    }

    /** Clears an optional slot (headwear, face, hand or pet) back to "nothing". */
    fun unequip(category: ItemCategory) {
        val profile = _state.value.profile ?: return
        val current = _state.value.config
        val updated = when (category) {
            ItemCategory.ACCESSORY_HEAD -> current.copy(accessoryHead = null)
            ItemCategory.ACCESSORY_FACE -> current.copy(accessoryFace = null)
            ItemCategory.ACCESSORY_HAND -> current.copy(accessoryHand = null)
            ItemCategory.PET -> current.copy(pet = null)
            else -> current
        }
        _state.value = _state.value.copy(config = updated)
        viewModelScope.launch { container.avatar.save(profile.id, updated) }
    }

    fun purchase(item: ShopItem) {
        val profile = _state.value.profile ?: return
        viewModelScope.launch {
            when (val result = container.avatar.purchase(profile.id, item)) {
                is CatoResult.Ok -> {
                    _state.value = _state.value.copy(
                        owned = container.avatar.ownedKeys(profile.id),
                        message = "You got ${item.name}! 🎉",
                    )
                    equip(item)
                }
                is CatoResult.Err -> _state.value = _state.value.copy(message = result.message)
            }
        }
    }

    fun clearMessage() { _state.value = _state.value.copy(message = null) }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AvatarViewModel(container) as T
        }
    }
}
