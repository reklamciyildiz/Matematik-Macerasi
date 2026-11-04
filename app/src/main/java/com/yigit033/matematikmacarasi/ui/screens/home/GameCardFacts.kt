package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

/** Consistent learning topic, school-grade relationship and difficulty on every game card. */
@Composable
fun GameCardFacts(game:GameCatalogItem,grade:Int,
    compact:Boolean=false,modifier:Modifier=Modifier,onDark:Boolean=true){
    val text=if(onDark)Color(0xFFC5E8F5) else Color(0xFF16466E)
    val accent=if(onDark)Color(0xFF8CF3F6) else Color(0xFF175A73)
    Column(modifier,verticalArrangement=Arrangement.spacedBy(if(compact)1.dp else 3.dp)){
        Text(game.focus,color=text,fontSize=if(compact)10.sp else 11.sp,
            lineHeight=if(compact)12.sp else 14.sp,maxLines=1,
            overflow=TextOverflow.Ellipsis)
        Text(if(compact)game.compactGradeNote(grade) else game.gradeNote(grade),
            color=accent,fontWeight=FontWeight.Bold,
            fontSize=if(compact)10.sp else 11.sp,
            lineHeight=if(compact)12.sp else 14.sp,
            maxLines=if(compact)1 else 2,overflow=TextOverflow.Ellipsis)
        Text(game.difficultyNote(),color=text,
            fontSize=if(compact)9.sp else 10.sp,
            lineHeight=if(compact)11.sp else 13.sp,
            maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}
