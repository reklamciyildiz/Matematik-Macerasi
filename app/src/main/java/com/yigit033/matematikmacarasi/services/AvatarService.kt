package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.UUID
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.models.AvatarAnimationType
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.EventBus
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * Avatar değişikliklerini dinlemek için listener arayüzü
 */
interface AvatarChangeListener {
    fun onAvatarChanged(newAvatarId: Int)
}

class AvatarService(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("avatar_prefs", Context.MODE_PRIVATE)
    private val _unlockedAvatarIds = mutableSetOf<Int>()
    private var _currentAvatarId: Int = 1 // Varsayılan avatar ID'si (Sıfır Sayısı)
    private val _avatars = mutableListOf<Avatar>()
    
    // Coroutine scope
    private val scope = CoroutineScope(Dispatchers.Main)
    
    init {
        // SharedPreferences'dan verileri yükle
        val storedAvatarId = prefs.getInt("current_avatar_id", 1)
        _currentAvatarId = storedAvatarId
        
        val unlockedAvatarIdsSet = prefs.getStringSet("unlocked_avatar_ids", setOf("1")) ?: setOf("1")
        val tempUnlockedIds = mutableSetOf<Int>()
        unlockedAvatarIdsSet.forEach { 
            try {
                tempUnlockedIds.add(it.toInt())
            } catch (e: Exception) {
                Log.e("AvatarService", "Avatar ID dönüştürülürken hata: $it, ${e.message}")
            }
        }
        
        // ** Düzeltme: Yüklenen ID'leri temizle **
        try {
            val purchaseManager = com.yigit033.matematikmacarasi.services.payments.PurchaseManager.getInstance(context)
            val isPremium = purchaseManager.isPremiumSync() 
            Log.d("AvatarService", "Başlangıçta premium durum kontrolü: $isPremium")
            
            // Eğer kullanıcı premium DEĞİLSE, premium avatar ID'lerini (7-15) setten çıkar
            if (!isPremium) {
                val initialCount = tempUnlockedIds.size
                tempUnlockedIds.removeAll { it in 7..15 }
                if (tempUnlockedIds.size < initialCount) {
                    Log.w("AvatarService", "Premium olmayan kullanıcı için SharedPreferences'ten premium avatarlar temizlendi.")
                    // ** Düzeltme: Temizlenmiş ID'leri tekrar SharedPreferences'e kaydet **
                    prefs.edit().putStringSet("unlocked_avatar_ids", tempUnlockedIds.map { it.toString() }.toSet()).apply()
                }
            }
            // Not: Kullanıcı premium ise, ID'leri olduğu gibi bırakıyoruz. Kilidi açma mantığı yıldız kontrolünü zaten yapıyor.
        } catch (e: Exception) {
            Log.e("AvatarService", "Premium durumu kontrol edilirken hata, avatar ID temizleme atlandı: ${e.message}", e)
            // Hata durumunda ID'leri olduğu gibi bırak, varsayılan olarak devam et
        }

        // Temizlenmiş ID'leri asıl sete ata
        _unlockedAvatarIds.clear()
        _unlockedAvatarIds.addAll(tempUnlockedIds)
        
        // Avatarları yükle
        loadAvatars()
        
        // Açık durumlarını güncelle
        updateUnlockedStatus()
        
        Log.d("AvatarService", "Avatar Servisi başlatıldı: $_unlockedAvatarIds avatarlar açık, şu anki avatar: $_currentAvatarId")
    }
    
    private fun loadAvatars() {
        _avatars.clear()
        _avatars.addAll(listOf(
            // 1. Sıfır Sayısı - Başlangıç Avatar (Başlangıç için 0 yıldız)
            Avatar(
                id = 1,
                name = "Sıfır Sayısı",
                imageResId = R.drawable.avatar_number_0,
                price = 0,
                isUnlocked = (1 in _unlockedAvatarIds),
                category = "Semboller",
                description = "Matematiğin temel yapı taşlarından biri olan sihirli sayı!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_number_0_animation
            ),
            
            // 2. Matematik Robotu - 5 yıldız
            Avatar(
                id = 2,
                name = "Matematik Robotu",
                imageResId = R.drawable.avatar_math_robot,
                price = 10,
                isUnlocked = (2 in _unlockedAvatarIds),
                category = "Robotlar",
                description = "Matematik hesaplamalarını yıldırım hızında yapan sevimli robot!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_math_robot_animation
            ),
            
            // 3. İşlem Canavarı - 10 yıldız
            Avatar(
                id = 3,
                name = "İşlem Canavarı",
                imageResId = R.drawable.avatar_plus,
                price = 15,
                isUnlocked = (3 in _unlockedAvatarIds),
                category = "Semboller",
                description = "Temel işlemleri canavar gibi çözen çocuk!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_plus_sign_animation
            ),
            
            // 4. Matematik Çocuk - 15 yıldız
            Avatar(
                id = 4,
                name = "Matematik Çocuk",
                imageResId = R.drawable.avatar_math_kid,
                price = 15,
                isUnlocked = (4 in _unlockedAvatarIds),
                category = "Karakterler",
                description = "Matematiği seven ve çözen akıllı çocuk!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_math_kid_animation
            ),
            
            // 5. Dahi Kız - 20 yıldız
            Avatar(
                id = 5,
                name = "Dahi Kız",
                imageResId = R.drawable.avatar_math_girl_genius,
                price = 20,
                isUnlocked = (5 in _unlockedAvatarIds),
                category = "Karakterler",
                description = "Matematikte harikalar yaratan dahi kız!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_math_girl_genius_animation
            ),
            
            // 6. Kaşif Öğrenci - 25 yıldız
            Avatar(
                id = 6,
                name = "Kaşif Öğrenci",
                imageResId = R.drawable.avatar_explorer,
                price = 20,
                isUnlocked = (6 in _unlockedAvatarIds),
                category = "Karakterler",
                description = "Matematiksel keşifler yapan maceraperest öğrenci!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_explorer_animation
            ),
            
            // 7. Matematik Büyücüsü - 30 yıldız (Premium)
            Avatar(
                id = 7,
                name = "Matematik Büyücüsü",
                imageResId = R.drawable.avatar_math_wizard_kid,
                price = 30,
                isUnlocked = (7 in _unlockedAvatarIds),
                category = "Premium",
                description = "Matematik formüllerini büyü gibi kullanan bilge büyücü!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_math_wizard_kid_animation
            ),
            
            // 8. Sayıların Piri - 35 yıldız (Premium)
            Avatar(
                id = 8,
                name = "Sayıların Piri",
                imageResId = R.drawable.avatar_number_1,
                price = 30,
                isUnlocked = (8 in _unlockedAvatarIds),
                category = "Premium",
                description = "Sayıların dünyasında ustalaşmış, matematik yolculuğunun öncüsü!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_number_1_animation
            ),
            
            // 9. Şekiller Dehası - 40 yıldız (Premium)
            Avatar(
                id = 9,
                name = "Şekiller Dehası",
                imageResId = R.drawable.avatar_number_2,
                price = 40,
                isUnlocked = (9 in _unlockedAvatarIds),
                category = "Premium",
                description = "Geometrik şekillerin sırlarını çözen matematik dehası!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_number_2_animation
            ),
            
            // 10. Abaküs Beyin - 45 yıldız (Premium)
            Avatar(
                id = 10,
                name = "Abaküs Beyin",
                imageResId = R.drawable.avatar_number_3,
                price = 40,
                isUnlocked = (10 in _unlockedAvatarIds),
                category = "Premium",
                description = "Hızlı hesaplamalarıyla tanınan, abaküs ustası matematikçi!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_number_three_animation
            ),
            
            // 11. Heyecanlı Matematikçi - 50 yıldız (Premium)
            Avatar(
                id = 11,
                name = "Heyecanlı Matematikçi",
                imageResId = R.drawable.avatar_euclid,
                price = 50,
                isUnlocked = (11 in _unlockedAvatarIds),
                category = "Premium",
                description = "Matematik çözümlerini büyük bir heyecanla anlatan coşkulu bilim insanı!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_euclid_animation
            ),
            
            // 12. Bilge Çocuk - 55 yıldız (Premium)
            Avatar(
                id = 12,
                name = "Bilge Çocuk",
                imageResId = R.drawable.avatar_pythagoras,
                price = 60,
                isUnlocked = (12 in _unlockedAvatarIds),
                category = "Premium",
                description = "Matematiksel teoremlerle problemleri çözen bilgeliğiyle ünlü çocuk!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_pythagoras_animation
            ),
            
            // 13. Einstein - 60 yıldız (Premium)
            Avatar(
                id = 13,
                name = "Einstein",
                imageResId = R.drawable.avatar_einstein,
                price = 60,
                isUnlocked = (13 in _unlockedAvatarIds),
                category = "Premium",
                description = "Dahice matematik formülleriyle bilinen bilim insanı!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_einstein_animation
            ),
            
            // 14. Şekiller Uzmanı - 65 yıldız (Premium)
            Avatar(
                id = 14,
                name = "Şekiller Uzmanı",
                imageResId = R.drawable.avatar_ada_lovelace,
                price = 70,
                isUnlocked = (14 in _unlockedAvatarIds),
                category = "Premium",
                description = "Karmaşık geometrik şekilleri kolayca çözebilen matematik yıldızı!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_ada_lovelace_animation
            ),
            
            // 15. Zeka Küpü - 70 yıldız (Premium)
            Avatar(
                id = 15,
                name = "Zeka Küpü",
                imageResId = R.drawable.avatar_marie_curie,
                price = 70,
                isUnlocked = (15 in _unlockedAvatarIds),
                category = "Premium",
                description = "Parlak zekasıyla en zor matematik problemlerini çözen matematik dehası!",
                animationType = AvatarAnimationType.LOTTIE,
                animationResId = R.raw.avatar_marie_curie_animation
            )
        ))
    }
    
    /**
     * Avatar durumunu günceller, açık/kapalı durumlarını belirler
     */
    private fun updateUnlockedStatus() {
        // Avatarların açık/kapalı durumlarını güncelleyen yeni bir liste oluşturalım
        val updatedAvatars = _avatars.map { avatar ->
            // Orijinal avatar'ı yeni isUnlocked değeriyle kopyalayalım
            avatar.copy(isUnlocked = _unlockedAvatarIds.contains(avatar.id))
        }
        
        // Mevcut listeyi temizleyip yeni listeyi ekleyelim
        _avatars.clear()
        _avatars.addAll(updatedAvatars)
        
        Log.d("AvatarService", "Avatar durumları güncellendi: ${_avatars.filter { it.isUnlocked }.map { it.id }}")
    }
    
    /**
     * Şu anki avatarı alır
     */
    fun getCurrentAvatar(): Avatar {
        return try {
            getAvatarById(_currentAvatarId)
        } catch (e: Exception) {
            Log.e("AvatarService", "Mevcut avatar alınırken hata: ${e.message}", e)
            _avatars.first() // Hata durumunda ilk avatarı döndür
        }
    }
    
    /**
     * Şu anki avatarı değiştirir
     */
    fun setCurrentAvatar(avatarId: Int): Boolean {
        try {
            // Avatar ID'si geçerli mi kontrol et
            val avatar = _avatars.find { it.id == avatarId }
            if (avatar == null) {
                Log.e("AvatarService", "Avatar bulunamadı: $avatarId")
                return false
            }
            
            // Avatar kilidi açık mı kontrol et
            if (!avatar.isUnlocked) {
                Log.e("AvatarService", "Avatar kilidi açık değil: $avatarId")
                return false
            }
            
            // Değişiklik gerekli mi kontrol et
            if (_currentAvatarId == avatarId) {
                // Zaten seçili, işlem başarılı
                return true
            }
            
            // Premium avatar cannot be selected on Free even if previously
            // unlocked with stars. Honor the existing offline entitlement cache.
            if(avatarId>6 && !com.yigit033.matematikmacarasi.services.payments
                    .PurchaseManager.getInstance(context).isPremiumSync()) {
                return false
            }

            // Avatarı güncelle
            _currentAvatarId = avatarId
            prefs.edit().putInt("current_avatar_id", avatarId).apply()
            
            // EventBus ile bildir
            EventBus.updateAvatarId(avatarId)
            
            Log.d("AvatarService", "Avatar başarıyla değiştirildi: $avatarId")
            return true
        } catch (e: Exception) {
            Log.e("AvatarService", "Avatar değiştirilirken hata: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Avatar kilidini açar
     */
    fun unlockAvatar(avatarId: Int): Boolean {
        Log.d("AvatarService", "Avatar kilidi açma başladı: $avatarId")
        
        try {
            // Avatar nesnesini bul
            val avatar = _avatars.find { it.id == avatarId }
            if (avatar == null) {
                Log.e("AvatarService", "Kilidini açmaya çalışılan avatar bulunamadı: $avatarId")
                return false
            }
            
            // Eğer avatar zaten açıksa hata döndür
            if (avatar.isUnlocked) {
                Log.w("AvatarService", "Avatar zaten açık: $avatarId")
                return false
            }
            
            // Premium kontrolü
            try {
                val purchaseManager = com.yigit033.matematikmacarasi.services.payments.PurchaseManager.getInstance(context)
                val isPremium = purchaseManager.isPremiumSync()
                
                // Premium olmayan kullanıcılar için ID sınırlaması
                if (avatarId > 6 && !isPremium) {
                    Log.e("AvatarService", "Premium olmayan kullanıcı premium avatar açamaz: $avatarId")
                    return false
                }
            } catch (e: Exception) {
                Log.e("AvatarService", "Premium durumu kontrol edilirken hata: ${e.message}", e)
                // Fail closed: do not deduct stars or unlock Premium content.
                return false
            }
            
            // Yıldız kontrolü - StarManager'ı başlat
            val starManager = StarManager.getInstance(context)
            
            // Mevcut yıldız sayısını al
            val currentDisplayStars = starManager.getDisplayStars()
            
            // Fiyat kontrolü
            if (currentDisplayStars < avatar.price) {
                Log.w("AvatarService", "Yetersiz yıldız: Sahip olunan=$currentDisplayStars, " +
                      "Gerekli=${avatar.price}")
                return false
            }
            
            // 1. Yeni yıldız sayısını hesapla
            val newStars = currentDisplayStars - avatar.price
            Log.d("AvatarService", "Avatar satın alınıyor: Eski yıldızlar=$currentDisplayStars, " +
                  "Fiyat=${avatar.price}, Yeni yıldızlar=$newStars")
            
            // 2. ÖNEMLİ: Yıldızları güncelle - önce güncelle
            if (!starManager.setDisplayStars(newStars)) {
                Log.e("AvatarService", "Yıldızlar güncellenemedi, işlem iptal ediliyor")
                return false
            }
            
            // 3. KRİTİK: Hemen EventBus ile bildir
            try {
                // Bildirim - UI güncelleme
                CoroutineScope(Dispatchers.Main).launch {
                    EventBus.forceStarNotification(newStars)
                    Log.d("AvatarService", "Yıldız azaltma bildirimi yapıldı: $newStars")
                }
            } catch (e: Exception) {
                Log.e("AvatarService", "Yıldız bildirimi hatası: ${e.message}", e)
                // Kritik değil, devam et
            }
            
            // 4. Avatar kilidini aç
            _unlockedAvatarIds.add(avatarId)
            
            // 5. Kalıcı olarak kaydet, hata olursa işlemi geri al
            val saveResult = saveUnlockedAvatarsToPrefs(true)
            if (!saveResult) {
                Log.e("AvatarService", "Avatar kilidi açılamadı, SharedPreferences'a kaydedilemedi")
                // İşlem başarısız, yıldızları geri ver
                starManager.setDisplayStars(currentDisplayStars)
                _unlockedAvatarIds.remove(avatarId)
                
                // Geri alma bildirimini UI'a yap
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        EventBus.forceStarNotification(currentDisplayStars)
                        Log.d("AvatarService", "Yıldız iadesi bildirimi yapıldı: $currentDisplayStars")
                    }
                } catch (e: Exception) {
                    Log.e("AvatarService", "Yıldız iadesi bildirimi hatası: ${e.message}", e)
                }
                
                return false
            }
            
            // 6. Modeli güncelle
            updateUnlockedStatus()
            
            Log.d("AvatarService", "Avatar başarıyla açıldı: ${avatar.name} (ID: $avatarId)")
            Log.d("AvatarService", "Yıldız durumu güncellendi: $currentDisplayStars -> $newStars")
            
            // 7. KRİTİK: Doğrulama ve ek güvence için tekrar UI güncelleme
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Kısa bir gecikme ile tekrar kontrol et ve bildir
                    delay(300)
                    
                    // Güncel yıldız sayısını al ve kontrol et
                    val verifiedStars = starManager.getDisplayStars()
                    if (verifiedStars != newStars) {
                        Log.w("AvatarService", "Yıldız tutarsızlığı bulundu: Beklenen=$newStars, Gerçek=$verifiedStars")
                        
                        // Tutarsızlık varsa düzelt ve bildir
                        starManager.setDisplayStars(newStars)
                        delay(100)
                        EventBus.forceStarNotification(newStars)
                        Log.d("AvatarService", "Yıldız tutarsızlığı düzeltildi ve bildirildi")
                    } else {
                        // Doğru olsa bile bir kez daha bildir
                        EventBus.forceStarNotification(newStars)
                        Log.d("AvatarService", "Yıldız doğrulaması tamam: $newStars")
                    }
                    
                    // 500ms sonra tekrar kontrol et
                    delay(500)
                    EventBus.forceStarNotification(newStars)
                    Log.d("AvatarService", "Son kontrol bildirimi yapıldı: $newStars")
                } catch (e: Exception) {
                    Log.e("AvatarService", "Doğrulama kontrolü sırasında hata: ${e.message}", e)
                }
            }
            
            return true
        } catch (e: Exception) {
            Log.e("AvatarService", "Avatar açılırken işlem hatası: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Kullanıcının avatar kilidi açmak için kullanabileceği mevcut yıldız sayısını döndürür.
     * Bu, kullanıcı arayüzünde gösterilen yıldız sayısıdır.
     */
    fun getUserStars(): Int {
        try {
            val starManager = StarManager.getInstance(context)
            // Display yıldızları al - bu metod tamamen yerel depodan okur, herhangi bir senkronizasyon yapmaz
            return starManager.getDisplayStars()
        } catch (e: Exception) {
            Log.e("AvatarService", "Yıldız sayısı alınırken hata: ${e.message}", e)
            return 0
        }
    }
    
    companion object {
        @Volatile
        private var instance: AvatarService? = null
        
        @Synchronized
        fun getInstance(context: Context): AvatarService {
            return instance ?: synchronized(this) {
                instance ?: AvatarService(context.applicationContext).also { instance = it }
            }
        }
        
        // Belleği temizle - uygulama kapatılırken çağrılabilir
        fun clearInstance() {
            instance = null
        }
    }
    
    // ID'ye göre avatar getir
    fun getAvatarById(avatarId: Int): Avatar {
        return try {
            _avatars.find { it.id == avatarId } ?: run {
                Log.w("AvatarService", "Avatar bulunamadı (ID: $avatarId), varsayılan avatar döndürülüyor")
                _avatars[0] // Varsayılan ilk avatarı döndür
            }
        } catch (e: Exception) {
            Log.e("AvatarService", "Avatar ID'ye göre alınırken hata: ${e.message}", e)
            _avatars[0] // Hata durumunda varsayılan avatarı döndür
        }
    }
    
    /**
     * Tüm avatarları sıfırlar, sadece varsayılan avatarı açık bırakır.
     * Uygulama sıfırlandığında veya ilk kez kurulduğunda çağrılır.
     */
    fun resetAllAvatars() {
        try {
            // Sadece varsayılan avatar açık olacak
            _unlockedAvatarIds.clear()
            _unlockedAvatarIds.add(1) // ID 1 ücretsiz başlangıç avatarı (Sıfır Sayısı)
            
            // Mevcut seçili avatarı da varsayılana ayarla
            val defaultAvatarId = 1
            
            // SharedPreferences'e kaydet
            prefs.edit()
                .putStringSet("unlocked_avatar_ids", setOf("1"))
                .putInt("current_avatar_id", defaultAvatarId)
                .commit()
            
            // EventBus'u güncelle
            EventBus.updateAvatarId(defaultAvatarId)
            
            // _currentAvatarId değişkenini güvenli şekilde güncelle
            setCurrentAvatar(defaultAvatarId)
            
            // Modeli güncelle
            updateUnlockedStatus()
            
            Log.d("AvatarService", "Tüm avatarlar sıfırlandı, sadece varsayılan avatar açık")
        } catch (e: Exception) {
            Log.e("AvatarService", "Avatarlar sıfırlanırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Belirli bir Avatar'ın kilidinin açılıp açılamayacağını kontrol eder.
     * Kullanıcının yıldız sayısı ve premium durumuna bağlıdır.
     */
    fun canUnlockAvatar(avatarId: Int, userStars: Int, isPremium: Boolean): Boolean {
        try {
            // Önce Avatar'ı bul
            val avatar = _avatars.find { it.id == avatarId } ?: return false
            
            // Eğer avatar zaten açıksa, tekrar kilidi açılamaz
            if (avatar.isUnlocked) return false
            
            // Premium kontrolü - 7-15 arası avatarlar premium gerektirir
            if (avatarId > 6 && !isPremium) {
                return false // 6'dan büyük avatar ID'leri sadece premium kullanıcılar için
            }
            
            // Yıldız kontrolü - tüm avatarlar için yıldız sayısı yeterli mi?
            return userStars >= avatar.price
        } catch (e: Exception) {
            Log.e("AvatarService", "canUnlockAvatar hatası: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Yıldız sayısını günceller
     */
    private fun setUserStars(stars: Int) {
        try {
            val starManager = StarManager.getInstance(context)
            starManager.setTotalStars(stars)
        } catch (e: Exception) {
            Log.e("AvatarService", "Yıldız sayısı güncellenirken hata: ${e.message}", e)
        }
    }
    
    /**
     * Açık avatarları SharedPreferences'a kaydeder
     * @param useCommit commit kullanarak senkron kaydetme için true, apply kullanarak asenkron kaydetmek için false
     * @return Kaydetme işlemi başarılı olduysa true, değilse false
     */
    private fun saveUnlockedAvatarsToPrefs(useCommit: Boolean = false): Boolean {
        try {
            val stringIds = _unlockedAvatarIds.map { it.toString() }.toSet()
            val editor = prefs.edit().putStringSet("unlocked_avatar_ids", stringIds)
            
            val result = if (useCommit) {
                // Senkron kaydet, hemen kaydedilmesini sağlar
                editor.commit()
            } else {
                // Asenkron kaydet, UI performansını etkilemez
                editor.apply()
                true // apply her zaman true döndürür varsayalım
            }
            
            Log.d("AvatarService", "Açık avatarlar kaydedildi: $_unlockedAvatarIds, useCommit=$useCommit, sonuç=$result")
            return result
        } catch (e: Exception) {
            Log.e("AvatarService", "Açık avatarlar kaydedilirken hata: ${e.message}", e)
            return false
        }
    }
    
    val avatars: List<Avatar>
        get() = _avatars.toList()
} 