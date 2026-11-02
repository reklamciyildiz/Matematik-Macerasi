package com.yigit033.matematikmacarasi.games.interactive

import kotlin.math.abs
import kotlin.math.sin

/**
 * Deterministic normalized-space projectile physics; pure Kotlin and replayable.
 * Frame time is deliberately local/visual. Only a confirmed target hit is persisted.
 */
data class SpaceLaser(val id:Int,val x:Float,val y:Float)
data class SpaceTarget(val id:Int,val value:Int,val baseX:Float,val phase:Float)
data class SpaceTargetPosition(val x:Float,val y:Float)
data class SpaceCombatFrame(
    val elapsed:Float=0f,
    val lasers:List<SpaceLaser> = emptyList(),
    val nextLaserId:Int=1
)
data class SpaceCombatTick(val frame:SpaceCombatFrame,val impact:Int?=null)

object SpaceCombatPhysics {
    private const val SHOT_SPEED=1.30f
    fun targets(run:SpaceRun):List<SpaceTarget> {
        val q=SpaceExpeditionEngine.puzzle(run)
        val values=when(run.stage) {
            0->q.crystals
            1->listOf(-5,1,Int.MIN_VALUE,5,10)
            else->q.nodes
        }
        return values.mapIndexed {index,value->
            val x=(index+.5f)/values.size
            SpaceTarget(index,value,x,index*.19f)
        }
    }
    fun position(target:SpaceTarget,time:Float,tier:Int):SpaceTargetPosition {
        val speed=when(tier.coerceIn(0,2)){
            0->.102f
            1->.122f
            else->.143f
        }
        // Enemies move downward, loop when off-screen. Center target is stable enough
        // to aim without the game becoming a static multiple-choice exercise.
        val vertical=(time.coerceAtLeast(0f)*speed+target.phase)%1.0f
        val sway=sin(time*1.05f+target.id*1.7f)*.025f
        return SpaceTargetPosition(
            (target.baseX+sway).coerceIn(.07f,.93f),
            -.10f+vertical*.84f
        )
    }
    fun fire(frame:SpaceCombatFrame,shipX:Float):SpaceCombatFrame {
        if(!shipX.isFinite()||frame.lasers.size>=7)return frame
        return frame.copy(
            lasers=frame.lasers+SpaceLaser(frame.nextLaserId,shipX.coerceIn(.06f,.94f),.81f),
            nextLaserId=frame.nextLaserId+1
        )
    }
    fun advance(
        frame:SpaceCombatFrame,
        dt:Float,
        targets:List<SpaceTarget>,
        tier:Int
    ):SpaceCombatTick {
        if(!dt.isFinite() || dt<=0f)return SpaceCombatTick(frame)
        val nextTime=frame.elapsed+dt.coerceAtMost(.06f)
        val survivors=ArrayList<SpaceLaser>(frame.lasers.size)
        var hit:Int?=null
        for(laser in frame.lasers) {
            val nextY=laser.y-dt.coerceAtMost(.06f)*SHOT_SPEED
            if(nextY<-.14f)continue
            if(hit==null) {
                val struck=targets.firstOrNull{target->
                    val position=position(target,nextTime,tier)
                    abs(laser.x-position.x)<.082f &&
                        abs(nextY-position.y)<.078f
                }
                if(struck!=null) {
                    hit=struck.value
                    continue
                }
            }
            survivors.add(laser.copy(y=nextY))
        }
        return SpaceCombatTick(
            frame.copy(elapsed=nextTime,lasers=survivors),
            hit
        )
    }
}
