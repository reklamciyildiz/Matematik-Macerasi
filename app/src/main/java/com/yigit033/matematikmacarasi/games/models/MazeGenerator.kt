package com.yigit033.matematikmacarasi.games.models

import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Labirent ve soru oluşturucu
 */
object MazeGenerator {
    
    /**
     * Verilen seviyeye göre labirent oluşturur
     *
     * @param level Labirent seviyesi
     * @return Oluşturulan labirent ve başlangıç pozisyonu
     */
    fun generateMaze(level: MazeLevel): Pair<Array<Array<MazeCell>>, Pair<Int, Int>> {
        try {
            // Seviye parametrelerini kontrol et
            val size = if (level.size < 5) 5 else level.size // En az 5x5 labirent
            
            // Başlangıçta tüm hücreler duvar
            val maze = Array(size) { row ->
                Array(size) { col ->
                    MazeCell(type = MazeCellType.WALL)
                }
            }
            
            // Labirenti oluştur 
            val visited = Array(size) { BooleanArray(size) }
            
            // Başlangıç noktasını belirleme - artık daha iyi bir pozisyon seçiyoruz
            val startRow = 1
            val startCol = 1
            
            // Başlangıç noktasını belirle
            maze[startRow][startCol].type = MazeCellType.START
            visited[startRow][startCol] = true
            
            // Recursive labirent oluşturma
            try {
                generatePath(maze, visited, startRow, startCol)
                
                // Labirent oluşturma sonrası, erişilebilirlik kontrolü yap
                // Tüm labirent içinde kaç tane erişilebilir hücre var kontrolü
                val accessibleCells = countAccessibleCells(maze, startRow, startCol)
                
                // Eğer erişilebilir hücre sayısı çok azsa, daha basit bir labirent oluştur
                if (accessibleCells < (size * size) / 3) { // 1/4 yerine 1/3 yapıyoruz - daha fazla erişilebilir hücre olsun
                    simplifyMaze(maze, size)
                }
            } catch (e: Exception) {
                // Labirent oluşturma hatası, basit labirent oluştur
                simplifyMaze(maze, size)
            }
            
            // Bitiş noktasını belirle - artık daha akıllı bir konum seçiyoruz
            // Bitiş noktası başlangıca çok yakın olmamalı ve erişilebilir olmalı
            var endRow = size - 2
            var endCol = size - 2
            
            // Bitiş hücresini erişilebilir bir PATH hücresine yerleştir
            var endPlaced = false
            
            // Önce köşelerde dene - en iyi bitiş pozisyonları
            val corners = listOf(
                Pair(size - 2, size - 2), // Sağ alt köşe (en iyi seçenek)
                Pair(size - 2, 1),        // Sol alt köşe
                Pair(1, size - 2)         // Sağ üst köşe
            )
            
            for ((r, c) in corners) {
                // Köşeye erişilebilir yol olduğundan emin ol
                if (isAccessible(maze, startRow, startCol, r, c)) {
                    endRow = r
                    endCol = c
                    endPlaced = true
                    break
                }
            }
            
            // Köşelerde erişilebilir PATH bulunamadıysa, başlangıca uzak bir PATH hücresini bul
            if (!endPlaced) {
                // Başlangıç noktasından uzaklığa göre sırala
                val candidates = mutableListOf<Triple<Int, Int, Int>>() // (row, col, distance)
                
                for (r in 1 until size - 1) {
                    for (c in 1 until size - 1) {
                        if (maze[r][c].type == MazeCellType.PATH && 
                            !(r == startRow && c == startCol)) {
                            // Başlangıç noktasına manhattan uzaklığı
                            val distance = abs(r - startRow) + abs(c - startCol)
                            candidates.add(Triple(r, c, distance))
                        }
                    }
                }
                
                // Uzaklığa göre azalan sırada sırala ve erişilebilir en uzak noktayı seç
                candidates.sortByDescending { it.third }
                
                for ((r, c, _) in candidates) {
                    if (isAccessible(maze, startRow, startCol, r, c)) {
                        endRow = r
                        endCol = c
                        endPlaced = true
                        break
                    }
                }
            }
            
            // Hala bitiş noktası bulunamadıysa, zorla bir PATH aç ve bitiş yap
            if (!endPlaced) {
                endRow = size - 2
                endCol = size - 2
                maze[endRow][endCol].type = MazeCellType.PATH
                // Başlangıç ve bitiş arasında yol oluştur
                ensurePathBetweenStartAndEnd(maze, startRow, startCol, endRow, endCol)
            }
            
            // Bitiş hücresinin tipini ayarla
            maze[endRow][endCol].type = MazeCellType.END
            
            // Soru hücreleri ekle - geliştirilmiş sürüm
            try {
                addQuestionCellsImproved(maze, level.questionCount, startRow, startCol, endRow, endCol)
            } catch (e: Exception) {
                // Soru ekleme hatası, basit soru hücresi ekle
                var addedQuestions = 0
                for (row in 1 until size - 1) {
                    for (col in 1 until size - 1) {
                        if (maze[row][col].type == MazeCellType.PATH) {
                            maze[row][col].type = MazeCellType.QUESTION
                            maze[row][col].questionId = ++addedQuestions
                            
                            if (addedQuestions >= level.questionCount) {
                                break
                            }
                        }
                    }
                    if (addedQuestions >= level.questionCount) {
                        break
                    }
                }
            }
            
            // Son bir kontrol - başlangıç ve bitiş noktalarının bağlantısını garanti et
            ensurePathBetweenStartAndEnd(maze, startRow, startCol, endRow, endCol)
            
            // Tüm soru hücrelerine erişilebilir olduğunu doğrula
            ensureAllQuestionCellsAccessible(maze, startRow, startCol)
            
            // Labirenti optimize et - gereksiz çıkmaz yolları temizle
            optimizeMaze(maze, startRow, startCol, endRow, endCol)
            
            return Pair(maze, Pair(startRow, startCol))
        } catch (e: Exception) {
            // Herhangi bir hata durumunda basit bir labirent döndür
            val size = 5
            val simpleMaze = Array(size) { row ->
                Array(size) { col ->
                    MazeCell(
                        type = if (row == 0 || col == 0 || row == size-1 || col == size-1) 
                            MazeCellType.WALL else MazeCellType.PATH
                    )
                }
            }
            
            // Basit, elle oluşturulmuş labirent yerleşimi
            simpleMaze[1][1].type = MazeCellType.START
            simpleMaze[3][3].type = MazeCellType.END
            simpleMaze[2][2].type = MazeCellType.QUESTION
            simpleMaze[1][3].type = MazeCellType.QUESTION
            simpleMaze[2][2].questionId = 1
            simpleMaze[1][3].questionId = 2
            
            return Pair(simpleMaze, Pair(1, 1))
        }
    }
    
    /**
     * İki nokta arasında erişilebilirlik kontrolü
     */
    private fun isAccessible(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int, targetRow: Int, targetCol: Int): Boolean {
        val size = maze.size
        val visited = Array(size) { BooleanArray(size) }
        
        fun dfs(row: Int, col: Int): Boolean {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return false
            }
            
            if (row == targetRow && col == targetCol) {
                return true
            }
            
            visited[row][col] = true
            
            // 4 yöne de bak
            return dfs(row + 1, col) || dfs(row - 1, col) || 
                   dfs(row, col + 1) || dfs(row, col - 1)
        }
        
