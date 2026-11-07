package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.yigit033.matematikmacarasi.R
import java.util.concurrent.ConcurrentHashMap

/**
 * S4.4: shared, low-latency audio direction for all Maths Adventure games.
 *
 * One SoundPool holds short, original PCM effects. One ExoPlayer loops the
 * musical theme and manages audio focus. Legacy public APIs are preserved;
 * old activity cleanup calls no longer destroy sound resources mid-session.
 */
class AudioService private constructor(private val context: Context) {
    companion object {
        private const val TAG = "AudioService"
        private const val PREFS_NAME = "audio_settings"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_VOLUME = "music_volume_v2"
        private const val KEY_EFFECTS_VOLUME = "effects_volume_v2"

        @Volatile private var instance: AudioService? = null
        fun getInstance(context: Context): AudioService =
            instance ?: synchronized(this) {
                instance ?: AudioService(context.applicationContext).also { instance = it }
            }
    }

    enum class Cue {
        CORRECT, WRONG, SUCCESS, FAILURE, LEVEL, QUEST, COIN,
        COMBO, UI, POP, WHOOSH, LASER, IMPACT
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val attributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_GAME)
        .build()
    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(attributes)
        .build()
    private val loaded = ConcurrentHashMap.newKeySet<Int>()
    private val samples = mutableMapOf<Cue, Int>()
    private val lastPlayed = mutableMapOf<Cue, Long>()
    private val duckRestore = Runnable { musicPlayer?.volume = currentMusicVolume() }

