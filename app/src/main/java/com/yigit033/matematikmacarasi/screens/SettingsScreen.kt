package com.yigit033.matematikmacarasi.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.NotificationService
import com.yigit033.matematikmacarasi.services.PremiumCodeValidator
import com.yigit033.matematikmacarasi.services.payments.PremiumFeaturePolicy
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.settings.IconSelectorActivity
import com.yigit033.matematikmacarasi.utils.AppVersionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * S3.3.1 — a single design system for Settings.
 * Every section paints its own contrasting surface: no white-on-white content.
 */
private object SettingsPalette {
    val deep = Color(0xFF071F38)
    val ocean = Color(0xFF0D3556)
    val panel = Color(0xFF133E5E)
    val border = Color(0xFF316785)
    val mint = Color(0xFF9CF3DC)
    val gold = Color(0xFFFFD47B)
    val text = Color(0xFFF3FAFF)
    val secondary = Color(0xFFB5D2E3)
}

@Composable
private fun SettingsSection(
    title:String,
    subtitle:String,
    modifier:Modifier=Modifier,
    content:@Composable ()->Unit
) {
    Surface(
        modifier=modifier.fillMaxWidth(),
        color=SettingsPalette.panel,
        shape=RoundedCornerShape(22.dp),
        border=BorderStroke(1.dp,SettingsPalette.border)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text(title,color=SettingsPalette.text,fontSize=18.sp,
                fontWeight=FontWeight.ExtraBold)
            if(subtitle.isNotBlank()) Text(subtitle,color=SettingsPalette.secondary,
                fontSize=12.sp,lineHeight=17.sp)
            content()
        }
    }
}

@Composable
private fun SettingsAction(
    label:String,
    onClick:()->Unit,
    modifier:Modifier=Modifier,
    prominent:Boolean=false,
    enabled:Boolean=true
) {
    Button(onClick=onClick,enabled=enabled,
        modifier=modifier.fillMaxWidth().heightIn(min=50.dp),
        shape=RoundedCornerShape(15.dp),
        colors=ButtonDefaults.buttonColors(
            containerColor=if(prominent)SettingsPalette.gold else Color(0xFF245C7E),
            contentColor=if(prominent)SettingsPalette.deep else Color.White,
            disabledContainerColor=Color(0xFF3D6075),
            disabledContentColor=Color(0xFFB6C6D4))
    ) {
        Text(label,fontWeight=FontWeight.Bold,fontSize=13.sp)
    }
}

@Composable
private fun FeatureLine(text:String,emphasized:Boolean=false) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top,
        horizontalArrangement=Arrangement.spacedBy(9.dp)) {
        Icon(Icons.Default.CheckCircle,contentDescription=null,
            tint=if(emphasized)SettingsPalette.gold else SettingsPalette.mint,
            modifier=Modifier.size(17.dp))
        Text(text,color=SettingsPalette.text,fontSize=13.sp,lineHeight=18.sp,
            modifier=Modifier.weight(1f))
    }
}

@Composable
private fun SettingsToggle(
    title:String,description:String,checked:Boolean,onChange:(Boolean)->Unit
) {
    Row(Modifier.fillMaxWidth().heightIn(min=64.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
            Text(title,color=SettingsPalette.text,fontSize=15.sp,
                fontWeight=FontWeight.Bold)
            Text(description,color=SettingsPalette.secondary,fontSize=12.sp,
                lineHeight=16.sp)
        }
        Switch(checked=checked,onCheckedChange=onChange,
            colors=SwitchDefaults.colors(
                checkedTrackColor=Color(0xFF2DAD96),
                checkedThumbColor=Color.White,
                uncheckedTrackColor=Color(0xFF49677C),
                uncheckedThumbColor=Color.White))
    }
}