        return dfs(startRow, startCol)
    }
    
    /**
     * Labirenti optimize et - gereksiz çıkmaz yolları temizle, açıklık ekle
     */
    private fun optimizeMaze(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        val size = maze.size
        
        // Tüm önemli noktaları (başlangıç, bitiş, sorular) topla
        val importantPoints = mutableListOf<Pair<Int, Int>>()
        importantPoints.add(Pair(startRow, startCol))
        importantPoints.add(Pair(endRow, endCol))
        
        // Soru hücrelerini ekle
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if (maze[row][col].type == MazeCellType.QUESTION) {
                    importantPoints.add(Pair(row, col))
                }
            }
        }
        
        // Bazı noktalarda genişletme yap - labirenti daha açık hale getir
        val openUpCount = size / 3 // Seviyeye göre açıklık sayısı
        val openUpCandidates = mutableListOf<Pair<Int, Int>>()
        
        // Potansiyel açıklık noktalarını topla
        for (row in 2 until size - 2) {
            for (col in 2 until size - 2) {
                if (maze[row][col].type == MazeCellType.WALL) {
                    // Çevresinde en az 2 PATH veya önemli hücre varsa aç
                    var pathNeighbors = 0
                    if (maze[row-1][col].type != MazeCellType.WALL) pathNeighbors++
                    if (maze[row+1][col].type != MazeCellType.WALL) pathNeighbors++
                    if (maze[row][col-1].type != MazeCellType.WALL) pathNeighbors++
                    if (maze[row][col+1].type != MazeCellType.WALL) pathNeighbors++
                    
                    if (pathNeighbors >= 2) {
                        openUpCandidates.add(Pair(row, col))
                    }
                }
            }
        }
        
        // Rastgele açıklıklar ekle
        openUpCandidates.shuffled().take(openUpCount).forEach { (row, col) ->
            maze[row][col].type = MazeCellType.PATH
        }
        
        // Son olarak çok fazla PATH yan yana olmasın, bazı duvarları koru
        for (row in 2 until size - 2) {
            for (col in 2 until size - 2) {
                // 2x2 PATH bloğu varsa, ortasına duvar koy
                if (maze[row][col].type == MazeCellType.PATH &&
                    maze[row+1][col].type == MazeCellType.PATH &&
                    maze[row][col+1].type == MazeCellType.PATH &&
                    maze[row+1][col+1].type == MazeCellType.PATH) {
                    
                    // Önemli nokta değilse rastgele birini duvar yap
                    val points = listOf(
                        Pair(row, col),
                        Pair(row+1, col),
                        Pair(row, col+1),
                        Pair(row+1, col+1)
                    )
                    
                    val nonImportant = points.filter { (r, c) -> 
                        !importantPoints.contains(Pair(r, c)) &&
                        maze[r][c].type == MazeCellType.PATH
                    }
                    
                    if (nonImportant.isNotEmpty()) {
                        val (r, c) = nonImportant.random()
                        // Bu noktayı duvar yapmadan önce, önemli noktalara erişilebilirliği kontrol et
                        maze[r][c].type = MazeCellType.WALL
                        
                        // Eğer erişim bozulduysa, geri al
                        if (!checkAllPointsAccessible(maze, importantPoints)) {
                            maze[r][c].type = MazeCellType.PATH
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Tüm önemli noktaların birbirine erişilebilir olduğunu kontrol et
     */
    private fun checkAllPointsAccessible(maze: Array<Array<MazeCell>>, points: List<Pair<Int, Int>>): Boolean {
        // Her noktadan diğer tüm noktalara erişimi kontrol et
        for (i in 0 until points.size - 1) {
            for (j in i + 1 until points.size) {
                if (!isAccessible(maze, points[i].first, points[i].second, 
                                  points[j].first, points[j].second)) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * Recursive labirent yolu oluşturma (geliştirilmiş)
     */
    private fun generatePath(
        maze: Array<Array<MazeCell>>,
        visited: Array<BooleanArray>,
        row: Int,
        col: Int
    ) {
        try {
            // Geçersiz indeks kontrolü
            if (row < 0 || col < 0 || row >= maze.size || col >= maze[0].size) {
                return
            }
            
            // Tüm yönleri karıştır (rastgele yönler seç)
            val directions = mutableListOf(
                Pair(0, 1),  // Sağ
                Pair(1, 0),  // Aşağı
                Pair(0, -1), // Sol
                Pair(-1, 0)  // Yukarı
            ).shuffled()
            
            // Her yönü dene
            for ((rowOffset, colOffset) in directions) {
                // Daha uzun koridor oluşturmak için 2 birim ilerle
                val newRow = row + rowOffset * 2
                val newCol = col + colOffset * 2
                
                // Sınırlar içinde mi?
                if (newRow in 1 until maze.size - 1 && newCol in 1 until maze[0].size - 1) {
                    // Daha önce ziyaret edilmemiş mi?
                    if (!visited[newRow][newCol]) {
                        // Ara hücreyi de yol yap (koridor oluşturmak için)
                        maze[row + rowOffset][col + colOffset].type = MazeCellType.PATH
                        
                        // Hedef hücreyi yol yap
                        maze[newRow][newCol].type = MazeCellType.PATH
                        
                        // Ziyaret edildi olarak işaretle
                        visited[newRow][newCol] = true
                        
                        // Recursively devam et
                        generatePath(maze, visited, newRow, newCol)
                    }
                }
            }
            
            // Daha fazla bağlantı oluşturmak için rastgele çapraz yollar ekle
            if (maze.size > 7 && Random.nextDouble() < 0.3) { // %30 olasılıkla ek yollar oluştur
                // Rastgele iki bitişik hücre arasında yeni bir yol oluştur
                val randomDirection = directions.firstOrNull()
                if (randomDirection != null) {
                    val (rowOffset, colOffset) = randomDirection
                    val neighborRow = row + rowOffset
                    val neighborCol = col + colOffset
                    
                    if (neighborRow in 1 until maze.size - 1 && 
                        neighborCol in 1 until maze[0].size - 1 &&
                        maze[neighborRow][neighborCol].type == MazeCellType.WALL) {
                        
                        // Yeni bir yol aç
                        maze[neighborRow][neighborCol].type = MazeCellType.PATH
                    }
                }
            }
        } catch (e: Exception) {
            // Sessizce hata yakala
        }
    }
    
    /**
     * Erişilebilir hücre sayısını hesapla
     */
    private fun countAccessibleCells(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int): Int {
        val size = maze.size
        val visited = Array(size) { BooleanArray(size) }
        var count = 0
        
        fun dfs(row: Int, col: Int) {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return
            }
            
            visited[row][col] = true
            count++
            
            dfs(row + 1, col)
            dfs(row - 1, col)
            dfs(row, col + 1)
            dfs(row, col - 1)
        }
        
        dfs(startRow, startCol)
        return count
    }
    
    /**
     * Basit labirent oluştur (daha çok yol içeren)
     */
    private fun simplifyMaze(maze: Array<Array<MazeCell>>, size: Int) {
        // Labirent özellikleri için parametreler
        val pathDensity = 0.45 // Yol yoğunluğu - daha düşük değer daha fazla duvar demek
        
        // Mevcut durumun üzerine rastgele yolları içeren kararlı bir desen oluştur
        // Bu, tekrarlı seferlerde bile benzer zorlukta labirentler oluşturmaya yarar
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                // Kenarları daha açık tutmak için merkeze uzaklığa göre yol olasılığını artır
                val distanceFromCenter = abs(row - size/2) + abs(col - size/2)
                val normalizedDistance = distanceFromCenter.toDouble() / (size - 2)
                val pathProbability = pathDensity + (0.3 * normalizedDistance) // Kenarlara yakın daha fazla yol olsun
                
                // Temel deseni oluştur - satranç benzeri dama tahtası deseni + rastgele varyasyonlar
                val isCheckerboardPath = (row + col) % 2 == 0
                val isRandomPath = Random.nextDouble() < pathProbability
                val shouldBePath = isCheckerboardPath || isRandomPath
                
                if (shouldBePath) {
                    maze[row][col].type = MazeCellType.PATH
                } else {
                    maze[row][col].type = MazeCellType.WALL
                }
                
                // Köşelerde ve başlangıç/bitiş noktaları yakınında yeterli alan olduğundan emin ol
                if ((row <= 2 && col <= 2) ||  // Sol üst köşe yakını
                    (row <= 2 && col >= size - 3) ||  // Sağ üst köşe yakını
                    (row >= size - 3 && col <= 2) ||  // Sol alt köşe yakını
                    (row >= size - 3 && col >= size - 3)) {  // Sağ alt köşe yakını
                    // Köşeler etrafında daha fazla yol olsun
                    if (Random.nextDouble() < 0.7) {
                        maze[row][col].type = MazeCellType.PATH
                    }
                }
            }
        }
        
        // Tamamen izole hücreleri temizle (çıkmaz sokaklar dışında)
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if (maze[row][col].type == MazeCellType.PATH) {
                    // Komşu sayısını kontrol et
                    var neighborCount = 0
                    if (row > 1 && maze[row-1][col].type == MazeCellType.PATH) neighborCount++
                    if (row < size-2 && maze[row+1][col].type == MazeCellType.PATH) neighborCount++
                    if (col > 1 && maze[row][col-1].type == MazeCellType.PATH) neighborCount++
                    if (col < size-2 && maze[row][col+1].type == MazeCellType.PATH) neighborCount++
                    
                    // Tamamen izole hücreleri bağla
                    if (neighborCount == 0) {
                        // En yakın yola bir koridor oluştur
                        val direction = Random.nextInt(4)
                        when (direction) {
                            0 -> if (row > 1) maze[row-1][col].type = MazeCellType.PATH
                            1 -> if (row < size-2) maze[row+1][col].type = MazeCellType.PATH
                            2 -> if (col > 1) maze[row][col-1].type = MazeCellType.PATH
                            3 -> if (col < size-2) maze[row][col+1].type = MazeCellType.PATH
                        }
                    }
                }
            }
        }
        
        // Başlangıç ve bitiş noktaları arasında bir yolun olduğundan emin ol
        // Ve bu yol etrafında çeşitli dallanmalar ekle
        ensurePathBetweenStartAndEnd(maze, 1, 1, size - 2, size - 2)
        
        // Birkaç rastgele dallanma daha ekle - labirenti ilginç hale getirmek için
        for (i in 0 until size / 3) {
            val row = Random.nextInt(1, size - 1)
            val col = Random.nextInt(1, size - 1)
            
            if (maze[row][col].type == MazeCellType.PATH) {
                // Rastgele bir yönde koridor oluştur
                val direction = Random.nextInt(4)
                var length = Random.nextInt(1, 3) // Kısa bir koridor
                
                var currRow = row
                var currCol = col
                
                while (length > 0) {
                    val nextRow = when (direction) {
                        0 -> currRow - 1  // Yukarı
                        1 -> currRow + 1  // Aşağı
                        else -> currRow
                    }
                    
                    val nextCol = when (direction) {
                        2 -> currCol - 1  // Sol
                        3 -> currCol + 1  // Sağ
                        else -> currCol
                    }
                    
                    if (nextRow in 1 until size - 1 && nextCol in 1 until size - 1) {
                        maze[nextRow][nextCol].type = MazeCellType.PATH
                        currRow = nextRow
                        currCol = nextCol
                    }
                    
                    length--
                }
            }
        }
    }
    
    /**
     * Başlangıç ve bitiş arasında bir yol olduğundan emin ol
     */
    private fun ensurePathBetweenStartAndEnd(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int, endRow: Int, endCol: Int) {
        // Basit bir yol oluştur
        var currentRow = startRow
        var currentCol = startCol
        
        // Önce yatay sonra dikey hareket et - daha düzgün bir yol için
        // Önce sütunlarda hareket et
        while (currentCol != endCol) {
            currentCol += if (currentCol < endCol) 1 else -1
            
            // Eğer duvar ise, yol yap (başlangıç veya bitiş değilse)
            if (maze[currentRow][currentCol].type == MazeCellType.WALL) {
                maze[currentRow][currentCol].type = MazeCellType.PATH
            }
        }
        
        // Sonra satırlarda hareket et
        while (currentRow != endRow) {
            currentRow += if (currentRow < endRow) 1 else -1
            
            // Eğer duvar ise, yol yap
            if (maze[currentRow][currentCol].type == MazeCellType.WALL) {
                maze[currentRow][currentCol].type = MazeCellType.PATH
            }
        }
        
        // Erişilebilirlik kontrolü ile tüm PATH ve QUESTION hücrelerinin başlangıçtan erişilebilir olduğunu doğrula
        val size = maze.size
        val visited = Array(size) { BooleanArray(size) }
        
        fun dfs(row: Int, col: Int) {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return
            }
            
            visited[row][col] = true
            
            dfs(row + 1, col)
            dfs(row - 1, col)
            dfs(row, col + 1)
            dfs(row, col - 1)
        }
        
        dfs(startRow, startCol)
        
        // Erişilemeyen PATH veya QUESTION hücreleri için yollar oluştur
        var changed = false
        
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if ((maze[row][col].type == MazeCellType.PATH || maze[row][col].type == MazeCellType.QUESTION) && !visited[row][col]) {
                    // En yakın erişilebilir hücreyi bul
                    var minDistance = Int.MAX_VALUE
                    var nearestRow = -1
                    var nearestCol = -1
                    
                    for (r in 1 until size - 1) {
                        for (c in 1 until size - 1) {
                            if (visited[r][c]) {
                                val distance = abs(r - row) + abs(c - col)
                                if (distance < minDistance) {
                                    minDistance = distance
                                    nearestRow = r
                                    nearestCol = c
                                }
                            }
                        }
                    }
                    
                    if (nearestRow != -1 && nearestCol != -1) {
                        // Erişilebilir en yakın hücreye bir yol oluştur
                        var pathRow = row
                        var pathCol = col
                        
                        // Önce yatay, sonra dikey hareket et
                        while (pathCol != nearestCol) {
                            pathCol += if (pathCol < nearestCol) 1 else -1
                            
                            // Eğer duvar ise, yol yap (QUESTION, START, END değilse)
                            if (maze[pathRow][pathCol].type == MazeCellType.WALL) {
                                maze[pathRow][pathCol].type = MazeCellType.PATH
                            }
                        }
                        
                        while (pathRow != nearestRow) {
                            pathRow += if (pathRow < nearestRow) 1 else -1
                            
                            // Eğer duvar ise, yol yap
                            if (maze[pathRow][pathCol].type == MazeCellType.WALL) {
                                maze[pathRow][pathCol].type = MazeCellType.PATH
                            }
                        }
                        
                        changed = true
                    }
                }
            }
        }
        
        // Eğer değişiklik yapıldıysa, erişilebilirliği tekrar kontrol et
        if (changed) {
            ensurePathBetweenStartAndEnd(maze, startRow, startCol, endRow, endCol)
        }
    }
    
    /**
     * Geliştirilmiş soru yerleştirme
     */
    private fun addQuestionCellsImproved(
        maze: Array<Array<MazeCell>>, 
        questionCount: Int,
        startRow: Int,
        startCol: Int,
        endRow: Int,
        endCol: Int
    ) {
        val size = maze.size
        
        // Başlangıç ve bitiş arasındaki yol üzerine sorular ekle
        val pathCells = findPathBetweenStartAndEnd(maze, startRow, startCol, endRow, endCol)
        
        // Erişilebilir tüm PATH hücrelerini bul
        val allAccessiblePaths = findAllAccessiblePathCells(maze, startRow, startCol)
        
        // Erişilebilir olduğu kesin olan PATH hücrelerinin kontrolü
        if (allAccessiblePaths.isEmpty()) {
            throw IllegalStateException("Erişilebilir yol hücresi bulunamadı!")
        }
        
        // Önemli yol noktalarını belirle - sorular için en iyi adaylar
        val priorityCells = mutableListOf<Pair<Int, Int>>()
        
        // Ana yoldan belirli aralıklarla soru ekle - bu soruların erişilebilir olması garantidir
        if (pathCells.isNotEmpty()) {
            val step = maxOf(1, pathCells.size / (questionCount / 2 + 1))
            for (i in 0 until pathCells.size step step) {
                if (i < pathCells.size) {
                    priorityCells.add(pathCells[i])
                }
            }
        }
        
        // Labirent alanını dört çeyreğe böl ve her çeyreğe eşit sayıda soru yerleştir
        val quarterSize = questionCount / 4
        
        // Labirenti dört çeyreğe bölerek hücre grupları oluştur
        val quadrants = listOf(
            // Sol üst çeyrek
            allAccessiblePaths.filter { (row, col) -> 
                row < size/2 && col < size/2 &&
                !(row == startRow && col == startCol) && // başlangıç değil
                !(row == endRow && col == endCol)        // bitiş değil
            },
            // Sağ üst çeyrek
            allAccessiblePaths.filter { (row, col) -> 
                row < size/2 && col >= size/2 &&
                !(row == startRow && col == startCol) && 
                !(row == endRow && col == endCol)
            },
            // Sol alt çeyrek
            allAccessiblePaths.filter { (row, col) -> 
                row >= size/2 && col < size/2 &&
                !(row == startRow && col == startCol) && 
                !(row == endRow && col == endCol)
            },
            // Sağ alt çeyrek
            allAccessiblePaths.filter { (row, col) -> 
                row >= size/2 && col >= size/2 &&
                !(row == startRow && col == startCol) && 
                !(row == endRow && col == endCol)
            }
        )
        
        // Her çeyrekten soruları dengeli bir şekilde ekle
        for (quadrant in quadrants) {
            if (quadrant.isNotEmpty()) {
                // Bu çeyrekten kaç soru eklemeliyiz
                val questionPerQuadrant = minOf(quarterSize, quadrant.size)
                
                // Rastgele hücreler seç
                val selectedCells = quadrant.shuffled().take(questionPerQuadrant)
                
                // Prioritiye ekle
                for (cell in selectedCells) {
                    if (!priorityCells.contains(cell) && 
                        maze[cell.first][cell.second].type == MazeCellType.PATH) {
                        priorityCells.add(cell)
                    }
                }
            }
        }
        
        // Kavşak noktalarını bul - bu noktalara soru eklemek mantıklı
        val junctionCells = mutableListOf<Pair<Int, Int>>()
        
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if (maze[row][col].type == MazeCellType.PATH) {
                    // Kaç komşu yolu var
                    var pathNeighbors = 0
                    if (row > 0 && maze[row-1][col].type != MazeCellType.WALL) pathNeighbors++
                    if (row < size-1 && maze[row+1][col].type != MazeCellType.WALL) pathNeighbors++
                    if (col > 0 && maze[row][col-1].type != MazeCellType.WALL) pathNeighbors++
                    if (col < size-1 && maze[row][col+1].type != MazeCellType.WALL) pathNeighbors++
                    
                    // Kavşak noktaları (3+ komşu) veya çıkmaz sokak (1 komşu)
                    if (pathNeighbors >= 3 || pathNeighbors == 1) {
                        if (!((row == startRow && col == startCol) || (row == endRow && col == endCol)) &&
                            !priorityCells.contains(Pair(row, col))) {
                            junctionCells.add(Pair(row, col))
                        }
                    }
                }
            }
        }
        
        // Rastgele kavşak noktaları ekle
        if (junctionCells.isNotEmpty()) {
            val junctionsToAdd = junctionCells.shuffled().take(minOf(questionCount / 5 + 1, junctionCells.size))
            for (cell in junctionsToAdd) {
                if (!priorityCells.contains(cell)) {
                    priorityCells.add(cell)
                }
            }
        }
        
        // Eğer hala yeterli soru yoksa, kalan erişilebilir hücrelerden rastgele ekle
        if (priorityCells.size < questionCount) {
            val remainingCells = allAccessiblePaths.filter { cell ->
                !priorityCells.contains(cell) &&
                !(cell.first == startRow && cell.second == startCol) &&
                !(cell.first == endRow && cell.second == endCol) &&
                maze[cell.first][cell.second].type == MazeCellType.PATH
            }
            
            if (remainingCells.isNotEmpty()) {
                // Kalan hücrelerden seçerken satır bazında çok fazla soru olmamasını sağla
                // Her satırda maksimum 2 soru olsun
                val rowCounts = priorityCells.groupBy { it.first }.mapValues { it.value.size }.toMutableMap()
                
                val selectedRemainingCells = mutableListOf<Pair<Int, Int>>()
                val neededQuestions = questionCount - priorityCells.size
                
                // Kalan hücreleri karıştır ve sırayla kontrol et
                for (cell in remainingCells.shuffled()) {
                    val row = cell.first
                    // Bu satırda kaç soru var
                    val questionsInRow = rowCounts.getOrDefault(row, 0)
                    
                    // Eğer bu satırda zaten 2 veya daha fazla soru varsa, bu satırdan başka soru ekleme
                    if (questionsInRow < 2) {
                        selectedRemainingCells.add(cell)
                        // Satır sayacını güncelle
                        rowCounts[row] = questionsInRow + 1
                        
                        // Yeterli soru eklendi mi kontrol et
                        if (selectedRemainingCells.size >= neededQuestions) break
                    }
                }
                
                // Eğer hala yeterli soru yoksa ve bazı satırlar atlandıysa, 
                // kalan rastgele hücreleri ekle (satır sınırlaması olmadan)
                if (selectedRemainingCells.size < neededQuestions) {
                    val remainingNeeded = neededQuestions - selectedRemainingCells.size
                    val extraCells = remainingCells.filter { !selectedRemainingCells.contains(it) }
                                                 .shuffled()
                                                 .take(remainingNeeded)
                    selectedRemainingCells.addAll(extraCells)
                }
                
                priorityCells.addAll(selectedRemainingCells)
            }
        }
        
        // Öncelikli hücreleri soru hücrelerine dönüştür
        var questionId = 1
        for (cell in priorityCells.take(questionCount)) {
            val row = cell.first
            val col = cell.second
            if (maze[row][col].type == MazeCellType.PATH) {
                maze[row][col].type = MazeCellType.QUESTION
                maze[row][col].questionId = questionId++
            }
        }
        
        // Soruların erişilebilirliğini doğrula
        val visited = Array(size) { BooleanArray(size) }
        
        fun checkAccessibility(r: Int, c: Int) {
            if (r < 0 || c < 0 || r >= size || c >= size || 
                visited[r][c] || maze[r][c].type == MazeCellType.WALL) {
                return
            }
            
            visited[r][c] = true
            
            checkAccessibility(r + 1, c)
            checkAccessibility(r - 1, c)
            checkAccessibility(r, c + 1)
            checkAccessibility(r, c - 1)
        }
        
        checkAccessibility(startRow, startCol)
        
        // Erişilemeyen soru var mı kontrol et ve gerekirse yeniden yerleştir
        val unreachableQuestions = mutableListOf<Pair<Int, Int>>()
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if (maze[row][col].type == MazeCellType.QUESTION && !visited[row][col]) {
                    unreachableQuestions.add(Pair(row, col))
                }
            }
        }
        
        // Erişilemeyen soruları erişilebilir noktalara taşı
        if (unreachableQuestions.isNotEmpty()) {
            for ((row, col) in unreachableQuestions) {
                // Erişilebilir bir PATH hücresi bul
                var relocated = false
                for (r in (1 until size - 1).shuffled()) {
                    for (c in (1 until size - 1).shuffled()) {
                        if (maze[r][c].type == MazeCellType.PATH && visited[r][c]) {
                            // Bu hücreyi soru hücresi yap
                            maze[r][c].type = MazeCellType.QUESTION
                            maze[r][c].questionId = maze[row][col].questionId
                            
                            // Eski soru hücresini yol yap
                            maze[row][col].type = MazeCellType.PATH
                            maze[row][col].questionId = 0
                            
                            relocated = true
                            break
                        }
                    }
                    if (relocated) break
                }
            }
        }
        
        // Eğer herhangi bir satırda aşırı sayıda soru varsa, bazılarını taşı
        val maxQuestionsPerRow = 3  // Bir satırda izin verilen maksimum soru sayısı
        var needToRedistribute = false
        
        // Satır bazında soru sayımı yap
        val rowCounts = mutableMapOf<Int, Int>()
        for (row in 0 until size) {
            for (col in 0 until size) {
                if (maze[row][col].type == MazeCellType.QUESTION) {
                    rowCounts[row] = rowCounts.getOrDefault(row, 0) + 1
                }
            }
        }
        
        // Aşırı yüklü satırları bul
        val overloadedRows = rowCounts.filter { it.value > maxQuestionsPerRow }.keys
        if (overloadedRows.isNotEmpty()) {
            // Aşırı yüklü satırlardan soruları taşı
            for (overloadedRow in overloadedRows) {
                val excessQuestions = rowCounts[overloadedRow]!! - maxQuestionsPerRow
                if (excessQuestions <= 0) continue
                
                // Bu satırdaki soruları bul
                val questionsInRow = mutableListOf<Pair<Int, Int>>()
                for (col in 0 until size) {
                    if (maze[overloadedRow][col].type == MazeCellType.QUESTION) {
                        questionsInRow.add(Pair(overloadedRow, col))
                    }
                }
                
                // Rastgele excessQuestions kadar soruyu taşımak için seç
                val questionsToMove = questionsInRow.shuffled().take(excessQuestions)
                
                // Seçilen soruları az yüklü satırlara taşı
                for ((oldRow, oldCol) in questionsToMove) {
                    // Bu soruyu PATH'e geri çevir
                    maze[oldRow][oldCol].type = MazeCellType.PATH
                    maze[oldRow][oldCol].questionId = 0
                    
                    // Az soru bulunan satırları bul
                    val targetRows = (0 until size)
                                      .filter { rowCounts.getOrDefault(it, 0) < 2 }
                                      .shuffled()
                    
                    if (targetRows.isNotEmpty()) {
                        val newRow = targetRows.first()
                        
                        // Bu satırda boş bir PATH hücresi bul
                        var placed = false
                        for (newCol in (0 until size).shuffled()) {
                            if (maze[newRow][newCol].type == MazeCellType.PATH &&
                                !(newRow == startRow && newCol == startCol) &&
                                !(newRow == endRow && newCol == endCol) &&
                                visited[newRow][newCol]) { // Eklenen erişilebilirlik kontrolü
                                
                                // Yeni hücreyi soru hücresi yap
                                maze[newRow][newCol].type = MazeCellType.QUESTION
                                maze[newRow][newCol].questionId = maze[oldRow][oldCol].questionId
                                
                                // Satır sayaçlarını güncelle
                                rowCounts[oldRow] = rowCounts.getOrDefault(oldRow, 0) - 1
                                rowCounts[newRow] = rowCounts.getOrDefault(newRow, 0) + 1
                                
                                placed = true
                                break
                            }
                        }
                        
                        // Eğer yerleştiremediysen, herhangi bir uygun hücre bul
                        if (!placed) {
                            for (r in (0 until size).shuffled()) {
                                if (rowCounts.getOrDefault(r, 0) < 2) {
                                    for (c in (0 until size).shuffled()) {
                                        if (maze[r][c].type == MazeCellType.PATH &&
                                            !(r == startRow && c == startCol) &&
                                            !(r == endRow && c == endCol) &&
                                            visited[r][c]) { // Eklenen erişilebilirlik kontrolü
                                            
                                            maze[r][c].type = MazeCellType.QUESTION
                                            maze[r][c].questionId = maze[oldRow][oldCol].questionId
                                            
                                            rowCounts[oldRow] = rowCounts.getOrDefault(oldRow, 0) - 1
                                            rowCounts[r] = rowCounts.getOrDefault(r, 0) + 1
                                            
                                            placed = true
                                            break
                                        }
                                    }
                                }
                                if (placed) break
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Başlangıç noktasından erişilebilir tüm PATH hücrelerini bul
     */
    private fun findAllAccessiblePathCells(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int): List<Pair<Int, Int>> {
        val size = maze.size
        val visited = Array(size) { BooleanArray(size) }
        val accessibleCells = mutableListOf<Pair<Int, Int>>()
        
        fun dfs(row: Int, col: Int) {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return
            }
            
            visited[row][col] = true
            
            // PATH veya herhangi erişilebilir hücre ise listeye ekle
            if (maze[row][col].type != MazeCellType.WALL) {
                accessibleCells.add(Pair(row, col))
            }
            
            // 4 yöne de bak
            dfs(row + 1, col)
            dfs(row - 1, col)
            dfs(row, col + 1)
            dfs(row, col - 1)
        }
        
        dfs(startRow, startCol)
        return accessibleCells
    }
    
    /**
     * Başlangıç ve bitiş arasındaki yolu bul
     */
    private fun findPathBetweenStartAndEnd(
        maze: Array<Array<MazeCell>>, 
        startRow: Int, 
        startCol: Int, 
        endRow: Int, 
        endCol: Int
    ): List<Pair<Int, Int>> {
        val size = maze.size
        val visited = Array(size) { BooleanArray(size) }
        val path = mutableListOf<Pair<Int, Int>>()
        
        fun dfs(row: Int, col: Int): Boolean {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return false
            }
            
            visited[row][col] = true
            path.add(Pair(row, col))
            
            if (row == endRow && col == endCol) {
                return true
            }
            
            if (dfs(row + 1, col) || dfs(row - 1, col) || 
                dfs(row, col + 1) || dfs(row, col - 1)) {
                return true
            }
            
            path.removeAt(path.size - 1)
            return false
        }
        
        dfs(startRow, startCol)
        
        // Başlangıç ve bitiş hücrelerini çıkar
        return path.filter { (r, c) -> 
            !(r == startRow && c == startCol) && !(r == endRow && c == endCol) 
        }
    }
    
    /**
     * Tüm soru hücrelerine erişilebilir olduğunu doğrula ve gerekirse düzelt
     */
    private fun ensureAllQuestionCellsAccessible(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int) {
        val size = maze.size
        
        // Erişilebilir hücreleri işaretlemek için kullanılacak
        val visited = Array(size) { BooleanArray(size) }
        
        // DFS ile erişilebilir hücreleri işaretle
        fun dfs(row: Int, col: Int) {
            if (row < 0 || col < 0 || row >= size || col >= size || 
                visited[row][col] || maze[row][col].type == MazeCellType.WALL) {
                return
            }
            
            visited[row][col] = true
            
            dfs(row + 1, col)
            dfs(row - 1, col)
            dfs(row, col + 1)
            dfs(row, col - 1)
        }
        
        // Başlangıç noktasından erişilebilir hücreleri işaretle
        dfs(startRow, startCol)
        
        // Erişilemeyen soru hücrelerini kontrol et ve düzelt
        var changed = false
        
        // Erişilemeyen tüm soru hücrelerini bul
        val unreachableQuestions = mutableListOf<Pair<Int, Int>>()
        for (row in 1 until size - 1) {
            for (col in 1 until size - 1) {
                if (maze[row][col].type == MazeCellType.QUESTION && !visited[row][col]) {
                    unreachableQuestions.add(Pair(row, col))
                }
            }
        }
        
        // Her erişilemeyen soru için en yakın erişilebilir hücreye bir yol oluştur
        for ((row, col) in unreachableQuestions) {
            // En yakın erişilebilir hücreyi bul
            var minDistance = Int.MAX_VALUE
            var nearestRow = -1
            var nearestCol = -1
            
            for (r in 1 until size - 1) {
                for (c in 1 until size - 1) {
                    if (visited[r][c]) {
                        val distance = abs(r - row) + abs(c - col)
                        if (distance < minDistance) {
                            minDistance = distance
                            nearestRow = r
                            nearestCol = c
                        }
                    }
                }
            }
            
            if (nearestRow != -1 && nearestCol != -1) {
                // Erişilebilir en yakın hücreye bir yol oluştur
                var pathRow = row
                var pathCol = col
                
                // Önce yatay, sonra dikey hareket et
                while (pathCol != nearestCol) {
                    pathCol += if (pathCol < nearestCol) 1 else -1
                    
                    // Eğer duvar ise, yol yap (QUESTION, START, END değilse)
                    if (maze[pathRow][pathCol].type == MazeCellType.WALL) {
                        maze[pathRow][pathCol].type = MazeCellType.PATH
                    }
                }
                
                while (pathRow != nearestRow) {
                    pathRow += if (pathRow < nearestRow) 1 else -1
                    
                    // Eğer duvar ise, yol yap
                    if (maze[pathRow][pathCol].type == MazeCellType.WALL) {
                        maze[pathRow][pathCol].type = MazeCellType.PATH
                    }
                }
                
                changed = true
            }
        }
        
        // Eğer değişiklik yapıldıysa, erişilebilirliği tekrar kontrol et
        if (changed) {
            // Bitiş noktasını bul
            var endRow = -1
            var endCol = -1
            for (r in 0 until size) {
                for (c in 0 until size) {
                    if (maze[r][c].type == MazeCellType.END) {
                        endRow = r
                        endCol = c
                        break
                    }
                }
                if (endRow != -1) break
            }
            
            if (endRow != -1 && endCol != -1) {
                ensurePathBetweenStartAndEnd(maze, startRow, startCol, endRow, endCol)
            } else {
                // Eğer bitiş noktası bulunamazsa, tekrar DFS ile yolları kontrol et
                val newVisited = Array(size) { BooleanArray(size) }
                
                fun checkAllReachable(r: Int, c: Int) {
                    if (r < 0 || c < 0 || r >= size || c >= size || 
                        newVisited[r][c] || maze[r][c].type == MazeCellType.WALL) {
                        return
                    }
                    
                    newVisited[r][c] = true
                    
                    checkAllReachable(r + 1, c)
                    checkAllReachable(r - 1, c)
                    checkAllReachable(r, c + 1)
                    checkAllReachable(r, c - 1)
                }
                
                checkAllReachable(startRow, startCol)
                
                // Hala erişilemeyen soru varsa, yeniden düzeltmeyi dene
                for (r in 1 until size - 1) {
                    for (c in 1 until size - 1) {
                        if (maze[r][c].type == MazeCellType.QUESTION && !newVisited[r][c]) {
                            // Bu soruyu erişilebilir bir konuma taşı
                            var relocated = false
                            for (nr in (1 until size - 1).shuffled()) {
                                for (nc in (1 until size - 1).shuffled()) {
                                    if (maze[nr][nc].type == MazeCellType.PATH && newVisited[nr][nc]) {
                                        // Yeni konuma taşı
                                        maze[nr][nc].type = MazeCellType.QUESTION
                                        maze[nr][nc].questionId = maze[r][c].questionId
                                        
                                        // Eski konumu PATH yap
                                        maze[r][c].type = MazeCellType.PATH
                                        maze[r][c].questionId = 0
                                        
                                        relocated = true
                                        break
                                    }
                                }
                                if (relocated) break
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Verilen seviyeye göre matematik soruları oluşturur
     */
    fun generateQuestions(level: MazeLevel): List<MathQuestion> {
        val questions = mutableListOf<MathQuestion>()
        
        // Zorluk seviyesine göre sayı aralıklarını belirle
        val numberRange = when (level.difficultyLevel) {
            1 -> 1..10
            2 -> 1..20
            3 -> 1..30
            4 -> 1..50
            5 -> 1..100
            else -> 1..10
        }
        
        // Seviye 7 ve 8 için özel aralıklar tanımla
        val isLevel7 = level.level == 7
        val isLevel8 = level.level == 8
        val isLevel5 = level.level == 5
        
        // Seviyenin üç katı soru oluştur (soruların tükenmemesi için)
        for (i in 0 until level.questionCount * 3) {
            // Seviye bazlı özel sayı aralıkları
            var num1 = 0
            var num2 = 0
            
            // Seviye 5 (Bölme) özel işlem tipi için ayarlama
            // operationType = 4 yerine 4 değeri kullanılarak sadece bölme işlemleri oluşturulacak
            var operationType = level.operationType
            if (isLevel5) {
                // Sadece bölme işlemlerini zorla
                operationType = 4 // Bölme işlemi tipi 
                
                // Bölme için uygun sayılar oluştur
                num2 = (2..10).random() // Bölen
                val result = (1..10).random() // Bölüm (sonuç)
                num1 = num2 * result // Bölünen
                
                // Bölme sorusu oluştur
                val question = "${num1} ÷ ${num2} = ?"
                val answer = result.toString()
                
                // Seçenekleri oluştur
                val options = mutableListOf<String>()
                options.add(answer)
                
                // 3 yanlış şık ekle
                while (options.size < 4) {
                    val answerInt = answer.toInt()
                    // Yanıltıcı cevaplar için daha geniş bir aralık ve pozitif kontrol
                    val wrongAnswer = (answerInt + (-3..5).filter { it != 0 && (answerInt + it) > 0 }.random()).toString()
                    
                    // Yanlış cevap doğru cevapla aynı olmamalı
                    if (!options.contains(wrongAnswer)) {
                        options.add(wrongAnswer)
                    }
                }
                
                // Seçenekleri karıştır
                options.shuffle()
                
                // Doğru cevabın indeksini bul
                val correctAnswerIndex = options.indexOf(answer)
                
                // Soruyu listeye ekle
                questions.add(MathQuestion(
                    id = i,
                    question = question,
                    options = options,
                    correctAnswerIndex = correctAnswerIndex
                ))
                
                // Bu soruyu tamamladık, döngünün kalanını atla
                continue
            }
            
            if (isLevel7 || isLevel8) {
                // Seviye 7-8 için özel aralıklar
                when (level.operationType) {
                    4 -> {
                        // İleri seviye karışık işlemler için rastgele işlem seç
                        val operation = (1..4).random()
                        
                        when (operation) {
                            1, 2 -> { // Toplama, Çıkarma
                                if (isLevel7) {
                                    // Seviye 7: Toplama/Çıkarma: 20-100
                                    num1 = (20..100).random()
                                    num2 = (20..100).random()
                                } else {
                                    // Seviye 8: Toplama/Çıkarma: 50-200
                                    num1 = (50..200).random()
                                    num2 = (50..200).random()
                                }
                            }
                            3 -> { // Çarpma
                                if (isLevel7) {
                                    // Seviye 7: Çarpma: 1-20
                                    num1 = (1..20).random()
                                    num2 = (1..20).random()
                                } else {
                                    // Seviye 8: Çarpma: 1-30
                                    num1 = (1..30).random()
                                    num2 = (1..30).random()
                                }
                            }
                            4 -> { // Bölme
                                if (isLevel7) {
                                    // Seviye 7: Bölme: 1-50
                                    num2 = (1..10).random() // Bölen
                                    val result = (1..5).random() // Bölüm
                                    num1 = num2 * result // Bölünen
                                } else {
                                    // Seviye 8: Bölme: 1-100
                                    num2 = (1..20).random() // Bölen
                                    val result = (1..5).random() // Bölüm
                                    num1 = num2 * result // Bölünen
                                }
                            }
                        }
                        
                        // İleri seviye işlemler için belirlenmiş işlem türünü kullanıyoruz
                        operationType = operation
                    }
                }
            } else if (level.level == 4) {
                // Seviye 4 (Çarpma) için 1-5 arası sayılar
                num1 = (1..5).random()
                num2 = (1..5).random()
            } else if (level.level == 6) {
                // Seviye 6: Çarpma-Bölme karışık (1-12 arası)
                if (level.operationType == 6 && (1..2).random() == 2) { // Bölme sorusu
                    // Bölme için uygun sayılar oluştur
                    val result = (1..5).random() // Bölüm (sonuç)
                    val divisor = (1..10).random() // Bölen
                    num1 = result * divisor // Bölünen
                    num2 = divisor
                    operationType = 4 // Bölme işlemi
                } else {
                    // Çarpma için sayılar
                    num1 = (1..12).random()
                    num2 = (1..12).random()
                    operationType = 3 // Çarpma işlemi
                }
            } else {
                // Diğer seviyeler için normal sayı aralıkları
                num1 = numberRange.random()
                num2 = numberRange.random()
                
                // Çıkarma ve diğer işlemler için pozitif sonuç garantisi
                if (operationType == 2 || (operationType == 5 && (1..2).random() == 2)) {
                    // Çıkarma işlemi için ayarla
                    val larger = kotlin.math.max(num1, num2)
                    val smaller = kotlin.math.min(num1, num2)
                    num1 = larger
                    num2 = smaller
                }
            }
            
            // İşlem tipini belirle (seviye 5, 7 ve 8 için özel durumlar yukarıda halledildi)
            if (level.operationType == 5) {
                // Toplama-Çıkarma karışık
                operationType = if (kotlin.random.Random.nextBoolean()) 1 else 2
            } else if (level.operationType == 6) {
                // Çarpma-Bölme karışık
                // Bu durum yukarıda ele alındı
            } else if (level.level != 5 && level.level != 7 && level.level != 8) {
                // Diğer standart işlemler için operationType belirleme
                operationType = when (level.operationType) {
                    0 -> 1               // Toplama işlemleri
                    1 -> 2               // Çıkarma işlemleri
                    2 -> 3               // Çarpma işlemleri
                    3 -> (1..3).random() // Karışık işlemler (toplama, çıkarma, çarpma)
                    4 -> (1..4).random() // İleri seviye işlemler (toplama, çıkarma, çarpma, bölme)
                    else -> 1            // Varsayılan toplama
                }
            }
            
            // Soru ve cevabı oluştur
            val (question, answer) = when (operationType) {
                1 -> Pair("${num1} + ${num2} = ?", (num1 + num2).toString())
                2 -> {
                    // Çıkarma işlemi için negatif sonuç olmamasını sağla
                    val larger = kotlin.math.max(num1, num2)
                    val smaller = kotlin.math.min(num1, num2)
                    Pair("${larger} - ${smaller} = ?", (larger - smaller).toString())
                }
                3 -> {
                    // Çarpma için gerekli ayarlamalar
                    val factor1 = if (level.level <= 4) (1..5).random() else num1
                    val factor2 = if (level.level <= 4) (1..5).random() else num2
                    Pair("${factor1} × ${factor2} = ?", (factor1 * factor2).toString())
                }
                4 -> {
                    // Bölme işlemi için tam sonuç olmasını sağla
                    // Özel durumlar yukarıda ele alındı, standart durum için:
                    if (num1 == 0 || num2 == 0) {
                        // Sıfır durumu için güvenli değerler
                        val divisor = 2 // Bölen
                        val result = 5 // Sonuç (bölüm)
                        val dividend = divisor * result // Bölünen
                        Pair("${dividend} ÷ ${divisor} = ?", result.toString())
                    } else {
                        // Bölme işlemini düzelt - her zaman tam bölünebilen sayılar oluştur
                        val divisor = num2.coerceAtLeast(1) // Bölen, en az 1 olmalı
                        val result = (1..10).random() // Sonuç (bölüm)
                        val dividend = divisor * result // Bölünen - sonucun tam sayı olmasını sağla
                        Pair("${dividend} ÷ ${divisor} = ?", result.toString())
                    }
                }
                else -> Pair("${num1} + ${num2} = ?", (num1 + num2).toString())
            }
            
            // Seçenekleri oluştur
            val options = mutableListOf<String>()
            options.add(answer)
            
            // 3 yanlış şık ekle
            while (options.size < 4) {
                val wrongAnswer = when (operationType) {
                    1 -> (num1 + num2 + (-5..5).filter { it != 0 }.random()).toString()
                    2 -> {
                        val larger = kotlin.math.max(num1, num2)
                        val smaller = kotlin.math.min(num1, num2)
                        (larger - smaller + (-5..5).filter { it != 0 }.random()).toString()
                    }
                    3 -> {
                        val factor1 = if (level.difficultyLevel <= 3) num1 else num1
                        val factor2 = if (level.difficultyLevel <= 3) num2 else num2
                        (factor1 * factor2 + (-10..10).filter { it != 0 }.random()).toString()
                    }
                    4 -> {
                        // Bölme için yanıltıcı cevaplar
                        if (answer.isNotEmpty()) {
                            try {
                                val answerInt = answer.toInt()
                                (answerInt + (-3..3).filter { it != 0 }.random()).toString()
                            } catch (e: Exception) {
                                // Herhangi bir dönüşüm hatası olursa basit bir yanıltıcı cevap oluştur
                                (1..10).random().toString()
                            }
                        } else {
                            // Cevap boşsa basit bir yanıltıcı cevap oluştur
                            (1..10).random().toString()
                        }
                    }
                    else -> (num1 + num2 + (-5..5).filter { it != 0 }.random()).toString()
                }
                
                // Yanlış cevap doğru cevapla aynı olmamalı
                if (!options.contains(wrongAnswer) && wrongAnswer != answer) {
                    options.add(wrongAnswer)
                }
            }
            
            // Seçenekleri karıştır
            options.shuffle()
            
            // Doğru cevabın indeksini bul
            val correctAnswerIndex = options.indexOf(answer)
            
            // Soruyu listeye ekle
            questions.add(MathQuestion(
                id = i,
                question = question,
                options = options,
                correctAnswerIndex = correctAnswerIndex
            ))
        }
        
        return questions
    }
    
    /**
     * Rastgele matematik sorusu oluşturur
     */
    fun generateRandomQuestion(id: Int, difficultyLevel: Int): MathQuestion {
        try {
            // Zorluk seviyesine göre sayı aralığını belirle
            val maxNumber = when (difficultyLevel) {
                1 -> 10
                2 -> 20
                3 -> 50
                4 -> 100
                else -> 20 // Varsayılan
            }
            
            // Rastgele işlemi seç
            val operationType = Random.nextInt(4)
            
            // İşleme göre soruyu oluştur
            val (question, correctAnswer) = when (operationType) {
                0 -> { // Toplama
                    val a = Random.nextInt(1, maxNumber)
                    val b = Random.nextInt(1, maxNumber)
                    Pair("$a + $b = ?", (a + b).toString())
                }
                1 -> { // Çıkarma
                    val b = Random.nextInt(1, maxNumber / 2)
                    val a = Random.nextInt(b, maxNumber)
                    Pair("$a - $b = ?", (a - b).toString())
                }
                2 -> { // Çarpma
                    val a = Random.nextInt(1, (maxNumber / 10) + 1)
                    val b = Random.nextInt(1, (maxNumber / 10) + 1)
                    Pair("$a × $b = ?", (a * b).toString())
                }
                else -> { // Bölme
                    val b = Random.nextInt(1, 10).coerceAtLeast(1) // 0'a bölünmeyi önle
                    val result = Random.nextInt(1, (maxNumber / 10).coerceAtLeast(1))
                    val a = b * result
                    Pair("$a ÷ $b = ?", result.toString())
                }
            }
            
            // Doğru cevabı ve rastgele yanlış cevapları oluştur
            val options = mutableListOf<String>()
            options.add(correctAnswer)
            
            // Sayısal değer olduğunu garantile
            val correctInt = correctAnswer.toIntOrNull() ?: 0
            
            // 3 adet yanlış cevap oluştur
            var attempts = 0
            while (options.size < 4 && attempts < 20) {
                attempts++
                val wrongAnswer = when (operationType) {
                    0 -> (correctInt + Random.nextInt(-5, 6)).toString()
                    1 -> (correctInt + Random.nextInt(-5, 6)).toString()
                    2 -> (correctInt + Random.nextInt(-10, 11)).toString()
                    else -> (correctInt + Random.nextInt(-3, 4)).toString()
                }
                
                // Yanlış cevap doğru cevapla aynı olmamalı ve pozitif olmalı
                val wrongInt = wrongAnswer.toIntOrNull() ?: continue
                if (wrongInt != correctInt && wrongInt > 0 && !options.contains(wrongAnswer)) {
                    options.add(wrongAnswer)
                }
            }
            
            // 4 seçenek oluşturulamadıysa, manuel ekle
            while (options.size < 4) {
                val extraOption = (correctInt + options.size).toString()
                if (!options.contains(extraOption)) {
                    options.add(extraOption)
                }
            }
            
            // Seçenekleri karıştır
            val shuffledOptions = options.shuffled()
            val correctAnswerIndex = shuffledOptions.indexOf(correctAnswer)
            
            // Doğru cevabın seçenekler arasında olduğundan emin ol
            if (correctAnswerIndex == -1) {
                // Eğer doğru cevap listeye eklenemediyse, ilk seçeneği doğru yap
                return MathQuestion(
                    id = id,
                    question = question,
                    options = listOf(correctAnswer, options[0], options[1], options[2]),
                    correctAnswerIndex = 0
                )
            }
            
            return MathQuestion(
                id = id,
                question = question,
                options = shuffledOptions,
                correctAnswerIndex = correctAnswerIndex
            )
        } catch (e: Exception) {
            // Hata durumunda basit bir soru döndür
            return MathQuestion(
                id = id,
                question = "1 + 1 = ?",
                options = listOf("1", "2", "3", "4"),
                correctAnswerIndex = 1
            )
        }
    }
} 