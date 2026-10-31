package com.yigit033.matematikmacarasi.games.models

/**
 * Tüm oyunlar için ortak kullanılabilecek oyun durumlarını temsil eden enum sınıfı.
 * Bu sınıf farklı oyun aktivitelerinde aynı durum kodlarını kullanabilmek için oluşturulmuştur.
 */
enum class GameState {
    /**
     * Oyun başlamadan önceki hazırlık durumu
     */
    READY,
    
    /**
     * Oyun aktif olarak oynanıyor
     */
    PLAYING,
    
    /**
     * Oyun duraklatıldı
     */
    PAUSED,
    
    /**
     * Oyun tamamlandı (başarılı)
     */
    COMPLETED,
    
    /**
     * Oyun bitti (başarısız)
     */
    GAME_OVER
} 