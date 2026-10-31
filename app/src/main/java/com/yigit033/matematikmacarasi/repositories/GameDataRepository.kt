package com.yigit033.matematikmacarasi.repositories

import com.yigit033.matematikmacarasi.models.GameProgress

/**
 * GameDataRepository, oyun verilerine erişim için soyut bir arayüz sunar.
 * Bu arayüz, verilerin nereden (yerel veya bulut) geldiğinden bağımsız olarak
 * uygulamanın veri erişimini sağlar.
 */
interface GameDataRepository {
    /**
     * Bir oyunun ilerlemesini kaydeder.
     * @param gameId Oyunun benzersiz kimliği
     * @param level Oyun seviyesi
     * @param score Oyun puanı
     * @param stars Kazanılan yıldız sayısı
     */
    fun saveProgress(gameId: String, level: Int, score: Int, stars: Int)
    
    /**
     * Bir oyunun ilerlemesini alır.
     * @param gameId Oyunun benzersiz kimliği
     * @return GameProgress nesnesi olarak oyun ilerlemesi
     */
    fun getProgress(gameId: String): GameProgress
    
    /**
     * Toplam yıldız sayısını alır.
     * @return Tüm oyunlardan kazanılan toplam yıldız sayısı
     */
    fun getTotalStars(): Int
    
    /**
     * Mevcut yıldız sayısı için oyunu kilidi açılmış mı kontrol eder.
     * @param requiredStars Gereken yıldız sayısı
     * @return Oyunun kilidi açılmışsa true
     */
    fun isGameUnlocked(requiredStars: Int): Boolean
} 