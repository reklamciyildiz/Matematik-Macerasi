package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.games.basic.GradeAwareOperationPolicy

private val deep=Color(0xFF071D3B)
private val panel=Color(0xFF11345C)
private val aqua=Color(0xFF7DECF8)
private val gold=Color(0xFFFFDA77)
private val cloud=Color(0xFFF7FCFF)

/** Compact selection chips; categories and grade matching are separate filters. */
@Composable
private fun GameFilterChip(text:String,selected:Boolean,onClick:()->Unit){
    Surface(modifier=Modifier.clickable(onClick=onClick),
        shape=RoundedCornerShape(15.dp),
        color=if(selected)Color(0xFF2C649E) else deep.copy(alpha=.78f),
        border=BorderStroke(if(selected)2.dp else 1.dp,
            if(selected)gold else aqua.copy(alpha=.37f))) {
        Text(text,Modifier.padding(horizontal=12.dp,vertical=11.dp),
            color=cloud,fontSize=12.sp,fontWeight=FontWeight.Bold,
            maxLines=1)
    }
}

@Composable
private fun GameCatalogCard(item:GameCatalogItem,grade:Int,
    modifier:Modifier=Modifier,onPlay:()->Unit){
    val locked=item.isLocked(grade)
    val gradeNote=item.gradeNote(grade)
    val fontScale=LocalDensity.current.fontScale
    val cardHeight=(282f+(fontScale-1f).coerceAtLeast(0f)*115f).dp
    Surface(
        modifier=modifier.height(cardHeight)
            .semantics { contentDescription="${item.title}. ${item.focus}. $gradeNote. ${item.difficultyNote()}" }
            .clickable(enabled=!locked,onClick=onPlay),
        shape=RoundedCornerShape(21.dp),
        color=panel,
        border=BorderStroke(1.5.dp,item.accent.copy(alpha=if(locked).36f else .93f))
    ){
        Column(Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement=Arrangement.spacedBy(3.dp)){
            Box(Modifier.fillMaxWidth().height(115.dp).clip(RoundedCornerShape(16.dp))){
                Image(painterResource(item.coverRes),contentDescription=null,
                    contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
                if(locked) Box(Modifier.fillMaxSize()
                    .background(deep.copy(alpha=.74f)),contentAlignment=Alignment.Center){
                    Text("🔒 ${item.minGrade}. SINIF",color=cloud,
                        fontWeight=FontWeight.Black,fontSize=12.sp)
                }
            }
            Text(item.title,Modifier.padding(horizontal=5.dp,vertical=3.dp),
                color=cloud,fontSize=14.sp,lineHeight=17.sp,
                fontWeight=FontWeight.Black,maxLines=2,overflow=TextOverflow.Ellipsis)
            GameCardFacts(item,grade,
                modifier=Modifier.padding(horizontal=5.dp))
            Spacer(Modifier.weight(1f))
            Text(if(locked)"KİLİTLİ" else "▶ OYNA",
                Modifier.padding(horizontal=5.dp,vertical=5.dp),
                color=if(locked)Color.LightGray else gold,
                fontSize=12.sp,fontWeight=FontWeight.Black)
        }
    }
}

@Composable
fun GamesTabScreen(
    modifier:Modifier=Modifier,
    userClass:String="1. Sınıf",
    recentlyOpenedId:String?=null,
    onChangeGrade:()->Unit={}
){
    val context=LocalContext.current
    val grade=GradeAwareOperationPolicy.grade(userClass)
    val catalog=GameCatalog.games
    var filter by rememberSaveable {mutableStateOf("all")}
    val shown=when(filter) {
        "all" -> catalog
        "my_grade" -> catalog.filter { it.isGradeMatched(grade) }
        else -> catalog.filter { it.topic.name==filter }
    }
    val topics=if(filter=="all" || filter=="my_grade")GameTopic.entries.toList()
        else GameTopic.entries.filter { it.name==filter }
    val recentlyOpened=GameCatalog.byId(recentlyOpenedId)?.takeUnless { it.isLocked(grade) }

    Column(
        modifier=modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF086BA9),Color(0xFF07568B),deep)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal=12.dp,vertical=10.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ){
        Surface(shape=RoundedCornerShape(23.dp),
            color=Color(0xFF11365F).copy(alpha=.92f),
            border=BorderStroke(1.dp,aqua.copy(alpha=.53f))){
            Column(Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("🎮 OYUN DÜNYASI",color=gold,
                    fontSize=23.sp,fontWeight=FontWeight.Black)
                Text("${catalog.size} oyun · Konulara göre keşfet",
                    color=cloud,fontSize=13.sp)
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.SpaceBetween) {
                    Text("$grade. sınıf profili",color=aqua,
                        fontWeight=FontWeight.Bold,fontSize=13.sp)
                    TextButton(onClick=onChangeGrade,
                        contentPadding=PaddingValues(horizontal=5.dp,vertical=2.dp)){
                        Text("Sınıfı değiştir ›",color=gold,fontSize=12.sp,
                            fontWeight=FontWeight.Bold)
                    }
                }
                Text("Her oyunun sınıf ilişkisi kartında belirtilir. Bazıları sınıfa göre soru üretir, bazıları beceri tekrarıdır.",
                    color=Color(0xFFCADFEF),fontSize=11.sp,lineHeight=15.sp)
            }
        }

        // Keep the approved premium art; don't turn the hub into a redundant fifth operation card.
        val hub=GameCatalog.operationsHub
        Box(Modifier.fillMaxWidth().height(132.dp)
            .clip(RoundedCornerShape(21.dp))
            .clickable { GameLaunchHistory.open(context,hub) }){
            Image(painterResource(hub.coverRes),contentDescription=null,
                contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                listOf(deep.copy(alpha=.96f),deep.copy(alpha=.11f)))))
            Column(Modifier.align(Alignment.CenterStart).padding(16.dp),
                verticalArrangement=Arrangement.spacedBy(5.dp)){
                Text("✨ DÖRT İŞLEM MERKEZİ",color=gold,
                    fontSize=17.sp,fontWeight=FontWeight.Black)
                Text("Topla · Çıkar · Çarp · Böl",color=cloud,fontSize=12.sp)
                Text("Sınıfa göre işlem macerası  ▶",color=aqua,
                    fontSize=12.sp,fontWeight=FontWeight.Black)
            }
        }

        if(recentlyOpened!=null) {
            Surface(
                modifier=Modifier.fillMaxWidth()
                    .clickable { GameLaunchHistory.open(context,recentlyOpened) },
                shape=RoundedCornerShape(18.dp),color=Color(0xFF15557D),
                border=BorderStroke(1.dp,aqua.copy(alpha=.38f))
            ){
                Row(Modifier.padding(10.dp),
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    Image(painterResource(recentlyOpened.coverRes),
                        contentDescription=null,contentScale=ContentScale.Crop,
                        modifier=Modifier.size(55.dp).clip(RoundedCornerShape(12.dp)))
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                        Text("SON AÇTIĞIN",color=aqua,fontSize=10.sp,
                            fontWeight=FontWeight.Black)
                        Text(recentlyOpened.title,color=cloud,fontSize=14.sp,
                            fontWeight=FontWeight.Bold,maxLines=1,
                            overflow=TextOverflow.Ellipsis)
                    }
                    Text("AÇ ›",color=gold,fontWeight=FontWeight.Black,fontSize=12.sp)
                }
            }
        }

        Row(Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement=Arrangement.spacedBy(8.dp)){
            GameFilterChip("Tümü",filter=="all"){filter="all"}
            GameFilterChip("Sınıfıma göre",filter=="my_grade"){filter="my_grade"}
            GameTopic.entries.forEach{topic->
                GameFilterChip(topic.label,filter==topic.name){filter=topic.name}
            }
        }

        if(filter=="my_grade") Text(
            "Yalnızca sınıf bazlı soru üreten veya bu sınıfa özel beceri sunan oyunlar. Diğer oyunlar Tümü sekmesinde kalır.",
            color=Color(0xFFD4E7F2),fontSize=12.sp,lineHeight=17.sp)

        topics.forEach { topic ->
            val entries=shown.filter { it.topic==topic }
            if(entries.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(),
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.SpaceBetween){
                    Column(Modifier.weight(1f)){
                        Text("${topic.symbol} ${topic.label}",color=cloud,
                            fontSize=17.sp,fontWeight=FontWeight.Black)
                        Text(topic.summary,color=Color(0xFFBCDAED),fontSize=11.sp,
                            maxLines=2,overflow=TextOverflow.Ellipsis)
                    }
                    Text("${entries.size} oyun",color=aqua,fontSize=12.sp)
                }
                BoxWithConstraints(Modifier.fillMaxWidth()){
                    val columns=if(maxWidth<310.dp)1 else 2
                    Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                        entries.chunked(columns).forEach{pair->
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.spacedBy(10.dp)){
                                pair.forEach{item->
                                    GameCatalogCard(item,grade,Modifier.weight(1f)){
                                        GameLaunchHistory.open(context,item)
                                    }
                                }
                                repeat(columns-pair.size){Spacer(Modifier.weight(1f))}
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
