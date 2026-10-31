package com.yigit033.matematikmacarasi.config

/**
 * Supabase API yapılandırmasını içeren sınıf
 * Bu sınıf, Supabase API'sine bağlanmak için gereken temel yapılandırmayı içerir.
 * NOT: Bu yapılandırma şu anda devre dışı bırakılmıştır ve gerçek değerler kullanılmamaktadır.
 */
object SupabaseConfig {
    /**
     * Supabase URL adresi.
     * Şu anda uygulama bulut senkronizasyonu aktif olarak kullanmadığı için
     * geçersiz bir URL kullanılmıştır.
     */
    const val SUPABASE_URL = "https://devre-disi-supabase-url.co"
    
    /**
     * Supabase anonim API anahtarı.
     * Şu anda uygulama bulut senkronizasyonu aktif olarak kullanmadığı için
     * geçersiz bir API anahtarı kullanılmıştır.
     */
    const val SUPABASE_ANON_KEY = "devre-disi-api-anahtari"
    
    /**
     * Kullanıcı tablosunun adı.
     */
    const val USER_TABLE = "user_profiles"
    
    /**
     * Oyun ilerlemesi tablosunun adı.
     */
    const val GAME_PROGRESS_TABLE = "game_progress"
    
    // REST API ve Auth sabitleri
    const val ENDPOINT_AUTH = "auth/v1"
    const val ENDPOINT_REST = "rest/v1"
} 