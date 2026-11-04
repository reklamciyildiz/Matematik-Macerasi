package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.SubtractionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import com.yigit033.matematikmacarasi.games.basic.OperationsHubActivity
import com.yigit033.matematikmacarasi.games.geometry.GeometryCastleActivity
import com.yigit033.matematikmacarasi.games.fractions.FractionCrystalsActivity
import com.yigit033.matematikmacarasi.games.time.TimeTowerActivity
import com.yigit033.matematikmacarasi.games.pattern.PatternForestActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestAdventureActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity

/** One catalog drives the home carousel, personal recommendation and the full game grid. */
enum class GameTopic(val label:String, val symbol:String, val summary:String) {
    OPERATIONS("Sayılar ve İşlemler","✦","Toplama, çıkarma, çarpma ve bölme"),
    NUMBERS("Sayılar ve Kesirler","◆","Ritmik sayma, eş gruplama ve kesirler"),
    REASONING("Geometri ve Mantık","✧","Şekiller, simetri, ölçme ve örüntüler"),
    REAL_LIFE("Zaman ve Günlük Hayat","◷","Saat, takvim, alışveriş ve bütçe"),
    ADVENTURES("Matematik Maceraları","✦","Hareketli görevler ve keşif oyunları")
}

/** This is a description of the *actual engine*, not a promise of official curriculum coverage. */
enum class GameGradeMode {
    GRADE_1_8, // content is produced from the selected school grade
    GRADE_1_4, // higher profiles replay the grade-4 cap
    FOUNDATION_2, // fixed grade-2 skill, useful for revision
    TIER_ONLY // difficulty is selectable, but grade is not a hard curriculum boundary
}

data class GameCatalogItem(
    val id:String,
    val title:String,
    val description:String,
    val focus:String,
    val topic:GameTopic,
    @DrawableRes val coverRes:Int,
    val accent:Color,
    val activityClass:Class<*>,
    val gradeMode:GameGradeMode,
    val minGrade:Int=1,
    val quickTitle:String=title
) {
    fun isLocked(grade:Int):Boolean = grade < minGrade

    fun isGradeMatched(grade:Int):Boolean =
        !isLocked(grade) && when(gradeMode) {
            GameGradeMode.GRADE_1_8 -> true
            GameGradeMode.GRADE_1_4 -> grade in 1..4
            GameGradeMode.FOUNDATION_2 -> grade == 2
            GameGradeMode.TIER_ONLY -> false
        }

    fun gradeNote(grade:Int):String {
        if(isLocked(grade))return "${minGrade}. sınıfta açılır"
        return when(gradeMode) {
            GameGradeMode.GRADE_1_8 -> when {
                id=="fractions" && grade<=2 -> "$grade. sınıf · görsel keşif"
                id=="time_tower" && grade==1 -> "1. sınıf · saat keşfi"
                else -> "$grade. sınıfa göre sorular"
            }
            GameGradeMode.GRADE_1_4 -> if(grade<=4)
                "$grade. sınıfa göre sorular" else "4. sınıf düzeyi · tekrar"
            GameGradeMode.FOUNDATION_2 -> when {
                grade==2 -> "2. sınıf · eş gruplama"
                grade>2 -> "2. sınıftan tekrar"
                else -> "2. sınıf becerisine giriş"
            }
            GameGradeMode.TIER_ONLY -> "Seviyeni seçerek oyna"
        }
    }
}

object GameCatalog {
    val operationsHub=GameCatalogItem("operations","Dört İşlem Merkezi",
        "Toplama · çıkarma · çarpma · bölme","Dört işlem macerası",
        GameTopic.OPERATIONS,R.drawable.card_four_operations,Color(0xFFFFB74D),
        OperationsHubActivity::class.java,GameGradeMode.GRADE_1_8,
        quickTitle="Dört İşlem\nOyunları")

