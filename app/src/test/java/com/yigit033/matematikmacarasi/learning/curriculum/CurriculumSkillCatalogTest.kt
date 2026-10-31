package com.yigit033.matematikmacarasi.learning.curriculum

import org.junit.Assert.*
import org.junit.Test

class CurriculumSkillCatalogTest {
    @Test fun official2026ScopeDoesNotMislabelFourthGrade() {
        assertEquals(CurriculumProgram.TYMM_2026_27, CurriculumSkillCatalog.programForGrade(2))
        assertEquals(CurriculumProgram.TYMM_2026_27, CurriculumSkillCatalog.programForGrade(3))
        assertEquals(CurriculumProgram.LEGACY_2026_27, CurriculumSkillCatalog.programForGrade(4))
        assertEquals(CurriculumProgram.LEGACY_2026_27, CurriculumSkillCatalog.programForGrade(8))
    }
    @Test fun onlyVerifiedPilotOutcomesHaveOfficialCodes() {
        val map = CurriculumSkillCatalog.mapping
        assertTrue(map.isNotEmpty())
        map.forEach {
            assertTrue(it.schoolGrade in 1..3)
            assertNotNull(it.officialSourceUrl)
            assertTrue(it.officialSourceUrl!!.startsWith("https://tymm.meb.gov.tr/"))
            assertTrue(it.outcomeCode!!.startsWith("MAT."))
            assertFalse(it.note.isBlank())
        }
        assertEquals("MAT.2.2.4",
            CurriculumSkillCatalog.forGradeAndSkill(2, "count_4")?.outcomeCode)
        assertEquals(CurriculumMatch.REVIEW_FROM_PREVIOUS_GRADE,
            CurriculumSkillCatalog.forGradeAndSkill(3, "count_4")?.relation)
        assertNull(CurriculumSkillCatalog.forGradeAndSkill(4, "multiply"))
    }
    @Test fun neverClaimsCompleteCurriculumOrUnverifiedGrade() {
        assertTrue(CurriculumSkillCatalog.displayStatus(4, "multiply").contains("yapılmadı"))
        assertTrue(CurriculumSkillCatalog.displayStatus(8, "count_3").contains("yapılmadı"))
        assertTrue(CurriculumSkillCatalog.gradeScopeNotice("4. Sınıf").contains("önceki program"))
    }
}