    private var musicPlayer: ExoPlayer? = null
    private var musicPrepared = false
    private var released = false
    @Volatile private var appForeground = false
    @Volatile private var gameplayActive = false
    @Volatile private var lessonNarrationActive = false
    @Volatile private var musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
    @Volatile private var effectsEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    @Volatile private var musicVolume =
        prefs.getFloat(KEY_MUSIC_VOLUME, .28f).coerceIn(0f, 1f)
    @Volatile private var effectsVolume =
        prefs.getFloat(KEY_EFFECTS_VOLUME, .76f).coerceIn(0f, 1f)

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded.add(sampleId)
            else Log.w(TAG, "Sound sample $sampleId load failed ($status)")
        }
        val assets = mapOf(
            Cue.CORRECT to R.raw.mm2_correct,
            Cue.WRONG to R.raw.mm2_wrong,
            Cue.SUCCESS to R.raw.mm2_success,
            Cue.FAILURE to R.raw.mm2_failure,
            Cue.LEVEL to R.raw.mm2_level,
            Cue.QUEST to R.raw.mm2_quest,
            Cue.COIN to R.raw.mm2_coin,
            Cue.COMBO to R.raw.mm2_combo,
            Cue.UI to R.raw.mm2_ui,
            Cue.POP to R.raw.mm2_pop,
            Cue.WHOOSH to R.raw.mm2_whoosh,
            Cue.LASER to R.raw.mm2_laser,
            Cue.IMPACT to R.raw.mm2_impact
        )
        assets.forEach { (cue, res) ->
            runCatching { samples[cue] = pool.load(context, res, 1) }
                .onFailure { Log.w(TAG, "Cannot preload $cue", it) }
        }
    }

    private fun onMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action()
        else mainHandler.post { if (!released) action() }
    }

    private fun currentMusicVolume(): Float =
        if (musicEnabled) musicVolume *
            (if (gameplayActive) .53f else 1f) *
            (if (lessonNarrationActive) .24f else 1f)
        else 0f

    /**
     * Momentarily reduce (not stop) the continuing adventure theme while the
     * accessibility/TTS lesson speaker reads a teaching step.
     */
    fun onLessonNarrationChanged(active: Boolean) {
        onMain {
            lessonNarrationActive = active
            if (!released) musicPlayer?.volume = currentMusicVolume()
        }
    }

    private fun prepareMusic() {
        if (released || musicPrepared) return
        try {
            val player = musicPlayer ?: ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                setAudioAttributes(
                    androidx.media3.common.AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build(),
                    true
                )
                volume = currentMusicVolume()
            }.also { musicPlayer = it }

            val uri = "android.resource://" + context.packageName + "/" +
                R.raw.mm2_adventure_theme
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            musicPrepared = true
        } catch (e: Exception) {
            Log.e(TAG, "Music initialization failed", e)
            runCatching { musicPlayer?.release() }
            musicPlayer = null
            musicPrepared = false
        }
    }

    /** Application-level lifecycle owns music; navigation must not stop the loop. */
    fun onAppForeground(gameplay: Boolean) {
        onMain {
            appForeground = true
            gameplayActive = gameplay
            resumeBackgroundMusic()
        }
    }

    fun onGameplayChanged(gameplay: Boolean) {
        onMain {
            gameplayActive = gameplay
            if (!released) musicPlayer?.volume = currentMusicVolume()
        }
    }

    fun onAppBackground() {
        onMain {
            appForeground = false
            pauseBackgroundMusic()
        }
    }

    fun playBackgroundMusic() = resumeBackgroundMusic()

    fun resumeBackgroundMusic() {
        onMain {
            if (released || !musicEnabled || !appForeground) return@onMain
            if (!musicPrepared) prepareMusic()
            musicPlayer?.apply {
                volume = currentMusicVolume()
                playWhenReady = true
            }
        }
    }

    fun pauseBackgroundMusic() {
        onMain {
            if (!released) musicPlayer?.playWhenReady = false
        }
    }

    /**
     * Mix-level ducking keeps completion stingers intelligible over the theme.
     * A short cooldown avoids repeated effects from rapid touches.
     */
    fun playCue(cue: Cue) {
        onMain {
            if (released || !effectsEnabled || effectsVolume <= 0f) return@onMain
            val sample = samples[cue] ?: return@onMain
            if (!loaded.contains(sample)) return@onMain
            val now = SystemClock.elapsedRealtime()
            if (now - (lastPlayed[cue] ?: 0L) < 90L) return@onMain
            lastPlayed[cue] = now
            val weight = when (cue) {
                Cue.WRONG, Cue.FAILURE -> .66f
                Cue.UI -> .42f
                Cue.LASER, Cue.POP, Cue.IMPACT -> .56f
                else -> .83f
            }
            val volume = (effectsVolume * weight).coerceIn(0f, 1f)
            runCatching { pool.play(sample, volume, volume, 1, 0, 1f) }
                .onFailure { Log.w(TAG, "Cannot play $cue", it) }
            if (cue in setOf(Cue.SUCCESS, Cue.FAILURE, Cue.LEVEL, Cue.QUEST)) {
                mainHandler.removeCallbacks(duckRestore)
                musicPlayer?.volume = currentMusicVolume() * .48f
                mainHandler.postDelayed(duckRestore, 1350L)
            }
        }
    }

    fun playCorrectSound() = playCue(Cue.CORRECT)
    fun playWrongSound() = playCue(Cue.WRONG)
    fun playSuccessSound() = playCue(Cue.SUCCESS)
    fun playFailureSound() = playCue(Cue.FAILURE)
    fun playVictorySound() = playCue(Cue.SUCCESS)
    fun playLevelCompleteSound() = playCue(Cue.LEVEL)
    fun playQuestCompleteSound() = playCue(Cue.QUEST)
    fun playCoinSound() = playCue(Cue.COIN)
    fun playComboSound() = playCue(Cue.COMBO)
    fun playTapSound() = playCue(Cue.UI)
    fun playPopSound() = playCue(Cue.POP)
    fun playWhooshSound() = playCue(Cue.WHOOSH)
    fun playLaserSound() = playCue(Cue.LASER)
    fun playImpactSound() = playCue(Cue.IMPACT)

    // Kept for old callers; samples are preloaded once and shared.
    fun resetMediaPlayers() = Unit
    fun clearEffectSounds() = Unit

    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
        if (enabled && appForeground) resumeBackgroundMusic() else if (!enabled) pauseBackgroundMusic()
    }

    fun setSoundEnabled(enabled: Boolean) {
        effectsEnabled = enabled
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun isMusicEnabled(): Boolean = musicEnabled
    fun isSoundEnabled(): Boolean = effectsEnabled
    fun getMusicVolume(): Float = musicVolume
    fun getEffectsVolume(): Float = effectsVolume

    fun setMusicVolume(value: Float) {
        musicVolume = value.coerceIn(0f, 1f)
        prefs.edit().putFloat(KEY_MUSIC_VOLUME, musicVolume).apply()
        onMain { if (!released) musicPlayer?.volume = currentMusicVolume() }
    }

    fun setEffectsVolume(value: Float) {
        effectsVolume = value.coerceIn(0f, 1f)
        prefs.edit().putFloat(KEY_EFFECTS_VOLUME, effectsVolume).apply()
    }

    fun playSoundTest() {
        if (!effectsEnabled) {
            Toast.makeText(context, "Önce ses efektlerini aç.", Toast.LENGTH_SHORT).show()
            return
        }
        playCorrectSound()
        mainHandler.postDelayed({ playWrongSound() }, 600L)
        mainHandler.postDelayed({ playLevelCompleteSound() }, 1200L)
        mainHandler.postDelayed({ playQuestCompleteSound() }, 2700L)
    }

    fun checkSoundFilesStatus(): String {
        return listOf(
            "Ana tema" to R.raw.mm2_adventure_theme,
            "Doğru cevap" to R.raw.mm2_correct,
            "Yanlış cevap" to R.raw.mm2_wrong,
            "Zafer" to R.raw.mm2_success,
            "Kaybediş" to R.raw.mm2_failure
        ).joinToString("\n") { (name, resource) ->
            val exists = runCatching {
                context.resources.openRawResource(resource).use { it.read() != -1 }
            }.getOrDefault(false)
            "$name: " + if (exists) "hazır" else "yüklenemedi"
        }
    }

    fun release() {
        onMain {
            if (released) return@onMain
            released = true
            mainHandler.removeCallbacks(duckRestore)
            musicPlayer?.release()
            musicPlayer = null
            musicPrepared = false
            pool.release()
            loaded.clear()
            synchronized(Companion) {
                if (instance === this) instance = null
            }
        }
    }
}
