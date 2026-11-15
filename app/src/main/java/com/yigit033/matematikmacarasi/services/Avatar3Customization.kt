package com.yigit033.matematikmacarasi.services

import android.content.Context
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager

/**
 * Avatar 3.0 appearance inventory. This is additive to legacy AvatarService:
 * never renumber or alter existing avatar IDs, purchases or selected avatars.
 *
 * Looks are real in-app collectible themes applied over the existing Lottie
 * character, not a claim that a separate animated character body exists.
 */
data class Avatar3Look(
    val id: Int,
    val title: String,
    val subtitle: String,
    val starCost: Int,
    val premiumRequired: Boolean
)

object Avatar3LookCatalog {
    // Names identify the actual on-character vector palette, not fictional outfits.
    // IDs remain stable for the original saved look selections.
    private val titles = listOf(
        listOf("Klasik Sıfır", "Turkuaz Sıfır", "Galaksi Sıfır"),
        listOf("Klasik Robot", "Turkuaz Robot", "Galaksi Robotu"),
        listOf("Klasik Canavar", "Turkuaz Canavar", "Galaksi Canavarı"),
        listOf("Klasik Matematikçi", "Turkuaz Matematikçi", "Galaksi Matematikçisi"),
        listOf("Klasik Dahi Kız", "Turkuaz Dahi", "Galaksi Dahisi"),
        listOf("Klasik Kaşif", "Orman Kaşifi", "Uzay Kaşifi"),
        listOf("Klasik Büyücü", "Turkuaz Büyücü", "Galaksi Büyücüsü"),
        listOf("Klasik Bilge", "Turkuaz Bilge", "Galaksi Bilgesi"),
        listOf("Klasik Şekiller", "Turkuaz Şekiller", "Galaksi Şekilleri"),
        listOf("Klasik Abaküs", "Turkuaz Abaküs", "Galaksi Abaküsü"),
        listOf("Klasik Heyecan", "Turkuaz Heyecan", "Galaksi Heyecanı"),
        listOf("Klasik Bilge Çocuk", "Turkuaz Bilge", "Galaksi Bilgesi"),
        listOf("Klasik Einstein", "Turkuaz Einstein", "Galaksi Einstein"),
        listOf("Klasik Şekil Uzmanı", "Turkuaz Uzman", "Galaksi Uzmanı"),
        listOf("Klasik Zeka Küpü", "Turkuaz Zeka", "Galaksi Zekası")
    )

    fun looks(avatarId: Int): List<Avatar3Look> {
        val names = titles.getOrNull(avatarId - 1) ?: titles.first()
        return names.mapIndexed { index, name ->
            Avatar3Look(
                id = index,
                title = name,
                subtitle = when {
                    avatarId == 6 && index == 0 -> "Orijinal Kaşif Öğrenci Lottie animasyonu"
                    avatarId == 6 && index == 1 -> "Yeşil kaşif kıyafeti, sırt çantası ve orman şapkası"
                    avatarId == 6 && index == 2 -> "Kask, uzay tulumu ve sırt roketi"
                    index == 0 -> "Orijinal animasyon"
                    index == 1 -> "Turkuaz tonlarda Lottie renkleri"
                    else -> "Mor ve altın galaksi paleti"
                },
                // These are color treatments on the original Lottie,
                // not separately illustrated costumes. Never charge stars
                // for palette-only changes.
                starCost = 0,
                premiumRequired = index == 2
            )
        }
    }
}

/** Preferences deliberately separate from the legacy avatar_prefs inventory. */
class Avatar3Customization(context: Context) {
    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("avatar3_customization_v1", Context.MODE_PRIVATE)
    private val avatarPrefs = app.getSharedPreferences("avatar_prefs", Context.MODE_PRIVATE)

    fun avatarOwned(avatarId: Int): Boolean {
        if (avatarId !in 1..15) return false
        if (avatarId == 1) return true
        return avatarPrefs.getStringSet("unlocked_avatar_ids", setOf("1"))
            ?.contains(avatarId.toString()) == true
    }

    fun premiumActive(): Boolean = try {
        PurchaseManager.getInstance(app).isPremiumSync()
    } catch (_: Exception) {
        false
    }

    private fun ownedKey(avatarId: Int) = "looks_owned_$avatarId"
    private fun selectedKey(avatarId: Int) = "look_selected_$avatarId"

    fun lookOwned(avatarId: Int, lookId: Int): Boolean {
        if (avatarId !in 1..15 || lookId !in 0..2) return false
        if (lookId == 0) return true
        // Color treatments are not paid skins. A basic palette comes free
        // with an owned avatar; the special palette requires Premium access.
        if (avatarOwned(avatarId) && lookId == 1) return true
        if (avatarOwned(avatarId) && lookId == 2 && premiumActive()) return true
        // Preserve any appearance previously unlocked in older builds.
        return prefs.getStringSet(ownedKey(avatarId), emptySet())
            ?.contains(lookId.toString()) == true
    }

    fun selectedLook(avatarId: Int): Int {
        if (!avatarOwned(avatarId)) return 0
        val chosen = prefs.getInt(selectedKey(avatarId), 0)
        if (!lookOwned(avatarId, chosen)) return 0
        if (Avatar3LookCatalog.looks(avatarId)[chosen].premiumRequired && !premiumActive()) {
            // Retain the chosen look in prefs; only temporarily fall back.
            return 0
        }
        return chosen
    }

    fun selectLook(avatarId: Int, lookId: Int): Boolean {
        if (!avatarOwned(avatarId) || !lookOwned(avatarId, lookId)) return false
        if (Avatar3LookCatalog.looks(avatarId)[lookId].premiumRequired && !premiumActive()) {
            return false
        }
        return prefs.edit().putInt(selectedKey(avatarId), lookId).commit()
    }

    // Color palettes and the two Kaşif pilot costumes have no star price.
    // The obsolete buyLook() wallet transaction was intentionally removed:
    // ownership/equipment is gated by avatar purchase + Premium entitlement.

    fun selectedAccessory(avatarId: Int): Int =
        if (avatarOwned(avatarId)) prefs.getInt("accessory_$avatarId", 0).coerceIn(0, 2) else 0

    fun selectedEffect(avatarId: Int): Int =
        if (avatarOwned(avatarId)) prefs.getInt("effect_$avatarId", 0).coerceIn(0, 2) else 0

    fun setAccessory(avatarId: Int, value: Int): Boolean =
        avatarOwned(avatarId) && value in 0..2 &&
            prefs.edit().putInt("accessory_$avatarId", value).commit()

    fun setEffect(avatarId: Int, value: Int): Boolean =
        avatarOwned(avatarId) && value in 0..2 &&
            prefs.edit().putInt("effect_$avatarId", value).commit()

    /** Previews are free for everyone, including locked avatars. */
    fun recordPreview(avatarId: Int, lookId: Int) {
        if (avatarId !in 1..15 || lookId !in 0..2) return
        val key = "look_previewed_$avatarId"
        val visited = (prefs.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
        if (visited.add(lookId.toString())) {
            prefs.edit().putStringSet(key, visited).apply()
        }
    }

    fun previewCount(avatarId: Int): Int =
        if (avatarId in 1..15) {
            (prefs.getStringSet("look_previewed_$avatarId", emptySet()) ?: emptySet()).size
        } else 0

    fun unlockedLooks(avatarId: Int): Int = (0..2).count { lookOwned(avatarId, it) }
}