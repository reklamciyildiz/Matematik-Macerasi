package com.yigit033.matematikmacarasi.games.basic

/**
 * Kullanıcı cevabı için geri bildirim durumu
 */
enum class AnswerFeedbackState {
    IDLE,      // Henüz cevap yok
    CORRECT,   // Doğru cevap
    INCORRECT, // Yanlış cevap
    TIMEOUT    // Süre doldu
} 