@Composable
fun SettingsScreen(onBackClick:()->Unit = {},audioService:AudioService) {
    val context=LocalContext.current
    val prefs=remember(context) {
        context.getSharedPreferences("matematik_macerasi_ayarlar",Context.MODE_PRIVATE)
    }
    val purchaseManager=remember(context){PurchaseManager.getInstance(context)}
    val premiumFlow by purchaseManager.premiumState.collectAsState()
    val restoration by purchaseManager.restoreState.collectAsState()
    val isPremium=premiumFlow || purchaseManager.isPremiumSync()
    val codeValidator=remember(context){PremiumCodeValidator.getInstance(context)}
    val notificationService=remember(context){NotificationService.getInstance(context)}
    val scope=rememberCoroutineScope()
    var music by remember { mutableStateOf(audioService.isMusicEnabled()) }
    var effects by remember { mutableStateOf(audioService.isSoundEnabled()) }
    var notifications by remember {
        mutableStateOf(prefs.getBoolean("notifications_enabled",true))
    }
    var codeDialog by remember {mutableStateOf(false)}
    var code by remember {mutableStateOf("")}
    var verifying by remember {mutableStateOf(false)}
    var codeError by remember {mutableStateOf<String?>(null)}

    LaunchedEffect(Unit) {
        purchaseManager.checkPremiumStatusAsync()
        runCatching {
            withContext(Dispatchers.IO){codeValidator.checkAndSyncPremiumStatus()}
        }
        purchaseManager.refreshPremiumState()
    }

    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            SettingsPalette.ocean,SettingsPalette.deep,Color(0xFF06182C))))
        .verticalScroll(rememberScrollState())
        .padding(horizontal=16.dp,vertical=15.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp)) {

        Row(Modifier.fillMaxWidth().heightIn(min=54.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier=Modifier.size(46.dp).clickable(onClick=onBackClick),
                color=Color(0xFF215979),
                shape=CircleShape,
                border=BorderStroke(1.dp,SettingsPalette.border)) {
                Box(contentAlignment=Alignment.Center){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription="Profile dön",
                        tint=Color.White,modifier=Modifier.size(24.dp))
                }
            }
            Column(Modifier.weight(1f)) {
                Text("Ayarlar",fontSize=27.sp,color=Color.White,
                    fontWeight=FontWeight.ExtraBold)
                Text("Macera deneyimini kişiselleştir",
                    color=SettingsPalette.secondary,fontSize=12.sp)
            }
        }

        Surface(color=Color(0xFF1D5274),shape=RoundedCornerShape(24.dp),
            border=BorderStroke(1.dp,SettingsPalette.gold.copy(alpha=.65f))) {
            Column(Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Surface(color=SettingsPalette.gold,shape=CircleShape,
                        modifier=Modifier.size(48.dp)) {
                        Box(contentAlignment=Alignment.Center) {
                            Icon(Icons.Default.Star,contentDescription=null,
                                tint=SettingsPalette.deep,modifier=Modifier.size(27.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(if(isPremium)"Premium Aktif" else "Macera Premium",
                            color=SettingsPalette.gold,fontSize=19.sp,
                            fontWeight=FontWeight.Black)
                        Text(if(isPremium)
                            "Ek mağazalar ve kişiselleştirme erişimin açık"
                            else "Daha fazla mağaza ve seçenek keşfet",
                            color=SettingsPalette.text,fontSize=12.sp,
                            lineHeight=16.sp)
                    }
                }
                if(isPremium) {
                    Text("✓ Mevcut üyeliğin korunuyor. Tekrar satın alma gerekmez.",
                        color=SettingsPalette.mint,fontSize=12.sp)
                } else {
                    SettingsAction("Premium Özellikleri Aç",prominent=true,
                        onClick={ (context as? ComponentActivity)?.let {
                            purchaseManager.buyPremium(it)
                        } })
                    OutlinedButton(onClick={codeDialog=true},
                        modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                        border=BorderStroke(1.dp,SettingsPalette.gold),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=SettingsPalette.gold)) {
                        Text("Premium Kodum Var")
                    }
                }
            }
        }

        SettingsSection("Free ve Premium","Nelerin ücretsiz olduğunu açıkça gör") {
            Text("ÜCRETSİZ",color=SettingsPalette.mint,
                fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
            FeatureLine("Pazar: ${PremiumFeaturePolicy.marketShopCount(false)} mağaza, 12 matematik kararı")
            FeatureLine("Temel oyunlar ve öğrenme raporu")
            FeatureLine("Standart avatar ve uygulama ikonu")
            HorizontalDivider(color=SettingsPalette.border)
            Text("PREMIUM",color=SettingsPalette.gold,
                fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
            FeatureLine("Pazar: ${PremiumFeaturePolicy.marketShopCount(true)} mağaza, 40 matematik kararı",true)
            FeatureLine("Kendi Tezgâhım: stok, satış ve kâr",true)
            FeatureLine("Özel avatarlar ve alternatif uygulama ikonları",true)
            FeatureLine("Labirentte sınırsız oyun ve mevcut günlük bonuslar",true)
            Text("Çocuk güvenliği denetimi süresince reklamlar tüm oyuncular için kapalıdır.",
                color=SettingsPalette.secondary,fontSize=11.sp,lineHeight=16.sp)
        }

        SettingsSection("Satın Alma ve Geri Yükleme",
            "Google Play satın alımını ücretsiz olarak yeniden kontrol et") {
            SettingsAction(
                if(restoration is PurchaseManager.RestoreState.Loading)
                    "Satın Alma Kontrol Ediliyor…" else "Satın Alımları Geri Yükle",
                enabled=restoration !is PurchaseManager.RestoreState.Loading,
                onClick={purchaseManager.restorePurchases()})
            val resultText=when(val state=restoration) {
                is PurchaseManager.RestoreState.Idle ->
                    "Mevcut Premium kodu hakların da korunur."
                is PurchaseManager.RestoreState.Loading ->
                    "Google Play hesabı kontrol ediliyor…"
                is PurchaseManager.RestoreState.Found ->
                    if(state.googlePlay)"Google Play Premium satın alımın bulundu."
                    else "Premium kod hakkın etkin."
                is PurchaseManager.RestoreState.NotFound ->
                    "Bu Google Play hesabında aktif satın alım bulunamadı."
                is PurchaseManager.RestoreState.Error -> state.message
            }
            Text(resultText,color=SettingsPalette.secondary,fontSize=12.sp,
                lineHeight=17.sp)
        }

        SettingsSection("Ses ve Müzik","Oynarken nasıl duymak istediğini seç") {
            SettingsToggle("Müzik","Arka plan müziği",music) { enabled ->
                audioService.setMusicEnabled(enabled)
                music=enabled
            }
            HorizontalDivider(color=SettingsPalette.border)
            SettingsToggle("Ses efektleri","Doğru, yanlış ve ödül sesleri",effects) {
                audioService.setSoundEnabled(it)
                effects=it
            }
        }

        SettingsSection("Bildirimler","Günlük görev hatırlatıcılarını yönet") {
            SettingsToggle("Bildirimler","Günlük görevler için hatırlatma",notifications) {
                notifications=it
                prefs.edit().putBoolean("notifications_enabled",it).apply()
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            if(it) {
                                notificationService.createNotificationChannels()
                                notificationService.loadNotificationSettings()
                            } else {
                                notificationService.cancelAllNotifications()
                            }
                        }
                    } catch(e:Exception) {
                        Toast.makeText(context,"Bildirim ayarı güncellenemedi.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,
                verticalAlignment=Alignment.CenterVertically) {
                Text("Hatırlatma saati",color=SettingsPalette.secondary,fontSize=12.sp)
                Text("19.00",color=SettingsPalette.mint,
                    fontWeight=FontWeight.Bold,fontSize=13.sp)
            }
            Text("Bildirim göndermek için telefon izinlerinin açık olması gerekir.",
                color=SettingsPalette.secondary,fontSize=11.sp)
        }

        SettingsSection("Görünüm ve İkon",
            "Robot, uzay veya klasik uygulama simgesi") {
            SettingsAction("Uygulama İkonunu Değiştir") {
                context.startActivity(Intent(context,IconSelectorActivity::class.java))
            }
        }

        SettingsSection("Uygulama Hakkında","") {
            Text("Matematik Macerası  ·  ${AppVersionUtils.getFullVersionInfo(context)}",
                color=SettingsPalette.text,fontSize=13.sp)
            Text("Geliştirici: YGT Studio",color=SettingsPalette.secondary,fontSize=12.sp)
        }
        Spacer(Modifier.height(20.dp))
    }

    if(codeDialog) {
        AlertDialog(
            onDismissRequest={if(!verifying){codeDialog=false;code="";codeError=null}},
            title={Text("Premium Kodunu Etkinleştir")},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text("Sana verilen Premium kodunu gir. Yeni satın alma yapılmaz.")
                    OutlinedTextField(
                        value=code,onValueChange={code=it.trim().take(64).uppercase();codeError=null},
                        singleLine=true,enabled=!verifying,
                        label={Text("Premium kod")},modifier=Modifier.fillMaxWidth())
                    if(verifying) CircularProgressIndicator(modifier=Modifier.size(22.dp))
                    codeError?.let {Text(it,color=Color(0xFFC42B39),fontSize=12.sp)}
                }
            },
            confirmButton={
                TextButton(onClick={
                    verifying=true;codeError=null
                    scope.launch {
                        try {
                            val result=withContext(Dispatchers.IO){
                                codeValidator.validateAndActivateCode(code)
                            }
                            when(result){
                                is PremiumCodeValidator.ValidationResult.Success,
                                is PremiumCodeValidator.ValidationResult.AlreadyActivated -> {
                                    purchaseManager.refreshPremiumState()
                                    codeDialog=false;code=""
                                    Toast.makeText(context,"Premium erişimin etkin.",
                                        Toast.LENGTH_SHORT).show()
                                }
                                is PremiumCodeValidator.ValidationResult.InvalidCode ->
                                    codeError="Kod geçersiz. Lütfen kontrol et."
                                is PremiumCodeValidator.ValidationResult.CodeExpired ->
                                    codeError="Kodun süresi dolmuş."
                                is PremiumCodeValidator.ValidationResult.CodeLimitReached ->
                                    codeError="Kod kullanım sınırına ulaşılmış."
                                is PremiumCodeValidator.ValidationResult.NetworkError ->
                                    codeError="İnternet bağlantısını kontrol et."
                                is PremiumCodeValidator.ValidationResult.Error ->
                                    codeError=result.message
                                else -> codeError="Kod doğrulanamadı."
                            }
                        } catch(e:Exception) {
                            codeError="Bağlantı veya doğrulama hatası. Tekrar dene."
                        } finally {verifying=false}
                    }
                },enabled=!verifying&&code.isNotBlank()){
                    Text("Kodu Doğrula")
                }
            },
            dismissButton={TextButton(
                onClick={codeDialog=false;code="";codeError=null},
                enabled=!verifying){Text("Vazgeç")}}
        )
    }
}
