# RTDB mevcut uygulama uyumlu **kısmi** güvenlik sıkılaştırması

**DURUM: HAZIR TASLAK — CANLIDA YAYINLANMADI.**
Bu dosya `realtime-database.lockdown.rules.json` ile karıştırılmamalı.
Tam kilitleme mevcut Android Premium kod aktivasyonunu ve kod geri
yüklemesini kırar. Bu belge, hesap zorunluluğu getirmeden güvenlik
riskini **kısmen azaltan** geçici yaklaşımı açıklar.

## Ne değişir?

- `/premium_codes` ve `/code_activations` koleksiyonlarının
  **toplu anonim okunması** engellenir.
- Mevcut APK'nın kullandığı `/premium_codes/<code>` ve
  `/code_activations/<deviceId>` **tekil okuma** yolları açık kalır.
- `used_count` yalnızca 0/boştan 1'e veya mevcut sayıdan tam +1'e
  geçebilir. Geri çekme, çoklu sıçrama, silme, `max_uses` sınırı
  aşma reddedilir. `active:false` olan kodun sayacı artmaz.
- Aktivasyon yazma: yeni cihaz kaydı, mevcut aktif bir kod
  referansı ve geçerli `code`/`activated_at` alanları gerektirir.
  Var olan aktivasyonun yeniden yazılması veya silinmesi reddedilir.
- `app_info` okunabilir; istemciden değiştirilemez.

## Kritik kalan riskler

**Bu çözüm güvenlik açığını tamamen kapatmaz.**
Doğrulanmış kullanıcı ya da güvenilir backend bulunmadığından:

1. Kodu tahmin eden/ele geçiren biri tekil kod kaydını okuyabilir.
2. Yetkisiz bir istemci gerçek bir kodun sayacını +1 artırabilir;
   bu kod kapasitesini tüketen **hizmet engelleme** riskidir.
3. Gerçek bir kodla başka bir cihaz adına sahte aktivasyon kaydı
   yazılabilir; doğrulanmış cihaz sahipliği yoktur.
4. Kullanım sayısı ile cihaz aktivasyonu **tek atomik işlem değildir**.
   Eşzamanlı kullanımlarda iki işlemden birinin başarısız olması
   hâlâ mümkündür; eski Android kodu bunu düzeltmez.
5. Eski uygulamanın `deviceId` kaydı gerçek insan/öğrenci
   kimliği değildir. Firebase App Check varsa bile tek başına
   lisans sahibi ispatı sayılmaz.

## Manuel sürümleme ve doğrulama

1. Firebase Console → Realtime Database → **Rules** bölümündeki
   mevcut kuralları **yerel ve özel bir yedeğe** kaydet;
   kodları ve aktivasyon kayıtlarını silme.
2. Veri ekranında aktif `premium_codes` kayıtlarındaki
   `used_count`, `max_uses`, `active` alanlarını **özel olarak**
   denetle; ekran görüntüsünde gerçek kodları ifşa etme.
   `used_count` sayı değilse, `max_uses` yanlış tipteyse,
   bu taslak aktif kullanıcılar için hata doğurabilir.
3. Rules Playground ile şu yolları **yeniden yayınlamadan**
   kontrol et:
   - `/premium_codes` ve `/code_activations` -> READ DENY;
   - `/premium_codes/<existing-test-code>` -> READ ALLOW;
   - `/code_activations/<existing-test-device-id>` -> READ ALLOW;
   - `/app_info` -> READ ALLOW;
   - `/premium_codes/<test-code>/used_count` -> sadece
     `current+1` ve izinli kapasite içinde WRITE ALLOW;
     silme, geri alma, kapasiteyi aşma -> DENY;
   - `/code_activations/<unused-test-id>` -> yalnız mevcut
     aktif test kodu ve geçerli alanlarla CREATE ALLOW;
     önceden var olan bir kaydı değiştirme -> DENY.
4. Gerçek test Firebase projesi/emülatöründe Android'den yeni kod
   aktivasyonu, mevcut aktivasyon yenileme, uygulama yeniden yükleme
   ve Play satın alma geri yükleme akışlarını dene.
5. **Ancak** bunlar geçer ve eski sürümlere etkisi kabul edilirse
   kuralları Firebase Console'da **manuel** yayımla.

## Nihai çözüm

Spark korunacaksa kod doğrulama ve atomik kontenjan/aktivasyon
kontrolü ayrı güvenilir backend'e taşınacak; sonra koleksiyon
okuma/yazma izinleri gerçekten kapatılacak. Mevcut çalışan hakları
geri almadan kademeli istemci geçişi gerekiyor.
