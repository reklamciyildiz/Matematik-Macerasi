package com.yigit033.matematikmacarasi.games.models

/**
 * Labirent hücre türleri
 */
enum class MazeCellType {
    WALL,       // Duvar
    PATH,       // Yol
    START,      // Başlangıç
    END,        // Bitiş
    QUESTION,   // Soru
    CURRENT     // Şu anki pozisyon
}

/**
 * Labirent hücresi
 */
data class MazeCell(
    var type: MazeCellType,
    var questionId: Int? = null,
    var visited: Boolean = false
) 