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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    Box(Modifier.fillMaxWidth().height(if(big) 180.dp else 102.dp)
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
    Surface(shape=RoundedCornerShape(14.dp),
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
            Text(MarketAdventureEngine.format(product.priceKurus),
                color=orange,fontSize=13.sp,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween) {
                OutlinedButton(onClick=onMinus,enabled=quantity>0,
                    modifier=Modifier.size(47.dp),
                    contentPadding=androidx.compose.foundation.layout.PaddingValues(0.dp),
                    shape=CircleShape,
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                    border=androidx.compose.foundation.BorderStroke(1.dp,turquoise)) {
                    Text("−",fontSize=23.sp)
                }
                Text("$quantity",fontSize=19.sp,color=Color.White,
                    fontWeight=FontWeight.Bold)
                OutlinedButton(onClick=onPlus,enabled=quantity<3,
                    modifier=Modifier.size(47.dp),
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
    suggestedTier:LearningTier,
    onExit:()->Unit,
    onCompletion:(MarketAdventureRun)->Unit
) {
    var run by remember { mutableStateOf(store.load()) }
    var chosenTier by remember { mutableStateOf(suggestedTier) }
    var message by remember { mutableStateOf("") }
    var isPositive by remember { mutableStateOf(true) }
    var showExit by remember { mutableStateOf(false) }
    val scroll=rememberScrollState()
    LaunchedEffect(run.seed,run.visit,run.phase) { scroll.scrollTo(0) }

    fun persist(next:MarketAdventureRun):Boolean {
        if(!store.save(next)) {
            message="Kaydetme başarısız. Lütfen yeniden dene."
            isPositive=false
            return false
        }
        run=next
        return true
    }
    fun start(tier:LearningTier) {
        val started=store.begin(tier)
        if(started!=null) {
            run=started;message=""
        } else {isPositive=false;message="Oyun başlatılamadı. Depolamayı kontrol et."}
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
        }
    }

    BackHandler(enabled=run.seed>0L&&!run.finished) { showExit=true }
    if(showExit) AlertDialog(
        onDismissRequest={showExit=false},
        title={Text("Pazardan ayrıl?")},
        text={Text("Sepetin ve tamamladığın görevler cihazında kaydedildi.")},
        confirmButton={TextButton(onClick={showExit=false;onExit()}) {
            Text("Haritaya dön") }},
        dismissButton={TextButton(onClick={showExit=false}) {Text("Devam et")}}
    )
    val scene=MarketAdventureEngine.scenario(run)
    val stage=run.phase
    Column(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                Color(0xFF1E799D),Color(0xFF165170),Color(0xFF102D4B)
            )))
            .verticalScroll(scroll)
            .padding(horizontal=16.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        OutlinedButton(
            onClick={if(run.seed>0&&!run.finished)showExit=true else onExit()},
            colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
            border=androidx.compose.foundation.BorderStroke(1.dp,turquoise),
            modifier=Modifier.heightIn(min=48.dp)) {Text("← Oyunlara dön")}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("PAZAR MACERASI",fontSize=18.sp,color=orange,
                fontWeight=FontWeight.Black)
            Spacer(Modifier.weight(1f))
            Text(if(stage==MarketAdventurePhase.WELCOME) chosenTier.title
                else run.tier.title,fontSize=13.sp,color=turquoise)
        }
        val completed=if(run.finished) 3 else run.visit
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            repeat(3) { index ->
                Box(Modifier.weight(1f).height(8.dp).clip(CircleShape).background(
                    if(index<completed) orange else Color(0xFF5C8BA2)))
            }
        }
        if(stage==MarketAdventurePhase.WELCOME || run.finished) {
            MarketScene(R.drawable.market_adventure_hero,
                "Renkli çizgi film pazarında meyve ve alışveriş tezgâhları",
                big=true)
            if(!run.finished) {
                MarketHeadline("Pazar Simülatörü 2.0",
                    "Alışveriş yap, bütçeni planla ve para üstünü hesapla.")
                listOf("🍎 Güneşli Manav","🥖 Mahalle Fırını",
                    "🧸 Oyuncak Atölyesi").forEachIndexed { i,name ->
                    Surface(color=Color(0xFF245C79),
                        shape=RoundedCornerShape(14.dp),
                        border=androidx.compose.foundation.BorderStroke(1.dp,turquoise)) {
                        Row(Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment=Alignment.CenterVertically) {
                            Text("${i+1}. $name",color=Color.White,
                                fontWeight=FontWeight.Bold,fontSize=15.sp)
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
                Text("3 durak · 9 karar · Çevrimdışı · Zaman sınırı yok",
                    color=cream,fontSize=12.sp)
                MarketAction("Pazara Gir ▶") {start(chosenTier)}
            } else {
                MarketHeadline("Bugünün alışverişi tamamlandı!",
                    "Üç pazarda da doğru kararlar vererek yolculuğu bitirdin.")
                Surface(color=panel,shape=RoundedCornerShape(20.dp),
                    border=androidx.compose.foundation.BorderStroke(2.dp,orange)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement=Arrangement.spacedBy(9.dp)) {
                        Text("✦   3 durak tamamlandı",fontSize=23.sp,
                            fontWeight=FontWeight.ExtraBold,color=orange)
                        Text("İlk denemede bağımsız: ${run.perfectDecisions}/9",
                            color=Color.White,fontSize=15.sp)
                        Text("Toplam: ${run.attempts} deneme · ${run.errors} hata · ${run.hints} ipucu",
                            color=cream,fontSize=13.sp)
                        Text("Tamamlanan pazar turları: ${run.completedRuns}",
                            color=turquoise,fontSize=13.sp)
                        Text("Bu özet oyun içi çalışmaları gösterir; resmî öğrenme yeterliliği değildir.",
                            color=cream,fontSize=11.sp)
                    }
                }
                MarketAction("Yeni Alışveriş Turu ↻") {start(run.tier)}
                MarketAction("Haritaya Dön",onClick=onExit)
            }
        } else if(scene!=null) {
            MarketHeadline("${run.visit+1}/3 · ${scene.location}",
                scene.hint)
            val art=when(run.visit) {
                0 -> R.drawable.market_adventure_manav
                1 -> R.drawable.market_adventure_firin
                else -> R.drawable.market_adventure_toys
            }
            MarketScene(art,scene.location+" pazar tezgâhı")
            val total=MarketAdventureEngine.amount(run,scene)
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                Box(Modifier.weight(1f)) {
                    MarketInfo("BÜTÇE",MarketAdventureEngine.format(scene.budgetKurus))
                }
                Box(Modifier.weight(1f)) {
                    MarketInfo("SEPET",MarketAdventureEngine.format(total))
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
                            Text("${if(completed)"✓" else "○"}  ${product.emoji} ${product.name} · ${line.quantity} adet",
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
                                Text("${MarketAdventureEngine.format(p.priceKurus)} / adet",
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
            }
            if(stage in listOf(MarketAdventurePhase.SHOP,
                MarketAdventurePhase.PAYMENT,MarketAdventurePhase.CHANGE)) {
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
                Text("İlk denemede doğru karar: ${run.perfectDecisions}/9",
                    color=cream,fontSize=12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
