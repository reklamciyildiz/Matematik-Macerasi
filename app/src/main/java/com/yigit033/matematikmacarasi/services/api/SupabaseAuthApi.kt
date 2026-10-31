package com.yigit033.matematikmacarasi.services.api

import retrofit2.Response
import retrofit2.http.*

/**
 * Supabase Auth API için veri sınıfları
 */
data class SignUpRequest(
    val email: String? = null,
    val password: String? = null,
    val data: Map<String, Any>? = null
)

data class SignInRequest(
    val email: String? = null,
    val password: String? = null
)

data class SignInAnonymouslyRequest(
    val data: Map<String, Any>? = null
)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val user: AuthUser
)

data class AuthUser(
    val id: String,
    val email: String? = null,
    val user_metadata: Map<String, Any>? = null,
    val app_metadata: Map<String, Any>? = null,
    val created_at: String
)

/**
 * Supabase Auth API'si için Retrofit interface'leri.
 */
interface SupabaseAuthApi {
    
    /**
     * Anonim oturum açan API çağrısı.
     */
    @POST("auth/v1/signup")
    suspend fun signUpAnonymously(
        @Body request: SignInAnonymouslyRequest = SignInAnonymouslyRequest(mapOf()),
        @Header("apikey") apiKey: String
    ): Response<AuthResponse>
    
    /**
     * E-posta ve şifre ile kayıt olan API çağrısı.
     */
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SignUpRequest,
        @Header("apikey") apiKey: String
    ): Response<AuthResponse>
    
    /**
     * E-posta ve şifre ile giriş yapan API çağrısı.
     */
    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(
        @Body request: SignInRequest,
        @Header("apikey") apiKey: String
    ): Response<AuthResponse>
    
    /**
     * Oturumu kapatan API çağrısı.
     */
    @POST("auth/v1/logout")
    suspend fun signOut(
        @Header("Authorization") authToken: String,
        @Header("apikey") apiKey: String
    ): Response<Void>
    
    /**
     * Mevcut kullanıcı bilgilerini getiren API çağrısı.
     */
    @GET("auth/v1/user")
    suspend fun getUser(
        @Header("Authorization") authToken: String,
        @Header("apikey") apiKey: String
    ): Response<AuthUser>
} 