    val games:List<GameCatalogItem> = listOf(
        GameCatalogItem("add","Toplama","Sayıları birleştir · yıldızları kazan",
            "Toplama işlemi",GameTopic.OPERATIONS,R.drawable.card_addition_premium,
            Color(0xFF49C2FE),AdditionGameActivity::class.java,GameGradeMode.GRADE_1_8),
        GameCatalogItem("subtract","Çıkarma","Farkı keşfet · matematik ustası ol",
            "Çıkarma işlemi",GameTopic.OPERATIONS,R.drawable.card_subtraction_premium,
            Color(0xFFFF83BA),SubtractionGameActivity::class.java,GameGradeMode.GRADE_1_8),
        GameCatalogItem("multiply","Çarpma","Eş gruplarla çarpma",
            "Çarpma işlemi",GameTopic.OPERATIONS,R.drawable.card_multiplication_premium,
            Color(0xFF65E6BA),MultiplicationGameActivity::class.java,GameGradeMode.GRADE_1_8,2),
        GameCatalogItem("divide","Bölme","Eşit paylaş · sonucu bul",
            "Bölme işlemi",GameTopic.OPERATIONS,R.drawable.card_division_premium,
            Color(0xFFFFC768),DivisionGameActivity::class.java,GameGradeMode.GRADE_1_8,2),
        GameCatalogItem("hunt","Sayı Avı","Harita parçalarını bul · hazineyi aç",
            "Üçer ritmik sayma",GameTopic.NUMBERS,R.drawable.card_number_hunt_premium,
            Color(0xFF88F0BA),SayiAvi2Activity::class.java,GameGradeMode.FOUNDATION_2),
        GameCatalogItem("forest","Orman Keşfi","Kadim ormandaki sayıları keşfet",
            "Dörder ritmik sayma",GameTopic.NUMBERS,R.drawable.card_forest_premium,
            Color(0xFF80F0AA),ForestAdventureActivity::class.java,GameGradeMode.FOUNDATION_2),
        GameCatalogItem("fractions","Kesir Kristalleri","Eş parçaları keşfet · kristalleri birleştir",
            "Eşit parçalar · kesirler",GameTopic.NUMBERS,R.drawable.fraction_crystals_hero,
            Color(0xFFAF8BFF),FractionCrystalsActivity::class.java,GameGradeMode.GRADE_1_8,
            quickTitle="Kesir\nKristalleri"),
        GameCatalogItem("geometry","Geometri Kalesi","Şekilleri tanı · kaleyi kurtar",
            "Şekiller · simetri · ölçme",GameTopic.REASONING,
            R.drawable.geometry_castle_hero_premium,Color(0xFF78D6FF),
            GeometryCastleActivity::class.java,GameGradeMode.GRADE_1_8,
            quickTitle="Şekiller &\nGeometri"),
        GameCatalogItem("pattern_forest","Örüntü Ormanı","Büyülü ormanın kurallarını çöz",
            "Örüntüler · akıl yürütme",GameTopic.REASONING,
            R.drawable.pattern_forest_hero,Color(0xFF7FE8B9),
            PatternForestActivity::class.java,GameGradeMode.GRADE_1_8,
            quickTitle="Örüntü\nOrmanı"),
        GameCatalogItem("time_tower","Zaman Kulesi","Saati kur · zamanı ve takvimi keşfet",
            "Saat · takvim · zaman",GameTopic.REAL_LIFE,R.drawable.time_tower_hero,
            Color(0xFFFFD98A),TimeTowerActivity::class.java,GameGradeMode.GRADE_1_8,
            quickTitle="Zaman\nKulesi"),
        GameCatalogItem("market","Pazar Simülatörü","Alışveriş yap · bütçeni hesapla",
            "Para · alışveriş · bütçe",GameTopic.REAL_LIFE,R.drawable.market_adventure_hero,
            Color(0xFF73E4A9),MarketSimulatorActivity::class.java,GameGradeMode.TIER_ONLY,
            quickTitle="Pazar\nSimülatörü"),
        GameCatalogItem("balloon","Balon Patlatma","Uçan doğru balonları yakala",
            "Hızlı işlem alıştırması",GameTopic.ADVENTURES,R.drawable.card_balloon_pop,
            Color(0xFF67ECF1),BalloonPopGameActivity::class.java,GameGradeMode.TIER_ONLY,
            quickTitle="Balon\nPatlatma"),
        GameCatalogItem("space","Uzay Macerası","Geminle matematik meteorlarını vur",
            "İşlem · uzay keşfi",GameTopic.ADVENTURES,R.drawable.card_space_adventure,
            Color(0xFFC4A5FC),SpaceMathAdventureActivity::class.java,GameGradeMode.GRADE_1_4,
            quickTitle="Uzay\nMacerası"),
        GameCatalogItem("maze","Matematik Labirenti","Kapıları aç · çıkışa ilerle",
            "İşlem · mantık · keşif",GameTopic.ADVENTURES,R.drawable.card_maze_premium,
            Color(0xFFBE95FF),MathMazeGameActivity::class.java,GameGradeMode.TIER_ONLY,
            quickTitle="Matematik\nLabirenti")
    )

    fun byId(id:String?):GameCatalogItem? =
        if(id=="operations") operationsHub else games.firstOrNull { it.id==id }

    /** Explicit grade-based suggestions; not described as adaptive mastery analysis. */
    fun suggestionFor(grade:Int, excludingId:String?=null):GameCatalogItem {
        val candidates=when(grade) {
            1 -> listOf("add","time_tower","pattern_forest","geometry")
            2 -> listOf("hunt","geometry","pattern_forest","time_tower","add")
            3 -> listOf("multiply","geometry","fractions","space","pattern_forest")
            4 -> listOf("fractions","divide","geometry","pattern_forest","time_tower")
            else -> listOf("pattern_forest","fractions","time_tower","geometry","divide")
        }
        return candidates.mapNotNull(::byId).firstOrNull {
            it.id != excludingId && it.isGradeMatched(grade)
        } ?: games.first { it.id=="add" }
    }
}

/** Keeps only a game id; does not alter achievements, progress checkpoints or rewards. */
object GameLaunchHistory {
    private const val NAME="game_catalog_recent_v1"
    private const val LAST="last_opened_id"

    fun lastOpenedId(context:Context):String? {
        val id=context.applicationContext.getSharedPreferences(NAME,Context.MODE_PRIVATE)
            .getString(LAST,null)
        return GameCatalog.byId(id)?.id
    }

    fun open(context:Context,game:GameCatalogItem) {
        context.startActivity(Intent(context,game.activityClass))
        context.applicationContext.getSharedPreferences(NAME,Context.MODE_PRIVATE)
            .edit().putString(LAST,game.id).apply()
    }
}
