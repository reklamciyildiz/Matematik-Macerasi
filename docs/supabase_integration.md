# Matematik Macerası - Supabase Entegrasyonu Rehberi

Bu belge, Matematik Macerası uygulamasının Supabase ile entegrasyonu için önemli bilgiler içerir. Kullanıcıların Supabase'de görünmesi ve verilerin doğru şekilde kaydedilmesi için yapılması gerekenler detaylı olarak açıklanmıştır.

## 1. Gerekli Bağımlılıklar

Aşağıdaki bağımlılıkları `app/build.gradle.kts` dosyasına ekleyin:

```kotlin
plugins {
    // Mevcut eklentiler
    kotlin("plugin.serialization") version "1.9.20"
}

dependencies {
    // Mevcut bağımlılıklar
    
    // Retrofit ve networking bağımlılıkları
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}
```

## 2. SupabaseService Sınıfında Kritik Değişiklikler

### 2.1. Kullanıcı Oluşturma (signInAnonymously metodu)

```kotlin
private suspend fun signInAnonymously() {
    try {
        Log.d(TAG, "🔄 Anonim kullanıcı oluşturuluyor...")
        
        // Benzersiz bir kullanıcı ID ve kullanıcı adı oluştur
        val newUserId = UUID.randomUUID().toString()
        val username = "user_${Random.nextInt(10000, 99999)}"
        
        // Kullanıcı ID'yi lokalde kaydet
        userId = newUserId
        prefs.edit()
            .putString(KEY_USER_ID, newUserId)
            .putBoolean(KEY_IS_REGISTERED, true)
            .apply()
        
        Log.d(TAG, "✅ Yeni kullanıcı oluşturuldu. ID: $newUserId, Username: $username")
        _connectionState.value = ConnectionState.CONNECTED
        
        // Kullanıcı profilini oluştur (3 deneme)
        var created = false
        var attempts = 0
        
        while (!created && attempts < 3) {
            attempts++
            Log.d(TAG, "🔄 Kullanıcı profili oluşturma girişimi $attempts/3")
            
            try {
                val userProfile = UserProfile(
                    id = newUserId,
                    username = username,
                    created_at = System.currentTimeMillis().toString()
                )
                
                val supabaseApi = RetrofitClient.createSupabaseApi(SupabaseConfig.SUPABASE_URL)
                val response = withContext(Dispatchers.IO) {
                    try {
                        supabaseApi.createUserProfile(
                            apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                            userProfile = userProfile
                        )
                        true
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ API profil oluşturma hatası: ${e.message}")
                        false
                    }
                }
                
                if (response) {
                    created = true
                    Log.d(TAG, "✅ Kullanıcı profili başarıyla oluşturuldu")
                    break
                } else {
                    Log.w(TAG, "⚠️ Kullanıcı profili oluşturulamadı, yeniden deneniyor...")
                    delay(1000) // Tekrar denemeden önce kısa bir bekleme
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Profil oluşturma hatası: ${e.message}")
            }
        }
        
        if (!created) {
            Log.e(TAG, "❌ Kullanıcı profili oluşturulamadı (3 deneme sonrası)")
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Kullanıcı oluşturma hatası", e)
        _connectionState.value = ConnectionState.ERROR
    }
}
```

### 2.2. Bağlantı Testi (testConnection metodu)

```kotlin
suspend fun testConnection(): Boolean {
    try {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.w(TAG, "⚠️ Bağlantı durumu: ${_connectionState.value}")
            // Bağlantı durumunu kontrol et
            checkAndInitializeSession()
            delay(1000) // Kısa bir gecikme
        }
        
        // Kullanıcı ID kontrolü
        if (userId == null) {
            Log.e(TAG, "❌ Bağlantı testi başarısız: User ID null")
            return false
        }
        
        // Sunucu bağlantısını test et
        try {
            Log.d(TAG, "🔄 Supabase API ping testi yapılıyor...")
            val supabaseApi = RetrofitClient.createSupabaseApi(SupabaseConfig.SUPABASE_URL)
            
            val testResponse = withContext(Dispatchers.IO) {
                try {
                    // basit bir test call
                    val response = supabaseApi.testConnection()
                    response.isSuccessful
                } catch (e: Exception) {
                    Log.e(TAG, "❌ API ping hatası: ${e.message}")
                    false
                }
            }
            
            if (!testResponse) {
                Log.e(TAG, "❌ Sunucu bağlantı testi başarısız")
                return false
            }
            
            Log.d(TAG, "✅ Sunucu bağlantı testi başarılı")
            
            // Kullanıcı varlığını kontrol et
            Log.d(TAG, "🔄 Kullanıcı varlığı kontrol ediliyor: $userId")
            val userExists = withContext(Dispatchers.IO) {
                try {
                    val userProfiles = supabaseApi.getUserProfile(
                        apiKey = SupabaseConfig.SUPABASE_ANON_KEY,
                        userId = userId!!,
                        select = "*"
                    )
                    
                    userProfiles.isNotEmpty()
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Kullanıcı varlığı kontrolü sırasında hata: ${e.message}")
                    false
                }
            }
            
            if (!userExists) {
                Log.w(TAG, "⚠️ Kullanıcı profili bulunamadı, yeniden oluşturulacak")
                signInAnonymously() // Kullanıcıyı tekrar oluştur
                return true // İşlem başarılı sayalım
            }
            
            Log.d(TAG, "✅ Kullanıcı profili bulundu ve aktif")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Bağlantı testi genel hatası", e)
            return false
        }
        
        Log.d(TAG, "✅ Bağlantı testi tamamen başarılı")
        return true
    } catch (e: Exception) {
        Log.e(TAG, "❌ Bağlantı testi ana hatası", e)
        return false
    }
}
```

