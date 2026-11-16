package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.util.Base64

/**
 * Exports ONLY app-authored fixed teaching sentences, never student data.
 * Opt-in: MM_NARRATION_EXPORT=1 ./gradlew :app:testDebugUnitTest
 * Only then does the build create build/narration/manifest.tsv.
 */
class LessonVoiceManifestTest {
    private fun rawNarrationTexts(): List<String> = buildList {
        for (grade in 1..8) {
            for (lesson in LearningPathCatalog.forGrade("$grade. Sınıf")) {
                val script = LessonTeachingCatalog.forLesson(lesson)
                add(script.headline + ". " + script.explanationFor(grade))
                add(lesson.hint)
                val last = script.steps.lastIndex
                for ((i, step) in script.steps.withIndex()) {
                    if (i != last) {
                        add(step)
                    } else {
                        for (choices in listOf(
                            script.check.choices, script.check.choices.reversed()
                        )) {
                            add(step + ". " + script.check.prompt + ". Seçenekler: " +
                                choices.joinToString(". "))
                        }
                    }
                }
                val total = LearningPathCatalog.practiceQuestionCount(lesson)
                add("Ders tamamlandı. $total soruluk çalışmanı bitirdin.")
            }
        }
    }.map { it.trim() }.filter { it.isNotEmpty() }.distinct().sorted()

    private fun sha(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 255) }

    @Test fun voiceManifestIncludesAllRealGradeSpecificLearningScripts() {
        val lines = rawNarrationTexts()
        assertTrue("No recorded teaching content", lines.size >= 200)
        val hashes = lines.map(::sha)
        assertEquals("Hash collision or repeated text", lines.size, hashes.toSet().size)
        assertTrue(lines.none { it.contains("\n") || it.isBlank() })
        assertTrue(lines.all { TurkishMathSpeech.normalize(it).isNotBlank() })
        if (System.getenv("MM_NARRATION_EXPORT") == "1") {
            val output = File(System.getProperty("user.dir"), "build/narration/manifest.tsv")
            output.parentFile?.mkdirs()
            output.bufferedWriter(Charsets.UTF_8).use { writer ->
                for (line in lines) {
                    val speech = TurkishMathSpeech.normalize(line)
                    val base64 = Base64.getEncoder().encodeToString(
                        speech.toByteArray(Charsets.UTF_8)
                    )
                    // Key by the exact source text. Changed content never
                    // reuses an out-of-date MP3, even when normalized alike.
                    writer.append(sha(line)).append('\t').append(base64).append('\n')
                }
            }
            assertTrue(output.length() > 1000)
        }
    }
}
