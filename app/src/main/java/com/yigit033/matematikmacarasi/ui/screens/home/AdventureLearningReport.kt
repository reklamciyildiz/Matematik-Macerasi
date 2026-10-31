package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.curriculum.CurriculumSkillCatalog

/**
 * Oyuncunun GERÇEK oturum olayları: akademik başarı testi değildir.
 * Mevcut Premium ve yıldızlardan bağımsız salt okunur öğrenme raporu.
 */
internal data class GameSkillReport(
    val sessions: Int,
    val attempts: Int,
    val errors: Int,
    val hints: Int,
    val tier: String
) {
    val accuracyPercent: Int? get() =
        if (attempts == 0) null else (attempts - errors).coerceAtLeast(0) * 100 / attempts
    val status: String get() = when {
        sessions == 0 || attempts < 8 -> "Başlangıç verisi bekleniyor"
        hints >= 3 || accuracyPercent != null && accuracyPercent!! < 60 ->
            "Tekrar alıştırması önerilir"
        else -> "Oyun içi pratik kaydedildi"
    }
    val recommendation: String get() = when {
        sessions == 0 -> "İlgili macerayı tamamlayarak ilk öğrenme gözlemini oluştur."
        hints >= 3 || errors > attempts / 3 ->
            "Eş gruplarla ve görsel ipuçlarıyla tekrar çalış."
        else -> "Farklı başlangıç sayılarıyla yeni bir oturum dene."
    }

    companion object {
        fun fromProgress(p: AdaptiveProgress): GameSkillReport =
            GameSkillReport(p.completedRuns, p.totalAttempts, p.totalErrors,
                p.totalHints, p.currentTier.title)
    }
}

@Composable
internal fun AdventureLearningReport(userClass: String, refreshKey: Int) {
    val context = LocalContext.current
    val store = remember(context) { AdaptiveLearningStore(context) }
    val grade = remember(userClass) { GradePolicy.parse(userClass) }
    val countThree = remember(refreshKey, userClass) {
        GameSkillReport.fromProgress(store.progress(AdventureSkill.THREE_COUNTING))
    }
    val countFour = remember(refreshKey, userClass) {
        GameSkillReport.fromProgress(store.progress(AdventureSkill.FOUR_COUNTING))
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF112D45), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF51C5BD).copy(alpha = .55f),
                RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text("📊 BECERİ GELİŞİM GÖZLEMLERİ", fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Text(CurriculumSkillCatalog.gradeScopeNotice(userClass),
            fontSize = 11.sp, color = Color(0xFFB8D4E6))
        LearningSkillCard("Üçerli eş gruplama", "Kumsal Koyu",
            CurriculumSkillCatalog.displayStatus(grade, "count_3"), countThree)
        LearningSkillCard("Dörtlü eş gruplama", "Gizemli Orman",
            CurriculumSkillCatalog.displayStatus(grade, "count_4"), countFour)
        Text("Raporlar yalnızca bu cihazdaki oyun oturumlarına dayanır. " +
            "MEB kazanımı tamamlandı, sınıfı geçti veya kalıcı öğrendi anlamına gelmez. " +
            "Önceki sürümlerin oturumları uyarlama geçmişine aktarılmadı.",
            fontSize = 10.sp, color = Color(0xFFBDD5E1), lineHeight = 15.sp)
    }
}

@Composable
private fun LearningSkillCard(
    skill: String,
    region: String,
    curriculum: String,
    report: GameSkillReport
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF21445C),
        border = androidx.compose.foundation.BorderStroke(1.dp,
            Color(0xFF55798A))
    ) {
        Column(modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(skill, fontSize = 14.sp,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text(report.tier, fontSize = 12.sp, color = Color(0xFFFFD98A))
            }
            Text("$region  ·  $curriculum", fontSize = 11.sp,
                color = Color(0xFFBEDCE1), lineHeight = 16.sp)
            Text("${report.sessions} oturum  ·  ${report.attempts} hamle  ·  " +
                "${report.errors} yanlış  ·  ${report.hints} ipucu",
                fontSize = 12.sp, color = Color.White)
            Text("Oyun içi doğru hamle oranı: " +
                (report.accuracyPercent?.let { "$it%" } ?: "Yeterli veri yok"),
                fontSize = 12.sp, fontWeight = FontWeight.Medium,
                color = Color(0xFF9DEBD6))
            Text(report.status, fontSize = 11.sp, color = Color(0xFFFFD98A))
            Text(report.recommendation, fontSize = 11.sp,
                color = Color(0xFFD3EAF0), lineHeight = 16.sp)
        }
    }
}
