package com.yigit033.matematikmacarasi.learning.report

import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill

/**
 * Deterministic, offline report based only on completed adaptive-learning runs.
 * This is practice telemetry, NOT a curriculum mastery or diagnosis instrument.
 */
data class PracticeSkill(
    val id: AdventureSkill,
    val title: String,
    val region: String,
    val exercise: String
)

data class PracticeEvidence(
    val skill: PracticeSkill,
    val sessions: Int,
    val attempts: Int,
    val errors: Int,
    val hints: Int,
    val tier: String
) {
    val correctAttempts: Int get() = (attempts-errors).coerceAtLeast(0)
    val accuracyPercent: Int? get() =
        if(attempts==0) null else (correctAttempts.toLong()*100L/attempts).toInt()
    val hasEvidence: Boolean get() = sessions>0 && attempts>0
    val enoughToDiscuss: Boolean get() = sessions>=2 && attempts>=8
    val needsPractice: Boolean get() = enoughToDiscuss &&
        (errors.toLong()*100L>=attempts.toLong()*35L || hints>=3)
    val interpretation: String get() = when {
        !hasEvidence -> "Henüz tamamlanmış oyun kaydı yok"
        !enoughToDiscuss -> "İlk oyun kayıtları oluşuyor"
        needsPractice -> "Birlikte tekrar yapmak faydalı olabilir"
        else -> "Oyun içi alıştırmalar tamamlanıyor"
    }
    val nextPractice: String get() = when(skill.id) {
        AdventureSkill.THREE_COUNTING ->
            "Üçer nesne gruplarıyla sayma alıştırmasını tekrar deneyin."
        AdventureSkill.FOUR_COUNTING ->
            "Dörderli gruplar kurup sayı örüntüsünü karşılaştırın."
        AdventureSkill.GEOMETRY ->
            "Şekillerin kenar, köşe ve simetri özelliklerini karşılaştırın."
        AdventureSkill.MARKET ->
            "Ürün miktarı, toplam tutar ve para üstü hesabını oyunla çalışın."
    }
}

data class PracticeOverview(val skills: List<PracticeEvidence>) {
    val completedSessions: Int get() = skills.sumOf { it.sessions }
    val attempts: Int get() = skills.sumOf { it.attempts }
    val errors: Int get() = skills.sumOf { it.errors }
    val hints: Int get() = skills.sumOf { it.hints }
    val practicedSkills: Int get() = skills.count { it.hasEvidence }
    val accuracyPercent: Int? get() =
        if(attempts==0) null else ((attempts.toLong()-errors)*100L/attempts).toInt()
    val suggested: PracticeEvidence? get() =
        skills.firstOrNull { it.needsPractice } ?:
            skills.firstOrNull { !it.hasEvidence } ?:
            skills.firstOrNull { !it.enoughToDiscuss }
}

object LearningReportEngine {
    val coveredSkills = listOf(
        PracticeSkill(AdventureSkill.THREE_COUNTING,
            "Üçerli eş gruplama","Kumsal Koyu","Sayı gruplama"),
        PracticeSkill(AdventureSkill.FOUR_COUNTING,
            "Dörtlü eş gruplama","Gizemli Orman","Sayı örüntüleri"),
        PracticeSkill(AdventureSkill.GEOMETRY,
            "Geometrik şekiller ve simetri","Geometri Kalesi","Geometri"),
        PracticeSkill(AdventureSkill.MARKET,
            "Alışveriş ve para hesabı","Pazar Simülatörü","Para ve ölçme")
    )
    fun build(progress: Map<AdventureSkill, AdaptiveProgress>): PracticeOverview =
        PracticeOverview(coveredSkills.map { skill ->
            val p=progress[skill.id]
            PracticeEvidence(skill,
                sessions=p?.completedRuns?.coerceAtLeast(0)?:0,
                attempts=p?.totalAttempts?.coerceAtLeast(0)?:0,
                errors=(p?.totalErrors?:0).coerceIn(0,(p?.totalAttempts?:0).coerceAtLeast(0)),
                hints=p?.totalHints?.coerceAtLeast(0)?:0,
                tier=p?.currentTier?.title?:"Henüz seçilmedi"
            )
        })
}
