package com.yigit033.matematikmacarasi.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.services.payments.PremiumFeaturePolicy
import com.yigit033.matematikmacarasi.services.payments.PremiumPlan
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager

private val navy = Color(0xFF081F3E)
private val gold = Color(0xFFFFD77E)
private val mint = Color(0xFF8CEBCC)
private val white = Color(0xFFF4F8FF)
private val pale = Color(0xFFC7DCF3)
private val blue = Color(0xFF1C416D)

/** The supplied premium design is a visual reference, never a nonfunctional screenshot. */
@Composable
private fun Benefit(icon: @Composable () -> Unit, label: String) {
    Row(modifier=Modifier.padding(horizontal=6.dp,vertical=9.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(5.dp)) {
        icon()
        Text(label,color=white,fontSize=10.sp,lineHeight=13.sp,fontWeight=FontWeight.SemiBold)
    }
}

@Composable
private fun PremiumPlanRow(
    plan: PremiumPlan, price: String, selected: Boolean, onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val fg = if(selected) navy else white
    val symbol = when(plan) {
        PremiumPlan.MONTHLY -> Icons.Default.CalendarMonth
        PremiumPlan.YEARLY -> Icons.Default.Star
        PremiumPlan.LIFETIME -> Icons.Default.AllInclusive
    }
    val note = when(plan) {
        PremiumPlan.LIFETIME -> "tek ödeme • süresiz"
        else -> "otomatik yenilenir"
    }
    Row(modifier=Modifier.fillMaxWidth().heightIn(min=77.dp)
        .shadow(if(selected) 7.dp else 0.dp,shape)
        .clip(shape)
        .background(if(selected) Brush.horizontalGradient(listOf(
            Color(0xFFFFD17B),Color(0xFFFFE7AA),Color(0xFFFFC977)))
        else Brush.horizontalGradient(listOf(Color(0xFF284B77),Color(0xFF1C3964))))
        .border(if(selected) 2.dp else 1.dp,
            if(selected) Color(0xFFFFEDB2) else Color(0xFF4D78A5),shape)
        .selectable(selected=selected,role=Role.RadioButton,onClick=onSelect)
        .padding(horizontal=12.dp,vertical=10.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.spacedBy(9.dp)) {
        Box(Modifier.size(22.dp).border(2.dp,
            if(selected) Color(0xFF81602F) else Color(0xFFA5BFE0),CircleShape),
            contentAlignment=Alignment.Center) {
            if(selected) Box(Modifier.size(10.dp).background(navy,CircleShape))
        }
        Icon(symbol,null,Modifier.size(25.dp),tint=if(selected) Color(0xFF815F2A) else pale)
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
            Text("${plan.label} • $price",color=fg,fontSize=13.sp,
                fontWeight=FontWeight.ExtraBold,lineHeight=18.sp)
            Text(note,color=if(selected) Color(0xFF755837) else pale,fontSize=11.sp)
        }
        if(plan==PremiumPlan.YEARLY) {
            Box(Modifier.clip(RoundedCornerShape(22.dp))
                .background(if(selected)Color(0xFFB76B22) else Color(0xFF315379))
                .padding(horizontal=7.dp,vertical=5.dp)) {
                Text("★ Avantajlı",fontWeight=FontWeight.Bold,fontSize=10.sp,
                    lineHeight=12.sp,color=if(selected)white else gold)
            }
        }
    }
}

@Composable
fun PremiumAdventureOffer(
    isPremium: Boolean,
    subscriptionActive: Boolean,
    incompatibleLicense: Boolean,
    subscriptionsEnabled: Boolean,
    prices: Map<PremiumPlan,String>,
    catalog: PurchaseManager.CatalogStatus,
    purchaseAttempted: Boolean,
    lastCheckoutStatus: PurchaseManager.PurchaseStatus?,
    onPurchase: (PremiumPlan) -> Unit,
    onCodeClick: () -> Unit,
    onRetryPrices: () -> Unit,
    onManageSubscription: () -> Unit
) {
    val offeredPlans=if(subscriptionsEnabled)
        listOf(PremiumPlan.MONTHLY,PremiumPlan.YEARLY,PremiumPlan.LIFETIME)
    else listOf(PremiumPlan.LIFETIME)
    var chosenName by rememberSaveable {
        mutableStateOf(if(subscriptionsEnabled) PremiumPlan.YEARLY.name
            else PremiumPlan.LIFETIME.name)
    }
    val chosen = offeredPlans.firstOrNull { it.name == chosenName } ?: offeredPlans.first()
    // Play's queried price and Ready catalog are mandatory. No hardcoded TRY.
    val enabled = prices[chosen] != null && catalog is PurchaseManager.CatalogStatus.Ready
    val shape = RoundedCornerShape(28.dp)
    Column(modifier=Modifier.fillMaxWidth().shadow(11.dp,shape).clip(shape)
        .background(Brush.verticalGradient(listOf(Color(0xFF293455),
            Color(0xFF1B3057),Color(0xFF102B53))))
        .border(1.5.dp,Color(0xFFF9D785),shape)
        .padding(15.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(112.dp)) {
            Image(painterResource(R.drawable.mm_premium_kids),null,
                Modifier.align(Alignment.CenterEnd).width(142.dp).fillMaxHeight(),
                contentScale=ContentScale.Fit)
            Row(Modifier.align(Alignment.CenterStart).fillMaxWidth().padding(end=87.dp),
                verticalAlignment=Alignment.CenterVertically) {
                Image(painterResource(R.drawable.mm_premium_crown),null,
                    Modifier.size(57.dp),contentScale=ContentScale.Fit)
                Column(Modifier.weight(1f)) {
                    Text(if(isPremium)"Premium Aktif" else "Macera Premium",
                        color=gold,fontSize=19.sp,lineHeight=22.sp,fontWeight=FontWeight.Black)
                    Text(if(isPremium)"Tüm macera dünyaları erişimine açık"
                        else "Daha fazla mağaza ve seçenek keşfet!",
                        color=white,fontSize=11.sp,lineHeight=14.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF333C65))
            .border(1.dp,Color(0xFF9D8163),RoundedCornerShape(22.dp))
            .horizontalScroll(rememberScrollState()),
            verticalAlignment=Alignment.CenterVertically) {
            Benefit({Icon(Icons.Default.Storefront,null,Modifier.size(22.dp),tint=gold)},
                "Daha fazla\nmağaza içeriği")
            Benefit({Icon(Icons.Default.SportsEsports,null,Modifier.size(22.dp),tint=gold)},
                "Oyun\nseçenekleri")
            Benefit({Icon(Icons.Default.Insights,null,Modifier.size(22.dp),tint=gold)},
                "Gelişmiş\nistatistikler")
        }
        if(incompatibleLicense) Text(
            "⚠ Eski Premium kodu bu yapılandırmada okunamıyor. " +
                "Verilerini silme; doğru lisans sürümünü kullan.",
            color=gold,fontSize=12.sp,lineHeight=17.sp)
        if(isPremium) {
            Row(verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                Icon(Icons.Default.CheckCircle,null,Modifier.size(20.dp),tint=mint)
                Text(if(subscriptionActive)
                    "Aboneliğin aktif. Yenileme ve iptal bilgileri Google Play'de."
                    else "Premium erişimin aktif; tekrar satın alma gerekmez.",
                    color=mint,fontSize=12.sp,lineHeight=17.sp)
            }
            if(subscriptionActive) Button(onClick=onManageSubscription,
                modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),
                colors=ButtonDefaults.buttonColors(containerColor=gold,contentColor=navy)) {
                Text("Aboneliği Google Play'de Yönet",fontWeight=FontWeight.Bold)
            }
        } else {
            offeredPlans.forEach { plan ->
                val displayedPrice=prices[plan]?.let { p -> when(plan) {
                    PremiumPlan.MONTHLY -> "$p / ay"
                    PremiumPlan.YEARLY -> "$p / yıl"
                    PremiumPlan.LIFETIME -> p
                } } ?: if(catalog is PurchaseManager.CatalogStatus.Loading)
                    "Fiyat sorgulanıyor" else "Şu anda kullanılamıyor"
                PremiumPlanRow(plan,displayedPrice,chosen==plan){chosenName=plan.name}
            }
            Row(Modifier.padding(horizontal=5.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info,null,Modifier.size(18.dp),tint=pale)
                Text("Aylık ve yıllık abonelikler aynı Premium haklarını sağlar. " +
                    "İptal durumunda erişim ödenmiş sürenin sonunda sona erer; " +
                    "ömür boyu plan otomatik yenilenmez.",color=pale,fontSize=11.sp,
                    lineHeight=15.sp,modifier=Modifier.weight(1f))
            }
            when(val status=catalog) {
                is PurchaseManager.CatalogStatus.Loading -> Row(
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    CircularProgressIndicator(Modifier.size(17.dp),strokeWidth=2.dp,color=gold)
                    Text("Google Play ürünleri kontrol ediliyor…",color=pale,fontSize=11.sp)
                }
                is PurchaseManager.CatalogStatus.Error -> Text(status.message,color=gold,fontSize=12.sp)
                is PurchaseManager.CatalogStatus.Ready ->
                    if(status.unavailablePlans.isNotEmpty()) Text(
                        "Bazı planlar bu Google Play hesabında kullanılamıyor.",
                        color=gold,fontSize=12.sp)
            }
            if(catalog !is PurchaseManager.CatalogStatus.Loading &&
                (catalog is PurchaseManager.CatalogStatus.Error ||
                    prices.size < offeredPlans.size)) {
                TextButton(onClick=onRetryPrices) {
                    Text("Google Play fiyatlarını yeniden kontrol et",color=gold)
                }
            }
            val checkoutMessage=if(purchaseAttempted) when(lastCheckoutStatus) {
                is PurchaseManager.PurchaseStatus.ServiceUnavailable ->
                    "Google Play ödeme servisine bağlanılamadı."
                is PurchaseManager.PurchaseStatus.ProductNotFound ->
                    "Seçilen ödeme planı Google Play'de bulunamadı."
                is PurchaseManager.PurchaseStatus.Failed ->
                    "Google Play ödeme ekranı açılamadı (kod ${lastCheckoutStatus.code})."
                is PurchaseManager.PurchaseStatus.Pending ->
                    "Satın alma işlemi beklemede. Google Play'den tamamlanmasını bekleyin."
                is PurchaseManager.PurchaseStatus.AlreadyOwned ->
                    "Premium erişiminiz zaten etkin."
                is PurchaseManager.PurchaseStatus.Canceled -> "Satın alma iptal edildi."
                is PurchaseManager.PurchaseStatus.Success ->
                    "Satın alma doğrulandı. Premium erişiminiz güncelleniyor."
                else -> null
            } else null
            if(checkoutMessage!=null) Text(checkoutMessage,color=gold,fontSize=12.sp)
            Button(onClick={onPurchase(chosen)},enabled=enabled,
                modifier=Modifier.fillMaxWidth().heightIn(min=55.dp),
                shape=RoundedCornerShape(25.dp),
                colors=ButtonDefaults.buttonColors(containerColor=gold,contentColor=navy,
                    disabledContainerColor=Color(0xFF536580),disabledContentColor=pale)) {
                Icon(Icons.Default.Star,null,Modifier.size(21.dp))
                Spacer(Modifier.width(9.dp))
                Text("Premium’a Geç",fontSize=17.sp,fontWeight=FontWeight.Black)
                Icon(Icons.Default.ChevronRight,null)
            }
        }
        OutlinedButton(onClick=onCodeClick,
            modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
            border=BorderStroke(1.5.dp,gold),shape=RoundedCornerShape(24.dp),
            colors=ButtonDefaults.outlinedButtonColors(contentColor=gold)) {
            Icon(Icons.Default.LocalOffer,null,Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Premium Kodum Var",fontWeight=FontWeight.Bold)
        }
    }
}

@Composable
private fun CompareLine(value:String,premium:Boolean) {
    Row(verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        Icon(Icons.Default.CheckCircle,null,Modifier.size(17.dp),
            tint=if(premium)gold else mint)
        Text(value,color=white,fontSize=12.sp,lineHeight=17.sp,modifier=Modifier.weight(1f))
    }
}

@Composable
private fun CompareColumn(premium:Boolean,modifier:Modifier=Modifier) {
    Column(modifier,verticalArrangement=Arrangement.spacedBy(11.dp)) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(30.dp))
            .background(if(premium)Color(0xFF3C4C66) else Color(0xFF265C87))
            .border(1.dp,if(premium)gold else Color(0xFF518FB9),RoundedCornerShape(30.dp))
            .padding(horizontal=8.dp,vertical=9.dp),contentAlignment=Alignment.Center) {
            Text(if(premium)"♛ PREMIUM İLE AÇILANLAR" else "ÜCRETSİZ MACERA",
                color=if(premium)gold else white,fontSize=11.sp,
                fontWeight=FontWeight.ExtraBold,lineHeight=14.sp)
        }
        if(premium) {
            CompareLine("Orta, Zor, Çok Zor ve Uzman oyun seçenekleri",true)
            CompareLine("Daha fazla mağaza içeriği ve özel avatarlar",true)
            CompareLine("Gelişmiş istatistikler ve ilerleme takibi",true)
        } else {
            CompareLine("Sınıfına uygun Temel oyunlar ve başlangıç dersleri",false)
            CompareLine("6 başlangıç avatarı ve temel macera istatistikleri",false)
        }
    }
}

