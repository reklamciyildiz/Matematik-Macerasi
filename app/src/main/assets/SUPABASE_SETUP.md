# Matematik Macerası - Supabase Kurulum Rehberi

Bu rehber, Matematik Macerası uygulaması için Supabase bulut veritabanı kurulumunu adım adım açıklar.

## 1. Supabase Hesabı Oluşturma

1. [Supabase](https://supabase.com/) web sitesine gidin
2. "Start your project" butonuna tıklayın
3. GitHub hesabınızla giriş yapın veya e-posta ile kayıt olun

## 2. Yeni Proje Oluşturma

1. Supabase Dashboard'da "New Project" butonuna tıklayın
2. Proje adı olarak "MatematikMacerasi" girin
3. Bir veritabanı şifresi belirleyin (bunu güvenli bir yerde saklayın)
4. Bölge olarak size en yakın veri merkezini seçin (örn. Frankfurt, Almanya)
5. "Create new project" butonuna tıklayın ve projenin oluşturulmasını bekleyin

## 3. Veritabanı Şemasını Oluşturma

1. Supabase Dashboard'da projenizi açın
2. Sol menüden "SQL Editor" seçeneğine tıklayın
3. "New Query" butonuna tıklayın
4. Bu projedeki `app/src/main/assets/supabase_schema.sql` dosyasının içeriğini kopyalayın
5. SQL Editor'a yapıştırın
6. "Run" butonuna tıklayarak SQL sorgusunu çalıştırın

## 4. Anonim Kimlik Doğrulamasını Etkinleştirme

1. Sol menüden "Authentication" seçeneğine tıklayın
2. "Settings" sekmesine geçin
3. "Providers" bölümünde "Email" ve "Anonymous" seçeneklerinin etkin olduğundan emin olun

## 5. API Anahtarlarını Alma

1. Sol menüden "Settings" seçeneğine tıklayın
2. "API" sekmesine geçin
3. "Project URL" ve "anon public" değerlerini not alın

## 6. Uygulamayı Yapılandırma

1. `app/src/main/java/com/yigit033/matematikmacarasi/config/SupabaseConfig.kt` dosyasını açın
2. `SUPABASE_URL` değerini Supabase projenizin URL'si ile değiştirin
3. `SUPABASE_ANON_KEY` değerini Supabase projenizin anonim anahtarı ile değiştirin

```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://your-project-url.supabase.co"
    const val SUPABASE_ANON_KEY = "your-anon-key"
}
```

## 7. Veritabanı Tablolarını Kontrol Etme

1. Sol menüden "Table Editor" seçeneğine tıklayın
2. "users" ve "game_progress" tablolarının oluşturulduğunu doğrulayın

## 8. Güvenlik Ayarlarını Kontrol Etme

1. Sol menüden "Authentication" seçeneğine tıklayın
2. "Policies" sekmesine geçin
3. "users" ve "game_progress" tabloları için RLS (Row Level Security) politikalarının doğru şekilde yapılandırıldığını kontrol edin

## 9. Test Etme

1. Uygulamayı derleyin ve çalıştırın
2. Ayarlar ekranında "Bulut Senkronizasyon" seçeneğini etkinleştirin
3. Bağlantı durumunun "Bağlı" olarak göründüğünü doğrulayın
4. Birkaç oyun oynayın ve puanları kaydedin
5. Supabase Dashboard'da "Table Editor" üzerinden "game_progress" tablosunu kontrol ederek verilerin kaydedildiğini doğrulayın

## Sorun Giderme

- **Bağlantı Hatası**: URL ve anonim anahtarın doğru olduğundan emin olun
- **Kimlik Doğrulama Hatası**: Anonim kimlik doğrulamasının etkin olduğunu kontrol edin
- **Veri Kaydetme Hatası**: RLS politikalarının doğru yapılandırıldığını kontrol edin
- **Logcat Hataları**: Android Studio'da Logcat'i kontrol ederek "SupabaseService" etiketli hata mesajlarını inceleyin 