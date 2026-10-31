package com.yigit033.matematikmacarasi.services.api

import com.yigit033.matematikmacarasi.services.SupabaseGameProgress
import com.yigit033.matematikmacarasi.services.SupabaseUser
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Supabase REST API'si için Retrofit interface'leri.
 * Bu interface'ler, Supabase postgrest API'si ile etkileşim kurulmasını sağlar.
 */
interface SupabaseApi {
    
    /**
     * Kullanıcı bilgilerini getiren API çağrısı.
     * @param userId Kullanıcı ID'si
     * @return Kullanıcı bilgisi
     */
    @GET("rest/v1/users")
    suspend fun getUser(
        @Query("id") userId: String,
        @Query("select") select: String = "*",
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String
    ): Response<List<SupabaseUser>>
    
    /**
     * Yeni kullanıcı oluşturan API çağrısı.
     * @param user Oluşturulacak kullanıcı
     * @return API yanıtı
     */
    @POST("rest/v1/users")
    suspend fun createUser(
        @Body user: SupabaseUser,
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<SupabaseUser>
    
    /**
     * Kullanıcı bilgilerini güncelleyen API çağrısı.
     * @param userId Güncellenecek kullanıcı ID'si
     * @param updates Güncellenecek alanlar
     * @return API yanıtı
     */
    @PATCH("rest/v1/users")
    suspend fun updateUser(
        @Query("id") userId: String,
        @Body updates: Map<String, Any>,
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<SupabaseUser>
    
    /**
     * Belirli bir oyun için ilerleme bilgisini getiren API çağrısı.
     * @param userId Kullanıcı ID'si
     * @param gameId Oyun ID'si
     * @return Oyun ilerleme bilgisi
     */
    @GET("rest/v1/game_progress")
    suspend fun getGameProgress(
        @Query("user_id") userId: String,
        @Query("game_id") gameId: String,
        @Query("select") select: String = "*",
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String
    ): Response<List<SupabaseGameProgress>>
    
    /**
     * Yeni oyun ilerlemesi oluşturan API çağrısı.
     * @param progress Kaydedilecek ilerleme
     * @return API yanıtı
     */
    @POST("rest/v1/game_progress")
    suspend fun createGameProgress(
        @Body progress: SupabaseGameProgress,
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<SupabaseGameProgress>
    
    /**
     * Oyun ilerlemesini güncelleyen API çağrısı.
     * @param id Güncellenecek ilerleme ID'si
     * @param updates Güncellenecek alanlar
     * @return API yanıtı
     */
    @PATCH("rest/v1/game_progress")
    suspend fun updateGameProgress(
        @Query("id") id: Int,
        @Body updates: Map<String, Any>,
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String,
        @Header("Prefer") prefer: String = "return=representation"
    ): Response<SupabaseGameProgress>
    
    /**
     * Bir kullanıcının tüm oyun ilerlemelerini getiren API çağrısı.
     * @param userId Kullanıcı ID'si
     * @return Oyun ilerlemeleri listesi
     */
    @GET("rest/v1/game_progress")
    suspend fun getAllGameProgress(
        @Query("user_id") userId: String,
        @Query("select") select: String = "*",
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String
    ): Response<List<SupabaseGameProgress>>
    
    /**
     * Supabase bağlantısını test etmek için basit bir API çağrısı.
     * @return API yanıtı
     */
    @GET("rest/v1/")
    suspend fun testConnection(
        @Header("Authorization") apiKey: String,
        @Header("apikey") authKey: String
    ): Response<ResponseBody>
} 