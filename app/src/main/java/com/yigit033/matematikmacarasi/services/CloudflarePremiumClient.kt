package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Base64
import android.util.Log
import com.yigit033.matematikmacarasi.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * Code entitlements only: Google Play Billing is independent and unchanged.
 *
 * Server-signed RSA license: initial activation once, automatic renewal on day 60,
 * 7 extra offline days. After day 67 do not grant without a valid online renewal.
 * Old signed tokens can renew after the offline window without re-entering a code.
 * Never trust a locally editable "premium=true" flag in the new mode.
 */
internal class CloudflarePremiumClient(private val context: Context) {
    private val prefs = context.getSharedPreferences("cf_premium_license_v1", Context.MODE_PRIVATE)
    private val oldPrefs = context.getSharedPreferences("premium_code_prefs", Context.MODE_PRIVATE)
    private val device = DeviceIdManager.getInstance(context)
    private val logTag = "CloudflarePremiumClient"

    enum class Error { INVALID_CODE, ALREADY_ACTIVE, RATE_LIMITED, NETWORK, UNAVAILABLE }
    sealed class Result {
        object Success : Result()
        data class Failure(val reason: Error) : Result()
    }
    private data class Claims(val recheckAt: Long, val expiresAt: Long)

    fun isPremiumOffline(): Boolean {
        val token = prefs.getString("signed_license", null) ?: return false
        val claims = verify(token) ?: return false
        return now() < claims.expiresAt
    }

    suspend fun activate(code: String): Result = withContext(Dispatchers.IO) {
        if (code.length !in 6..128 || code.trim() != code ||
            !code.matches(Regex("""[\p{L}\p{N}\p{M}\p{P}\p{S} ]+""")))
            return@withContext Result.Failure(Error.INVALID_CODE)
        val old = prefs.getString("signed_license", null)
        if (old != null && verify(old) != null)
            return@withContext Result.Failure(Error.ALREADY_ACTIVE)
        val reply = post("activate", JSONObject()
            .put("code", code).put("deviceId", device.getDeviceId()))
        if (reply.first == 200 && store(reply.second.optString("license"))) {
            // An old copy of the raw legacy code is no longer needed on this device.
            oldPrefs.edit().remove("activated_code").apply()
            return@withContext Result.Success
        }
        return@withContext Result.Failure(mapError(reply.first, reply.second))
    }

    suspend fun checkAndSync(): Boolean = withContext(Dispatchers.IO) {
        val token = prefs.getString("signed_license", null)
        val claims = token?.let { verify(it) }
        if (claims != null) {
            if (now() < claims.recheckAt) return@withContext isPremiumOffline()
            val reply = post("renew", JSONObject()
                .put("license", token).put("deviceId", device.getDeviceId()))
            if (reply.first == 200 && store(reply.second.optString("license")))
                return@withContext true
            // Explicit server denial takes effect immediately. Transient failures
            // preserve the remaining 7-day grace window, never extend its expiry.
            if (reply.first == 403 || reply.first == 401) {
                prefs.edit().remove("signed_license").apply()
                return@withContext false
            }
            return@withContext isPremiumOffline()
        }

        // One-time migration for an already premium user with a code stored by
        // the old app. NO direct Firebase request when the new mode is enabled.
        // This only succeeds after audited legacy records are imported into D1.
        if (oldPrefs.getBoolean("is_code_premium", false)) {
            val oldCode = oldPrefs.getString("activated_code", null)
            if (!oldCode.isNullOrBlank() && activate(oldCode) is Result.Success)
                return@withContext true
        }
        false
    }

    private fun mapError(status: Int, body: JSONObject): Error = when {
        status == 409 -> Error.ALREADY_ACTIVE
        status == 429 -> Error.RATE_LIMITED
        status == 400 -> Error.INVALID_CODE
        status < 0 -> Error.NETWORK
        else -> Error.UNAVAILABLE
    }

    private fun post(route: String, data: JSONObject): Pair<Int, JSONObject> {
        return try {
            val base = BuildConfig.MM_CLOUDFLARE_URL.trimEnd('/')
            if (!base.startsWith("https://") || base.contains("?") || base.contains("#"))
                return -1 to JSONObject()
            val connection = URL("$base/v1/$route").openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.outputStream.use { it.write(data.toString().toByteArray(Charsets.UTF_8)) }
                val status = connection.responseCode
                val input = if (status in 200..299) connection.inputStream else connection.errorStream
                val body = input?.bufferedReader()?.use { it.readText() } ?: "{}"
                status to (runCatching { JSONObject(body) }.getOrDefault(JSONObject()))
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            Log.w(logTag, "Premium service unavailable: ${e.javaClass.simpleName}")
            -1 to JSONObject()
        }
    }

    private fun store(token: String): Boolean {
        val claims = verify(token) ?: return false
        if (claims.expiresAt <= now()) return false
        return prefs.edit().putString("signed_license", token).commit()
    }

    private fun verify(token: String): Claims? {
        return try {
        if (token.length !in 64..4000) return null
        val parts = token.split(".")
        if (parts.size != 3) return null
        val header = JSONObject(String(Base64.decode(parts[0], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), Charsets.UTF_8))
        if (header.optString("alg") != "RS256" || header.optString("kid") != "mm1" ||
            header.optString("typ") != "JWT") return null
        val pubBytes = Base64.decode(BuildConfig.MM_CLOUDFLARE_PUBLIC_KEY_B64, Base64.DEFAULT)
        val key = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(pubBytes))
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(key)
        signature.update((parts[0] + "." + parts[1]).toByteArray(Charsets.UTF_8))
        if (!signature.verify(Base64.decode(parts[2], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)))
            return null
        val obj = JSONObject(String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP), Charsets.UTF_8))
        if (obj.optInt("v") != 1 || obj.optString("aud") != "matematik-macerasi-premium")
            return null
        if (obj.optString("device_tag") != deviceTag(device.getDeviceId())) return null
        val issued = obj.getLong("iat")
        val check = obj.getLong("recheck_at")
        val exp = obj.getLong("exp")
        if (issued > now() + 120 || check <= issued || exp <= check ||
            exp - check != 7L * 86400L || check - issued != 60L * 86400L) return null
        Claims(check, exp)
        } catch (_: Exception) {
            null
        }
    }

    fun resetForTests() {
        prefs.edit().remove("signed_license").apply()
    }

    private fun deviceTag(id: String): String {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(("license-device:v1:" + id).toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it.toInt() and 255) }
    }
    private fun now() = System.currentTimeMillis() / 1000L
}