@Composable
fun PremiumAdventureComparison(
    expanded:Boolean,onToggle:()->Unit,subscriptionsEnabled:Boolean
) {
    val shape=RoundedCornerShape(26.dp)
    Column(Modifier.fillMaxWidth().shadow(8.dp,shape).clip(shape)
        .background(Brush.verticalGradient(listOf(Color(0xFF164977),
            Color(0xFF113455),Color(0xFF18507C))))
        .border(1.dp,Color(0xFF6AA5D0),shape)
        .padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Star,null,Modifier.size(34.dp),tint=gold)
            Column {
                Text("Ücretsiz Macera / Premium",color=white,fontSize=18.sp,
                    fontWeight=FontWeight.ExtraBold)
                Text("İçerik farklarını keşfet, sana uygun macerayı seç.",
                    color=pale,fontSize=11.sp)
            }
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if(maxWidth >= 332.dp) {
                Row(horizontalArrangement=Arrangement.spacedBy(11.dp)) {
                    CompareColumn(false,Modifier.weight(1f))
                    Box(Modifier.width(1.dp).height(195.dp).background(Color(0xFF4D7BA3)))
                    CompareColumn(true,Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement=Arrangement.spacedBy(15.dp)) {
                    CompareColumn(false)
                    HorizontalDivider(color=Color(0xFF4977A1))
                    CompareColumn(true)
                }
            }
        }
        TextButton(onClick=onToggle,modifier=Modifier.fillMaxWidth()) {
            Text(if(expanded)"Ayrıntıları gizle" else "Tüm hakları karşılaştır",
                color=gold,fontWeight=FontWeight.Bold)
            Icon(Icons.Default.ChevronRight,null,tint=gold)
        }
        if(expanded) {
            HorizontalDivider(color=Color(0xFF4977A1))
            Text("ÜCRETSİZ",color=mint,fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
            listOf(
                "Dört işlem: seçilen sınıfa uygun Temel zorluk",
                "Orman Keşfi ve Antik Tapınak öğretici maceraları",
                "Sayı Avı, Geometri, Kesir, Zaman, Örüntü, Balon ve Uzay: Temel",
                "Her sınıfta iki farklı konudan ücretsiz başlangıç dersi",
                "Pazar: ${PremiumFeaturePolicy.marketShopCount(false)} mağaza, 12 karar",
                "Labirent: günde 5 oyun",
                "İlk 6 avatar (bazıları yıldızla açılır) ve standart ikon",
                "Yıldız ve biten oyun özeti"
            ).forEach { CompareLine(it,false) }
            HorizontalDivider(color=Color(0xFF4977A1))
            Text("PREMIUM",color=gold,fontWeight=FontWeight.ExtraBold,fontSize=12.sp)
            listOf(
                "Pazar: ${PremiumFeaturePolicy.marketShopCount(true)} mağaza, 40 karar",
                "Öğren: 1–8. sınıf kısa ve derinleşme dersleri",
                "Kendi Tezgâhım: stok, satış ve kâr",
                "Dört işlem ve Karışık Mod: Orta, Zor, Çok Zor, Uzman",
                "Sayı Avı, Geometri, Kesir, Zaman, Örüntü, Balon ve Uzay: Orta ve Zor",
                "9 ek özel avatar (yıldızla açılır) ve alternatif ikonlar",
                "Labirentte sınırsız oyun, oyun bazlı analizler ve günlük bonus"
            ).forEach { CompareLine(it,true) }
            Text(if(subscriptionsEnabled)
                "Aylık/yıllık otomatik yenilenir; ömür boyu Premium tek seferliktir."
                else "Ömür boyu Premium tek seferlik ödeme ile alınır.",
                color=pale,fontSize=11.sp)
        }
        Text("Çocuk güvenliği denetimi süresince reklamlar tüm oyuncular için kapalıdır.",
            color=pale,fontSize=11.sp,lineHeight=16.sp)
        Image(painterResource(R.drawable.mm_premium_footer),null,
            modifier=Modifier.fillMaxWidth().height(72.dp),
            contentScale=ContentScale.FillBounds)
    }
}