package com.catokids.app.data.repository

import com.catokids.app.core.CatoResult
import com.catokids.app.core.catoRunCatching
import com.catokids.app.data.local.AppPreferences
import com.catokids.app.data.local.ShopCatalog
import com.catokids.app.data.model.AvatarConfig
import com.catokids.app.data.model.ShopItem
import com.catokids.app.data.remote.StudentAvatarDto
import com.catokids.app.data.remote.StudentInventoryDto
import com.catokids.app.data.remote.SupabaseProvider
import io.github.jan.supabase.postgrest.from

/**
 * The character creator and the coin shop, offline-first like everything else here:
 * the device copy is what the UI reads, Supabase is a best-effort mirror so a new
 * device (or a teacher peeking at a report) sees the same look.
 */
class AvatarRepository(
    private val prefs: AppPreferences,
    private val auth: AuthRepository,
) {

    private val online: Boolean
        get() = SupabaseProvider.isConfigured && !auth.isDemo.value

    suspend fun snapshot(userId: String): AvatarConfig = prefs.avatarConfig(userId) ?: AvatarConfig()

    /** Pulls the server copy of the look and the owned-items list into the local mirror. */
    suspend fun sync(userId: String) {
        if (!online) return
        runCatching {
            val remoteAvatar = SupabaseProvider.client.from("student_avatar")
                .select { filter { eq("student_id", userId) } }
                .decodeSingleOrNull<StudentAvatarDto>()?.config
            if (remoteAvatar != null) prefs.saveAvatarConfig(userId, remoteAvatar)

            val remoteOwned = SupabaseProvider.client.from("student_inventory")
                .select { filter { eq("student_id", userId) } }
                .decodeList<StudentInventoryDto>().map { it.itemKey }.toSet()
            if (remoteOwned.isNotEmpty()) {
                prefs.saveOwnedItemKeys(userId, prefs.ownedItemKeys(userId) + remoteOwned)
            }
        }
    }

    suspend fun save(userId: String, config: AvatarConfig) {
        prefs.saveAvatarConfig(userId, config)
        if (!online) return
        runCatching {
            SupabaseProvider.client.from("student_avatar")
                .upsert(StudentAvatarDto(studentId = userId, config = config)) { onConflict = "student_id" }
        }
    }

    /** Free catalogue items plus whatever this child has bought. */
    suspend fun ownedKeys(userId: String): Set<String> = ShopCatalog.freeKeys + prefs.ownedItemKeys(userId)

    suspend fun purchase(userId: String, item: ShopItem): CatoResult<Unit> = catoRunCatching {
        if (item.isFree) error("${item.name} doesn't cost anything — just pick it in the creator!")
        val owned = prefs.ownedItemKeys(userId)
        if (item.key in owned) error("You already own ${item.name}.")
        val spent = auth.spendCoins(item.price)
        if (!spent) error("You need ${item.price} coins for ${item.name}. Play more lessons to earn some!")
        prefs.saveOwnedItemKeys(userId, owned + item.key)
        if (online) {
            runCatching {
                SupabaseProvider.client.from("student_inventory")
                    .upsert(StudentInventoryDto(studentId = userId, itemKey = item.key)) {
                        onConflict = "student_id,item_key"
                    }
            }
        }
    }
}
