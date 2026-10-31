package com.yigit033.matematikmacarasi.services.api

import com.yigit033.matematikmacarasi.config.SupabaseConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.concurrent.TimeUnit

/**
 * Retrofit istemcisini oluşturan ve yöneten sınıf.
 */
object RetrofitClient {
    
    private const val TIMEOUT_SECONDS = 60L
    
    /**
     * Supabase API için Retrofit istemcisi oluşturur.
     */
    fun createSupabaseApi(): SupabaseApi {
        return createRetrofit(SupabaseConfig.SUPABASE_URL)
            .create(SupabaseApi::class.java)
    }
    
    /**
     * Supabase Auth API için Retrofit istemcisi oluşturur.
     */
    fun createSupabaseAuthApi(): SupabaseAuthApi {
        return createRetrofit(SupabaseConfig.SUPABASE_URL)
            .create(SupabaseAuthApi::class.java)
    }
    
    /**
     * Retrofit istemcisi oluşturur.
     * @param baseUrl API'nin temel URL'si
     */
    private fun createRetrofit(baseUrl: String): Retrofit {
        val gson = GsonBuilder().create()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(object : retrofit2.Converter.Factory() {
                override fun responseBodyConverter(
                    type: java.lang.reflect.Type,
                    annotations: Array<out Annotation>,
                    retrofit: Retrofit
                ): retrofit2.Converter<okhttp3.ResponseBody, *> {
                    return retrofit2.Converter<okhttp3.ResponseBody, Any> { responseBody ->
                        val json = responseBody.string()
                        if (json.isEmpty()) {
                            null
                        } else {
                            gson.fromJson(json, type)
                        }
                    }
                }
            })
            .build()
    }
    
    /**
     * OkHttpClient istemcisi oluşturur.
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}
