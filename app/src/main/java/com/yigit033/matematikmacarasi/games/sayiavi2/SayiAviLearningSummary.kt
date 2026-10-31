package com.yigit033.matematikmacarasi.games.sayiavi2

/** Hamle ve ipucu olayları. Kalıcı öğrenmeyi ölçmez. */
data class SayiAviRunCounters(
    val aAttempts: Int = 0, val aErrors: Int = 0,
    val bAttempts: Int = 0, val bErrors: Int = 0,
    val cAttempts: Int = 0, val cErrors: Int = 0,
    val hintsShown: Int = 0,
    val groupAttempts: Int = 0, val groupErrors: Int = 0,
    val transferAttempts: Int = 0, val transferErrors: Int = 0
) {
    fun attempt(stage: Int, correct: Boolean): SayiAviRunCounters = when (stage) {
        1 -> copy(aAttempts = aAttempts + 1, aErrors = aErrors + if (correct) 0 else 1)
        2 -> copy(bAttempts = bAttempts + 1, bErrors = bErrors + if (correct) 0 else 1)
        3 -> copy(cAttempts = cAttempts + 1, cErrors = cErrors + if (correct) 0 else 1)
        4 -> copy(groupAttempts = groupAttempts + 1, groupErrors = groupErrors + if (correct) 0 else 1)
        5 -> copy(transferAttempts = transferAttempts + 1, transferErrors = transferErrors + if (correct) 0 else 1)
        else -> this
    }
    fun hint() = copy(hintsShown = hintsShown + 1)
    // İlk yedi alan sabit: eski sürümün kayıtları aynen okunur.
    fun encode() = listOf(aAttempts,aErrors,bAttempts,bErrors,cAttempts,cErrors,hintsShown,
        groupAttempts,groupErrors,transferAttempts,transferErrors).joinToString(",")
    companion object {
        fun decode(input: String?): SayiAviRunCounters? {
            val values = input?.split(",")?.map { it.toIntOrNull() } ?: return null
            if (values.size !in listOf(7, 11) || values.any { it == null || it < 0 }) return null
            val p = values.map { it!! }
            if (p[1] > p[0] || p[3] > p[2] || p[5] > p[4]) return null
            if (p.size == 11 && (p[8] > p[7] || p[10] > p[9])) return null
            return SayiAviRunCounters(p[0],p[1],p[2],p[3],p[4],p[5],p[6],
                if (p.size == 11) p[7] else 0, if (p.size == 11) p[8] else 0,
                if (p.size == 11) p[9] else 0, if (p.size == 11) p[10] else 0)
        }
    }
}

/** Yalnızca oyun içi hamle oranı; beceri düzeyi veya tanı değildir. */
data class SayiAviLearningSummary(val counters: SayiAviRunCounters, val elapsedSeconds: Long) {
    val complete: Boolean get() = counters.aAttempts > 0 && counters.bAttempts > 0 && counters.cAttempts >= 4
    val attempts: Int get() = counters.aAttempts + counters.bAttempts + counters.cAttempts +
        counters.groupAttempts + counters.transferAttempts
    val errors: Int get() = counters.aErrors + counters.bErrors + counters.cErrors +
        counters.groupErrors + counters.transferErrors
    val actionAccuracyPercent: Int get() = if (attempts == 0) 0 else (attempts - errors) * 100 / attempts
    val playedStages: Int get() = 3 +
        (if (counters.groupAttempts > 0) 1 else 0) + (if (counters.transferAttempts > 0) 1 else 0)
    val firstTryStages: Int get() =
        listOf(counters.aAttempts == 1, counters.bAttempts == 1, counters.cAttempts == 4).count { it } +
        (if (counters.groupAttempts == 1) 1 else 0) +
        (if (counters.transferAttempts == 3) 1 else 0)
    val practiceRecommendation: String get() = when {
        errors == 0 && counters.hintsShown == 0 -> "Üçer saymada yeni bir örüntüyü dene."
        counters.groupErrors > 0 || counters.transferErrors > 0 ->
            "Üçlü grupları nesnelerle tekrar saymayı dene."
        counters.aErrors > 0 -> "Sayı tahtasında 3'ün katlarını tekrar ara."
        counters.bErrors > 0 -> "Şifre kasasında sayıların arasındaki farkı kontrol et."
        else -> "Hazine rotasında üçer ilerleme alıştırması yap."
    }
}
