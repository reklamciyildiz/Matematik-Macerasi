package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yigit033.matematikmacarasi.learning.GradeCurriculumCatalog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real emulator/phone executable tests. Building this APK in CI only proves
 * test compilation. Run connectedAndroidTest on a device before release.
 */
@RunWith(AndroidJUnit4::class)
class UpperGradeConceptExplorerAndroidTest {
    @get:Rule val compose = createComposeRule()

    @Test fun gradeSevenRationalLineMovesInExactHalves() {
        val topic = GradeCurriculumCatalog.forGrade(7).topics.first {
            it.id == "g7_numbers"
        }
        compose.setContent { UpperGradeConceptExplorer(topic) }
        compose.onNodeWithText("İşaretli değer: -2").assertExists()
        compose.onNodeWithText("Sonraki örnek").performClick()
        compose.onNodeWithText("İşaretli değer: -3/2").assertExists()
    }

    @Test fun gradeEightSquaresStayMathematicallyTrueAcrossSteps() {
        val topic = GradeCurriculumCatalog.forGrade(8).topics.first {
            it.id == "g8_powers"
        }
        compose.setContent { UpperGradeConceptExplorer(topic) }
        compose.onNodeWithText("1² = 1 · √1 = 1").assertExists()
        compose.onNodeWithText("Sonraki örnek").performClick()
        compose.onNodeWithText("2² = 4 · √4 = 2").assertExists()
    }

    @Test fun gradeFiveStartsWithNaturalNumbers() {
        val topic = GradeCurriculumCatalog.forGrade(5).topics.first {
            it.id == "g5_numbers"
        }
        compose.setContent { UpperGradeConceptExplorer(topic) }
        compose.onNodeWithText("İşaretli değer: 0").assertExists()
        compose.onNodeWithText("Sonraki örnek").performClick()
        compose.onNodeWithText("İşaretli değer: 1").assertExists()
    }

    @Test fun gradeSixLinearRelationshipUpdatesBothNumberAndExplanation() {
        val topic = GradeCurriculumCatalog.forGrade(6).topics.first {
            it.id == "g6_algebra"
        }
        compose.setContent { UpperGradeConceptExplorer(topic) }
        compose.onNodeWithText("x=-2 iken y=-1. x bir artarsa y iki artar.").assertExists()
        compose.onNodeWithText("Sonraki örnek").performClick()
        compose.onNodeWithText("x=-1 iken y=1. x bir artarsa y iki artar.").assertExists()
    }
}
