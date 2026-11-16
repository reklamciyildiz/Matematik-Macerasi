package com.yigit033.matematikmacarasi.learning

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import java.security.MessageDigest

/**
 * Plays licensed, human-quality studio/HD voiceovers bundled with the APK.
 * Source text is hashed, so a changed explanation CANNOT accidentally play
 * the old recording. There is no network call or child-generated text upload.
 *
 * Missing assets are normal during development: LessonNarrator will fall back
 * to the on-device Turkish voice. No placeholder "premium" audio is shipped.
 */
internal class PremiumLessonAudio(context: Context) {
    private val assets = context.applicationContext.assets
    private var player: MediaPlayer? = null
    private var onFinished: (() -> Unit)? = null
    private val clipAvailability = HashMap<String, Boolean>()

    companion object {
        const val ASSET_FOLDER = "lesson_narration/tr-TR"

        fun hash(text: String): String = MessageDigest.getInstance("SHA-256")
            .digest(text.trim().toByteArray(Charsets.UTF_8))
            .joinToString("") { b -> "%02x".format(b.toInt() and 0xff) }

        fun assetPath(text: String): String =
            "$ASSET_FOLDER/" + hash(text) + ".mp3"
    }

    fun hasClip(text: String): Boolean {
        if (text.isBlank()) return false
        val path = assetPath(text)
        return clipAvailability.getOrPut(path) {
            runCatching { assets.openFd(path).use { it.length > 0L } }
                .getOrDefault(false)
        }
    }

    fun hasAnyClips(): Boolean = runCatching {
        assets.list(ASSET_FOLDER)?.any { it.endsWith(".mp3") } == true
    }.getOrDefault(false)

    /**
     * Calls the callback after completion or playback error; only one clip may
     * be active. A missing (or compressed) asset returns false immediately.
     */
    fun play(text: String, callback: () -> Unit): Boolean {
        stop()
        val afd = runCatching { assets.openFd(assetPath(text)) }.getOrNull()
            ?: return false
        val current = MediaPlayer()
        return try {
            afd.use { descriptor ->
                current.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                current.setDataSource(
                    descriptor.fileDescriptor, descriptor.startOffset, descriptor.length
                )
            }
            player = current
            onFinished = callback
            current.setOnPreparedListener {
                if (player === current) {
                    runCatching { current.start() }
                        .onFailure { finishCurrent(current) }
                }
            }
            current.setOnCompletionListener { finishCurrent(current) }
            current.setOnErrorListener { _, _, _ ->
                finishCurrent(current)
                true
            }
            current.prepareAsync()
            true
        } catch (_: Exception) {
            if (player === current) {
                player = null
                onFinished = null
            }
            current.release()
            false
        }
    }

    private fun finishCurrent(current: MediaPlayer) {
        if (player !== current) return
        val callback = onFinished
        player = null
        onFinished = null
        runCatching { current.release() }
        callback?.invoke()
    }

    fun stop() {
        val current = player
        player = null
        onFinished = null // interruption must not invoke a stale completion
        current?.let { runCatching { it.release() } }
    }
}
