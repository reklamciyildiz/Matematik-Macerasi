package com.yigit033.matematikmacarasi.learning

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yigit033.matematikmacarasi.services.AudioService
import java.util.Locale

/**
 * Child-directed Turkish narration with system-provided *offline* voices only.
 * No cloud voice provider, microphone access, child input or text upload.
 * If an offline Turkish voice is not installed, content remains readable;
 * the UI explains how to enable it. Lifecycle owner must call close().
 */
class LessonNarrator(context: Context) {
    private val app = context.applicationContext
    private val audio = AudioService.getInstance(app)
    private val main = Handler(Looper.getMainLooper())
    private var speaker: TextToSpeech? = null
    private var closed = false
    private var utteranceSerial = 0L
    private var activeUtterance: String? = null
    var available by mutableStateOf(false)
        private set
    var loading by mutableStateOf(true)
        private set
    var speaking by mutableStateOf(false)
        private set

    init {
        speaker = TextToSpeech(app) { status ->
            main.post {
                if (!closed) {
                    val tts = speaker
                    if (status != TextToSpeech.SUCCESS || tts == null) {
                        available = false
                    } else {
                        val offlineVoice = runCatching {
                            tts.voices?.filter {
                                it.locale.language == "tr" &&
                                    !it.isNetworkConnectionRequired
                            }?.minByOrNull {
                                if (it.locale.country.equals("TR", ignoreCase = true)) 0 else 1
                            }
                        }.getOrNull()
                        if (offlineVoice != null) {
                            available = tts.setVoice(offlineVoice) == TextToSpeech.SUCCESS
                            if (available) {
                                tts.setSpeechRate(0.90f)
                                tts.setPitch(1f)
                                tts.setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                        .build()
                                )
                                tts.setOnUtteranceProgressListener(
                                    object : UtteranceProgressListener() {
                                        override fun onStart(utteranceId: String) {
                                            main.post {
                                                if (!closed && utteranceId == activeUtterance) {
                                                    speaking = true
                                                    audio.onLessonNarrationChanged(true)
                                                }
                                            }
                                        }

                                        override fun onDone(utteranceId: String) = markStopped(utteranceId)
                                        override fun onError(utteranceId: String) = markStopped(utteranceId)
                                        override fun onStop(utteranceId: String, interrupted: Boolean) =
                                            markStopped(utteranceId)

                                        private fun markStopped(utteranceId: String) {
                                            main.post {
                                                if (!closed && utteranceId == activeUtterance) {
                                                    activeUtterance = null
                                                    speaking = false
                                                    audio.onLessonNarrationChanged(false)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            // Fail closed rather than silently using a cloud voice.
                            available = false
                        }
                    }
                    loading = false
                }
            }
        }
    }

    val audioAllowed: Boolean
        get() = audio.isSoundEnabled() && audio.getEffectsVolume() > 0f

    fun say(text: String) {
        if (closed || !available || !audioAllowed || text.isBlank()) return
        // QUEUE_FLUSH may invoke an older utterance's onStop AFTER
        // the new one starts. Only callbacks for the current ID may
        // change the audio state, or a lesson can advance mid-sentence.
        val id = "mm-lesson-" + (++utteranceSerial)
        activeUtterance = id
        audio.onLessonNarrationChanged(true)
        speaking = true
        val result = runCatching {
            speaker?.speak(text.take(1200), TextToSpeech.QUEUE_FLUSH, null, id)
        }.getOrNull()
        if (result != TextToSpeech.SUCCESS && activeUtterance == id) {
            activeUtterance = null
            speaking = false
            audio.onLessonNarrationChanged(false)
        }
    }

    fun stop() {
        if (closed) return
        activeUtterance = null
        runCatching { speaker?.stop() }
        speaking = false
        audio.onLessonNarrationChanged(false)
    }

    fun close() {
        if (closed) return
        closed = true
        activeUtterance = null
        runCatching { speaker?.stop() }
        runCatching { speaker?.shutdown() }
        speaker = null
        audio.onLessonNarrationChanged(false)
    }
}
