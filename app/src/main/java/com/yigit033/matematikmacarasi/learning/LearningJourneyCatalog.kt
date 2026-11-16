package com.yigit033.matematikmacarasi.learning

/**
 * P2: short connected learning routes. A route is a curated subset of genuine
 * lessons in the selected grade, not a claim of full national curriculum cover.
 * Every step still passes the existing free/Premium lesson gate on launch.
 */
data class LearningQuest(
    val id: String,
    val grade: Int,
    val title: String,
    val invitation: String,
    val lessonIds: List<String>
) {
    init {
        require(grade in 1..8 && id.isNotBlank())
        require(lessonIds.isNotEmpty() && lessonIds.distinct() == lessonIds)
    }

    fun completedCount(done: (String) -> Boolean): Int = lessonIds.count(done)
    fun nextLesson(done: (String) -> Boolean): String =
        lessonIds.firstOrNull { !done(it) } ?: lessonIds.last()
    fun finished(done: (String) -> Boolean): Boolean = lessonIds.all(done)
}

object LearningJourneyCatalog {
    fun forGrade(grade: Int): List<LearningQuest> {
        require(grade in 1..8)
        val authored = when (grade) {
            2 -> listOf(
                LearningQuest("g2_count_route", 2, "Sayı Kaşifleri",
                    "Onlukları keşfet, ritmi fark et, geriye say.",
                    listOf("g2_place", "count_3", "g2_backward_count")),
                LearningQuest("g2_operations_route", 2, "İşlem Dedektifleri",
                    "Toplama, çıkarma ve eş gruplar arasında bağlantı kur.",
                    listOf("add_20", "g2_inverse_ops", "multiply_intro")),
                LearningQuest("g2_fractions_route", 2, "Eş Parçalar Diyarı",
                    "Yarımı keşfet, dörtte biri oluştur, yeni sorularla dene.",
                    listOf("g2_half", "g2_quarter"))
            )
            3 -> listOf(
                LearningQuest("g3_operations_route", 3, "Problem Çözme Yolculuğu",
                    "Eş grupları çöz, iki adımda problem kur ve paylaş.",
                    listOf("multiply", "g3_story_two_step", "g3_share_equal")),
                LearningQuest("g3_data_route", 3, "Grafik Araştırmacıları",
                    "Veriyi oku, sütunları karşılaştır, farkları açıkla.",
                    listOf("g3_chart", "g3_data_difference")),
                LearningQuest("g3_fractions_route", 3, "Kesir Ustaları",
                    "Üçte bir ve çeyrek arasındaki düşünceyi geliştir.",
                    listOf("g3_third", "g3_quarter_of_set"))
            )
            4 -> listOf(
                LearningQuest("g4_numbers_route", 4, "Büyük Sayı Atölyesi",
                    "Sayıyı çözümle, komşu onluklarını bul, yuvarla.",
                    listOf("g4_large_place", "g4_round_tens")),
                LearningQuest("g4_fractions_route", 4, "Kesirler Laboratuvarı",
                    "Kesirleri modelle, birim parçaları ve eş paydaları karşılaştır.",
                    listOf("g4_fraction", "g4_unit_fraction_compare", "g4_same_denominator")),
                LearningQuest("g4_operations_route", 4, "Çok Adımlı İşlemler",
                    "Gruplarla çarpmadan iki adımlı problem çözümüne ilerle.",
                    listOf("multiply", "g4_two_step"))
            )
            else -> emptyList()
        }
        val valid = LearningPathCatalog.forGrade("$grade. Sınıf")
            .map { it.id }.toSet()
        if (authored.isNotEmpty()) {
            require(authored.all { quest -> quest.lessonIds.all { it in valid } }) {
                "P2 journey references a missing lesson for grade $grade"
            }
            return authored
        }
        // Other grades: real, already authored grade-scoped lessons, not
        // fabricated new content or an unreviewed curriculum ordering.
        val topics = GradeCurriculumCatalog.forGrade(grade).topics
            .filter { it.lessonIds.isNotEmpty() && it.area != TopicArea.REVIEW }
            .take(3)
        return topics.mapIndexed { index, topic ->
            LearningQuest(
                id = "g${grade}_ready_route_${index + 1}",
                grade = grade,
                title = topic.title,
                invitation = "Bu konudaki hazır dersleri sırayla keşfet ve oyunla pekiştir.",
                lessonIds = topic.lessonIds.take(3)
            )
        }
    }
}
