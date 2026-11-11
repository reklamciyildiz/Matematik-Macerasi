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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import com.yigit033.matematikmacarasi.services.payments.PremiumPlan
import com.yigit033.matematikmacarasi.BuildConfig
import android.net.Uri
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
    val prices by purchaseManager.planPrices.collectAsState()
    val subscriptionActive by purchaseManager.subscriptionActive.collectAsState()
    // In Cloudflare mode the aggregate StateFlow is NOT a source of truth:
    // it may outlive a revoked code or an expired 24-hour Play subscription.
    // Recalculate current rights using the signed license / Play lifetime cache.
    val isPremium=if(BuildConfig.MM_USE_CLOUDFLARE)
        purchaseManager.isPremiumSync()
    else premiumFlow || purchaseManager.isPremiumSync()
    val codeValidator=remember(context){PremiumCodeValidator.getInstance(context)}
    val incompatibleLicense=codeValidator.hasCloudflareLicenseIgnoredByThisBuild()
    val notificationService=remember(context){NotificationService.getInstance(context)}
    val scope=rememberCoroutineScope()
    var music by remember { mutableStateOf(audioService.isMusicEnabled()) }
    var effects by remember { mutableStateOf(audioService.isSoundEnabled()) }
    var musicVolume by remember { mutableFloatStateOf(audioService.getMusicVolume()) }
    var effectsVolume by remember { mutableFloatStateOf(audioService.getEffectsVolume()) }
    var notifications by remember {
        mutableStateOf(prefs.getBoolean("notifications_enabled",true))
    }
    var codeDialog by remember {mutableStateOf(false)}
    var code by remember {mutableStateOf("")}
    var verifying by remember {mutableStateOf(false)}
    var codeError by remember {mutableStateOf<String?>(null)}
    // Keep the detailed plan comparison hidden until a Free player asks for it.
    var showPlanComparison by remember { mutableStateOf(false) }

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

        Row(Modifier.fillMaxWidth().heightIn(min=62.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Surface(
                modifier=Modifier.size(52.dp).clickable(onClick=onBackClick),
                color=Color(0xFF215979),
                shape=CircleShape,
                border=BorderStroke(1.dp,SettingsPalette.border)) {
                Box(contentAlignment=Alignment.Center){
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription="Ayarlardan profile geri dön",
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
                            "Tüm macera dünyaları ve zorluklar erişimine açık"
                            else "Daha fazla mağaza ve seçenek keşfet",
                            color=SettingsPalette.text,fontSize=12.sp,
                            lineHeight=16.sp)
                    }
                }
                if(incompatibleLicense) {
                    Text("⚠ Bu sürüm eski Cloudflare Premium lisansını okuyamıyor. " +
                        "Kod hakkın silinmiş olmayabilir; lisansı doğrulayan doğru " +
                        "yapılandırılmış APK'yı yükle. Uygulama verilerini temizleme.",
                        color=SettingsPalette.gold,fontSize=12.sp,lineHeight=18.sp)
                }
                if(isPremium) {
                    Text(if(subscriptionActive)
                            "✓ Premium aboneliğin aktif. Bitiş ve yenileme bilgileri Google Play'de."
                        else "✓ Premium erişimin korunuyor. Tekrar satın alma gerekmez.",
                        color=SettingsPalette.mint,fontSize=12.sp)
                    Text("✦ Tüm dersler ve zorluklar  ·  Özel avatarlar  ·  Ayrıntılı macera istatistikleri",
                        color=SettingsPalette.text,fontSize=12.sp,lineHeight=19.sp)
                    if(subscriptionActive) SettingsAction("Aboneliği Google Play'de Yönet",
                        onClick={context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/account/subscriptions")))})
                } else {
                    if(BuildConfig.MM_SUBSCRIPTIONS_ENABLED) {
                        Text("Tek Premium içerik · Üç farklı ödeme seçeneği",
                            color=SettingsPalette.gold,fontWeight=FontWeight.Bold,fontSize=13.sp)
                        PremiumPlan.entries.forEach { plan ->
                            val price=prices[plan]
                            val duration=when(plan) {
                                PremiumPlan.MONTHLY->" / ay · otomatik yenilenir"
                                PremiumPlan.YEARLY->" / yıl · otomatik yenilenir"
                                PremiumPlan.LIFETIME->" · tek ödeme, süresiz"
                            }
                            SettingsAction(
                                plan.label+" · "+(price ?: "Google Play fiyatı yükleniyor")+duration,
                                prominent=plan==PremiumPlan.YEARLY,
                                enabled=price!=null,
                                onClick={ (context as? ComponentActivity)?.let {
                                    purchaseManager.buyPremium(it,plan)
                                } }
                            )
                        }
                        Text("Aylık ve yıllık aynı Premium haklarını sağlar; " +
                            "iptal edilirse erişim ödenmiş sürenin sonunda sona erer. " +
                            "Ömür boyu plan otomatik yenilenmez.",
                            color=SettingsPalette.secondary,fontSize=11.sp,lineHeight=16.sp)
                    } else {
                        val lifetimePrice=prices[PremiumPlan.LIFETIME]
                        SettingsAction(
                            "Ömür Boyu Premium · " +
                                (lifetimePrice ?: "Google Play fiyatı yükleniyor"),
                            prominent=true,
                            enabled=lifetimePrice!=null,
                            onClick={ (context as? ComponentActivity)?.let {
                                purchaseManager.buyPremium(it,PremiumPlan.LIFETIME)
                            } }
                        )
                    }
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

        // Premium players already have everything unlocked. Don't put an
        // exhausting sales comparison under their active-membership card.
        if (!isPremium) {
            SettingsSection("Ücretsiz Macera / Premium",
                "Ücretsiz içeriklerle oyna, hazır olduğunda daha fazlasını keşfet") {
                Text("ÜCRETSİZ MACERAN",color=SettingsPalette.mint,
                    fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
                FeatureLine("Sınıfına uygun Temel oyunlar ve başlangıç dersleri")
                FeatureLine("6 başlangıç avatarı ve temel macera istatistikleri")
                HorizontalDivider(color=SettingsPalette.border)
                Text("PREMIUM İLE AÇILANLAR",color=SettingsPalette.gold,
                    fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
                FeatureLine("Orta, Zor, Çok Zor ve Uzman oyun seçenekleri",true)
                FeatureLine("Tüm öğrenme dersleri ve ayrıntılı oyun istatistikleri",true)
                FeatureLine("Özel avatarlar, Kendi Tezgâhım ve genişletilmiş Pazar",true)
                TextButton(onClick={showPlanComparison = !showPlanComparison},
                    modifier=Modifier.fillMaxWidth()) {
                    Text(if(showPlanComparison) "Ayrıntıları gizle" else "Tüm hakları karşılaştır",
                        color=SettingsPalette.gold,fontWeight=FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight,contentDescription=null,
                        tint=SettingsPalette.gold,modifier=Modifier.size(19.dp))
                }
                if (showPlanComparison) {
                    HorizontalDivider(color=SettingsPalette.border)
                    Text("ÜCRETSİZ",color=SettingsPalette.mint,
                        fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
                    FeatureLine("Dört işlem: seçilen sınıfa uygun Temel zorluk")
                    FeatureLine("Orman Keşfi ve Antik Tapınak öğretici maceraları")
                    FeatureLine("Sayı Avı, Geometri, Kesir, Zaman, Örüntü, Balon ve Uzay: Temel")
                    FeatureLine("Her sınıfta iki farklı konudan ücretsiz başlangıç dersi")
                    FeatureLine("Pazar: ${PremiumFeaturePolicy.marketShopCount(false)} mağaza, 12 karar")
                    FeatureLine("Labirent: günde 5 oyun")
                    FeatureLine("İlk 6 avatar (bazıları yıldızla açılır) ve standart ikon")
                    FeatureLine("Yıldız ve biten oyun özeti")
                    HorizontalDivider(color=SettingsPalette.border)
                    Text("PREMIUM",color=SettingsPalette.gold,
                        fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
                    FeatureLine("Pazar: ${PremiumFeaturePolicy.marketShopCount(true)} mağaza, 40 karar",true)
                    FeatureLine("Öğren: 1–8. sınıf kısa ve derinleşme dersleri",true)
                    FeatureLine("Kendi Tezgâhım: stok, satış ve kâr",true)
                    FeatureLine("Dört işlem ve Karışık Mod: Orta, Zor, Çok Zor, Uzman",true)
                    FeatureLine("Sayı Avı, Geometri, Kesir, Zaman, Örüntü, Balon ve Uzay: Orta ve Zor",true)
                    FeatureLine("9 ek özel avatar (yıldızla açılır) ve alternatif ikonlar",true)
                    FeatureLine("Labirentte sınırsız oyun, oyun bazlı analizler ve günlük bonus",true)
                    Text(if(BuildConfig.MM_SUBSCRIPTIONS_ENABLED)
                            "Aylık/yıllık otomatik yenilenir; ömür boyu Premium tek seferliktir."
                        else "Ömür boyu Premium tek seferlik ödeme ile alınır.",
                        color=SettingsPalette.secondary,fontSize=11.sp,lineHeight=16.sp)
                }
                Text("Çocuk güvenliği denetimi süresince reklamlar tüm oyuncular için kapalıdır.",
                    color=SettingsPalette.secondary,fontSize=11.sp,lineHeight=16.sp)
            }
        }

        SettingsSection("Satın Alma ve Premium Kodunu Geri Yükleme",
            "Ödeme yapmadan Google Play ve mevcut kod hakkını yeniden kontrol et") {
            SettingsAction(
                if(restoration is PurchaseManager.RestoreState.Loading)
                    "Satın Alma Kontrol Ediliyor…" else "Satın Alımları Geri Yükle",
                enabled=restoration !is PurchaseManager.RestoreState.Loading,
                onClick={purchaseManager.restorePurchases()})
            val resultText=when(val state=restoration) {
                is PurchaseManager.RestoreState.Idle ->
                    "Mevcut Premium kodu hakkın da kontrol edilir."
                is PurchaseManager.RestoreState.Loading ->
                    "Google Play hesabı kontrol ediliyor…"
                is PurchaseManager.RestoreState.Found ->
                    if(state.subscription)"Google Play Premium aboneliğin aktif."
                    else if(state.googlePlay)"Google Play ömür boyu satın alımın bulundu."
                    else "Premium kod hakkın etkin."
                is PurchaseManager.RestoreState.NotFound ->
                    "Bu Google Play hesabında aktif satın alım bulunamadı."
                is PurchaseManager.RestoreState.Error -> state.message
            }
            Text(resultText,color=SettingsPalette.secondary,fontSize=12.sp,
                lineHeight=17.sp)
        }

        SettingsSection("Ses ve Müzik","Özgün macera teması, yumuşak geri bildirimler ve oyun sesleri") {
            SettingsToggle("Müzik","Maceranın arka plan teması",music) { enabled ->
                audioService.setMusicEnabled(enabled)
                music=enabled
            }
            Text("Müzik seviyesi  ·  %${(musicVolume * 100).toInt()}",
                color=SettingsPalette.secondary,fontSize=12.sp)
            Slider(
                value=musicVolume,
                onValueChange={ musicVolume=it; audioService.setMusicVolume(it) },
                enabled=music,
                valueRange=0f..1f
            )
            HorizontalDivider(color=SettingsPalette.border)
            SettingsToggle("Ses efektleri","Doğru, yanlış, görev ve zafer sesleri",effects) {
                audioService.setSoundEnabled(it)
                effects=it
            }
            Text("Efekt seviyesi  ·  %${(effectsVolume * 100).toInt()}",
                color=SettingsPalette.secondary,fontSize=12.sp)
            Slider(
                value=effectsVolume,
                onValueChange={ effectsVolume=it; audioService.setEffectsVolume(it) },
                enabled=effects,
                valueRange=0f..1f
            )
            Text("Sesleri tek tek dene",color=SettingsPalette.secondary,
                fontSize=12.sp,fontWeight=FontWeight.Bold)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                listOf(
                    "✓ Doğru" to { audioService.playCorrectSound() },
                    "↻ Tekrar" to { audioService.playWrongSound() },
                    "★ Zafer" to { audioService.playVictorySound() }
                ).forEach { (label, play) ->
                    Button(
                        onClick=play,
                        enabled=effects,
                        modifier=Modifier.weight(1f).heightIn(min=48.dp),
                        contentPadding=PaddingValues(horizontal=4.dp),
                        shape=RoundedCornerShape(12.dp),
                        colors=ButtonDefaults.buttonColors(
                            containerColor=Color(0xFF245C7E)
                        )
                    ) { Text(label,fontSize=11.sp) }
                }
            }
            SettingsAction("🎵 Görev ve bölüm seslerini dinle",
                onClick={ audioService.playSoundTest() },enabled=effects)
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
            "Yeni macera, klasik, robot veya uzay uygulama simgesi") {
            SettingsAction("Uygulama İkonunu Değiştir",
                onClick={
                    context.startActivity(Intent(context,IconSelectorActivity::class.java))
                })
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
                        value=code,onValueChange={code=it.take(128);codeError=null},
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
                                is PremiumCodeValidator.ValidationResult.DifferentCodeAlreadyActivated ->
                                    codeError="Bu cihazda farklı bir Premium kod etkin. Yeni bir kodun aktarımı için destek gerekli."
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
