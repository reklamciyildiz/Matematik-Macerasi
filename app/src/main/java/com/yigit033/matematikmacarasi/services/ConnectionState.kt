package com.yigit033.matematikmacarasi.services

/**
 * Supabase bağlantı durumunu belirten enum sınıfı.
 */
enum class ConnectionState {
    /**
     * Bağlantı yok veya başlatılmamış
     */
    DISCONNECTED,
    
    /**
     * Bağlantı kurulmaya çalışılıyor
     */
    CONNECTING,
    
    /**
     * Bağlantı kuruldu
     */
    CONNECTED,
    
    /**
     * Bağlantı hatası
     */
    ERROR
} 