package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class GradeAwareOperationPolicyTest {
    @Test fun gradeLimitsAllExistingOperations() {
        for(grade in 1..8)for(level in DifficultyLevel.entries)
            for(op in OperationKind.entries){
                if(!GradeAwareOperationPolicy.allowed(grade,op)){
                    assertTrue(grade<=2)
                    continue
                }
                repeat(100) {seed->
                    val q=GradeAwareOperationPolicy.generate(grade,op,level,Random(seed))
                    assertTrue(q.first>=1 && q.second>=1 && q.answer>=0)
                    assertTrue(GradeAwareOperationPolicy.options(q,Random(seed)).contains(q.answer.toString()))
                    when(op){
                        OperationKind.ADD->assertEquals(q.first+q.second,q.answer)
                        OperationKind.SUBTRACT->assertEquals(q.first-q.second,q.answer)
                        OperationKind.MULTIPLY->assertEquals(q.first*q.second,q.answer)
                        OperationKind.DIVIDE->assertEquals(q.first/q.second,q.answer)
                    }
                    if(grade<=2)assertTrue(q.answer<=GradeAwareOperationPolicy.maxAnswer(grade,level))
                }
            }
    }
    @Test fun parsingUsesTheStudentGradeNotAnArbitraryAge(){
        assertEquals(2,GradeAwareOperationPolicy.grade("2. Sınıf"))
        assertEquals(1,GradeAwareOperationPolicy.grade(""))
        assertEquals(4,GradeAwareOperationPolicy.grade("4. sınıf"))
        assertFalse(GradeAwareOperationPolicy.allowed(2,OperationKind.DIVIDE))
        assertTrue(GradeAwareOperationPolicy.allowed(3,OperationKind.DIVIDE))
    }
}
