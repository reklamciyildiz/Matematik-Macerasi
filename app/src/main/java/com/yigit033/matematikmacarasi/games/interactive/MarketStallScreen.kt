package com.yigit033.matematikmacarasi.games.interactive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val stallGold=Color(0xFFFFD98A)
private val stallCyan=Color(0xFFB7FFF1)
private val stallInk=Color(0xFF153750)

@Composable
fun MarketStallScreen(store:MarketStallStore,onClose:()->Unit) {
    var state by remember {mutableStateOf(store.load())}
    var feedback by remember {mutableStateOf(
        "Toptancıdan stok al, müşteriye uygun fiyat teklif et ve kâr biriktir.")}
    var selected by remember {mutableStateOf(MarketStallEngine.products.first().id)}
    fun apply(next:StallState,message:String) {
        if(next==state) {feedback="İşlem mümkün değil: stok, fiyat ya da bakiyeyi kontrol et.";return}
        if(store.save(next)) {state=next;feedback=message}
        else feedback="İşlem kaydedilemedi. Yeniden dene."
    }
    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(Color(0xFF237DA0),Color(0xFF0E314E))))
        .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick=onClose,modifier=Modifier.heightIn(min=48.dp),
            colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
            border=BorderStroke(1.dp,stallCyan)) {Text("← Pazara Dön")}
        Text("🏪 Benim Tezgâhım",fontSize=25.sp,fontWeight=FontWeight.Black,
            color=stallGold)
        Text("Hayalî oyun parasıyla işletme macerası. Gerçek ödeme veya alışveriş yoktur.",
            fontSize=12.sp,color=stallCyan)
        Surface(shape=RoundedCornerShape(18.dp),color=stallInk,
            border=BorderStroke(2.dp,stallGold)) {
            Column(Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement=Arrangement.spacedBy(9.dp)) {
                Text("💰 Kasa: ${MarketAdventureEngine.format(state.walletKurus)}",
                    color=stallGold,fontWeight=FontWeight.Black,fontSize=20.sp)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                    Text("Seviye ${state.shopLevel} · Stok limiti ${MarketStallEngine.capacity(state)}",
                        color=Color.White,fontSize=12.sp)
                    Text("Satış: ${state.sales}",color=Color.White,fontSize=12.sp)
                }
                Text("Toplam kâr: ${MarketAdventureEngine.format(state.grossProfitKurus)}",
                    color=stallCyan,fontSize=14.sp,fontWeight=FontWeight.Bold)
                Button(onClick={
                    apply(MarketStallEngine.upgrade(state),"Tezgâh kapasitesi büyüdü!")
                },enabled=state.shopLevel<5 &&
                    state.walletKurus>=MarketStallEngine.upgradePrice(state),
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                    colors=ButtonDefaults.buttonColors(
                        containerColor=stallGold,contentColor=stallInk)) {
                    Text(if(state.shopLevel<5)
                        "Tezgâhı geliştir · ${MarketAdventureEngine.format(
                            MarketStallEngine.upgradePrice(state))}"
                        else "Tezgâh maksimum seviyede")
                }
            }
        }
        Text("📦 Stok satın al",color=stallGold,fontWeight=FontWeight.Bold)
        MarketStallEngine.products.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                pair.forEach { product ->
                    Surface(modifier=Modifier.weight(1f),
                        color=if(selected==product.id) Color(0xFF31766E)
                            else Color(0xFF26536D),
                        shape=RoundedCornerShape(17.dp),
                        border=BorderStroke(1.dp,
                            if(selected==product.id)stallGold else stallCyan)) {
                        Column(Modifier.padding(10.dp),
                            verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Text("${product.emoji} ${product.name}",
                                color=Color.White,fontWeight=FontWeight.Bold,fontSize=13.sp)
                            Text("Alış: ${MarketAdventureEngine.format(product.wholesaleKurus)}",
                                color=stallCyan,fontSize=12.sp)
                            Text("Stok: ${MarketStallEngine.stock(state,product.id)}",
                                color=stallGold,fontSize=12.sp)
                            Button(onClick={
                                selected=product.id
                                apply(MarketStallEngine.buy(state,product.id),
                                    "Bir ${product.name} stoğa eklendi.")
                            },enabled=state.walletKurus>=product.wholesaleKurus &&
                                MarketStallEngine.stock(state,product.id)<
                                    MarketStallEngine.capacity(state),
                                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                                colors=ButtonDefaults.buttonColors(
                                    containerColor=stallGold,contentColor=stallInk),
                                contentPadding=androidx.compose.foundation.layout.PaddingValues(5.dp)) {
                                Text("Stok al +1",fontSize=12.sp,fontWeight=FontWeight.Bold)
                            }
                            OutlinedButton(onClick={selected=product.id},
                                modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                                border=BorderStroke(1.dp,stallCyan),
                                colors=ButtonDefaults.outlinedButtonColors(
                                    contentColor=Color.White),
                                contentPadding=androidx.compose.foundation.layout.PaddingValues(5.dp)
                            ) {Text("Fiyat belirle",fontSize=11.sp)}
                        }
                    }
                }
            }
        }
        val item=MarketStallEngine.products.first {it.id==selected}
        Surface(shape=RoundedCornerShape(18.dp),color=stallInk,
            border=BorderStroke(2.dp,stallGold)) {
            Column(Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement=Arrangement.spacedBy(10.dp)) {
                Text("🛍️ Müşteri geldi!",fontSize=20.sp,
                    color=stallGold,fontWeight=FontWeight.Black)
                Text("${item.emoji} ${item.name} için satış fiyatını seç.",
                    color=Color.White,fontSize=15.sp)
                Text("Toptan alış: ${MarketAdventureEngine.format(item.wholesaleKurus)} · " +
                    "Müşterinin kabul edeceği en yüksek fiyat: " +
                    MarketAdventureEngine.format(item.fairPriceKurus),
                    color=stallCyan,fontSize=12.sp)
                MarketStallEngine.prices(item).forEach { price ->
                    val fair=price<=item.fairPriceKurus
                    OutlinedButton(onClick={
                        val next=MarketStallEngine.sell(state,item.id,price)
                        apply(next,if(fair)
                            "Satış tamam! Kâr: ${MarketAdventureEngine.format(
                                price-item.wholesaleKurus)}"
                            else "Müşteri fiyatı pahalı buldu; satış gerçekleşmedi.")
                    },enabled=MarketStallEngine.stock(state,item.id)>0,
                        modifier=Modifier.fillMaxWidth().heightIn(min=51.dp),
                        colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                        border=BorderStroke(1.dp,stallCyan)) {
                        Text("${MarketAdventureEngine.format(price)} ile sat" +
                            if(fair) " · Kâr ${MarketAdventureEngine.format(
                                price-item.wholesaleKurus)}" else " · Müşteri almayabilir",
                            fontSize=12.sp)
                    }
                }
            }
        }
        Surface(color=Color(0xFF327777),shape=RoundedCornerShape(14.dp)) {
            Text(feedback,Modifier.fillMaxWidth().padding(14.dp),
                color=Color.White,fontSize=14.sp)
        }
        Spacer(Modifier.heightIn(min=18.dp))
    }
}
