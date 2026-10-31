package com.yigit033.matematikmacarasi.games.sayiavi2

/** Hamle ve ipucu olayları. Kalıcı öğrenmeyi ölçmez. */
data class SayiAviRunCounters(
    val aAttempts: Int = 0, val aErrors: Int = 0,
    val bAttempts: Int = 0, val bErrors: Int = 0,
    val cAttempts: Int = 0, val cErrors: Int = 0,
    val hintsShown: Int = 0
) {
    fun attempt(stage: Int, correct: Boolean): SayiAviRunCounters = when (stage) {
        1 -> copy(aAttempts = aAttempts + 1, aErrors = aErrors + if (correct) 0 else 1)
        2 -> copy(bAttempts = bAttempts + 1, bErrors = bErrors + if (correct) 0 else 1)
        3 -> copy(cAttempts = cAttempts + 1, cErrors = cErrors + if (correct) 0 else 1)
        else -> this
    }
    fun hint() = copy(hintsShown = hintsShown + 1)
    fun encode() = listOf(aAttempts,aErrors,bAttempts,bErrors,cAttempts,cErrors,hintsShown).joinToString(",")
    companion object {
        fun decode(input: String?): SayiAviRunCounters? {
            val values = input?.split(",")?.map { it.toIntOrNull() } ?: return null
            if (values.size != 7 || values.any { it == null || it < 0 }) return null
            val p = values.map { it!! }
            if (p[1] > p[0] || p[3] > p[2] || p[5] > p[4]) return null
            return SayiAviRunCounters(p[0],p[1],p[2],p[3],p[4],p[5],p[6])
        }
    }
}

/** Yalnızca oyun içi hamle oranı; beceri düzeyi veya tanı değildir. */
data class SayiAviLearningSummary(val counters: SayiAviRunCounters, val elapsedSeconds: Long) {
    val complete: Boolean get() = counters.aAttempts > 0 && counters.bAttempts > 0 && counters.cAttempts >= 4
    val attempts: Int get() = counters.aAttempts + counters.bAttempts + counters.cAttempts
    val errors: Int get() = counters.aErrors + counters.bErrors + counters.cErrors
    val actionAccuracyPercent: Int get() = if (attempts == 0) 0 else (attempts - errors) * 100 / attempts
    val firstTryStages: Int get() =
        listOf(counters.aAttempts == 1, counters.bAttempts == 1, counters.cAttempts == 4).count { it }
}
