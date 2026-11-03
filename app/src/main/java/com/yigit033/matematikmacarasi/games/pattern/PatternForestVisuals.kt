package com.yigit033.matematikmacarasi.games.pattern

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal object ForestPalette {
    val deep=Color(0xFF0C254D)
    val forest=Color(0xFF155E64)
    val jade=Color(0xFF69E5C6)
    val gold=Color(0xFFFFD77C)
    val cream=Color(0xFFF6FBF7)
    val mist=Color(0xFFC8E5DF)
    val violet=Color(0xFFB9A3FF)
    val colors=listOf(Color(0xFFFF675F),Color(0xFF5EC4FA),
        Color(0xFFFFD66E),Color(0xFF54DFA4),Color(0xFFB77BEE),
        Color(0xFFFFA35C))
}

@Composable
internal fun ForestPanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Surface(modifier=modifier.fillMaxWidth(),shape=RoundedCornerShape(23.dp),
        color=Color(0xFF173F66),border=BorderStroke(1.dp,ForestPalette.jade.copy(alpha=.55f))) {
        Column(Modifier.padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
    }
}

@Composable
internal fun ForestButton(text:String,modifier:Modifier=Modifier,
    enabled:Boolean=true,onClick:()->Unit) {
    Button(onClick=onClick,enabled=enabled,
        modifier=modifier.heightIn(min=54.dp),shape=RoundedCornerShape(16.dp),
        colors=ButtonDefaults.buttonColors(containerColor=ForestPalette.gold,
            contentColor=ForestPalette.deep,disabledContainerColor=Color(0xFF5F7D87)),
        contentPadding=PaddingValues(horizontal=8.dp,vertical=8.dp)) {
        Text(text,fontSize=14.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
    }
}

@Composable
internal fun RuneTile(
    tile:PatternTile,modifier:Modifier=Modifier,selected:Boolean=false,
    enabled:Boolean=true,showLabel:Boolean=false,onClick:(()->Unit)?=null
) {
    val scale by animateFloatAsState(targetValue=if(selected)1.045f else 1f,
        animationSpec=tween(200),label="rune selection")
    val accent=when(tile.kind) {
        PatternKind.COLOR,PatternKind.DUAL->ForestPalette.colors[tile.value%6]
        PatternKind.SHAPE->ForestPalette.colors[(tile.value+1)%6]
        PatternKind.NUMBER->ForestPalette.gold
        PatternKind.DIRECTION->ForestPalette.jade
        PatternKind.SIZE->ForestPalette.violet
    }
    val clickable=if(onClick!=null)Modifier.clickable(enabled=enabled,onClick=onClick)
        else Modifier
    Surface(modifier=modifier.graphicsLayer(scaleX=scale,scaleY=scale)
        .semantics { contentDescription=tile.label() }.then(clickable),
        shape=RoundedCornerShape(15.dp),
        color=if(selected)Color(0xFF2B6587) else Color(0xFF244D79),
        border=BorderStroke(if(selected)2.dp else 1.dp,
            if(selected)ForestPalette.gold else accent.copy(alpha=.65f))) {
        Column(Modifier.padding(5.dp).fillMaxWidth(),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center) {
            Box(Modifier.fillMaxWidth().height(54.dp),contentAlignment=Alignment.Center) {
                when(tile.kind) {
                    PatternKind.NUMBER->Text(tile.value.toString(),color=ForestPalette.cream,
                        fontWeight=FontWeight.Black,fontSize=23.sp)
                    PatternKind.DIRECTION->Text(listOf("↑","→","↓","←")[tile.value%4],
                        color=ForestPalette.jade,fontWeight=FontWeight.Black,fontSize=35.sp)
                    PatternKind.SIZE->Text("◆",color=accent,fontWeight=FontWeight.Black,
                        fontSize=listOf(20,31,41)[tile.value%3].sp)
                    else->RuneGem(tile,accent,Modifier.size(48.dp))
                }
            }
            if(showLabel)Text(tile.label(),color=ForestPalette.cream,fontSize=10.sp,
                fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.Center)
        }
    }
}

@Composable
private fun RuneGem(tile:PatternTile,accent:Color,modifier:Modifier) {
    Canvas(modifier) {
        val cx=size.width/2f
        val cy=size.height/2f
        val radius=size.minDimension*.36f
        if(tile.kind==PatternKind.COLOR) {
            val path=Path().apply {
                moveTo(cx,cy-radius)
                lineTo(cx+radius*.84f,cy-radius*.14f)
                lineTo(cx+radius*.60f,cy+radius*.67f)
                lineTo(cx,cy+radius)
                lineTo(cx-radius*.6f,cy+radius*.67f)
                lineTo(cx-radius*.84f,cy-radius*.14f)
                close()
            }
            drawPath(path,Brush.linearGradient(
                listOf(accent.copy(alpha=.85f),Color.White.copy(alpha=.85f),accent)))
            drawPath(path,ForestPalette.cream.copy(alpha=.8f),style=Stroke(1.6.dp.toPx()))
            val glint=Path().apply {
                moveTo(cx-radius*.4f,cy-radius*.22f)
                lineTo(cx,cy-radius*.76f)
                lineTo(cx+radius*.12f,cy-radius*.17f)
                close()
            }
            drawPath(glint,Color.White.copy(alpha=.65f))
        } else {
            val shape=if(tile.kind==PatternKind.DUAL)tile.accent else tile.value
            val path=Path()
            when(shape%6) {
                0->drawCircle(accent,radius=radius,
                    center=androidx.compose.ui.geometry.Offset(cx,cy))
                1->{
                    path.moveTo(cx,cy-radius)
                    path.lineTo(cx+radius,cy+radius*.9f)
                    path.lineTo(cx-radius,cy+radius*.9f)
                    path.close();drawPath(path,accent)
                }
                2->drawRoundRect(accent,
                    topLeft=androidx.compose.ui.geometry.Offset(cx-radius,cy-radius),
                    size=androidx.compose.ui.geometry.Size(radius*2,radius*2),
                    cornerRadius=androidx.compose.ui.geometry.CornerRadius(radius*.19f))
                3->{
                    repeat(10){i->
                        val r=if(i%2==0)radius else radius*.45f
                        val a=i*PI/5-PI/2
                        val x=cx+cos(a).toFloat()*r
                        val y=cy+sin(a).toFloat()*r
                        if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
                    }
                    path.close();drawPath(path,accent)
                }
                4->{
                    repeat(6){i->
                        val a=i*PI/3-PI/2
                        val x=cx+cos(a).toFloat()*radius
                        val y=cy+sin(a).toFloat()*radius
                        if(i==0)path.moveTo(x,y) else path.lineTo(x,y)
                    }
                    path.close();drawPath(path,accent)
                }
                else->{
                    path.moveTo(cx,cy+radius)
                    path.cubicTo(cx-radius*1.3f,cy+radius*.1f,
                        cx-radius*.95f,cy-radius*.8f,cx,cy-radius*.27f)
                    path.cubicTo(cx+radius*.95f,cy-radius*.8f,
                        cx+radius*1.3f,cy+radius*.1f,cx,cy+radius)
                    path.close();drawPath(path,accent)
                }
            }
            if(shape%6!=0 && shape%6!=2)
                drawPath(path,Color.White.copy(alpha=.68f),style=Stroke(1.3.dp.toPx()))
            if(tile.kind==PatternKind.DUAL)
                drawCircle(ForestPalette.gold,radius=radius*.17f,
                    center=androidx.compose.ui.geometry.Offset(cx+radius*.7f,
                        cy-radius*.68f))
        }
    }
}

@Composable
internal fun EmptyRuneSlot(modifier:Modifier=Modifier,
    label:String="?",highlight:Boolean=false) {
    val pulse=rememberInfiniteTransition(label="pattern portal")
    val glow by pulse.animateFloat(initialValue=.42f,targetValue=.85f,
        animationSpec=infiniteRepeatable(tween(1050),RepeatMode.Reverse),
        label="portal glow")
    Box(modifier.height(64.dp).clip(RoundedCornerShape(15.dp))
        .background(Color(0xFF123F64))
        .border(2.dp,(if(highlight)ForestPalette.gold else ForestPalette.jade)
            .copy(alpha=glow),RoundedCornerShape(15.dp)),
        contentAlignment=Alignment.Center) {
        Text(label,color=ForestPalette.gold,fontSize=23.sp,fontWeight=FontWeight.Black)
    }
}

@Composable
internal fun ChapterStrip(index:Int) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        PatternWorld.entries.forEachIndexed { i,world->
            val complete=index>=(i+1)*6
            val active=index/6==i && index<PatternForestEngine.STOPS
            Surface(shape=RoundedCornerShape(14.dp),
                color=if(active)Color(0xFF2F6C77) else Color(0xFF1B4269),
                border=BorderStroke(if(active)2.dp else 1.dp,
                    if(active||complete)ForestPalette.gold
                    else Color(0xFF4D758C))) {
                Column(Modifier.width(109.dp).padding(9.dp),
                    horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(if(complete)"✓ "+world.symbol else world.symbol,
                        fontSize=19.sp,color=ForestPalette.gold)
                    Text(world.title,color=ForestPalette.cream,fontSize=11.sp,
                        fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.Center)
                    Text(if(complete)"6/6" else if(active)(index%6+1).toString()+"/6"
                        else "Kilidi aç",color=ForestPalette.mist,fontSize=10.sp)
                }
            }
        }
    }
}
