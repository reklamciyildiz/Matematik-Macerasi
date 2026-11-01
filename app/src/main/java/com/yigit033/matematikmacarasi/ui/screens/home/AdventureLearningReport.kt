package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.curriculum.CurriculumSkillCatalog
import com.yigit033.matematikmacarasi.learning.report.LearningReportEngine
import com.yigit033.matematikmacarasi.learning.report.PracticeEvidence

/** Backward-compatible small report model used by existing JVM tests. */
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

private val reportInk=Color(0xFF112D45)
private val reportPanel=Color(0xFF21445C)
private val reportCyan=Color(0xFF9DEBD6)
private val reportGold=Color(0xFFFFD98A)
private val reportLight=Color(0xFFBDD5E1)

/**
 * S3.2: four actual game skills, student-friendly and parent-readable views.
 * Never uploads a child's events and never asserts curriculum mastery.
 */
@Composable
internal fun AdventureLearningReport(userClass:String,refreshKey:Int) {
    val context=LocalContext.current
    val store=remember(context) { AdaptiveLearningStore(context) }
    val owner=LocalLifecycleOwner.current
    var screenVersion by remember { mutableIntStateOf(0) }
    var parentMode by remember { mutableStateOf(false) }
    DisposableEffect(owner) {
        val observer=LifecycleEventObserver { _,event ->
            if(event==Lifecycle.Event.ON_RESUME) screenVersion++
        }
        owner.lifecycle.addObserver(observer)
        onDispose {owner.lifecycle.removeObserver(observer)}
    }
    val report=remember(refreshKey,screenVersion,userClass) {
        LearningReportEngine.build(
            AdventureSkill.entries.associateWith { store.progress(it) })
    }
    val grade=remember(userClass) {GradePolicy.parse(userClass)}
    Column(
        Modifier.fillMaxWidth()
            .background(reportInk,RoundedCornerShape(20.dp))
            .border(1.dp,Color(0xFF51C5BD).copy(alpha=.65f),RoundedCornerShape(20.dp))
            .padding(15.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Text("📊 ÖĞRENME YOLCULUĞUM",fontSize=19.sp,
            color=Color.White,fontWeight=FontWeight.ExtraBold)
        Text("Oynadığın maceralardaki gerçek alıştırma kayıtlarını gör.",
            fontSize=12.sp,color=reportLight)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={parentMode=false},modifier=Modifier.weight(1f),
                colors=ButtonDefaults.buttonColors(
                    containerColor=if(!parentMode)reportGold else reportPanel,
                    contentColor=if(!parentMode)reportInk else Color.White)) {
                Text("🧒 Çocuk",fontSize=13.sp)
            }
            OutlinedButton(onClick={parentMode=true},modifier=Modifier.weight(1f),
                colors=ButtonDefaults.outlinedButtonColors(
                    contentColor=if(parentMode)reportGold else Color.White),
                border=androidx.compose.foundation.BorderStroke(
                    if(parentMode) 2.dp else 1.dp,if(parentMode)reportGold else reportLight)) {
                Text("👪 Ebeveyn",fontSize=13.sp)
            }
        }
        if(!parentMode) {
            Text("Keşfedilen beceriler: ${report.practicedSkills}/${report.skills.size}",
                color=reportGold,fontSize=18.sp,fontWeight=FontWeight.Bold)
            Text("Henüz oynamadığın dünyalar da burada görünecek.",
                color=reportLight,fontSize=12.sp)
            report.skills.forEach {item ->
                Surface(shape=RoundedCornerShape(15.dp),color=reportPanel,
                    border=androidx.compose.foundation.BorderStroke(
                        1.dp,Color(0xFF47758C))) {
                    Column(Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text(item.skill.title,color=Color.White,fontSize=15.sp,
                            fontWeight=FontWeight.Bold)
                        Text(item.skill.region+" · "+item.tier,
                            color=reportCyan,fontSize=12.sp)
                        val message=if(!item.hasEvidence) "Henüz oynanmadı"
                            else "${item.sessions} keşif tamamladın!"
                        Text(message,color=reportGold,fontSize=13.sp,
                            fontWeight=FontWeight.Medium)
                        if(item.hasEvidence) {
                            LinearProgressIndicator(
                                progress={ (item.accuracyPercent ?: 0)/100f },
                                modifier=Modifier.fillMaxWidth(),
                                color=reportCyan,
                                trackColor=Color(0xFF376277))
                            Text("Doğru deneme oranı: ${item.accuracyPercent}%",
                                fontSize=11.sp,color=reportLight)
                        }
                        Text(if(item.needsPractice) "Biraz daha alıştırma yapalım."
                            else "İstersen bu dünyada yeniden keşfe çık.",
                            fontSize=12.sp,color=Color.White)
                    }
                }
            }
            Text("Yıldızlar ve oyun puanları öğrenme düzeyini tek başına göstermez.",
                fontSize=11.sp,color=reportLight)
        } else {
            Text("EBEVEYN İÇİN OYUN İÇİ GÖZLEMLER",
                color=reportGold,fontSize=15.sp,fontWeight=FontWeight.Bold)
            Text(CurriculumSkillCatalog.gradeScopeNotice(userClass),
                color=reportLight,fontSize=11.sp)
            Text("${report.completedSessions} tamamlanan oyun oturumu · " +
                "${report.attempts} deneme · ${report.errors} yanlış · " +
                "${report.hints} ipucu",
                color=Color.White,fontSize=13.sp)
            Text("Toplam doğru deneme oranı: "+
                (report.accuracyPercent?.let{"$it%"}?:"Henüz veri yok"),
                color=reportCyan,fontSize=14.sp,fontWeight=FontWeight.Bold)
            report.skills.forEach {item ->
                ParentSkillEvidence(item,
                    if(item.skill.id==AdventureSkill.THREE_COUNTING)
                        CurriculumSkillCatalog.displayStatus(grade,"count_3")
                    else if(item.skill.id==AdventureSkill.FOUR_COUNTING)
                        CurriculumSkillCatalog.displayStatus(grade,"count_4")
                    else "Resmî kazanım eşleştirmesi yapılmadı · oyun içi pratik")
            }
            report.suggested?.let {item->
                Surface(color=Color(0xFF29595F),
                    shape=RoundedCornerShape(15.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        Text("Evde birlikte deneyebilirsiniz",color=reportGold,
                            fontWeight=FontWeight.Bold,fontSize=14.sp)
                        Text(item.nextPractice,color=Color.White,fontSize=13.sp)
                    }
                }
            }
            Text("Bu sayılar yalnızca bu cihazda, tamamlanan ve kayıtlı oyun " +
                "oturumlarına dayanır. Eski sürümlerdeki kaydedilmemiş oturumlar " +
                "dahil değildir. Öğrencinin okul başarısını, kalıcı öğrenmeyi, " +
                "MEB kazanımı tamamlamasını veya tanıyı kanıtlamaz. " +
                "Veriler otomatik olarak öğretmene ya da sunucuya gönderilmez.",
                fontSize=11.sp,lineHeight=16.sp,color=reportLight)
        }
    }
}

@Composable
private fun ParentSkillEvidence(item:PracticeEvidence,curriculum:String) {
    Surface(modifier=Modifier.fillMaxWidth(),color=reportPanel,
        shape=RoundedCornerShape(15.dp),
        border=androidx.compose.foundation.BorderStroke(1.dp,Color(0xFF47758C))) {
        Column(Modifier.padding(12.dp),
            verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(item.skill.title,color=Color.White,fontSize=14.sp,
                fontWeight=FontWeight.Bold)
            Text("${item.skill.region} · ${item.tier}",color=reportGold,fontSize=12.sp)
            Text(curriculum,color=reportLight,fontSize=11.sp)
            Text("${item.sessions} oturum · ${item.attempts} deneme · " +
                "${item.errors} yanlış · ${item.hints} ipucu",
                color=Color.White,fontSize=12.sp)
            Text("Doğru deneme oranı: "+
                (item.accuracyPercent?.let{"$it%"}?:"Henüz veri yok"),
                color=reportCyan,fontSize=12.sp)
            Text(item.interpretation,color=reportGold,fontSize=12.sp)
            Text(item.nextPractice,color=reportLight,fontSize=12.sp)
        }
    }
}
