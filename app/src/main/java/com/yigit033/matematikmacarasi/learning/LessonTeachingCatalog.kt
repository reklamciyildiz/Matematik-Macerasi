package com.yigit033.matematikmacarasi.learning

/** Ö3 teaching scripts, not separate games. Authored for all playable pilot skills. */
enum class TeachingVisual {
    GROUPS, EQUAL_GROUPS, NUMBER_STEPS,
    TAKE_AWAY, PLACE_VALUE, FRACTION_BAR, SHAPE, CLOCK,
    BAR_CHART, AREA_GRID, PERIMETER, DECIMAL, EQUATION, FACTORS, POWER_CHAIN,
    SYMMETRY, RIGHT_TRIANGLE, VOLUME_STACK, ANGLE, MULTIPLES
}

data class LessonConceptCheck(
    val prompt: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val wrongHint: String
) {
    init {
        require(choices.size == 2 && choices.distinct().size == 2)
        require(correctIndex in choices.indices)
    }
}

data class LessonTeachingScript(
    val visual: TeachingVisual,
    val headline: String,
    val earlyExplanation: String,
    val laterExplanation: String,
    val groups: List<Int> = emptyList(),
    val sequence: List<Int> = emptyList(),
    val diagramCaption: String,
    val steps: List<String>,
    val check: LessonConceptCheck
) {
    init {
        require(steps.size in 3..5 && steps.all { it.isNotBlank() })
        require(earlyExplanation.isNotBlank() && laterExplanation.isNotBlank())
        when (visual) {
            TeachingVisual.NUMBER_STEPS -> require(sequence.size >= 4 && groups.isEmpty())
            TeachingVisual.ANGLE -> require(
                groups.size == 2 && groups.all { it in 1..180 } && sequence.isEmpty()
            )
            TeachingVisual.GROUPS, TeachingVisual.EQUAL_GROUPS,
            TeachingVisual.TAKE_AWAY, TeachingVisual.PLACE_VALUE,
            TeachingVisual.FRACTION_BAR, TeachingVisual.SHAPE,
            TeachingVisual.CLOCK, TeachingVisual.BAR_CHART,
            TeachingVisual.AREA_GRID, TeachingVisual.PERIMETER,
            TeachingVisual.DECIMAL, TeachingVisual.EQUATION,
            TeachingVisual.FACTORS, TeachingVisual.POWER_CHAIN,
            TeachingVisual.SYMMETRY, TeachingVisual.RIGHT_TRIANGLE,
            TeachingVisual.VOLUME_STACK, TeachingVisual.MULTIPLES ->
                require(groups.size >= 2 && groups.all { it in 1..20 } && sequence.isEmpty())
        }
    }
    fun explanationFor(grade: Int): String =
        if (grade <= 2) earlyExplanation else laterExplanation
    fun visibleGroups(step: Int): Int = (step + 1).coerceIn(1, groups.size)
    fun visibleNumbers(step: Int): Int = (step + 2).coerceIn(1, sequence.size)
}

object LessonTeachingCatalog {
    fun forLesson(lesson: Lesson): LessonTeachingScript = when (lesson.skill) {
        Skill.EXTENDED -> ExpandedLessonTeachingCatalog.forLesson(lesson)
        Skill.TOPIC_STARTER -> TopicStarterCatalog.forLesson(lesson)
        Skill.DEEP_DIVE -> DeepLessonTeachingCatalog.forLesson(lesson)
        else -> forSkill(lesson.skill)
    }

