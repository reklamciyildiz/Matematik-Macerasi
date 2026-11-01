package com.yigit033.matematikmacarasi.games.interactive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

private val cream = Color(0xFFFFF5DC)
private val ink = Color(0xFF2C3C4C)
private val turquoise = Color(0xFF51E4CB)
private val orange = Color(0xFFFFD87D)
private val dark = Color(0xFF123E5D)
private val panel = Color(0xFF215775)

@Composable private fun MarketAction(
    text:String,enabled:Boolean=true,onClick:()->Unit
) {
    Button(
        onClick=onClick,enabled=enabled,
        modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
        shape=RoundedCornerShape(16.dp),
        colors=ButtonDefaults.buttonColors(
            containerColor=orange,contentColor=ink,
            disabledContainerColor=Color(0xFF4D7186),
            disabledContentColor=Color(0xFFE5EEEE))
    ) { Text(text,fontSize=16.sp,fontWeight=FontWeight.ExtraBold) }
}

@Composable private fun MarketScene(drawable:Int,label:String,big:Boolean=false) {
    Box(Modifier.fillMaxWidth().height(if(big) 156.dp else 105.dp)
        .clip(RoundedCornerShape(19.dp)).border(2.dp,orange,
            RoundedCornerShape(19.dp))) {
        Image(painter=painterResource(drawable),
            contentDescription=label,contentScale=ContentScale.Crop,
            modifier=Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent,dark.copy(alpha=.2f)))))
    }
}

@Composable private fun MarketHeadline(title:String,subtitle:String) {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(title,fontSize=23.sp,fontWeight=FontWeight.Black,color=orange)
        Text(subtitle,fontSize=13.sp,color=cream)
    }
}

@Composable private fun MarketInfo(label:String,value:String) {
    Surface(modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp),
        color=Color(0xFF1B4962),
        border=androidx.compose.foundation.BorderStroke(1.dp,
            turquoise.copy(alpha=.55f))) {
        Column(Modifier.padding(horizontal=12.dp,vertical=9.dp)) {
            Text(label,fontSize=11.sp,color=Color(0xFFAEEDE4))
            Text(value,fontWeight=FontWeight.Black,fontSize=17.sp,color=Color.White)
        }
    }
}

@Composable private fun MarketOffer(
    title:String,subtitle:String,selected:Boolean=false,
    onClick:()->Unit
) {
    Surface(
        modifier=Modifier.fillMaxWidth().heightIn(min=64.dp)
            .clickable(role=Role.Button,onClick=onClick)
            .semantics { contentDescription="$title. $subtitle" },
        shape=RoundedCornerShape(15.dp),
        color=if(selected) Color(0xFF377F77) else panel,
        border=androidx.compose.foundation.BorderStroke(
            if(selected) 2.dp else 1.dp,if(selected) orange else turquoise)
    ) {
        Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween) {
            Text(title,fontSize=18.sp,color=Color.White,
                fontWeight=FontWeight.ExtraBold)
            Text(subtitle,fontSize=13.sp,color=cream)
        }
    }
}

/** Real denomination badges; never suggest a fictional 400 TL banknote. */
@Composable private fun MarketCashStack(amount:Int,tier:LearningTier) {
    val note=when(tier) {
        LearningTier.TEMEL -> 5000
        LearningTier.GELISEN -> 10000
        LearningTier.USTALIK -> 20000
    }
    val noteCount=amount/note
    Surface(color=Color(0xFF397763),shape=RoundedCornerShape(16.dp),
        border=androidx.compose.foundation.BorderStroke(2.dp,orange)) {
        Row(Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(11.dp)) {
            Surface(shape=RoundedCornerShape(8.dp),color=Color(0xFFF7DDA1)) {
                Column(Modifier.padding(horizontal=13.dp,vertical=8.dp),
                    horizontalAlignment=Alignment.CenterHorizontally) {
                    Text("₺",fontSize=25.sp,color=ink,fontWeight=FontWeight.Black)
                    Text(MarketAdventureEngine.format(note),color=ink,
                        fontSize=12.sp,fontWeight=FontWeight.Bold)
                }
            }
            Column {
                Text("${noteCount} adet banknot",color=Color.White,
                    fontWeight=FontWeight.Bold,fontSize=14.sp)
                Text("Toplam verilen: ${MarketAdventureEngine.format(amount)}",
                    color=cream,fontSize=13.sp)
            }
        }
    }
}

