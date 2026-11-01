package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.yigit033.matematikmacarasi.DailyTask
import com.yigit033.matematikmacarasi.DailyTaskType
import com.yigit033.matematikmacarasi.GameType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.HashSet

/**
 * Günlük görevleri yöneten servis sınıfı.
 * Singleton pattern kullanılarak uygulamanın her yerinden erişilebilir.
 */
class DailyTaskManager private constructor(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("daily_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "DailyTaskManager"
    
    companion object {
        @Volatile
        private var instance: DailyTaskManager? = null
        
        fun getInstance(context: Context): DailyTaskManager {
            return instance ?: synchronized(this) {
                instance ?: DailyTaskManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Bugünün tarihini al
    private fun getCurrentDateKey(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    // Bugün için görevleri al
    fun getTodayTasks(): List<DailyTask> {
        val currentDateKey = getCurrentDateKey()
        val lastGeneratedDate = sharedPreferences.getString("last_generated_date", "")
        
        // Eğer bugün için görev oluşturulmamışsa veya tarih değiştiyse yeni görevler oluştur
        if (lastGeneratedDate != currentDateKey) {
            val tasks = generateDailyTasks()
            saveTodayTasks(tasks)
            EventBus.updateDailyTasks(tasks)
            return tasks
        }
        
        // Kaydedilmiş görevleri al
        val tasksJson = sharedPreferences.getString("tasks_$currentDateKey", null)
        return if (tasksJson != null) {
            try {
                gson.fromJson(tasksJson, Array<DailyTask>::class.java).toList()
            } catch (e: Exception) {
                Log.e(TAG, "Görevler yüklenirken hata: ${e.message}")
                val tasks = generateDailyTasks()
                saveTodayTasks(tasks)
                EventBus.updateDailyTasks(tasks)
                tasks
            }
        } else {
            val tasks = generateDailyTasks()
            saveTodayTasks(tasks)
            EventBus.updateDailyTasks(tasks)
            tasks
        }
    }
    
    // Görevleri kaydet
    fun saveTodayTasks(tasks: List<DailyTask>) {
        val currentDateKey = getCurrentDateKey()
        val editor = sharedPreferences.edit()
        val tasksJson = gson.toJson(tasks)
        
        editor.putString("tasks_$currentDateKey", tasksJson)
        editor.putString("last_generated_date", currentDateKey)
        editor.apply()
        
        Log.d(TAG, "Görevler kaydedildi: $currentDateKey, ${tasks.size} görev")
    }
    
    // Görev ilerlemesini güncelle
    fun updateTaskProgress(taskId: Int) {
        val tasks = getTodayTasks().toMutableList()
        val taskIndex = tasks.indexOfFirst { it.id == taskId }
        
        if (taskIndex != -1) {
            val task = tasks[taskIndex]
            if (task.currentCount < task.targetCount) {
                val newCount = task.currentCount + 1
                tasks[taskIndex] = task.copy(
                    currentCount = newCount,
                    isCompleted = newCount >= task.targetCount
                )
                saveTodayTasks(tasks)
                EventBus.updateDailyTasks(tasks.toList())
                Log.d(TAG, "Görev ilerleme güncellendi: ID=$taskId, Yeni İlerleme=${task.currentCount + 1}/${task.targetCount}")
            }
        } else {
            Log.w(TAG, "Görev bulunamadı: ID=$taskId")
        }
    }
    
    /**
     * Tek oyun bitişinde tek ilerleme kaydı: ID eşleşiyorsa o görev,
     * yoksa mevcut davranışla oyun türünün açık görevleri ilerletilir.
     */
    fun recordCompletedGameTask(gameType: GameType, taskId: Int = -1) {
        val matching = getTodayTasks().firstOrNull {
            it.id == taskId && it.gameType == gameType &&
                !it.isCompleted && it.currentCount < it.targetCount
        }
        if (matching != null) updateTaskProgress(matching.id)
        else updateTaskProgressByGameType(gameType)
    }

    /**
     * Belirli bir oyun türü için ilerlemeyi güncelle
     * Bu fonksiyon, bir oyun bittiğinde ilgili oyun türüne ait görevlerin ilerlemesini günceller.
     * @param gameType Oynanan oyun türü (GameType enum tipinde)
     */
    fun updateTaskProgressByGameType(gameType: GameType) {
        val tasks = getTodayTasks().toMutableList()
        var updated = false
        
        tasks.forEachIndexed { index, task ->
            // Sadece ilgili oyun türüyle eşleşen ve henüz tamamlanmamış görevleri güncelle
            if (task.gameType == gameType && task.currentCount < task.targetCount) {
                val newCount = task.currentCount + 1
                val isNowCompleted = newCount >= task.targetCount
                
                tasks[index] = task.copy(
                    currentCount = newCount,
                    isCompleted = isNowCompleted
                )
                updated = true
                Log.d(TAG, "Oyun türüne göre görev ilerlemesi: ID=${task.id}, Tip=$gameType, Yeni İlerleme=$newCount/${task.targetCount}, Tamamlandı=$isNowCompleted")
            }
        }
        
        if (updated) {
            saveTodayTasks(tasks)
            EventBus.updateDailyTasks(tasks) // Güncel listeyi EventBus üzerinden yayınla
            Log.d(TAG, "$gameType oyunu tamamlandı, ilgili görevler güncellendi ve UI'a yayınlandı.")
        } else {
            Log.d(TAG, "$gameType oyunu tamamlandı, ancak güncellenecek aktif görev bulunamadı.")
        }
    }
    
    // Matematik problemleri tamamlama sayısını artır
    fun incrementMathProblemsCompleted(count: Int = 1) {
        val tasks = getTodayTasks().toMutableList()
        
        // Matematik problemi çözme görevlerini bul
        tasks.forEachIndexed { index, task ->
            if (task.type == DailyTaskType.ANY) {
                val newCount = Math.min(task.currentCount + count, task.targetCount)
                tasks[index] = task.copy(currentCount = newCount)
                Log.d(TAG, "Matematik problemi ilerleme güncellendi: ID=${task.id}, Yeni İlerleme=$newCount/${task.targetCount}")
            }
        }
        
        saveTodayTasks(tasks)
    }
    
    // Oyun oynama sayısını artır
    fun incrementGamesPlayed() {
        val tasks = getTodayTasks().toMutableList()
        
        // Oyun oynama görevlerini bul
        tasks.forEachIndexed { index, task ->
            if (task.type == DailyTaskType.ANY) {
                val newCount = Math.min(task.currentCount + 1, task.targetCount)
                tasks[index] = task.copy(currentCount = newCount)
                Log.d(TAG, "Oyun oynama ilerleme güncellendi: ID=${task.id}, Yeni İlerleme=$newCount/${task.targetCount}")
            }
        }
        
        saveTodayTasks(tasks)
    }
    
    // 2 veya daha fazla yıldızla tamamlanan oyun sayısını artır
    fun incrementGamesCompletedWithTwoOrMoreStars() {
        val tasks = getTodayTasks().toMutableList()
        
        // 2+ yıldızlı oyun tamamlama görevlerini bul
        tasks.forEachIndexed { index, task ->
            if (task.type == DailyTaskType.ANY) {
                val newCount = Math.min(task.currentCount + 1, task.targetCount)
                tasks[index] = task.copy(currentCount = newCount)
                Log.d(TAG, "Yıldızlı oyun tamamlama ilerleme güncellendi: ID=${task.id}, Yeni İlerleme=$newCount/${task.targetCount}")
            }
        }
        
        saveTodayTasks(tasks)
    }
    
    // Rastgele günlük görevler üret
    private fun generateDailyTasks(): List<DailyTask> {
        val random = Random()
        val tasks = mutableListOf<DailyTask>()
        val availableGameTypes = GameType.values().toMutableList() // Shape now opens playable Geometry Castle
        val usedGameTypes = HashSet<GameType>()
        var taskId = 1
        
        // Günlük görev sayısı: 5-7 arasında rastgele
        val taskCount = random.nextInt(3) + 5
        
        // Görevleri oluştur
        while (tasks.size < taskCount && availableGameTypes.isNotEmpty()) {
            // Kullanılabilir oyun türlerinden rastgele birini seç
            val gameTypeIndex = random.nextInt(availableGameTypes.size)
            val gameType = availableGameTypes[gameTypeIndex]
            
            // Bu oyun türü daha önce kullanıldıysa atla (Bu kontrol Set ile yapıldığı için aslında gereksiz ama güvenlik için kalabilir)
            // if (usedGameTypes.contains(gameType)) continue // Bu satır kaldırılabilir
            
            // Hedef sayısını belirle - oyun tipine göre farklı aralıklar kullan
            val targetCount = when (gameType) {
                // Temel oyunlar için daha yüksek sayılar (1-3)
                GameType.ADDITION_GAME,
                GameType.SUBTRACTION_GAME,
                GameType.MULTIPLICATION_GAME,
                GameType.DIVISION_GAME -> random.nextInt(3) + 1 // 1, 2 veya 3
                
                // İnteraktif oyunlar için daha az sayı (1-2)
                GameType.BALLOON_POP_GAME,
                GameType.NUMBER_HUNT_GAME,
                GameType.MATH_MAZE_GAME -> random.nextInt(2) + 1 // 1 veya 2
                
                // Zorlu oyunlar için daha az sayı (sadece 1)
                GameType.SPACE_MATH_ADVENTURE,
                GameType.SHAPE_RECOGNITION,
                GameType.MARKET_SIMULATOR -> 1 // Market simülasyonu için tek görev
            }
            
            // Oyuna göre görev açıklamasını oluştur
            val description = when (gameType) {
                GameType.ADDITION_GAME ->
                    if (targetCount == 1) "Bir toplama oyunu oyna"
                    else "$targetCount toplama oyunu oyna"
                    
                GameType.SUBTRACTION_GAME ->
                    if (targetCount == 1) "Bir çıkarma oyunu oyna"
                    else "$targetCount çıkarma oyunu oyna"
                    
                GameType.MULTIPLICATION_GAME ->
                    if (targetCount == 1) "Bir çarpma oyunu oyna"
                    else "$targetCount çarpma oyunu oyna"
                    
                GameType.DIVISION_GAME ->
                    if (targetCount == 1) "Bir bölme oyunu oyna"
                    else "$targetCount bölme oyunu oyna"
                    
                GameType.BALLOON_POP_GAME ->
                    if (targetCount == 1) "Bir balon patlatma oyunu oyna"
                    else "$targetCount balon patlatma oyunu oyna"
                    
                GameType.NUMBER_HUNT_GAME ->
                    if (targetCount == 1) "Bir sayı avı oyunu oyna"
                    else "$targetCount sayı avı oyunu oyna"
                    
                GameType.MATH_MAZE_GAME ->
                    if (targetCount == 1) "Bir matematik labirenti oyunu oyna"
                    else "$targetCount matematik labirenti oyunu oyna"
                    
                GameType.SPACE_MATH_ADVENTURE ->
                    "Uzay matematik macerası oyna"
                    
                GameType.SHAPE_RECOGNITION ->
                    "Şekil tanıma oyunu oyna"
                    
                GameType.MARKET_SIMULATOR ->
                    if (targetCount == 1) "Bir market simülasyonu oyunu oyna"
                    else "$targetCount market simülasyonu oyunu oyna"
            }
            
            // Göreve verilecek ödül, görevin zorluğuna göre ayarla
            val reward = when (gameType) {
                // Temel işlemler - normal ödül
                GameType.ADDITION_GAME,
                GameType.SUBTRACTION_GAME -> 8 * targetCount // Ödülü biraz azalttık
                
                // Çarpma, bölme - biraz daha fazla
                GameType.MULTIPLICATION_GAME,
                GameType.DIVISION_GAME -> 10 * targetCount
                
                // İnteraktif oyunlar - orta seviye ödül
                GameType.BALLOON_POP_GAME,
                GameType.NUMBER_HUNT_GAME -> 12 * targetCount
                
                // Labirent - daha yüksek ödül
                GameType.MATH_MAZE_GAME -> 15 * targetCount
                
                // Uzay ve Şekil - en yüksek ödül
                GameType.SPACE_MATH_ADVENTURE,
                GameType.SHAPE_RECOGNITION,
                GameType.MARKET_SIMULATOR -> 20 * targetCount // Tek oyun olduğu için ödül yüksek
            }
            
            // Yeni görevi oluştur
            val newTask = DailyTask(
                id = taskId++,
                type = gameType.toDailyTaskType(), // Oyun türüne göre DailyTaskType belirle
                description = description,
                targetCount = targetCount,
                currentCount = 0, // Başlangıçta 0
                reward = reward,
                gameType = gameType, // İlgili oyun türünü sakla
                isCompleted = false // Başlangıçta tamamlanmamış
            )
            
            tasks.add(newTask)
            usedGameTypes.add(gameType) // Kullanılan oyun türünü sete ekle
            availableGameTypes.removeAt(gameTypeIndex) // Kullanılan türü listeden çıkar
            
            Log.d(TAG, "Görev eklendi: ID=${newTask.id}, Tip=${newTask.gameType}, Hedef=${newTask.targetCount}")
        } // while döngüsü sonu
        
        // Genel "Herhangi Bir" görevini kaldırdık.
        
        Log.d(TAG, "Toplam ${tasks.size} adet benzersiz görev oluşturuldu.")
        return tasks.toList() // Oluşturulan görev listesini döndür
    }
    
    // GameType için uygun DailyTaskType döndür
    private fun getDailyTaskTypeForGameType(gameType: GameType): DailyTaskType {
        return when (gameType) {
            GameType.ADDITION_GAME -> DailyTaskType.ADDITION
            GameType.SUBTRACTION_GAME -> DailyTaskType.SUBTRACTION
            GameType.MULTIPLICATION_GAME -> DailyTaskType.MULTIPLICATION
            GameType.DIVISION_GAME -> DailyTaskType.DIVISION
            GameType.NUMBER_HUNT_GAME, 
            GameType.SHAPE_RECOGNITION -> DailyTaskType.MATCHING
            GameType.MATH_MAZE_GAME -> DailyTaskType.COMPARISON
            else -> DailyTaskType.ANY
        }
    }
    
    // Ödülleri topla
    fun claimRewards(): Int {
        val tasks = getTodayTasks()
        var totalRewards = 0
        
        // Tamamlanan görevlerin ödüllerini hesapla
        for (task in tasks) {
            if (task.currentCount >= task.targetCount) {
                totalRewards += task.reward
            }
        }
        
        // Ödül bilgisini kaydet
        val currentDateKey = getCurrentDateKey()
        val editor = sharedPreferences.edit()
        editor.putBoolean("rewards_claimed_$currentDateKey", true)
        editor.apply()
        
        Log.d(TAG, "Ödüller toplandı: $totalRewards jeton")
        return totalRewards
    }
    
    // Ödüllerin toplanıp toplanmadığını kontrol et
    fun areRewardsClaimed(): Boolean {
        val currentDateKey = getCurrentDateKey()
        return sharedPreferences.getBoolean("rewards_claimed_$currentDateKey", false)
    }
    
    // Tüm görevlerin tamamlandı mı kontrolü
    fun areAllTasksCompleted(): Boolean {
        val tasks = getTodayTasks()
        // Eğer hiç görev yoksa, tamamlanmış sayılır
        if (tasks.isEmpty()) return true
        // Aksi halde tüm görevlerin isCompleted bayrağını kontrol et
        return tasks.all { it.isCompleted }
    }
    
    // Tamamlanmış görevlerin ödüllerini topla (Bu fonksiyon ödül vermez, sadece hesaplar)
    // Ödül verme işlemi StarManager üzerinden yapılmalı.
    
    // Tamamlanmış görevlerden kazanılan toplam yıldız sayısını getirir.
    fun getTotalStarsFromCompletedTasks(): Int {
        return try {
            val tasks = getTodayTasks()
            tasks.filter { it.isCompleted }.sumOf { it.reward }
        } catch (e: Exception) {
            Log.e(TAG, "Tamamlanan görevlerden yıldız hesaplanamadı", e)
            0
        }
    }
    
    // GameType enum'unu DailyTaskType enum'una dönüştürmek için yardımcı fonksiyon
    private fun GameType.toDailyTaskType(): DailyTaskType {
        return when (this) {
            GameType.ADDITION_GAME -> DailyTaskType.ADDITION
            GameType.SUBTRACTION_GAME -> DailyTaskType.SUBTRACTION
            GameType.MULTIPLICATION_GAME -> DailyTaskType.MULTIPLICATION
            GameType.DIVISION_GAME -> DailyTaskType.DIVISION
            GameType.BALLOON_POP_GAME -> DailyTaskType.ANY // Veya spesifik bir tip atanabilir
            GameType.NUMBER_HUNT_GAME -> DailyTaskType.MATCHING
            GameType.MATH_MAZE_GAME -> DailyTaskType.COMPARISON // Veya ANY
            GameType.SPACE_MATH_ADVENTURE -> DailyTaskType.ANY
            GameType.SHAPE_RECOGNITION -> DailyTaskType.MATCHING
            GameType.MARKET_SIMULATOR -> DailyTaskType.ANY
        }
    }
} 