package com.yigit033.matematikmacarasi.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns the actual vector colors inside the original Lottie composition into
 * alternate palettes. Animation, paths, timings and user identities are intact.
 * Skin-like, near-gray and very dark colors stay untouched.
 *
 * These are palette variants, NOT newly illustrated costumes.
 */
internal object Avatar3LottiePalette {
    private const val CACHE_LIMIT = 8
    private val cache = object : LinkedHashMap<String, String>(CACHE_LIMIT, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean =
            size > CACHE_LIMIT
    }

    fun load(context: Context, resId: Int, lookId: Int): String? {
        if (resId == 0 || lookId !in 1..2) return null
        val key = "$resId:$lookId"
        synchronized(cache) { cache[key]?.let { return it } }
        return try {
            val json = context.resources.openRawResource(resId)
                .bufferedReader(Charsets.UTF_8).use { it.readText() }
            val root = JSONObject(json)
            val changed = recolor(root, lookId)
            if (changed == 0) {
                Log.w("Avatar3LottiePalette", "No safe recolorable vectors in resource $resId")
                return null
            }
            val rendered = root.toString()
            synchronized(cache) { cache[key] = rendered }
            rendered
        } catch (e: Exception) {
            Log.w("Avatar3LottiePalette", "Could not style animation $resId", e)
            null
        }
    }

    private fun recolor(node: Any?, lookId: Int): Int {
        var count = 0
        when (node) {
            is JSONObject -> {
                if (node.optString("ty") == "fl" || node.optString("ty") == "st") {
                    val c = node.optJSONObject("c")
                    val k = c?.optJSONArray("k")
                    if (k != null) {
                        if (k.length() >= 3 && k.opt(0) is Number) {
                            if (rewrite(k, lookId)) count++
                        } else {
                            for (i in 0 until k.length()) {
                                val frame = k.optJSONObject(i) ?: continue
                                for (field in arrayOf("s", "e")) {
                                    val values = frame.optJSONArray(field)
                                    if (values != null && rewrite(values, lookId)) count++
                                }
                            }
                        }
                    }
                }
                // Some of the existing avatar Lotties are gradient-only
                // (e.g. Ada Lovelace). Recolor the actual gradient stops,
                // otherwise their visual palette never changes.
                if (node.optString("ty") == "gf" || node.optString("ty") == "gs") {
                    val gradient = node.optJSONObject("g")
                    val stops = gradient?.optInt("p", 0) ?: 0
                    val k = gradient?.optJSONObject("k")?.optJSONArray("k")
                    if (k != null && stops > 0) {
                        if (k.opt(0) is Number) {
                            for (i in 0 until stops) {
                                if (rewrite(k, lookId, 1 + i * 4)) count++
                            }
                        } else {
                            for (i in 0 until k.length()) {
                                val frame = k.optJSONObject(i) ?: continue
                                for (field in arrayOf("s", "e")) {
                                    val values = frame.optJSONArray(field) ?: continue
                                    for (j in 0 until stops) {
                                        if (rewrite(values, lookId, 1 + j * 4)) count++
                                    }
                                }
                            }
                        }
                    }
                }
                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    count += recolor(node.opt(key), lookId)
                }
            }
            is JSONArray -> {
                for (i in 0 until node.length()) {
                    count += recolor(node.opt(i), lookId)
                }
            }
        }
        return count
    }

    private fun rewrite(values: JSONArray, lookId: Int, offset: Int = 0): Boolean {
        if (values.length() < offset + 3 ||
            values.opt(offset) !is Number ||
            values.opt(offset + 1) !is Number ||
            values.opt(offset + 2) !is Number) return false
        val red = values.optDouble(offset).coerceIn(0.0, 1.0)
        val green = values.optDouble(offset + 1).coerceIn(0.0, 1.0)
        val blue = values.optDouble(offset + 2).coerceIn(0.0, 1.0)
        val c = AndroidColor.rgb((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(c, hsv)
        // Keep dark outlines and muted highlights. Protect plausible skin/hair
        // tones to avoid changing the character's face or complexion.
        if (hsv[1] < 0.19f || hsv[2] < 0.20f) return false
        val skinLike = red >= green && green >= blue &&
            hsv[0] in 5f..58f && hsv[1] <= 0.76f
        if (skinLike) return false
        val hue = if (lookId == 1) 171f else 278f
        // Slight variation keeps the character's existing material contrast.
        hsv[0] = (hue + (hsv[0] % 37f) - 18f + 360f) % 360f
        hsv[1] = (hsv[1] * 1.13f).coerceIn(0.44f, 0.98f)
        hsv[2] = (hsv[2] * 1.09f).coerceIn(0f, 1f)
        val newColor = AndroidColor.HSVToColor(hsv)
        values.put(offset, AndroidColor.red(newColor) / 255.0)
        values.put(offset + 1, AndroidColor.green(newColor) / 255.0)
        values.put(offset + 2, AndroidColor.blue(newColor) / 255.0)
        return true
    }
}