@Composable private fun ProductCard(
    product:MarketGood,quantity:Int,onPlus:()->Unit,onMinus:()->Unit
) {
    Surface(modifier=Modifier.fillMaxWidth(),
        shape=RoundedCornerShape(20.dp),
        color=Color(0xFF255F7A),
        border=androidx.compose.foundation.BorderStroke(
            if(quantity>0) 2.dp else 1.dp,if(quantity>0) orange else Color(0xFF6BBAAD))
    ) {
        Column(Modifier.padding(11.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(product.emoji,fontSize=38.sp)
            Text(product.name,color=Color.White,fontSize=15.sp,
                fontWeight=FontWeight.Bold,maxLines=1)
            Text(MarketAdventureEngine.format(product.priceKurus)+" / "+product.unit,
                color=orange,fontSize=13.sp,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween) {
                OutlinedButton(onClick=onMinus,enabled=quantity>0,
                    modifier=Modifier.size(48.dp).semantics {
                        contentDescription="${product.name} miktarını bir azalt"
                    },
                    contentPadding=androidx.compose.foundation.layout.PaddingValues(0.dp),
                    shape=CircleShape,
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise)) {
                    Text("−",fontSize=23.sp)
                }
                Text("$quantity",fontSize=19.sp,color=Color.White,
                    fontWeight=FontWeight.Bold)
                OutlinedButton(onClick=onPlus,enabled=quantity<3,
                    modifier=Modifier.size(48.dp).semantics {
                        contentDescription="${product.name} miktarını bir artır"
                    },
                    contentPadding=androidx.compose.foundation.layout.PaddingValues(0.dp),
                    shape=CircleShape,
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise)) {
                    Text("+",fontSize=22.sp)
                }
            }
        }
    }
}

