# Matematik Macerası Oyun Geliştirme Rehberi

Bu rehber, Matematik Macerası uygulamasına yeni oyunlar eklenirken izlenmesi gereken standartları ve adımları açıklar.

## İçindekiler

1. [Giriş](#1-giriş)
2. [BaseGameActivity Kullanımı](#2-basegameactivity-kullanımı)
3. [Oyun ID Formatı](#3-oyun-id-formatı)
4. [İstatistik Kaydı](#4-i̇statistik-kaydı)
5. [VALID_GAME_IDS Listesini Güncelleme](#5-valid_game_ids-listesini-güncelleme)
6. [Yeni Bir Temel İşlem Oyunu Ekleme](#6-yeni-bir-temel-i̇şlem-oyunu-ekleme)
7. [Yeni Bir İnteraktif Oyun Ekleme](#7-yeni-bir-i̇nteraktif-oyun-ekleme)
8. [GameStatisticsHelper Kullanımı](#8-gamestatisticshelper-kullanımı)
9. [İstatistiklerin Doğru Hesaplanması](#9-i̇statistiklerin-doğru-hesaplanması)
10. [Kontrol Listesi](#10-kontrol-listesi)
11. [Sorun Giderme](#11-sorun-giderme)

## 1. Giriş

Matematik Macerası uygulaması iki kategoride oyun içerir:

- **Temel İşlem Oyunları**: Toplama, Çıkarma, Çarpma, Bölme
- **İnteraktif Oyunlar**: Balon Patlatma, Sayı Avı, Matematik Labirenti, vb.

Her oyunun istatistiklerinin doğru şekilde kaydedilmesi ve gösterilmesi için bazı standartlara uyulması gerekir.

## 2. BaseGameActivity Kullanımı

Tüm matematik oyunları için `BaseGameActivity` sınıfını temel alın. Bu sınıf, oyun istatistiklerini kaydetme, yıldız hesaplama ve günlük görevleri yönetme gibi ortak işlevleri sağlar.

```kotlin
class YeniOyunActivity : BaseGameActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Oyun kurulumu
    }
    
    // BaseGameActivity'den gelen metodları override et
}
```

Eğer `BaseGameActivity`'den türetemiyorsanız, `GameStatisticsHelper` sınıfını kullanarak istatistikleri kaydedin:

```kotlin
private lateinit var gameStatisticsHelper: GameStatisticsHelper
private var gameStartTime: Long = 0L

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // GameStatisticsHelper'ı başlat
    gameStatisticsHelper = GameStatisticsHelper(this)
    gameStartTime = System.currentTimeMillis()
}
```

### 2.1 BaseGameActivity'nin Önemli Metodları

**endGame() Metodu**:
Oyun bittiğinde bu metodu çağırın. Bu metod içinde şu işlemler yapılır:
- Oyun durumunu günceller (`isGameRunning = false`)
- Arka plan müziğini duraklatır
- Günlük görevleri günceller
- Oyun ilerlemesini kaydeder (`saveGameProgress()`)
- Oyun istatistiklerini kaydeder (`recordGameStatistics()`)

**recordGameStatistics() Metodu**:
Bu metod, oyun istatistiklerini GameStatisticsService'e kaydeder:
- Oyun süresini hesaplar ve doğrular (1-3600 saniye arası)
- `getNormalizedGameId()` ile tutarlı bir oyun ID'si oluşturur
- Oyun ID'si, skor, yıldız ve süre bilgilerini kaydeder
- Tüm işlemleri try-catch bloğu içinde yaparak hata durumunda log kaydı tutar

**getNormalizedGameId() Metodu**:
BaseGameActivity'de tanımlı bu metod, tutarlı bir oyun ID formatı oluşturur:
- Temel işlem oyunları için: `addition_game_level_1` formatında
- İnteraktif oyunlar için: 
  - Seviye 1: `BalloonPopGameActivity` 
  - Seviye > 1: `BalloonPopGameActivity_level_2`

## 3. Oyun ID Formatı

Tüm oyunlar için tutarlı bir ID formatı kullanın. Bu format şöyle olmalıdır:
`oyun_adı_level_X` 

Örnek:
- `addition_game_level_1`
- `balloon_pop_game_level_2`
- `number_hunt_game_level_3`

İnteraktif oyunlar için seviye formatı tutarlı olmalıdır:
- `BalloonPopGameActivity_level_1` (küçük l)
- `BalloonPopGameActivity_Level_1` (büyük L)

Sistem her iki formatı da tanıyacak şekilde güncellenmiştir. Yeni oyunlarda küçük 'l' kullanımını tercih edin.

## 4. İstatistik Kaydı

### 4.1 BaseGameActivity Kullanan Oyunlar
`BaseGameActivity` kullanan oyunlar için, oyun bitiminde `endGame()` metodunu çağırın:

```kotlin
// Oyun bitiminde
val stars = calculateStars() // Oyuna özel yıldız hesaplama
val score = calculateScore() // Oyuna özel puan hesaplama

// currentLevel değişkeni doğru değeri içermelidir
endGame(completed = true, stars = stars, score = score)
```

Bu metod otomatik olarak `recordGameStatistics()` metodunu çağırarak tüm istatistiklerin kaydedilmesini sağlar.

### 4.2 GameStatisticsHelper Kullanan Oyunlar
`GameStatisticsHelper` kullanan oyunlar için, oyun bitiminde `recordGameCompletion()` metodunu çağırın:

```kotlin
// Oyun bitiminde
val stars = calculateStars() // Oyuna özel yıldız hesaplama
val score = calculateScore() // Oyuna özel puan hesaplama

gameStatisticsHelper.recordGameCompletion(
    gameType = GameType.YENI_OYUN,
    level = currentLevel,
    score = score,
    stars = stars,
    gameStartTime = gameStartTime,
    taskId = dailyTaskId // Opsiyonel
)
```

`GameStatisticsHelper.recordGameCompletion()` metodu şu işlemleri yapar:
- `getNormalizedGameId()` ile tutarlı bir oyun ID'si oluşturur
- Oyun süresini hesaplar ve doğrular (1-3600 saniye arası)
- GameStatisticsService ile istatistikleri kaydeder
- Günlük görev ilerlemesini günceller

## 5. VALID_GAME_IDS Listesini Güncelleme

Yeni bir oyun eklediğinizde, `GameStatisticsService` sınıfındaki `GAME_IDS` ve `GAME_NAMES` listelerini güncellemeniz gerekir:

```kotlin
// GameStatisticsService.kt içerisinde
private val GAME_IDS = listOf(
    "AdditionGameActivity", "SubtractionGameActivity", 
    "MultiplicationGameActivity", "DivisionGameActivity",
    "BalloonPopGameActivity", "NumberHuntGameActivity", 
    "MathMazeGameActivity", "SpaceMathAdventureActivity",
    "ShapeRecognitionGameActivity", "MarketSimulatorActivity",
    "NewGameActivity", // <-- Yeni oyun buraya eklenir
)

private val GAME_NAMES = mapOf(
    "AdditionGameActivity" to "Toplama Oyunu",
    "SubtractionGameActivity" to "Çıkarma Oyunu",
    // ... diğer oyunlar ...
    "NewGameActivity" to "Yeni Oyun Adı", // <-- Yeni oyun adı buraya eklenir
)
```

## 6. Yeni Bir Temel İşlem Oyunu Ekleme

Temel işlem oyunları için şu adımları izleyin:

1. `GameType` enum'una yeni oyun türünü ekleyin:
   ```kotlin
   enum class GameType {
       ADDITION_GAME,
       // ... diğer oyunlar ...
       NEW_BASIC_GAME
   }
   ```

2. `BaseGameActivity`'den türeyen bir aktivite oluşturun:
   ```kotlin
   class NewBasicGameActivity : BaseGameActivity() {
       override val gameType: GameType = GameType.NEW_BASIC_GAME
       // ... oyun mantığı ...
   }
   ```

3. `GameStatisticsService`'deki `getGameIdString` metodunu güncelleyin:
   ```kotlin
   internal fun getGameIdString(gameType: GameType): String {
       return when (gameType) {
           // ... diğer oyunlar ...
           GameType.NEW_BASIC_GAME -> "NewBasicGameActivity"
       }
   }
   ```

## 7. Yeni Bir İnteraktif Oyun Ekleme

İnteraktif oyunlar için şu adımları izleyin:

1. `GameType` enum'una yeni oyun türünü ekleyin:
   ```kotlin
   enum class GameType {
       // ... diğer oyunlar ...
       NEW_INTERACTIVE_GAME
   }
   ```

2. `BaseGameActivity`'den türeyen bir aktivite oluşturun ya da `GameStatisticsHelper` kullanın:

   **BaseGameActivity kullanımı**:
   ```kotlin
   class NewInteractiveGameActivity : BaseGameActivity() {
       override val gameType: GameType = GameType.NEW_INTERACTIVE_GAME
       // ... oyun mantığı ...
   }
   ```

   veya

   **GameStatisticsHelper kullanımı**:
   ```kotlin
   class NewInteractiveGameActivity : ComponentActivity() {
       private lateinit var gameStatisticsHelper: GameStatisticsHelper
       private val gameType = GameType.NEW_INTERACTIVE_GAME
       private var gameStartTime: Long = 0L
       
       override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           gameStatisticsHelper = GameStatisticsHelper(this)
           gameStartTime = System.currentTimeMillis()
           // ... diğer kodlar ...
       }
       
       // ... oyun mantığı ...
       
       private fun endGame() {
           // ... diğer kodlar ...
           gameStatisticsHelper.recordGameCompletion(
               gameType = gameType,
               level = selectedLevel,
               score = score,
               stars = stars,
               gameStartTime = gameStartTime
           )
       }
   }
   ```

3. `GameStatisticsService`'deki `getGameIdString` metodunu güncelleyin.

## 8. GameStatisticsHelper Kullanımı

`BaseGameActivity`'den türetmeyi tercih etmediğiniz durumlarda, `GameStatisticsHelper` sınıfını kullanarak istatistikleri kaydedebilirsiniz:

```kotlin
// 1. Servisi başlatın
private lateinit var gameStatisticsHelper: GameStatisticsHelper
private var gameStartTime: Long = 0L

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    gameStatisticsHelper = GameStatisticsHelper(this)
    gameStartTime = System.currentTimeMillis()
    // ... diğer kodlar ...
}

// 2. Oyun tamamlandığında istatistikleri kaydedin
private fun finishGame() {
    // ... diğer kodlar ...
    gameStatisticsHelper.recordGameCompletion(
        gameType = gameType,
        level = selectedLevel,
        score = score,
        stars = stars,
        gameStartTime = gameStartTime
    )
}
```

### 8.1 GameStatisticsHelper'ın getNormalizedGameId() Metodu

`GameStatisticsHelper` içinde bulunan bu metod, oyun türü ve seviye parametrelerini alarak tutarlı bir oyun ID formatı oluşturur:

```kotlin
private fun getNormalizedGameId(gameType: GameType, level: Int): String {
    // gameType'ı string formatına çevir
    val gameId = gameStatisticsService.getGameIdString(gameType)
    
    // Temel işlem oyunları için özel format (addition_game_level_1)
    if (isBasicOperationGame(gameType)) {
        val baseId = when (gameType) {
            GameType.ADDITION_GAME -> "addition_game"
            GameType.SUBTRACTION_GAME -> "subtraction_game"
            GameType.MULTIPLICATION_GAME -> "multiplication_game"
            GameType.DIVISION_GAME -> "division_game"
            else -> gameType.toString().lowercase()
        }
        return "${baseId}_level_$level"
    }
    
    // İnteraktif oyunlar için seviye bazlı ID
    return if (level > 1) {
        "${gameId}_level_$level"
    } else {
        gameId
    }
}
```

## 9. İstatistiklerin Doğru Hesaplanması

Profil sayfasında doğru istatistikler göstermek için `GameStatisticsService` sınıfında iki önemli metod vardır:

### 9.1 getTotalStars ve getTotalEarnedStars

Bu metodlar, profil sayfasında toplam yıldız sayısını göstermek için kullanılır. Her iki metod da hem temel oyunlar hem de interaktif oyunlar için tüm seviyelerdeki yıldızları toplar.

```kotlin
// GameStatisticsService.kt içerisinde
fun getTotalStars(): Int {
    // 1. Temel işlem oyunları için her seviyedeki yıldızları toplar
    // 2. İnteraktif oyunlar için ana ID ve seviye bazlı ID'lerdeki yıldızları toplar
}

fun getTotalEarnedStars(): Int {
    // getTotalStars metoduna benzer, ancak harcanan yıldızları da dikkate alır
}
```

**ÖNEMLİ**: Yeni bir oyun eklerken, bu metodları güncellemek **gerekmez**. Metodlar, `GAME_IDS` ve `GAME_NAMES` listelerinde bulunan tüm oyunları otomatik olarak kontrol eder.

### 9.2 Seviye Bazlı ID'ler

İnteraktif oyunlar için, yıldızlar seviye bazlı ID'lerde kaydedilir:

```
BalloonPopGameActivity_level_1
BalloonPopGameActivity_Level_1
```

Sistem her iki yazım şeklini (büyük ve küçük L) kontrol eder. Yeni bir oyun eklerken tutarlı olmaya çalışın, küçük `l` harfiyle `level` kullanımını tercih edin.

### 9.3 Yıldız İstatistikleri

Oyun istatistikleri şu formatları tanır:

- Temel işlem oyunları: `addition_game_level_1`
- İnteraktif oyunlar (ana ID): `BalloonPopGameActivity`
- İnteraktif oyunlar (seviye ID): `BalloonPopGameActivity_level_1`

İstatistik metodları tüm bu formatları analiz ederek toplam yıldızları hesaplar.

## 10. Kontrol Listesi

Yeni bir oyun eklerken bu kontrol listesini kullanın:

- [ ] GameType enum'una yeni oyun türü eklendi
- [ ] GAME_IDS ve GAME_NAMES listelerine yeni oyun eklendi
- [ ] getGameIdString metoduna yeni oyun türü eklendi
- [ ] BaseGameActivity'den türetilmiş veya GameStatisticsHelper kullanılmış
- [ ] Seviye bazlı ID formatı doğru uygulanmış (`oyun_adı_level_X` formatında)
- [ ] DailyTaskManager'da gerekli güncellemeler yapılmış
- [ ] gameStatisticsHelper ve gameStartTime değişkenleri doğru bir şekilde başlatılmış
- [ ] endGame veya recordGameCompletion metodu doğru parametrelerle çağrılmış

## 11. Sorun Giderme

### 11.1 Matematik Labirenti (MathMazeGameActivity)

`MathMazeGameActivity` şu anda BaseGameActivity'den türetilmemiş ve GameStatisticsHelper de kullanmıyor. Bu nedenle istatistik kayıtları standart formatı kullanmaz. Eğer Matematik Labirenti oyunu için istatistiklerde sorun yaşarsanız, aşağıdaki değişiklikleri yapın:

1. MathMazeGameActivity'yi BaseGameActivity'den türetin
2. Veya GameStatisticsHelper ekleyin:
```kotlin
private lateinit var gameStatisticsHelper: GameStatisticsHelper
private var gameStartTime: Long = 0L

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    gameStatisticsHelper = GameStatisticsHelper(this)
    gameStartTime = System.currentTimeMillis()
}

// endGame metodunu güncelle
private fun endGame(completed: Boolean, stars: Int, score: Int) {
    gameStatisticsHelper.recordGameCompletion(
        gameType = GameType.MATH_MAZE_GAME,
        level = currentLevel,
        score = score,
        stars = stars,
        gameStartTime = gameStartTime
    )
}
```

## Yardımcı Kaynaklar

- BaseGameActivity: `app/src/main/java/com/yigit033/matematikmacarasi/activities/BaseGameActivity.kt`
- GameStatisticsService: `app/src/main/java/com/yigit033/matematikmacarasi/services/GameStatisticsService.kt`
- GameStatisticsHelper: `app/src/main/java/com/yigit033/matematikmacarasi/services/GameStatisticsHelper.kt`
- Örnek temel işlem oyunu: `app/src/main/java/com/yigit033/matematikmacarasi/games/basic/AdditionGameActivity.kt`
- Örnek interaktif oyun (BaseGameActivity kullanan): `app/src/main/java/com/yigit033/matematikmacarasi/games/interactive/BalloonPopGameActivity.kt`
- Örnek interaktif oyun (GameStatisticsHelper kullanan): `app/src/main/java/com/yigit033/matematikmacarasi/games/interactive/NumberHuntGameActivity.kt` 