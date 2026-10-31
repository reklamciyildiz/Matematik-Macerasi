package com.yigit033.matematikmacarasi.models

/**
 * GameProgress, oyun ilerleme bilgilerini tutan veri sınıfı.
 *
 * @property gameId Oyunun benzersiz kimliği
 * @property level Oyun seviyesi
 * @property stars Kazanılan yıldız sayısı
 * @property highScore En yüksek skor
 */
data class GameProgress(
    val gameId: String,
    val level: Int = 1,
    val stars: Int = 0,
    val highScore: Int = 0
) 