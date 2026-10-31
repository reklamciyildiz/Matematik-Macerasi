package com.yigit033.matematikmacarasi.learning.curriculum

import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy

/**
 * S2.5: 2026-27 kapsamı kaynakla doğrulanmış DAR eşleştirme.
 * Tek bir mini oyunla resmi öğrenme çıktısının bütünü ölçülemez.
 * Oyun verisini "kanıt" değil, "alıştırma ilişkisi" olarak sun.
 */
enum class CurriculumProgram(val display: String) {
    TYMM_2026_27("Türkiye Yüzyılı Maarif Modeli · 2026–27"),
    LEGACY_2026_27("Önceki öğretim programı · 2026–27"),
    NOT_MAPPED("Henüz doğrulanmadı")
}
enum class CurriculumMatch { PRACTICE_RELATED, REVIEW_FROM_PREVIOUS_GRADE, NOT_MAPPED }
data class CurriculumSkillLink(
    val skillId: String,
    val title: String,
    val schoolGrade: Int,
    val program: CurriculumProgram,
    val outcomeCode: String?,
    val outcomeDescription: String,
    val relation: CurriculumMatch,
    val officialSourceUrl: String?,
    val note: String
)

/** Oyunlar için ilk sertifikalı içerik kapsamı, müfredatın tüm kazanımlarını içermez. */
object CurriculumSkillCatalog {
    const val CATALOG_VERSION = "2026-27-pilot-1"
    private const val G1_NUMBERS = "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/45"
    private const val G1_OPERATIONS = "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/99"
    private const val G2_OPERATIONS = "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/164"
    private const val G3_OPERATIONS = "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/264"
    private const val SCOPE = "https://menderes.meb.gov.tr/www/20262027-egitim-ve-ogretim-yili-turkiye-yuzyili-maarif-modeli-temel-egitim-taslak-cerceve-planlari-yayimlandi/icerik/1760/"

    fun programForGrade(grade: Int?): CurriculumProgram = when (grade) {
        1, 2, 3 -> CurriculumProgram.TYMM_2026_27
        4, 8 -> CurriculumProgram.LEGACY_2026_27
        5, 6, 7 -> CurriculumProgram.TYMM_2026_27
        else -> CurriculumProgram.NOT_MAPPED
    }

