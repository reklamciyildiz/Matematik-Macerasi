package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Opened activities only. Never present them as saved checkpoints or completions. */
@Composable
fun RecentGamesSection(recentIds:List<String>,grade:Int,modifier:Modifier=Modifier){
    val context=LocalContext.current
    val items=recentIds.mapNotNull(GameCatalog::byId)
        .filterNot{it.isLocked(grade)}.take(3)
    if(items.isEmpty())return
    Column(modifier.fillMaxWidth().padding(vertical=9.dp),
        verticalArrangement=Arrangement.spacedBy(9.dp)){
        Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically){
            Text("↻ Son Açtıkların",fontSize=18.sp,color=Color.White,
                fontWeight=FontWeight.Black)
            Text("Yeniden keşfet",fontSize=11.sp,color=Color(0xFFA9DEF2))
        }
        LazyRow(horizontalArrangement=Arrangement.spacedBy(9.dp),
            contentPadding=PaddingValues(horizontal=16.dp)){
            items(items,key={it.id}){game->
                Surface(modifier=Modifier.width(218.dp).height(90.dp)
                    .clickable{GameLaunchHistory.open(context,game)},
                    shape=RoundedCornerShape(18.dp),
                    color=Color(0xFF103E68),
                    border=BorderStroke(1.dp,game.accent.copy(alpha=.85f))){
                    Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.spacedBy(9.dp)){
                        Image(painterResource(game.coverRes),contentDescription=null,
                            modifier=Modifier.size(70.dp),contentScale=ContentScale.Crop)
                        Column(Modifier.weight(1f),
                            verticalArrangement=Arrangement.spacedBy(5.dp)){
                            Text(game.title,color=Color.White,fontSize=13.sp,
                                fontWeight=FontWeight.Bold,maxLines=2,
                                lineHeight=15.sp,overflow=TextOverflow.Ellipsis)
                            Text(game.compactGradeNote(grade),
                                color=Color(0xFF8DF0F5),fontSize=10.sp,maxLines=1,
                                overflow=TextOverflow.Ellipsis)
                            Text("Yeniden aç ›",color=Color(0xFFFFDB7E),
                                fontSize=10.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
