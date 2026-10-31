package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

import android.content.Context

/** Bölge 2 verileri ayrı alanda. Bölge 1, Premium ve yıldız anahtarlarına dokunmaz. */
class ForestProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("forest_adventure_v1", Context.MODE_PRIVATE)
    private val lock = Any()

    fun load(): ForestSession {
        val stage = runCatching {
            ForestStage.valueOf(prefs.getString("stage", ForestStage.INTRO.name) ?: "INTRO")
        }.getOrDefault(ForestStage.INTRO)
        val candidate = ForestSession(
            stage = stage,
            seed = prefs.getLong("seed", 0L),
            startedAt = prefs.getLong("start", 0L),
            bridgeSpan = prefs.getInt("bridge_span", 0),
            bridgeFill = prefs.getInt("bridge_fill", 0),
            leafRound = prefs.getInt("leaves", 0),
            trailStep = prefs.getInt("trail", 0),
            rootRound = prefs.getInt("roots", 0),
            attempts = prefs.getInt("attempts", 0),
            errors = prefs.getInt("errors", 0),
            hints = prefs.getInt("hints", 0),
            tier = LearningTier.fromSaved(prefs.getString("run_tier", null))
        )
        return if (candidate.valid()) candidate else ForestSession()
    }

    private fun commit(session: ForestSession): Boolean = prefs.edit()
        .putString("stage", session.stage.name)
        .putLong("seed", session.seed)
        .putLong("start", session.startedAt)
        .putInt("bridge_span", session.bridgeSpan)
        .putInt("bridge_fill", session.bridgeFill)
        .putInt("leaves", session.leafRound)
        .putInt("trail", session.trailStep)
        .putInt("roots", session.rootRound)
        .putInt("attempts", session.attempts)
        .putInt("errors", session.errors)
        .putInt("hints", session.hints)
        .putString("run_tier", session.tier.name)
        .commit()

    fun save(session: ForestSession): Boolean = synchronized(lock) {
        session.valid() && commit(session)
    }

    fun startNew(tier: LearningTier = LearningTier.GELISEN): ForestSession = synchronized(lock) {
        val now = System.currentTimeMillis().coerceAtLeast(1L)
        val next = ForestSession(stage = ForestStage.BRIDGE, seed = now, startedAt = now, tier = tier)
        if (commit(next)) next else load()
    }

    fun completedRuns(): Int = prefs.getInt("completed_runs", 0)
    fun bestAccuracy(): Int = prefs.getInt("best_accuracy", 0)
    fun hasCompleted(): Boolean = prefs.getBoolean("completed_ever", false)
    fun wasStarAwarded(): Boolean = prefs.getBoolean("star_awarded", false)

    fun markStarAwarded(): Boolean = synchronized(lock) {
        hasCompleted() && prefs.edit().putBoolean("star_awarded", true).commit()
    }

    /** Yalnızca ROOTS/3 durumundan SUMMARY'ye tek kez geçebilir.
     * Herhangi bir ekranın çift callback'i tamamlanmayı iki kere sayamaz.
     */
    fun finish(session: ForestSession): Boolean = synchronized(lock) {
        if (load().stage != ForestStage.ROOTS || load().rootRound != 3 ||
            session.stage != ForestStage.ROOTS || session.rootRound != 3) return@synchronized false
        val editor = prefs.edit()
            .putString("stage", ForestStage.SUMMARY.name)
            .putBoolean("completed_ever", true)
            .putInt("completed_runs", completedRuns() + 1)
            .putInt("best_accuracy", maxOf(bestAccuracy(), session.accuracy))
        editor.commit()
    }
}
