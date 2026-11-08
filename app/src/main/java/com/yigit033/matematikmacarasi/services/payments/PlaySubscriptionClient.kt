package com.yigit033.matematikmacarasi.services.payments

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.yigit033.matematikmacarasi.BuildConfig
import com.yigit033.matematikmacarasi.services.DeviceIdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Verified Play subscriptions only. Does not affect lifetime/code Premium. */
internal class PlaySubscriptionClient(private val context: Context) {
    enum class Verdict { VERIFIED, DENIED, UNAVAILABLE }
    private val prefs=context.getSharedPreferences("mm_play_sub_lease_v1",Context.MODE_PRIVATE)
    private val device=DeviceIdManager.getInstance(context)
    private val urlBase=BuildConfig.MM_PLAY_SUBS_URL.trimEnd('/')

    fun isPremiumOffline():Boolean {
        if(!BuildConfig.MM_SUBSCRIPTIONS_ENABLED)return false
        return verifyLease(prefs.getString("signed_lease",null) ?: return false,null)
    }
    fun clear() { prefs.edit().remove("signed_lease").apply() }

    suspend fun verifyAndStore(purchaseToken:String):Verdict = withContext(Dispatchers.IO) {
        if(!BuildConfig.MM_SUBSCRIPTIONS_ENABLED || purchaseToken.length !in 16..4096)
            return@withContext Verdict.DENIED
        try {
            val deviceId=device.getDeviceId()
            val integrityToken=integrityToken(purchaseToken,deviceId)
            if(!urlBase.startsWith("https://") || urlBase.contains("?") || urlBase.contains("#"))
                return@withContext Verdict.UNAVAILABLE
            val conn=URL("$urlBase/v1/verify").openConnection() as HttpURLConnection
            try {
                conn.requestMethod="POST"
                conn.connectTimeout=10000
                conn.readTimeout=10000
                conn.instanceFollowRedirects=false
                conn.doOutput=true
                conn.setRequestProperty("Content-Type","application/json")
                val request=JSONObject().put("purchaseToken",purchaseToken)
                    .put("deviceId",deviceId).put("integrityToken",integrityToken)
                conn.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
                if(conn.responseCode==403) {
                    val denied=conn.errorStream?.bufferedReader()?.use {
                        it.readText().take(300)
                    } ?: ""
                    if(denied.contains("NOT_ENTITLED")) {
                        clear()
                        return@withContext Verdict.DENIED
                    }
                    return@withContext Verdict.UNAVAILABLE
                }
                if(conn.responseCode!=200)return@withContext Verdict.UNAVAILABLE
                val reply=conn.inputStream.bufferedReader().use {
                    JSONObject(it.readText().take(8000))
                }
                if(reply.optString("status")!="ACTIVE")return@withContext Verdict.UNAVAILABLE
                val signed=reply.optString("lease")
                if(!verifyLease(signed,purchaseToken))return@withContext Verdict.UNAVAILABLE
                if(!prefs.edit().putString("signed_lease",signed).commit())
                    return@withContext Verdict.UNAVAILABLE
                Verdict.VERIFIED
            } finally { conn.disconnect() }
        } catch(e:Exception) {
            // NEVER log tokens, entitlement JWT or device IDs.
            Log.w("PlaySubscriptionClient","Play subscription unavailable: "+e.javaClass.simpleName)
            Verdict.UNAVAILABLE
        }
    }

    private suspend fun integrityToken(purchase:String,deviceId:String):String {
        val number=BuildConfig.MM_PLAY_INTEGRITY_PROJECT_NUMBER.toLong()
        val manager=IntegrityManagerFactory.createStandard(context)
        val prepared=suspendCancellableCoroutine<StandardIntegrityManager.StandardIntegrityTokenProvider> { cont ->
            manager.prepareIntegrityToken(
                StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(number).build()
            ).addOnSuccessListener { if(cont.isActive)cont.resume(it) }
                .addOnFailureListener { if(cont.isActive)cont.resumeWithException(it) }
        }
        val hash=digest("mm-play-subscription:v1:$deviceId:$purchase")
        val response=suspendCancellableCoroutine<StandardIntegrityManager.StandardIntegrityToken> { cont ->
            prepared.request(StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                .setRequestHash(hash).build()
            ).addOnSuccessListener { if(cont.isActive)cont.resume(it) }
                .addOnFailureListener { if(cont.isActive)cont.resumeWithException(it) }
        }
        return response.token()
    }

    private fun verifyLease(token:String,expectedPurchase:String?):Boolean = try {
        if(token.length !in 100..4000)return false
        val parts=token.split(".")
        if(parts.size!=3)return false
        val h=JSONObject(String(Base64.decode(parts[0],B64),Charsets.UTF_8))
        if(h.optString("alg")!="RS256" || h.optString("typ")!="JWT" ||
            h.optString("kid")!="mmplay1")return false
        val bytes=Base64.decode(BuildConfig.MM_PLAY_SUBS_PUBLIC_KEY_B64,Base64.DEFAULT)
        val key=KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(bytes))
        val sig=Signature.getInstance("SHA256withRSA")
        sig.initVerify(key)
        sig.update((parts[0]+"."+parts[1]).toByteArray(Charsets.UTF_8))
        if(!sig.verify(Base64.decode(parts[2],B64)))return false
        val body=JSONObject(String(Base64.decode(parts[1],B64),Charsets.UTF_8))
        if(body.optInt("v")!=1 ||
            body.optString("aud")!="matematik-macerasi-play-subscription")return false
        if(body.optString("device_tag")!=digest("license-device:v1:"+device.getDeviceId()))
            return false
        val tokenHash=body.optString("token_hash")
        if(!tokenHash.matches(Regex("[a-f0-9]{64}")))return false
        if(expectedPurchase!=null && tokenHash!=digest("subscription-token:v1:"+expectedPurchase))
            return false
        val iat=body.getLong("iat")
        val exp=body.getLong("exp")
        val now=System.currentTimeMillis()/1000L
        iat>0 && iat<=now+120L && exp>now && exp>iat && exp-iat<=86400L
    } catch(_:Exception) { false }

    private fun digest(value:String):String=MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it.toInt() and 255) }

    private companion object { const val B64=Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING }
}