    val mapping: List<CurriculumSkillLink> = listOf(
        CurriculumSkillLink("count_2", "İkişer ritmik sayma", 1,
            CurriculumProgram.TYMM_2026_27, "MAT.1.1.5",
            "100'e kadar ileriye ve 20'den geriye ritmik sayabilme",
            CurriculumMatch.PRACTICE_RELATED, G1_NUMBERS,
            "Yalnızca ileriye ikişer sayma örüntüsünü çalışır; çıktının tamamı ölçülmez."),
        CurriculumSkillLink("add_10", "10 içinde toplama", 1,
            CurriculumProgram.TYMM_2026_27, "MAT.1.2.1",
            "Günlük yaşamın içerdiği toplama ve çıkarma işlemlerini çözümleyebilme",
            CurriculumMatch.PRACTICE_RELATED, G1_OPERATIONS,
            "Dört kısa toplama sorusu, günlük yaşamda çözümleme becerisini bütünüyle ölçmez."),
        CurriculumSkillLink("count_3", "Üçlü eş gruplama", 2,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.4",
            "Çarpma ve bölmeyi toplama ve çıkarma işlemlerine dayalı çözümleyebilme",
            CurriculumMatch.PRACTICE_RELATED, G2_OPERATIONS,
            "Üçerli nesne grupları ve ritmik sayma; çarpma-bölme çıktısının yalnızca bir bölümünü destekler."),
        CurriculumSkillLink("count_4", "Dörtlü eş gruplama", 2,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.4",
            "Çarpma ve bölmeyi toplama ve çıkarma işlemlerine dayalı çözümleyebilme",
            CurriculumMatch.PRACTICE_RELATED, G2_OPERATIONS,
            "Dörderli grupları ve örüntüleri çalışır; tam çıktı değerlendirmesi değildir."),
        CurriculumSkillLink("add_20", "20 içinde toplama", 2,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.1",
            "Toplama ve çıkarma gerektiren günlük yaşam problemlerini çözebilme",
            CurriculumMatch.PRACTICE_RELATED, G2_OPERATIONS,
            "20 içindeki kısa işlemler, gerçek yaşam problemi çözme çıktısını kısmen destekler."),
        CurriculumSkillLink("multiply_intro", "Çarpmanın anlamı", 2,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.4",
            "Çarpma ve bölmeyi toplama ve çıkarma işlemlerine dayalı çözümleyebilme",
            CurriculumMatch.PRACTICE_RELATED, G2_OPERATIONS,
            "2–5 arası gruplardan tekrarlı toplamaya geçiş; tüm öğrenme çıktısını ölçmez."),
        CurriculumSkillLink("multiply", "Eş gruplarla çarpma", 3,
            CurriculumProgram.TYMM_2026_27, "MAT.3.2.3",
            "Çarpma ve bölme işlemlerinin sonuçlarını muhakeme edebilme",
            CurriculumMatch.PRACTICE_RELATED, G3_OPERATIONS,
            "Kısa çarpma alıştırmaları, muhakemenin bütün süreç bileşenlerini ölçmez."),
        CurriculumSkillLink("count_3", "Üçerli gruplama tekrarı", 3,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.4",
            "Önceki sınıf: çarpma ve bölmeyi toplama ve çıkarmaya dayalı çözümleme",
            CurriculumMatch.REVIEW_FROM_PREVIOUS_GRADE, G2_OPERATIONS,
            "Üçüncü sınıfta önceki yılın becerisini tekrar ettirir; üçüncü sınıf kazanımı diye sunulmaz."),
        CurriculumSkillLink("count_4", "Dörtlü gruplama tekrarı", 3,
            CurriculumProgram.TYMM_2026_27, "MAT.2.2.4",
            "Önceki sınıf: çarpma ve bölmeyi toplama ve çıkarmaya dayalı çözümleme",
            CurriculumMatch.REVIEW_FROM_PREVIOUS_GRADE, G2_OPERATIONS,
            "Üçüncü sınıfta önceki yılın becerisini tekrar ettirir; üçüncü sınıf kazanımı diye sunulmaz.")
    )

    fun forGradeAndSkill(grade: Int?, skill: String): CurriculumSkillLink? =
        mapping.firstOrNull { it.schoolGrade == grade && it.skillId == skill }

    fun displayStatus(grade: Int?, skill: String): String {
        val link = forGradeAndSkill(grade, skill)
        if (link == null) return when (programForGrade(grade)) {
            CurriculumProgram.NOT_MAPPED -> "Sınıf programı doğrulanmadı · serbest tekrar"
            else -> "Resmî çıktı eşleştirmesi yapılmadı · serbest tekrar"
        }
        return when (link.relation) {
            CurriculumMatch.PRACTICE_RELATED -> "${link.outcomeCode} · ilişkili alıştırma"
            CurriculumMatch.REVIEW_FROM_PREVIOUS_GRADE ->
                "${link.outcomeCode} · önceki sınıftan tekrar"
            CurriculumMatch.NOT_MAPPED -> "Serbest tekrar"
        }
    }

    fun gradeScopeNotice(rawGrade: String): String {
        val grade = GradePolicy.parse(rawGrade)
        return when (programForGrade(grade)) {
            CurriculumProgram.TYMM_2026_27 ->
                "2026–27 Maarif Modeli · uygulamadaki içerikler seçilmiş alıştırmalardır."
            CurriculumProgram.LEGACY_2026_27 ->
                "2026–27 önceki program · bu sınıf için resmî kod eşleştirmesi tamamlanmadı."
            CurriculumProgram.NOT_MAPPED ->
                "Sınıf programı doğrulanmadı · serbest tekrar içerikleri."
        }
    }
}
