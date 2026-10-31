package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.yigit033.matematikmacarasi.R
import java.io.File

/**
 * AudioService, oyun seslerini yönetir.
 * Arka plan müziği ve efekt seslerini çalar.
 * Basitleştirilmiş versiyon - DataStore yerine SharedPreferences kullanıyor.
 */
class AudioService private constructor(private val context: Context) {

    companion object {
        // Singleton pattern
        @Volatile
        private var instance: AudioService? = null
        
        fun getInstance(context: Context): AudioService {
            return instance ?: synchronized(this) {
                instance ?: AudioService(context.applicationContext).also { instance = it }
            }
        }
        
        // SharedPreferences anahtarları
        private const val PREFS_NAME = "audio_settings"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        
        // Log tag
        private const val TAG = "AudioService"
    }
    
    // Ses MediaPlayer nesneleri
    private var correctSoundPlayer: MediaPlayer? = null
    private var wrongSoundPlayer: MediaPlayer? = null
    private var successSoundPlayer: MediaPlayer? = null
    
    // SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Arka plan müziği hazır mı
    private var backgroundMusicPrepared = false
    
    // ExoPlayer for background music - lazy initialize
    private val exoPlayer by lazy { 
        try {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }
        } catch (e: Exception) {
            Log.e(TAG, "ExoPlayer oluşturulamadı: ${e.message}")
            null
        }
    }
    
    // Ayarlar - cache
    private var isMusicEnabled = true
    private var isSoundEnabled = true
    
    init {
        // SharedPreferences'den ayarları yükle
        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true)
        isSoundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        
        // Başlangıçta ses kaynaklarını yükle
        preloadSoundResources()
        
        Log.d(TAG, "AudioService başlatıldı. Müzik: $isMusicEnabled, Ses: $isSoundEnabled")
    }
    
    /**
     * Ses dosyalarını önceden belleğe yükler
     */
    private fun preloadSoundResources() {
        try {
            // Doğru cevap sesi yükle
            try {
                correctSoundPlayer = MediaPlayer.create(context, R.raw.correct_answer)
                Log.d(TAG, "Doğru cevap sesi yüklendi: ${correctSoundPlayer != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Doğru cevap sesi yüklenemedi: ${e.message}")
            }
            
            // Yanlış cevap sesi yükle
            try {
                wrongSoundPlayer = MediaPlayer.create(context, R.raw.wrong_answer)
                Log.d(TAG, "Yanlış cevap sesi yüklendi: ${wrongSoundPlayer != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Yanlış cevap sesi yüklenemedi: ${e.message}")
            }
            
            // Başarı sesi yükle
            try {
                successSoundPlayer = MediaPlayer.create(context, R.raw.success)
                Log.d(TAG, "Başarı sesi yüklendi: ${successSoundPlayer != null}")
            } catch (e: Exception) {
                Log.e(TAG, "Başarı sesi yüklenemedi: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ses kaynakları yüklenirken genel hata: ${e.message}")
        }
    }
    
    /**
     * MediaPlayer nesnelerini sıfırlar ve yeniden yükler
     * Oyunlar arası geçişlerde çağrılmalıdır
     */
    fun resetMediaPlayers() {
        try {
            // Mevcut MediaPlayer'ları serbest bırak
            correctSoundPlayer?.release()
            wrongSoundPlayer?.release()
            successSoundPlayer?.release()
            
            // Referansları null'a ayarla
            correctSoundPlayer = null
            wrongSoundPlayer = null
            successSoundPlayer = null
            
            // Yeniden yükle
            preloadSoundResources()
            
            Log.d(TAG, "MediaPlayer nesneleri sıfırlandı ve yeniden yüklendi")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer nesneleri sıfırlanırken hata: ${e.message}")
        }
    }
    
    /**
     * Sadece efekt seslerini temizler, arka plan müziğini etkilemez.
     * Oyundan ana ekrana dönüş sırasında çağrılmalıdır.
     */
    fun clearEffectSounds() {
        try {
            // Mevcut efekt MediaPlayer'larını serbest bırak
            correctSoundPlayer?.release()
            wrongSoundPlayer?.release()
            successSoundPlayer?.release()
            
            // Referansları null'a ayarla
            correctSoundPlayer = null
            wrongSoundPlayer = null
            successSoundPlayer = null
            
            // NOT: Arka plan müziği ExoPlayer'a dokunmuyoruz
            // NOT: Sesleri yeniden yüklemiyoruz - gerektiğinde lazy loading yapacak
            
            Log.d(TAG, "Efekt sesleri temizlendi, arka plan müziği korundu")
        } catch (e: Exception) {
            Log.e(TAG, "Efekt sesleri temizlenirken hata: ${e.message}")
        }
    }
    
    /**
     * Arka plan müziği çalar.
     */
    fun playBackgroundMusic() {
        try {
            if (!isMusicEnabled || exoPlayer == null) {
                Log.d(TAG, "Arka plan müziği çalınmadı: Müzik Etkin: $isMusicEnabled, ExoPlayer: ${exoPlayer != null}")
                return
            }
            
            // Stop if already playing
            if (exoPlayer?.isPlaying == true) {
                exoPlayer?.stop()
                backgroundMusicPrepared = false
            }
            
            // Try to load default music if exists
            try {
                val musicResId = R.raw.main_theme
                val musicUri = "android.resource://${context.packageName}/$musicResId"
                val mediaItem = MediaItem.fromUri(musicUri)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.playWhenReady = true
                backgroundMusicPrepared = true
                Log.d(TAG, "Arka plan müziği çalınıyor: $musicUri")
            } catch (e: Exception) {
                // Müzik dosyası bulunamadı, sessizce devam et
                Log.e(TAG, "Arka plan müziği çalınırken hata: ${e.message}")
                backgroundMusicPrepared = false
            }
        } catch (e: Exception) {
            // Genel hata, log ve devam
            Log.e(TAG, "Arka plan müziği çalınırken beklenmeyen hata: ${e.message}")
            backgroundMusicPrepared = false
        }
    }
    
    /**
     * Arka plan müziğini duraklatır.
     */
    fun pauseBackgroundMusic() {
        try {
            if (exoPlayer?.isPlaying == true) {
                exoPlayer?.pause()
                Log.d(TAG, "Arka plan müziği duraklatıldı")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Arka plan müziği duraklatılırken hata: ${e.message}")
        }
    }
    
    /**
     * Arka plan müziğini devam ettirir.
     */
    fun resumeBackgroundMusic() {
        try {
            if (!isMusicEnabled) {
                Log.d(TAG, "Arka plan müziği devam etmedi: Müzik devre dışı")
                return
            }
            
            if (exoPlayer == null) {
                Log.d(TAG, "ExoPlayer null, yeni oluşturuluyor")
                // ExoPlayer yeniden oluştur
                playBackgroundMusic()
                return
            }
            
            if (!backgroundMusicPrepared) {
                Log.d(TAG, "Arka plan müziği hazır değil, yeniden yükleniyor")
                playBackgroundMusic()
                return
            }
            
            if (exoPlayer?.isPlaying == false) {
                exoPlayer?.play()
                Log.d(TAG, "Arka plan müziği devam ediyor")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Arka plan müziği devam ettirirken hata: ${e.message}")
            // Hata durumunda yeniden başlatmayı dene
            try {
                playBackgroundMusic()
            } catch (e2: Exception) {
                Log.e(TAG, "Arka plan müziğini yeniden başlatırken ikinci hata: ${e2.message}")
            }
        }
    }
    
    /**
     * Doğru cevap sesi çalar.
     */
    fun playCorrectSound() {
        try {
            if (!isSoundEnabled) {
                Log.d(TAG, "Doğru cevap sesi çalınmadı: Ses devre dışı")
                return
            }
            
            if (correctSoundPlayer == null) {
                // Yeniden yüklemeyi dene
                correctSoundPlayer = MediaPlayer.create(context, R.raw.correct_answer)
            }
            
            if (correctSoundPlayer != null) {
                if (correctSoundPlayer!!.isPlaying) {
                    correctSoundPlayer!!.stop()
                    correctSoundPlayer!!.prepare()
                }
                correctSoundPlayer!!.start()
                Log.d(TAG, "Doğru cevap sesi çalındı")
            } else {
                // Alternatif olarak yeni bir MediaPlayer oluştur
                playSound(R.raw.correct_answer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Doğru cevap sesi çalınırken hata: ${e.message}")
            // Hata durumunda tek seferlik bir MediaPlayer ile tekrar dene
            try {
                playSound(R.raw.correct_answer)
            } catch (e2: Exception) {
                Log.e(TAG, "Doğru cevap sesi alternatif çalma hatası: ${e2.message}")
            }
        }
    }
    
    /**
     * Yanlış cevap sesi çalar.
     */
    fun playWrongSound() {
        try {
            if (!isSoundEnabled) {
                Log.d(TAG, "Yanlış cevap sesi çalınmadı: Ses devre dışı")
                return
            }
            
            if (wrongSoundPlayer == null) {
                // Yeniden yüklemeyi dene
                wrongSoundPlayer = MediaPlayer.create(context, R.raw.wrong_answer)
            }
            
            if (wrongSoundPlayer != null) {
                if (wrongSoundPlayer!!.isPlaying) {
                    wrongSoundPlayer!!.stop()
                    wrongSoundPlayer!!.prepare()
                }
                wrongSoundPlayer!!.start()
                Log.d(TAG, "Yanlış cevap sesi çalındı")
            } else {
                // Alternatif olarak yeni bir MediaPlayer oluştur
                playSound(R.raw.wrong_answer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yanlış cevap sesi çalınırken hata: ${e.message}")
            // Hata durumunda tek seferlik bir MediaPlayer ile tekrar dene
            try {
                playSound(R.raw.wrong_answer)
            } catch (e2: Exception) {
                Log.e(TAG, "Yanlış cevap sesi alternatif çalma hatası: ${e2.message}")
            }
        }
    }
    
    /**
     * Başarı sesi çalar.
     */
    fun playSuccessSound() {
        try {
            if (!isSoundEnabled) {
                Log.d(TAG, "Başarı sesi çalınmadı: Ses devre dışı")
                return
            }
            
            if (successSoundPlayer == null) {
                // Yeniden yüklemeyi dene
                successSoundPlayer = MediaPlayer.create(context, R.raw.success)
            }
            
            if (successSoundPlayer != null) {
                if (successSoundPlayer!!.isPlaying) {
                    successSoundPlayer!!.stop()
                    successSoundPlayer!!.prepare()
                }
                successSoundPlayer!!.start()
                Log.d(TAG, "Başarı sesi çalındı")
            } else {
                // Alternatif olarak yeni bir MediaPlayer oluştur
                playSound(R.raw.success)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Başarı sesi çalınırken hata: ${e.message}")
            // Hata durumunda tek seferlik bir MediaPlayer ile tekrar dene
            try {
                playSound(R.raw.success)
            } catch (e2: Exception) {
                Log.e(TAG, "Başarı sesi alternatif çalma hatası: ${e2.message}")
            }
        }
    }
    
    /**
     * Başarısızlık sesi çalar.
     * Oyun kaybedildiğinde veya görev tamamlanamadığında kullanılır.
     */
    fun playFailureSound() {
        try {
            if (!isSoundEnabled) {
                Log.d(TAG, "Başarısızlık sesi çalınmadı: Ses devre dışı")
                return
            }
            
            // Failure için wrong_answer sesini kullanacağız, çünkü failure sesi henüz oluşturulmamış
            if (wrongSoundPlayer == null) {
                // Yeniden yüklemeyi dene
                wrongSoundPlayer = MediaPlayer.create(context, R.raw.wrong_answer)
            }
            
            if (wrongSoundPlayer != null) {
                if (wrongSoundPlayer!!.isPlaying) {
                    wrongSoundPlayer!!.stop()
                    wrongSoundPlayer!!.prepare()
                }
                wrongSoundPlayer!!.start()
                Log.d(TAG, "Başarısızlık sesi çalındı")
            } else {
                // Alternatif olarak yeni bir MediaPlayer oluştur
                playSound(R.raw.wrong_answer)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Başarısızlık sesi çalınırken hata: ${e.message}")
            // Hata durumunda tek seferlik bir MediaPlayer ile tekrar dene
            try {
                playSound(R.raw.wrong_answer)
            } catch (e2: Exception) {
                Log.e(TAG, "Başarısızlık sesi alternatif çalma hatası: ${e2.message}")
            }
        }
    }
    
    /**
     * Test amacıyla tüm sesleri sırayla çalar.
     */
    fun playSoundTest() {
        try {
            Toast.makeText(context, "Ses testi başladı...", Toast.LENGTH_SHORT).show()
            
            // MediaPlayer nesnelerini sıfırla
            resetMediaPlayers()
            
            // Doğru cevap sesi
            playCorrectSound()
            
            // 1 saniye bekle
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                // Yanlış cevap sesi
                playWrongSound()
                
                // 1 saniye bekle
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    // Başarı sesi
                    playSuccessSound()
                    
                    Toast.makeText(context, "Ses testi tamamlandı", Toast.LENGTH_SHORT).show()
                }, 1000)
            }, 1000)
            
            // Arka plan müziği
            playBackgroundMusic()
        } catch (e: Exception) {
            Log.e(TAG, "Ses testi sırasında hata: ${e.message}")
            Toast.makeText(context, "Ses testi başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Ses dosyalarının durumunu kontrol eder.
     * @return Ses dosyalarının durumu hakkında bilgi
     */
    fun checkSoundFilesStatus(): String {
        val result = StringBuilder()
        
        try {
            // Doğru cevap sesi
            val correctSoundPath = context.resources.getResourceName(R.raw.correct_answer)
            val correctSoundFile = getResourceFile(R.raw.correct_answer)
            result.append("Doğru cevap sesi: ${correctSoundFile?.exists() ?: false}, Boyut: ${correctSoundFile?.length() ?: 0} byte\n")
            
            // Yanlış cevap sesi
            val wrongSoundPath = context.resources.getResourceName(R.raw.wrong_answer)
            val wrongSoundFile = getResourceFile(R.raw.wrong_answer)
            result.append("Yanlış cevap sesi: ${wrongSoundFile?.exists() ?: false}, Boyut: ${wrongSoundFile?.length() ?: 0} byte\n")
            
            // Başarı sesi
            val successSoundPath = context.resources.getResourceName(R.raw.success)
            val successSoundFile = getResourceFile(R.raw.success)
            result.append("Başarı sesi: ${successSoundFile?.exists() ?: false}, Boyut: ${successSoundFile?.length() ?: 0} byte\n")
            
            // Ana tema müziği
            val mainThemePath = context.resources.getResourceName(R.raw.main_theme)
            val mainThemeFile = getResourceFile(R.raw.main_theme)
            result.append("Ana tema müziği: ${mainThemeFile?.exists() ?: false}, Boyut: ${mainThemeFile?.length() ?: 0} byte\n")
            
        } catch (e: Exception) {
            result.append("Hata: ${e.message}")
            Log.e(TAG, "Ses dosyaları kontrol edilirken hata: ${e.message}")
        }
        
        return result.toString()
    }
    
    /**
     * Resource ID'si verilen kaynağın dosyasını döndürür.
     * @param resourceId Kaynak ID'si
     * @return Dosya nesnesi veya null
     */
    private fun getResourceFile(resourceId: Int): File? {
        try {
            val resources = context.resources
            val resourcePath = resources.getResourceName(resourceId)
            val resourceName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1)
            val resourceDir = "android.resource://${context.packageName}/raw"
            
            return File(context.cacheDir, resourceName)
        } catch (e: Exception) {
            Log.e(TAG, "Kaynak dosyası alınamadı: ${e.message}")
            return null
        }
    }
    
    /**
     * Belirtilen sesi çalar.
     * @param soundRes Ses kaynak kimliği
     */
    private fun playSound(soundRes: Int) {
        try {
            Log.d(TAG, "Ses çalınıyor: $soundRes")
            MediaPlayer.create(context, soundRes)?.apply {
                setOnPreparedListener {
                    Log.d(TAG, "MediaPlayer hazır, çalınıyor: $soundRes")
                    it.start()
                }
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer hata: what=$what, extra=$extra")
                    false
                }
                setOnCompletionListener { 
                    Log.d(TAG, "MediaPlayer tamamlandı: $soundRes")
                    it.release() 
                }
                prepareAsync()
            } ?: run {
                Log.e(TAG, "MediaPlayer oluşturulamadı: $soundRes")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ses çalınırken hata: ${e.message}")
        }
    }
    
    /**
     * Müzik ayarını değiştirir.
     * @param enabled Müzik etkinse true
     */
    fun setMusicEnabled(enabled: Boolean) {
        try {
            isMusicEnabled = enabled
            prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
            
            if (enabled) {
                playBackgroundMusic()  // Doğrudan resume yerine play çağırarak sıfırdan başlat
            } else {
                pauseBackgroundMusic()
            }
            
            Log.d(TAG, "Müzik ayarı değiştirildi: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Müzik ayarı değiştirilirken hata: ${e.message}")
        }
    }
    
    /**
     * Ses ayarını değiştirir.
     * @param enabled Ses etkinse true
     */
    fun setSoundEnabled(enabled: Boolean) {
        try {
            isSoundEnabled = enabled
            prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
            Log.d(TAG, "Ses ayarı değiştirildi: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Ses ayarı değiştirilirken hata: ${e.message}")
        }
    }
    
    /**
     * Müzik ayarını alır.
     * @return Müzik etkinse true
     */
    fun isMusicEnabled(): Boolean {
        return isMusicEnabled
    }
    
    /**
     * Ses ayarını alır.
     * @return Ses etkinse true
     */
    fun isSoundEnabled(): Boolean {
        return isSoundEnabled
    }
    
    /**
     * Kaynakları serbest bırakır.
     */
    fun release() {
        try {
            // MediaPlayer nesnelerini temizle
            correctSoundPlayer?.release()
            wrongSoundPlayer?.release()
            successSoundPlayer?.release()
            
            // Referansları null'a ayarla
            correctSoundPlayer = null
            wrongSoundPlayer = null
            successSoundPlayer = null
            
            // ExoPlayer'ı temizle
            exoPlayer?.release()
            
            // Hazırlık durumunu sıfırla
            backgroundMusicPrepared = false
            
            Log.d(TAG, "AudioService kaynakları serbest bırakıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Kaynaklar serbest bırakılırken hata: ${e.message}")
        }
    }
} 