@Composable
fun MarketAdventureScreen(
    store:MarketAdventureStore,
    stallStore:MarketStallStore,
    hasPremium:Boolean,
    onUpgrade:()->Unit,
    suggestedTier:LearningTier,
    onExit:()->Unit,
    onCompletion:(MarketAdventureRun)->Unit
) {
    var run by remember { mutableStateOf(store.load()) }
    var chosenTier by rememberSaveable { mutableStateOf(suggestedTier) }
    var message by remember { mutableStateOf("") }
    var isPositive by remember { mutableStateOf(true) }
    // Every new Activity entry opens the map. In-process rotation keeps the page.
    var showLobby by rememberSaveable { mutableStateOf(true) }
    var pendingNewRun by remember { mutableStateOf<Pair<LearningTier,Int?>?>(null) }
    var showParentalGate by remember { mutableStateOf(false) }
    var adultAnswer by remember { mutableStateOf("") }
    var showStall by remember { mutableStateOf(false) }
    var showAllShops by rememberSaveable { mutableStateOf(false) }
    val scroll=rememberScrollState()
    LaunchedEffect(run.seed,run.visit,run.phase,showLobby) { scroll.scrollTo(0) }

    fun persist(next:MarketAdventureRun):Boolean {
        if(!store.save(next)) {
            message="Kaydetme başarısız. Lütfen yeniden dene."
            isPositive=false
            return false
        }
        run=next
        return true
    }
    fun start(tier:LearningTier,shopIndex:Int?=null) {
        val started=store.begin(tier,hasPremium,shopIndex)
        if(started!=null) {
            run=started;message="";showLobby=false
        } else {isPositive=false;message="Oyun başlatılamadı. Depolamayı kontrol et."}
    }
    fun requestStart(tier:LearningTier, shopIndex:Int?=null) {
        // Do not overwrite a half-played shopping list without explicit consent.
        if(run.seed>0L && !run.finished) {
            pendingNewRun=tier to shopIndex
        } else start(tier,shopIndex)
    }
    fun chooseMoney(amount:Int) {
        val current=run
        if(current.phase==MarketAdventurePhase.PAYMENT) {
            val next=MarketAdventureEngine.choosePayment(current,amount)
            if(next==current) return
            if(persist(next)) {
                isPositive=next.phase!=current.phase
                message=if(isPositive) "Toplamı doğru hesapladın! Şimdi para üstünü bul."
                    else "Toplamı hesaplarken ürünlerin miktarını da hesaba kat."
            }
        } else if(current.phase==MarketAdventurePhase.CHANGE) {
            val next=MarketAdventureEngine.chooseChange(current,amount)
            if(next==current) return
            if(persist(next)) {
                isPositive=next.phase!=current.phase
                message=if(isPositive) "Doğru para üstü!"
                    else "Verilen paradan alışveriş tutarını çıkar."
                if(next.finished) onCompletion(next)
            }
        } else if(current.phase==MarketAdventurePhase.BONUS) {
            val next=MarketAdventureEngine.chooseBonus(current,amount)
            if(next==current) return
            if(persist(next)) {
                isPositive=next.phase!=current.phase
                message=if(isPositive) "Güzel hesapladın! Bir sonraki pazara geç."
                    else "Hesabı tekrar kontrol et. İpucunu da kullanabilirsin."
                if(next.finished) onCompletion(next)
            }
        }
    }

    BackHandler(enabled=showStall) {showStall=false}
    if(showStall && hasPremium) {
        MarketStallScreen(stallStore,onClose={showStall=false})
        return
    }
    if(showParentalGate) AlertDialog(
        onDismissRequest={showParentalGate=false;adultAnswer=""},
        title={Text("Yetişkin Bölgesi")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("Premium satın alma ekranı yalnızca yetişkinler içindir. " +
                    "Devam etmek için işlemi çöz.")
                Text("17 × 8 = ?",fontWeight=FontWeight.Bold,fontSize=19.sp)
                OutlinedTextField(value=adultAnswer,
                    onValueChange={adultAnswer=it.take(5).filter(Char::isDigit)},
                    label={Text("Sonuç")},singleLine=true)
            }
        },
        confirmButton={
            TextButton(onClick={showParentalGate=false;adultAnswer="";onUpgrade()},
                enabled=adultAnswer=="136") {Text("Yetişkin olarak devam et")}
        },
        dismissButton={TextButton(onClick={showParentalGate=false;adultAnswer=""}) {
            Text("Vazgeç")
        }}
    )
    BackHandler(enabled=!showLobby) { showLobby=true;message="" }
    val replacing=pendingNewRun
    if(replacing!=null) AlertDialog(
        onDismissRequest={pendingNewRun=null},
        title={Text("Yeni oyuna başlansın mı?")},
        text={Text(
            "Devam eden ${run.tier.title} seviyesindeki oyun tamamlanmadı. " +
            "Yeni oyun başlatırsan bu turun sepeti ve görev ilerlemesi değişecek. " +
            "Önce kaldığın yerden devam edebilirsin."
        )},
        confirmButton={TextButton(onClick={
            pendingNewRun=null
            start(replacing.first,replacing.second)
        }) {Text("Yeni Oyuna Başla")}},
        dismissButton={TextButton(onClick={pendingNewRun=null}) {Text("Vazgeç")}}
    )
    val scene=MarketAdventureEngine.scenario(run)
    val stage=if(showLobby) MarketAdventurePhase.WELCOME else run.phase
    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                Color(0xFF1E799D),Color(0xFF165170),Color(0xFF102D4B)
            )))
            .safeDrawingPadding()
            .verticalScroll(scroll)
            .padding(horizontal=16.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        OutlinedButton(
            onClick={if(showLobby) onExit() else {showLobby=true;message=""}},
            colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
            border=androidx.compose.foundation.BorderStroke(1.dp,turquoise),
            modifier=Modifier.heightIn(min=48.dp)) {
            Text(if(showLobby) "← Oyunlara Dön" else "← Pazar Haritası")
        }
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("PAZAR MACERASI",fontSize=18.sp,color=orange,
                fontWeight=FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(if(showLobby) "Seçim: ${chosenTier.title}"
                else "${run.tier.title} · ${if(run.singleShop) "Hızlı" else "Tam"}",
                fontSize=13.sp,color=turquoise)
        }
        if(!showLobby) {
            val totalStops=MarketAdventureEngine.visitCount(run)
            val completed=if(run.finished) totalStops
                else if(run.singleShop) 0 else run.visit
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                repeat(totalStops) { index ->
                    Box(Modifier.weight(1f).height(8.dp).clip(CircleShape)
                        .background(if(index<completed) orange else Color(0xFF5C8BA2)))
                }
            }
        }
        if(stage==MarketAdventurePhase.WELCOME || run.finished) {
            MarketScene(R.drawable.market_adventure_hero,
                "Renkli çizgi film pazarında meyve ve alışveriş tezgâhları",
                big=true)
            if(stage==MarketAdventurePhase.WELCOME) {
                MarketHeadline("Pazar Macerası",
                    "Alışveriş yap, bütçeni planla ve para üstünü hesapla.")
                Text("${MarketPremiumCatalog.shops.size} mağaza · " +
                    "${MarketPremiumCatalog.productCount} ürün · Tek mağaza veya tam macera",
                    color=cream,fontSize=12.sp)
                Text("Her mağazada 12 ürün var; raflara her turda altısı geliyor.",
                    color=turquoise,fontSize=12.sp)
                if(run.seed>0L && !run.finished) {
                    Surface(shape=RoundedCornerShape(18.dp),
                        color=Color(0xFF27665D),
                        border=androidx.compose.foundation.BorderStroke(2.dp,orange)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text("📌 Kaydedilmiş Oyunun",color=orange,
                                fontSize=17.sp,fontWeight=FontWeight.Black)
                            val resumeShop=MarketAdventureEngine.scenario(run)?.location
                                ?: "Pazar"
                            Text("$resumeShop · ${run.tier.title} · " +
                                if(run.singleShop) "Hızlı Oyun"
                                else "${run.visit+1}/${MarketAdventureEngine.visitCount(run)}. mağaza",
                                color=Color.White,fontSize=14.sp)
                            Text("Sepetin ve matematik ilerlemen korunuyor.",
                                color=cream,fontSize=12.sp)
                            MarketAction("▶ Kaldığım Yerden Devam Et") {
                                showLobby=false;message=""
                            }
                        }
                    }
                }
                Text("Zorluk seviyeni seç",color=Color.White,
                    fontWeight=FontWeight.Bold)
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    LearningTier.entries.forEach { tier ->
                        OutlinedButton(
                            onClick={chosenTier=tier},
                            modifier=Modifier.weight(1f).heightIn(min=49.dp),
                            contentPadding=androidx.compose.foundation.layout.PaddingValues(
                                horizontal=2.dp,vertical=5.dp),
                            border=androidx.compose.foundation.BorderStroke(1.5.dp,
                                if(chosenTier==tier) orange else turquoise),
                            colors=ButtonDefaults.outlinedButtonColors(
                                containerColor=if(chosenTier==tier) orange else dark,
                                contentColor=if(chosenTier==tier) ink else Color.White)
                        ) {Text(tier.title,fontSize=11.sp)}
                    }
                }
                Text(if(hasPremium) "Premium: 10 mağaza · 40 karar · Sınırsız tekrar" else
                    "Free: 3 mağaza · 12 karar · Premium için 7 yeni mağaza",
                    color=cream,fontSize=12.sp)
                MarketAction(if(hasPremium) "10 Mağazalık Maceraya Gir ▶"
                    else "3 Mağazalık Ücretsiz Maceraya Gir ▶") {requestStart(chosenTier)}
                if(hasPremium) {
                    OutlinedButton(onClick={showStall=true},
                        modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,orange),
                        colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) {
                        Text("🏪 Kendi Tezgâhını İşlet")
                    }
                } else {
                    OutlinedButton(onClick={showParentalGate=true},
                        modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,orange),
                        colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) {
                        Text("🔒 Premium Mağazaları Keşfet")
                    }
                }
                Text("Tek Mağazalık Hızlı Oyun",color=orange,
                    fontSize=17.sp,fontWeight=FontWeight.Black)
                Text("Bir mağazayı seçip dört matematik görevi çözebilirsin.",
                    color=cream,fontSize=12.sp)
                MarketPremiumCatalog.shops.forEachIndexed { index,shop ->
                    if(!showAllShops && index>=3) return@forEachIndexed
                    Surface(color=if(shop.isPro&&!hasPremium)Color(0xFF344F68)
                        else Color(0xFF245C79),
                        shape=RoundedCornerShape(14.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,
                            if(shop.isPro)orange else turquoise)) {
                        Column(Modifier.fillMaxWidth().padding(11.dp),
                            verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("${shop.emoji}  ${index+1}. ${shop.title}  " +
                                if(shop.isPro&&!hasPremium)"🔒 Premium" else "",
                                color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp)
                            Text(shop.learning,color=cream,fontSize=12.sp)
                            OutlinedButton(onClick={
                                if(shop.isPro&&!hasPremium)showParentalGate=true
                                else requestStart(chosenTier,index)
                            },modifier=Modifier.fillMaxWidth().heightIn(min=46.dp),
                                colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                                border=androidx.compose.foundation.BorderStroke(
                                    1.dp,if(shop.isPro)orange else turquoise)) {
                                Text(if(shop.isPro&&!hasPremium)
                                    "🔒 Premium ile Aç" else "Bu Mağazada Oyna",
                                    fontWeight=FontWeight.Bold)
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick={showAllShops=!showAllShops},
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise)
                ) {
                    Text(if(showAllShops) "Daha Az Mağaza Göster"
                        else "Tüm 10 Mağazayı Göster")
                }
            } else {
                MarketHeadline("Pazar maceran tamamlandı!",
                    "Alışveriş, ödeme ve ekonomik kararları bitirdin.")
                Surface(color=panel,shape=RoundedCornerShape(20.dp),
                    border=androidx.compose.foundation.BorderStroke(2.dp,orange)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(9.dp)) {
                        Text("✦   ${MarketAdventureEngine.visitCount(run)} mağaza tamamlandı",fontSize=23.sp,
                            fontWeight=FontWeight.ExtraBold,color=orange)
                        Text("★".repeat(MarketAdventureEngine.starsFor(run))+
                            "☆".repeat(3-MarketAdventureEngine.starsFor(run)),
                            color=orange,fontSize=31.sp,fontWeight=FontWeight.Black)
                        Text(if(run.singleShop)
                            "Kısa oyun: alıştırma yıldızları hesaba eklenmez."
                            else "En iyi ödül: ${store.bestAwardedStars()}/3 yıldız",
                            color=turquoise,fontSize=13.sp)
                        Text("Tekrar oynayarak en iyi sonucunu yükseltebilirsin.",
                            color=cream,fontSize=12.sp)
                        Text("İlk denemede bağımsız: ${run.perfectDecisions}/${MarketAdventureEngine.decisionCount(run)}",
                            color=Color.White,fontSize=15.sp)
                        Text("Toplam: ${run.attempts} deneme · ${run.errors} hata · ${run.hints} ipucu",
                            color=cream,fontSize=13.sp)
                        Text("Tamamlanan pazar turları: ${run.completedRuns}",
                            color=turquoise,fontSize=13.sp)
                        Text("Bu özet oyun içi çalışmaları gösterir; resmî öğrenme yeterliliği değildir.",
                            color=cream,fontSize=11.sp)
                    }
                }
                if(hasPremium) {
                    OutlinedButton(onClick={showStall=true},
                        modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,orange),
                        colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) {
                        Text("🏪 Kendi Tezgâhını İşlet")
                    }
                }
                MarketAction(if(run.singleShop) "Bu Mağazayı Tekrar Oyna ↻"
                    else "Yeni Alışveriş Turu ↻") {
                    requestStart(run.tier,if(run.singleShop) run.startShop else null)
                }
                OutlinedButton(onClick={showLobby=true},
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise),
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) {
                    Text("Pazar Haritasına Dön")
                }
            }
        } else if(scene!=null) {
            MarketHeadline("${if(run.singleShop)1 else run.visit+1}/${MarketAdventureEngine.visitCount(run)} · ${scene.location}",
                scene.hint)
            val art=if(run.contentVersion<3) when(run.visit) {
                0 -> R.drawable.market_adventure_manav
                1 -> R.drawable.market_adventure_firin
                2 -> R.drawable.market_adventure_toys
                3 -> R.drawable.market_adventure_stationery
                4 -> R.drawable.market_adventure_clothing
                else -> R.drawable.market_adventure_stall
            } else when(run.visit) {
                0 -> R.drawable.market_adventure_manav
                1 -> R.drawable.market_adventure_firin
                2 -> R.drawable.market_adventure_toys
                3 -> R.drawable.market_adventure_stationery
                4 -> R.drawable.market_adventure_clothing
                5 -> R.drawable.market_adventure_sports
                6 -> R.drawable.market_adventure_pets
                7 -> R.drawable.market_adventure_grocery
                8 -> R.drawable.market_adventure_cafe
                else -> R.drawable.market_adventure_stall
            }
            MarketScene(art,scene.location+" pazar tezgâhı")
            val total=MarketAdventureEngine.amount(run,scene)
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                Box(Modifier.weight(1f)) {
                    MarketInfo("BÜTÇE",MarketAdventureEngine.format(scene.budgetKurus))
                }
                Box(Modifier.weight(1f)) {
                    MarketInfo("SEPET",if(stage==MarketAdventurePhase.PAYMENT) "Sen hesapla" else MarketAdventureEngine.format(total))
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                (if(scene.bonus==null) listOf("1 · Alışveriş","2 · Toplam","3 · Para üstü")
                    else listOf("1 · Alışveriş","2 · Toplam","3 · Para üstü","4 · Ek Görev"))
                    .forEachIndexed { index,label ->
                    val current=index==when(stage) {
                        MarketAdventurePhase.SHOP -> 0
                        MarketAdventurePhase.PAYMENT -> 1
                        MarketAdventurePhase.CHANGE -> 2
                        else -> 3
                    }
                    Surface(Modifier.weight(1f),shape=RoundedCornerShape(10.dp),
                        color=if(current) orange else Color(0xFF235771)) {
                        Text(label,Modifier.padding(horizontal=4.dp,vertical=9.dp),
                            color=if(current) ink else cream,fontSize=10.sp,
                            textAlign=TextAlign.Center,fontWeight=FontWeight.Bold)
                    }
                }
            }
            if(stage==MarketAdventurePhase.SHOP) {
                Text("🧺 ALIŞVERİŞ LİSTEN",color=orange,fontWeight=FontWeight.Black,
                    fontSize=15.sp)
                Surface(color=panel,shape=RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement=Arrangement.spacedBy(6.dp)) {
                        scene.needed.forEach { line ->
                            val product=scene.products.first { it.id==line.productId }
                            val current=run.basket.firstOrNull {
                                it.productId==line.productId }?.quantity?:0
                            val completed=current==line.quantity
                            Text("${if(completed)"✓" else "○"}  ${product.emoji} ${product.name} · ${line.quantity} ${product.unit}",
                                color=if(completed)turquoise else Color.White,
                                fontSize=14.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
                Text("Tezgâhtan ürünleri seç",color=cream,fontSize=14.sp,
                    fontWeight=FontWeight.Bold)
                scene.products.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        pair.forEach { product ->
                            Box(Modifier.weight(1f)) {
                                ProductCard(product,
                                    run.basket.firstOrNull {
                                        it.productId==product.id }?.quantity?:0,
                                    onPlus={
                                        persist(MarketAdventureEngine.changeQuantity(
                                            run,product.id,1))
                                    },onMinus={
                                        persist(MarketAdventureEngine.changeQuantity(
                                            run,product.id,-1))
                                    })
                            }
                        }
                    }
                }
                val remaining=scene.budgetKurus-total
                Text(if(remaining>=0)
                    "Kalan bütçe: ${MarketAdventureEngine.format(remaining)}"
                    else "Bütçe aşıldı: ${MarketAdventureEngine.format(-remaining)}",
                    color=if(remaining>=0)turquoise else orange,
                    fontWeight=FontWeight.Bold)
                MarketAction("Kasaya Git →",enabled=run.basket.isNotEmpty()) {
                    val originalPhase=run.phase
                    val updated=MarketAdventureEngine.checkBasket(run)
                    if(persist(updated)) {
                        isPositive=updated.phase!=originalPhase
                        message=if(isPositive) "Sepetin doğru! Şimdi tutarı hesapla."
                            else if(total>scene.budgetKurus)
                                "Sepetin bütçeyi aştı. Fazla ürünleri kaldır."
                            else "Listendeki ürünleri ve miktarları kontrol et."
                    }
                }
            } else if(stage==MarketAdventurePhase.PAYMENT) {
                Text("🧮 KASADA TOPLAMI HESAPLA",color=orange,
                    fontWeight=FontWeight.Black,fontSize=18.sp)
                Surface(color=panel,shape=RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(15.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        run.basket.forEach { line ->
                            val p=scene.products.first { it.id==line.productId }
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.SpaceBetween) {
                                Text("${p.name} × ${line.quantity}",color=Color.White)
                                Text("${MarketAdventureEngine.format(p.priceKurus)} / ${p.unit}",
                                    color=cream)
                            }
                        }
                        Text("Tüm ürünlerin toplam fiyatı kaç TL?",
                            color=orange,fontWeight=FontWeight.Bold)
                    }
                }
                scene.payOptionsKurus.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                        pair.forEach { option ->
                            Box(Modifier.weight(1f)) {
                                MarketOffer(MarketAdventureEngine.format(option),"Toplam") {
                                    chooseMoney(option)
                                }
                            }
                        }
                    }
                }
            } else if(stage==MarketAdventurePhase.CHANGE) {
                Text("💰 PARA ÜSTÜ BULMACASI",color=orange,
                    fontWeight=FontWeight.Black,fontSize=18.sp)
                Surface(color=panel,shape=RoundedCornerShape(20.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)) {
                        Text("Ödemenin ardından kasiyer senin verdiğin paradan kalan tutarı iade edecek.",
                            color=Color.White,fontSize=14.sp)
                        MarketInfo("KASİYERE VERİLEN PARA",
                            MarketAdventureEngine.format(run.paidKurus))
                        MarketCashStack(run.paidKurus,run.tier)
                        MarketInfo("ALIŞVERİŞ TOPLAMI",
                            MarketAdventureEngine.format(total))
                        Text("Kaç TL para üstü alırsın?",
                            color=orange,fontWeight=FontWeight.Bold,fontSize=17.sp)
                    }
                }
                scene.changeOptionsKurus.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                        pair.forEach { option ->
                            Box(Modifier.weight(1f)) {
                                MarketOffer(MarketAdventureEngine.format(option),"Para üstü") {
                                    chooseMoney(option)
                                }
                            }
                        }
                    }
                }
            } else if(stage==MarketAdventurePhase.BONUS && scene.bonus!=null) {
                val bonus=scene.bonus
                Text(when(bonus.kind) {
                    MarketBonusKind.WEIGHT -> "⚖️ KİLOGRAM HESABI"
                    MarketBonusKind.VOLUME -> "🥛 LİTRE HESABI"
                    MarketBonusKind.SAVINGS -> "🐷 HARÇLIK VE BİRİKİM"
                    MarketBonusKind.DISCOUNT -> "🏷️ İNDİRİM HESABI"
                    MarketBonusKind.COMPARE -> "⚖️ FİYAT KARŞILAŞTIRMA"
                    MarketBonusKind.PROFIT -> "🏪 KENDİ TEZGÂHINI YÖNET"
                    MarketBonusKind.BUDGET -> "💰 BÜTÇE YÖNETİMİ"
                    MarketBonusKind.UNIT_PRICE -> "📦 BİRİM FİYAT HESABI"
                    MarketBonusKind.BUNDLE -> "🛒 ÇOKLU PAKET HESABI"
                },color=orange,fontSize=17.sp,fontWeight=FontWeight.Black)
                Surface(color=panel,shape=RoundedCornerShape(18.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(bonus.title,color=turquoise,fontSize=16.sp,
                            fontWeight=FontWeight.Bold)
                        Text(bonus.prompt,color=Color.White,fontSize=16.sp,
                            lineHeight=24.sp)
                    }
                }
                bonus.choicesKurus.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        pair.forEach { option ->
                            Box(Modifier.weight(1f)) {
                                MarketOffer(MarketAdventureEngine.format(option),"Hesap") {
                                    chooseMoney(option)
                                }
                            }
                        }
                    }
                }
            }
            if(stage in listOf(MarketAdventurePhase.SHOP,
                MarketAdventurePhase.PAYMENT,MarketAdventurePhase.CHANGE,
                MarketAdventurePhase.BONUS)) {
                if(!run.taskHadHint) OutlinedButton(onClick={
                    if(persist(MarketAdventureEngine.hint(run))) {
                        isPositive=true
                        message=when(run.phase) {
                            MarketAdventurePhase.SHOP ->
                                "Her ürünün istenen miktarı kadar sepette olmalı. Liste dışı ürün alma."
                            MarketAdventurePhase.PAYMENT ->
                                "Her ürünün fiyatını miktarıyla çarpıp sonuçları topla."
                            MarketAdventurePhase.CHANGE ->
                                "Verilen paradan kasanın hesapladığı toplamı çıkar."
                            MarketAdventurePhase.BONUS ->
                                scene.bonus?.explanation ?: "Fiyatları dikkatlice karşılaştır."
                            else -> ""
                        }
                    }
                },modifier=Modifier.heightIn(min=48.dp),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise),
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White)) {
                    Text("💡 İpucu Al")
                }
                AnimatedVisibility(visible=message.isNotBlank()) {
                    Surface(color=if(isPositive)Color(0xFF266A65)
                        else Color(0xFF78514B),shape=RoundedCornerShape(15.dp)) {
                        Text(message,color=Color.White,
                            modifier=Modifier.fillMaxWidth().padding(13.dp),fontSize=14.sp)
                    }
                }
                Text("İlk denemede doğru karar: ${run.perfectDecisions}/${MarketAdventureEngine.decisionCount(run)}",
                    color=cream,fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
