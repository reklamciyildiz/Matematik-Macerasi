package com.yigit033.matematikmacarasi.learning

/** Ö1 planning index, NOT verbatim official MEB outcome codes or finished courses.
 * Academic-year scope and official references: docs/O1_CURRICULUM.md.
 */
enum class InstructionBand { VISUAL_AUDIO, VISUAL_GUIDED, EXPLANATION_DIAGRAM }
enum class TopicArea(val label: String) {
    NUMBERS("Sayılar ve Nicelikler"), OPERATIONS("İşlemler"),
    GEOMETRY("Geometri ve Ölçme"), DATA("Veri ve Olasılık"),
    ALGEBRA("Örüntüler ve Cebir"), REVIEW("Temel Tekrar")
}
data class CurriculumTopic(
    val id: String, val grade: Int, val area: TopicArea,
    val title: String, val summary: String, val outcomes: List<String>,
    val lessonIds: List<String> = emptyList(), val relatedGame: String? = null
) {
    init {
        require(id.isNotBlank() && grade in 1..8 && outcomes.size >= 2)
        require(outcomes.all { it.isNotBlank() })
    }
    val playable: Boolean get() = lessonIds.isNotEmpty()
}
data class GradeCurriculum(
    val grade: Int, val program: String,
    val band: InstructionBand, val topics: List<CurriculumTopic>
) {
    val playableTopics: Int get() = topics.count { it.playable }
}
internal fun curriculumTopic(
    grade: Int, id: String, area: TopicArea, title: String, summary: String,
    outcomeA: String, outcomeB: String,
    lessons: String = "", game: String? = null
): CurriculumTopic = CurriculumTopic(
    "g" + grade + "_" + id, grade, area, title, summary,
    listOf(outcomeA, outcomeB),
    lessons.split(",").filter { it.isNotBlank() }, game
)

object GradeCurriculumCatalog {
    const val SCHOOL_YEAR = "2026–2027"
    fun gradeFromLabel(label: String): Int =
        (label.substringBefore(".").trim().toIntOrNull() ?: 1).coerceIn(1, 8)
    fun programName(grade: Int): String =
        if (grade == 4 || grade == 8) "Önceki öğretim programı"
        else "Türkiye Yüzyılı Maarif Modeli"
    fun band(grade: Int): InstructionBand = when (grade) {
        1, 2 -> InstructionBand.VISUAL_AUDIO
        3, 4 -> InstructionBand.VISUAL_GUIDED
        else -> InstructionBand.EXPLANATION_DIAGRAM
    }
    fun forGrade(grade: Int): GradeCurriculum {
        require(grade in 1..8)
        val topics = if (grade <= 4) primaryTopics(grade) else secondaryTopics(grade)
        val pilotIds = LearningPathCatalog.forGrade("$grade. Sınıf").map { it.id }.toSet()
        require(topics.flatMap { it.lessonIds }.toSet() == pilotIds) {
            "Every pilot must be mapped to its grade's curriculum"
        }
        require(topics.map { it.id }.distinct().size == topics.size)
        return GradeCurriculum(grade, programName(grade), band(grade), topics)
    }
    fun forClass(userClass: String): GradeCurriculum =
        forGrade(gradeFromLabel(userClass))
    fun topicForLesson(grade: Int, lessonId: String): CurriculumTopic? =
        forGrade(grade).topics.firstOrNull { lessonId in it.lessonIds }
}
