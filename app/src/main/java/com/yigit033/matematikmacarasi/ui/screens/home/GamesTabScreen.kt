package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.basic.*
import com.yigit033.matematikmacarasi.games.geometry.GeometryCastleActivity
import com.yigit033.matematikmacarasi.games.fractions.FractionCrystalsActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestAdventureActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.services.PlayerProfileService

private val navy=Color(0xFF071D3B)
private val ocean=Color(0xFF103A66)
private val aqua=Color(0xFF6DE8F6)
private val saffron=Color(0xFFFFDA77)
private val white=Color(0xFFF6FCFF)

data class GameCatalogItem(
    val id:String,
    val title:String,
    val description:String,
    val category:String,
    @DrawableRes val coverRes:Int,
    val accent:Color,
    val activityClass:Class<*>,
    val minGrade:Int=1
)
private fun catalog():List<GameCatalogItem> = listOf(
    GameCatalogItem("add","Toplama","Sayıları birleştir · yıldızları kazan","Temel İşlemler",
        R.drawable.card_addition_premium,Color(0xFF49C2FE),AdditionGameActivity::class.java),
    GameCatalogItem("subtract","Çıkarma","Farkı keşfet · matematik ustası ol","Temel İşlemler",
        R.drawable.card_subtraction_premium,Color(0xFFFF83BA),SubtractionGameActivity::class.java),
    GameCatalogItem("multiply","Çarpma","Grupları birleştir · çarpımları keşfet","Temel İşlemler",
        R.drawable.card_multiplication_premium,Color(0xFF65E6BA),MultiplicationGameActivity::class.java,3),
    GameCatalogItem("divide","Bölme","Eşit paylaş · sonuçları bul","Temel İşlemler",
        R.drawable.card_division_premium,Color(0xFFFFC768),DivisionGameActivity::class.java,3),
    GameCatalogItem("balloon","Balon Patlatma","Uçan doğru balonları yakala","Macera & Eğlence",
        R.drawable.card_balloon_pop,Color(0xFF67ECF1),BalloonPopGameActivity::class.java),
    GameCatalogItem("hunt","Sayı Avı","Sayıların peşinde hazine keşfi","Macera & Eğlence",
        R.drawable.card_number_hunt_premium,Color(0xFF88F0BA),SayiAvi2Activity::class.java),
    GameCatalogItem("space","Uzay Macerası","Geminle matematik meteorlarını vur","Macera & Eğlence",
        R.drawable.card_space_adventure,Color(0xFFC4A5FC),SpaceMathAdventureActivity::class.java),
    GameCatalogItem("maze","Matematik Labirenti","Kapıları aç · çıkışa ilerle","Macera & Eğlence",
        R.drawable.card_maze_premium,Color(0xFFBE95FF),MathMazeGameActivity::class.java),
    GameCatalogItem("forest","Orman Keşfi","Sayı Avı'nın orman bölgesinde macera","Macera & Eğlence",
        R.drawable.card_forest_premium,Color(0xFF80F0AA),ForestAdventureActivity::class.java),
    GameCatalogItem("market","Pazar Simülatörü","Alışveriş yap, bütçeyi hesapla","Hayat & Keşif",
        R.drawable.market_adventure_hero,Color(0xFF73E4A9),MarketSimulatorActivity::class.java),
    GameCatalogItem("geometry","Geometri Kalesi","Şekilleri tanı · kaleyi kurtar","Hayat & Keşif",
        R.drawable.geometry_castle_hero_premium,Color(0xFF78D6FF),GeometryCastleActivity::class.java),
    GameCatalogItem("fractions","Kesir Kristalleri","Eş parçaları oluştur · ışık köprüsünü tamamla","Hayat & Keşif",
        R.drawable.fraction_crystals_hero,Color(0xFFAF8BFF),FractionCrystalsActivity::class.java)
)
@Composable
fun GamesTabScreen(modifier:Modifier=Modifier){
    val context=LocalContext.current
    val grade=remember {GradeAwareOperationPolicy.grade(
        PlayerProfileService.getInstance(context).getUserClass())}
    val games=remember {catalog()}
    var filter by rememberSaveable {mutableStateOf("Tümü")}
    val tabs=listOf("Tümü","Temel İşlemler","Macera & Eğlence","Hayat & Keşif")
    val selected=if(filter=="Tümü")games else games.filter{it.category==filter}
    Column(
        modifier=modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                Color(0xFF086BA9),Color(0xFF07568B),navy
            ))).verticalScroll(rememberScrollState())
            .padding(horizontal=12.dp,vertical=8.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ){
        Surface(shape=RoundedCornerShape(22.dp),color=Color(0xFF122D58).copy(alpha=.80f),
            border=BorderStroke(1.dp,aqua.copy(alpha=.5f))){
            Column(Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement=Arrangement.spacedBy(4.dp)){
                Text("🎮 OYUN DÜNYASI",color=saffron,fontSize=23.sp,
                    fontWeight=FontWeight.Black)
                Text("12 macera · hepsi tek bir keşif haritasında",
                    color=white,fontSize=13.sp)
                Text("$grade. sınıf · eğitim içeriği sınıfa göre düzenlenir",
                    color=aqua,fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
        }
        Box(
            Modifier.fillMaxWidth().height(158.dp)
                .clip(RoundedCornerShape(22.dp))
                .clickable {context.startActivity(Intent(context,OperationsHubActivity::class.java))}
        ){
            Image(painterResource(R.drawable.card_four_operations),
                contentDescription=null,contentScale=ContentScale.Crop,
                modifier=Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                listOf(navy.copy(alpha=.94f),navy.copy(alpha=.20f)))))
            Column(Modifier.align(Alignment.CenterStart).padding(15.dp),
                verticalArrangement=Arrangement.spacedBy(6.dp)){
                Text("✨ DÖRT İŞLEM MERKEZİ",color=saffron,
                    fontSize=18.sp,fontWeight=FontWeight.Black)
                Text("Topla · Çıkar · Çarp · Böl\nKarışık matematik macerası",
                    color=white,fontSize=12.sp)
                Text("KEŞFET ▶",color=aqua,fontSize=13.sp,
                    fontWeight=FontWeight.Black)
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement=Arrangement.spacedBy(8.dp)){
            tabs.forEach{tab->
                Surface(
                    modifier=Modifier.clickable{filter=tab},
                    shape=RoundedCornerShape(15.dp),
                    color=if(filter==tab)Color(0xFF2C649E) else navy.copy(alpha=.72f),
                    border=BorderStroke(if(filter==tab)2.dp else 1.dp,
                        if(filter==tab)saffron else aqua.copy(alpha=.35f))
                ){
                    Text(tab,
                        modifier=Modifier.padding(horizontal=13.dp,vertical=10.dp),
                        color=white,fontSize=12.sp,fontWeight=FontWeight.Bold)
                }
            }
        }
        val categories=if(filter=="Tümü")listOf("Temel İşlemler","Macera & Eğlence","Hayat & Keşif")
            else listOf(filter)
        categories.forEach{category->
            val items=selected.filter{it.category==category}
            if(items.isNotEmpty()){
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically){
                    Text(when(category){
                        "Temel İşlemler"->"✦ $category"
                        "Macera & Eğlence"->"🚀 $category"
                        else->"🧭 $category"
                    },color=white,fontSize=17.sp,fontWeight=FontWeight.Black)
                    Text("${items.size} oyun",color=aqua,fontSize=12.sp)
                }
                items.chunked(2).forEach{pair->
                    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        pair.forEach{item->
                            val locked=grade<item.minGrade
                            Surface(
                                modifier=Modifier.weight(1f).height(234.dp)
                                    .clickable(enabled=!locked){
                                        context.startActivity(Intent(context,item.activityClass))
                                    },
                                shape=RoundedCornerShape(21.dp),
                                color=Color(0xFF10345E),
                                border=BorderStroke(2.dp,
                                    item.accent.copy(alpha=if(locked).35f else .95f))
                            ){
                                Column(Modifier.fillMaxSize(),
                                    verticalArrangement=Arrangement.spacedBy(4.dp)){
                                    Box(Modifier.fillMaxWidth().height(133.dp)
                                        .padding(5.dp).clip(RoundedCornerShape(17.dp))){
                                        Image(
                                            painterResource(item.coverRes),
                                            contentDescription=item.title,
                                            contentScale=ContentScale.Crop,
                                            modifier=Modifier.fillMaxSize()
                                        )
                                        if(locked)Box(
                                            Modifier.fillMaxSize()
                                                .background(navy.copy(alpha=.66f)),
                                            contentAlignment=Alignment.Center
                                        ){Text("🔒 3. SINIF",color=white,
                                            fontWeight=FontWeight.Black,fontSize=12.sp)}
                                    }
                                    Column(Modifier.fillMaxWidth()
                                        .padding(horizontal=10.dp),
                                        verticalArrangement=Arrangement.spacedBy(3.dp)){
                                        Text(item.title,color=white,
                                            fontSize=14.sp,fontWeight=FontWeight.Black,
                                            maxLines=1,overflow=TextOverflow.Ellipsis)
                                        Text(item.description,color=Color(0xFFC8E3F4),
                                            fontSize=10.sp,lineHeight=12.sp,
                                            maxLines=2,overflow=TextOverflow.Ellipsis)
                                        Text(if(locked)"3. sınıfta açılır" else "▶ OYNA",
                                            color=if(locked)Color(0xFFB6C2D0) else saffron,
                                            fontSize=11.sp,fontWeight=FontWeight.Black)
                                    }
                                }
                            }
                        }
                        if(pair.size==1)Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}
