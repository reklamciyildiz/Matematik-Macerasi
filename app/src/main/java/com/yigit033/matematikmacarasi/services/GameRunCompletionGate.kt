package com.yigit033.matematikmacarasi.services

/**
 * Bir çalışmanın bitiş yan etkilerini (görev, istatistik ve ödül) bir kez çalıştırır.
 * Yeni oyun/tekrar/sonraki seviyeye geçildiğinde reset() çağrılmalıdır.
 * Aktivite içi tek iş parçacığından (Android main thread) kullanılmak üzere tasarlanmıştır.
 */
class GameRunCompletionGate {
    private var completed = false

    fun tryFinish(): Boolean {
        if (completed) return false
        completed = true
        return true
    }

    fun reset() {
        completed = false
    }
}
