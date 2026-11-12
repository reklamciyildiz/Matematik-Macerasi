# Google Play abonelik onayı / RTDN kurtarma — 12 Kasım 2026

## Mimari ve kapsam
- **Google Play ömür boyu Premium (INAPP)**: Android `PurchaseManager.acknowledgePurchase`. Yeni SUBS düzeltmesi bu akışı değiştirmez.
- **Premium kodu**: ayrı Cloudflare Premium kod servisi, ayrı D1. Bu değişikliklere dahil değil.
- **Google Play monthly / yearly (SUBS)**: Google Play Developer API sunucu doğrulaması + Play Integrity + abonelik Worker + Pub/Sub RTDN + 24 saati geçmeyen imzalı cihaz lisansı.

## Tanımlanan hata

Google Play'de `Geliştirici ilk alışverişinizi onaylamadan planınızı değiştiremezsiniz`
uyarısı, ilk SUBS tokenının henüz Google tarafından *acknowledged* görülmediğine işaret eder.
Bu **Play Console'da geliştiricinin elle onaylaması gereken bir ekran değildir.**
Gerçek kullanıcı üç gün; lisans testi yaklaşık üç dakika içerisinde onay gerektirir.

Önceden Cloudflare /v1/verify Google'a ack gönderirken geçici API hatasında
tüm doğrulamayı kesiyordu. RTDN ise yalnızca alımı D1'e kaydedip **Google
satın alma onayını yapmıyordu**. Pull cron beş dakika olduğundan test
iadesinden daha geç çalışabiliyordu.

## Uygulanan iyileştirmeler
1. Worker satın alımı Google ile doğrular, D1'de kaydeder ve ancak bunun
   ardından imzalı Premium hakkı verir. Google ack geçici olarak başarısızsa
   doğrulanmış hak kaybolmaz; güvenli log ve `acknowledgement=RETRY_REQUIRED`
   yanıtı üretilir. Denied/on-hold/pending satın alma için hak/ack yoktur.
2. Android, **yalnızca** sunucudan VERIFIED sonucu alıp imzalı lisansı
   doğruladıktan sonra onaylanmamış SUBS alımına BillingClient acknowledgement
   isteği yollar; geçici ağ hatalarına sınırlı kısa tekrarlar yapar.
3. RTDN her *doğrulanmış* ve hak sahibi, henüz onaylanmamış SUBS tokenını
   Google Play API ile onaylar. Başarısızsa Pub/Sub bildirimini
   acknowledged işaretlemez; sonraki dakikada yeniden denenir.
4. RTDN polling aralığı 1 dakikaya düşer (**yalnızca Worker yeniden
   deploy edildiğinde**). Mevcut `wrangler.toml` yerel ve GitHub'da değildir.
5. Logs yalnızca güvenli onay sonucu ve izin verilen hata kodlarını içerir.
   Purchase token, Google credentials ve cihaz kimliği loglanmaz.

## Kullanıcının bilgisayarındaki zorunlu Cloudflare dağıtımı

Google Play Console'a, Firebase'e veya mevcut Premium kod Worker'ına dokunmadan:

```powershell
cd C:\active_projects\MatematikMacerasiii
git pull --ff-only origin main
powershell -ExecutionPolicy Bypass -File .\commercial\play-subscriptions\scripts\deploy-acknowledgement-recovery.ps1 -ForSubscriptionAckRepair
```

Script önce dedicated D1/Worker/RTDN kimliklerini kontrol eder, yerel
`wrangler.toml` için cron'u 5 dakikadan 1 dakikaya çevirir, Node testlerini
çalıştırır, sadece abonelik Worker'ını deploy eder, `/health` kontrol eder.
Yayınlanmış Android sürümünü **güncellemez** ve Play Store'a dosya yüklemez.
Cloudflare tarafında cron değişikliklerinin aktif olması biraz gecikebilir.

Örnek güvenli log izleme:

```powershell
cd C:\active_projects\MatematikMacerasiii\commercial\play-subscriptions
npx --yes wrangler@4.158.0 tail matematik-macerasi-play-subscriptions --format pretty
```

Bilgi mesajları: `mmplay_ack_confirmed`, `mmplay_rtdn_ack_confirmed`.
Uyarılar: `mmplay_ack_retry_required`,
`mmplay_rtdn_verify_or_ack_retry_required`,
`mmplay_verify_failed <ALLOWLISTED_CODE>`.
Bu logların görünmemesi başarısızlık kanıtı değildir; ilgili bildirim/
satın alma gerçekleşmemiş veya kayıtlar henüz ulaşmamış olabilir.

## Gerçek uçtan uca test (yayın öncesi)

1. Yeni Android sürümünü Play **Dahili test** üzerinden yükle. Lokal APK
   ile Play Integrity sonucu, Play imzalı dağıtımla aynı olmayabilir.
2. Ayrı lisans test hesapları ile aylık ve yıllık tek satın alma yap.
   `Test kartı, her zaman onaylanır` ekranını gör; gerçek ödeme yapma.
3. Her testte Premium açılıyor ve uygulama yeniden başlatılınca kalıyor mu?
   Play Console/Play API satın alım ack durumu onaylı mı?
4. İlk satın alım onaylanmadan plan değiştirmeyi deneme.
5. İptal/sona erme sonrası Google Play API durumu ile Premium erişimi uyumlu mu?
   İptal, **ödenmiş dönemin sonunda** erişimi kaldırır; hemen kaldırmaz.
   Çevrimdışı imzalı lisans azami 24 saat geçerlidir.

Eski başarısız lisans testi satın alımları otomatik iade edilmiş olabilir;
mevcut işlemi Play Console'da elle onaylamak mümkün değildir.
Yeni test tokenıyla gerçek başarılı onay görülmesi gerekir.

## Sınırlar
- Worker /health yalnızca yapılandırmayı kontrol eder; Google Publisher
  API izinlerini, Integrity'yi veya gerçek alımın onayını kanıtlamaz.
- RTDN'nin gerçek satın alma bildirimi aldığı ve yeni kodun üretimde
  çalıştığı testten sonra doğrulanabilir.
- Android, yeni AAB dağıtılmadıkça eski SUBS onay fallback'ine sahip değildir.
- İptal, iade, geri ödeme, mali mutabakat ve cihazlar arası hesap bağlantısı
  üretimde ayrıca gerçek QA ve gerekirse ilave kontrol gerektirir.