    fun forSkill(skill: Skill): LessonTeachingScript {
        fun check(prompt: String, right: String, wrong: String, explanation: String,
                  hint: String = "Gösterimdeki adımlara tekrar bak."): LessonConceptCheck =
            LessonConceptCheck(prompt, listOf(right, wrong), 0, explanation, hint)
        return when (skill) {
            Skill.ADD_10 -> LessonTeachingScript(
                TeachingVisual.GROUPS, "İki grup bir araya gelince",
                "Önce 3 elmayı say. Sonra 2 elma daha ekle. Kaç elma oldu?",
                "Toplama, iki miktarın bir araya getirilmesidir. 3 ve 2'yi birleştir.",
                groups = listOf(3, 2),
                diagramCaption = "3 + 2 = 5",
                steps = listOf(
                    "Önce ilk gruptaki 3 nesneyi say.",
                    "İkinci gruptaki 2 nesneyi de ekle.",
                    "Birlikte say: 1, 2, 3, 4, 5. Toplam 5 nesne var."
                ),
                check = check(
                    "Toplama yaparken bu iki gruba ne yaparız?",
                    "Gruplardaki nesneleri birleştiririz.",
                    "Bir gruptaki nesneleri eksiltiriz.",
                    "İki grup birleştiğinde 3 + 2 = 5 olur.",
                    "Toplama, eklemek ve birlikte saymak demektir."
                )
            )
            Skill.ADD_20 -> LessonTeachingScript(
                TeachingVisual.GROUPS, "Onluğu tamamlayarak toplama",
                "8'e önce 2 ekle: 10. Sonra kalan 3'ü ekle: 13.",
                "8 + 5 = 8 + 2 + 3 = 13. Beş sayısını 2 ve 3 diye ayır.",
                groups = listOf(8, 2, 3),
                diagramCaption = "8 + 5 = 8 + 2 + 3 = 13",
                steps = listOf(
                    "İlk grupta 8 nesne var.",
                    "Beşin içinden önce 2 ekle; 8 + 2 = 10.",
                    "Geriye kalan 3 nesneyi ekle; 10 + 3 = 13."
                ),
                check = check(
                    "8 + 5 işlemini kolaylaştırmak için önce ne yapabiliriz?",
                    "8'in üzerine 2 ekleyip 10 yaparız.",
                    "8'den 2 çıkarırız.",
                    "Önce 10'u tamamlamak işlemi kolaylaştırır."
                )
            )
            Skill.COUNT_2 -> countingScript(2)
            Skill.COUNT_3 -> countingScript(3)
            Skill.COUNT_4 -> countingScript(4)
            Skill.MULTIPLY_INTRO, Skill.MULTIPLY -> LessonTeachingScript(
                TeachingVisual.EQUAL_GROUPS, "Eş grupları birlikte say",
                "3 tabak var. Her tabakta 4 nesne var. Hepsini sayalım.",
                "Çarpma tekrarlı toplamanın kısa yazımıdır: 3 eş grup, her birinde 4 nesne.",
                groups = listOf(4, 4, 4),
                diagramCaption = "4 + 4 + 4 = 3 × 4 = 12",
                steps = listOf(
                    "Birinci grupta 4 nesne var.",
                    "İkinci gruptaki 4 nesneyi ekle: 4 + 4 = 8.",
                    "Üçüncü grubu ekle: 8 + 4 = 12.",
                    "Üç tane dörtlü grup: 3 × 4 = 12."
                ),
                check = check(
                    "3 × 4 işlemindeki 3 sayısı neyi gösteriyor?",
                    "Eş grup sayısını.",
                    "Her gruptaki nesne sayısını.",
                    "3 grup var ve her grubun içinde 4 nesne bulunuyor.",
                    "Şekilde kaç ayrı grup olduğunu say."
                )
            )
            Skill.DIVISIBLE_15 -> LessonTeachingScript(
                TeachingVisual.NUMBER_STEPS,
                "3'e ve 5'e birlikte bölünebilme",
                "15'er artan sayıları incele: 15, 30, 45, 60.",
                "3 ve 5'in ortak katları 15'in katlarıdır. Bu sayılar ikisine de kalansız bölünür.",
                sequence = listOf(0, 15, 30, 45, 60),
                diagramCaption = "15, 30, 45, 60 → 15'in katları",
                steps = listOf(
                    "15 sayısı hem 3'e hem 5'e kalansız bölünür.",
                    "15 daha ekle: 15 + 15 = 30.",
                    "15 daha ekle: 30 + 15 = 45.",
                    "Son bir adım: 45 + 15 = 60."
                ),
                check = check(
                    "Bir sayı hem 3'e hem 5'e bölünüyorsa hangisinin katıdır?",
                    "15'in katıdır.",
                    "Yalnızca 5'in katı olması yeterlidir.",
                    "3 ve 5'in en küçük ortak katı 15'tir.",
                    "15'in katlarını sayı şeridinde gör."
                )
            )
            Skill.EXTENDED, Skill.TOPIC_STARTER, Skill.DEEP_DIVE ->
                error("Use forLesson() for grade-specific lessons")
        }
    }

    private fun countingScript(step: Int): LessonTeachingScript {
        val seq = (0..4).map { it * step }
        return LessonTeachingScript(
            TeachingVisual.NUMBER_STEPS, "$step'er ritmik sayalım",
            "Her adımda $step ileri git. Sayılar sırayla büyür.",
            "Ritmik sayma, her adımda sabit miktar eklemektir. Burada artış $step.",
            sequence = seq,
            diagramCaption = seq.drop(1).joinToString(" → "),
            steps = listOf(
                "Başlangıç noktası 0. $step ileri git: $step.",
                "$step daha ekle: $step + $step = ${step * 2}.",
                "Yine $step ekle: ${step * 2} + $step = ${step * 3}.",
                "Son bir adım: ${step * 3} + $step = ${step * 4}."
            ),
            check = LessonConceptCheck(
                "$step'er sayarken bir sonraki sayıyı nasıl buluruz?",
                listOf("Önceki sayıya $step ekleriz.", "Önceki sayıdan $step çıkarırız."),
                0, "Her yeni sayıya ulaşmak için $step daha ekliyoruz.",
                "Sayı şeridindeki okları incele."
            )
        )
    }
}