## 3. RetrofitClient Sınıfında Converter Factory Eklemeleri

Retrofit istemcisini yapılandıran sınıfta, JSON ve string response'ları işleyebilmek için converter factory'ler ekleyeceğiz:

```kotlin
fun createRetrofitBuilder(baseUrl: String, useUnsafeClient: Boolean = false): Retrofit {
    val httpClient = if (useUnsafeClient) getUnsafeOkHttpClient() else getOkHttpClient()
    
    Log.d(TAG, "🔨 Retrofit builder oluşturuluyor: $baseUrl")
    
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        // String yanıtları işlemek için converter
        .addConverterFactory(retrofit2.converter.scalars.ScalarsConverterFactory.create())
        // JSON yanıtları işlemek için converter
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
}
```

## 4. SupabaseApi Sınıfında Tablo Adı Güncellemeleri

```kotlin
/**
 * Kullanıcı bilgilerini getirir.
 */
@GET("$REST_ENDPOINT/${SupabaseConfig.USER_TABLE}")
suspend fun getUserProfile(
    @Header("apikey") apiKey: String,
    @Query("id") userId: String,
    @Query("select") select: String = "*"
): List<UserProfile>

/**
 * Kullanıcı profili oluşturur.
 */
@POST("$REST_ENDPOINT/${SupabaseConfig.USER_TABLE}")
suspend fun createUserProfile(
    @Header("apikey") apiKey: String,
    @Body userProfile: UserProfile
): Response<Void>
```

## 5. SupabaseConfig Ayarları

SupabaseConfig sınıfında doğru tablo adları ve URL'lerin tanımlandığından emin olun:

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://klveiaimfacfegosrcz.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtsdmVpYWltZmFjZmVnb3NyY3oiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTY5MTYxMTY0MCwiZXhwIjoyMDA3MTg3NjQwfQ.2sYyItxLjy-S7mwSWVFnDVa3fLkIk6utQKe9lb3SwYQ"
    
    const val USER_TABLE = "user_profiles"
    const val GAME_PROGRESS_TABLE = "game_progress"
    
    const val ENDPOINT_AUTH = "auth/v1"
    const val ENDPOINT_REST = "rest/v1"
}
```

## 6. Supabase Side Ayarları

Supabase tarafında, kullanıcı verilerinin doğru şekilde saklanabilmesi için:

1. `user_profiles` tablosunu oluşturun:
   - `id`: primary key, string (user_id ile eşleşir)
   - `username`: string
   - `created_at`: string
   - `avatar_url`: string (nullable)
   - `total_stars`: integer (default: 0)

2. Tablo izinleri:
   - insert: authenticated users
   - select: authenticated users
   - update: authenticated users (kendi verilerini)
   - delete: disabled

## 7. Manual Test için Logo Önerileri

Yapılan değişiklikleri test etmek için, logları dikkatlice gözlemleyin:

- `✅ Yeni kullanıcı oluşturuldu. ID: [user-id]`: Kullanıcı başarıyla oluşturuldu
- `✅ Kullanıcı profili başarıyla oluşturuldu`: Supabase'e kayıt başarılı
- `❌ API profil oluşturma hatası`: API hatasını gösterir, detaylı bilgi için logları kontrol edin
- `✅ Bağlantı testi tamamen başarılı`: Tüm bağlantı adımları başarıyla tamamlandı

Supabase konsolunda, yeni oluşturulan kullanıcıların `user_profiles` tablosunda görünüp görünmediğini kontrol edin. 