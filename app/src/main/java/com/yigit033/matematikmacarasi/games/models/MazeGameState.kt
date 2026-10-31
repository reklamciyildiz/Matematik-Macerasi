package com.yigit033.matematikmacarasi.games.models

/**
 * Labirent oyunu durum sınıfı
 */
data class MazeGameState(
    val level: MazeLevel,
    val maze: Array<Array<MazeCell>>,
    val questions: List<MathQuestion>,
    val playerPosition: Pair<Int, Int>,
    val timeRemaining: Int,
    val score: Int = 0,
    val stars: Int = 0,
    val gameOver: Boolean = false,
    val gameCompleted: Boolean = false,
    val currentQuestionId: Int = -1
) {
    // Array sınıflar için equals ve hashCode fonksiyonlarını override ediyoruz
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MazeGameState

        if (level != other.level) return false
        if (!maze.contentDeepEquals(other.maze)) return false
        if (questions != other.questions) return false
        if (playerPosition != other.playerPosition) return false
        if (timeRemaining != other.timeRemaining) return false
        if (score != other.score) return false
        if (stars != other.stars) return false
        if (gameOver != other.gameOver) return false
        if (gameCompleted != other.gameCompleted) return false
        if (currentQuestionId != other.currentQuestionId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + maze.contentDeepHashCode()
        result = 31 * result + questions.hashCode()
        result = 31 * result + playerPosition.hashCode()
        result = 31 * result + timeRemaining
        result = 31 * result + score
        result = 31 * result + stars
        result = 31 * result + gameOver.hashCode()
        result = 31 * result + gameCompleted.hashCode()
        result = 31 * result + currentQuestionId
        return result
    }
    
    /**
     * Oyuncuyu belirtilen yönde hareket ettirir.
     */
    fun movePlayer(direction: Direction): Boolean {
        try {
            // Mevcut pozisyonu alalım
            val (row, col) = playerPosition
            
            // Yeni pozisyonu hesapla
            val newRow = when (direction) {
                Direction.UP -> row - 1
                Direction.DOWN -> row + 1
                else -> row
            }
            val newCol = when (direction) {
                Direction.LEFT -> col - 1
                Direction.RIGHT -> col + 1
                else -> col
            }
            
            // Sınırları kontrol et
            if (newRow < 0 || newRow >= maze.size || newCol < 0 || newCol >= maze[0].size) {
                return false
            }
            
            // Duvar mı kontrol et
            if (maze[newRow][newCol].type == MazeCellType.WALL) {
                return false
            }
            
            return true
        } catch (e: Exception) {
            return false
        }
    }
} 