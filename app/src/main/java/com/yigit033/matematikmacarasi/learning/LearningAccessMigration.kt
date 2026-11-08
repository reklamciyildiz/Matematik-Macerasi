package com.yigit033.matematikmacarasi.learning

import android.content.Context

/**
 * Free lessons previously completed before Ö6's paywall remain accessible.
 * This snapshot happens exactly once on first launch after the update;
 * newly completed Premium lessons are NOT added to the grandfather list.
 */
class LearningAccessMigration(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("o6_learning_access_v1", Context.MODE_PRIVATE)

    init {
        if (!prefs.getBoolean("captured", false)) {
            val oldProgress = LearningJourneyProgress(context)
            val previouslyCompleted = (1..8).flatMap {
                LearningPathCatalog.forGrade("$it. Sınıf")
            }.map { it.id }.distinct()
                .filter { oldProgress.completed(it) }
                .toSet()
            prefs.edit()
                .putStringSet("legacy_completed", previouslyCompleted)
                .putBoolean("captured", true)
                .commit()
        }
    }

    fun grandfatheredIds(): Set<String> =
        prefs.getStringSet("legacy_completed", emptySet())?.toSet() ?: emptySet()
}
