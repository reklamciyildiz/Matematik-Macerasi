package com.yigit033.matematikmacarasi.services

/**
 * Uygulama bağlantı modunu belirten enum sınıfı.
 */
enum class ConnectionMode {
    /**
     * Çevrimdışı mod - Sadece yerel depolama kullanılır
     */
    OFFLINE,
    
    /**
     * Çevrimiçi mod - Supabase bulut depolama kullanılır
     */
    ONLINE
} 