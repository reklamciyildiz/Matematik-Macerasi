package com.yigit033.matematikmacarasi.learning

/** Original, child-readable objectives organised around official programme themes.
 * Topic titles and objectives are planning summaries, not official outcome IDs.
 */
internal fun primaryTopics(grade: Int): List<CurriculumTopic> {
    fun t(key: String, area: TopicArea, name: String, desc: String,
          a: String, b: String, pilot: String = "", game: String? = null) =
        curriculumTopic(grade, key, area, name, desc, a, b, pilot, game)
    return when (grade) {
        1 -> listOf(
            t("count", TopicArea.NUMBERS, "20'ye Kadar Sayılar", "Nesneleri say ve eşleştir.",
                "20'ye kadar nesneleri sayıyla gösterebilirim.",
                "Bir sayıdan önce ve sonra gelen sayıları bulabilirim.", game="Sayı Avı"),
            t("compare", TopicArea.NUMBERS, "Az, Çok ve Eşit", "İki nesne grubunu karşılaştır.",
                "Hangi grupta daha çok nesne olduğunu gösterebilirim.",
                "Eşit miktardaki iki grubu eşleştirebilirim.", game="Sayı Avı"),
            t("rhythm", TopicArea.NUMBERS, "Ritmik Sayma ve Örüntü", "İkişer say ve düzeni keşfet.",
                "İkişer ritmik saymayı uygulayabilirim.",
                "Eksik örüntü adımını bulabilirim.", pilot="count_2", game="Sayı Avı"),
            t("addition", TopicArea.OPERATIONS, "10 İçinde Toplama", "Grupları birleştirerek öğren.",
                "İki grubun birleşmesini toplamayla anlatabilirim.",
                "10'u geçmeyen toplamları nesnelerle bulabilirim.", pilot="add_10", game="Dört İşlem"),
            t("subtraction", TopicArea.OPERATIONS, "Çıkarmayı Keşfet", "Bir gruptan nesne eksilt.",
                "Eksiltme durumunu çıkarma ile gösterebilirim.",
                "Geriye kalan nesne sayısını bulabilirim.", game="Dört İşlem"),
            t("shapes", TopicArea.GEOMETRY, "Şekiller ve Konum", "Şekilleri ve yerlerini tanı.",
                "Temel düzlemsel şekilleri ayırt edebilirim.",
                "Bir nesnenin yerini ve yönünü anlatabilirim.", game="Geometri Kalesi"),
            t("data", TopicArea.DATA, "Basit Veri Toplama", "Oyları ve nesneleri grupla.",
                "Bir sorunun yanıtlarını işaretleyerek kaydedebilirim.",
                "En çok veya en az seçilen grubu söyleyebilirim.")
        )
        2 -> listOf(
            t("place", TopicArea.NUMBERS, "Sayılar ve Basamaklar", "Onlukları ve birlikleri keşfet.",
                "Sayıları onluk ve birlikle gösterebilirim.",
                "İki sayının büyüklüğünü karşılaştırabilirim.", game="Sayı Avı"),
            t("rhythm", TopicArea.NUMBERS, "Ritmik Sayma", "Üçer ve dörder ilerle.",
                "Üçer sayarken eksik sayıyı bulabilirim.",
                "Dörder sayma düzenini sürdürebilirim.", pilot="count_3,count_4", game="Sayı Avı"),
            t("addition", TopicArea.OPERATIONS, "Toplama ve Çıkarma", "İşlemlerin nedenini kavra.",
                "Toplama için uygun strateji seçebilirim.",
                "Bulduğum sonucu kontrol edebilirim.", pilot="add_20", game="Dört İşlem"),
            t("multiply", TopicArea.OPERATIONS, "Eş Gruplarla Çarpma", "Tekrarlı toplamayı gör.",
                "Eş grupları tekrarlı toplamayla gösterebilirim.",
                "Küçük eş grupları çarpma olarak ifade edebilirim.", pilot="multiply_intro", game="Dört İşlem"),
            t("fractions", TopicArea.NUMBERS, "Bütün, Yarım ve Çeyrek", "Bir bütünü eş parçalara ayır.",
                "Eş parçaların neden önemli olduğunu açıklayabilirim.",
                "Yarım ve çeyreği şekillerle gösterebilirim.", game="Kesirler"),
            t("time", TopicArea.GEOMETRY, "Saat ve Ölçme", "Zaman ve uzunluğu tanı.",
                "Saatte temel zaman gösterimlerini okuyabilirim.",
                "Nesnelerin uzunluklarını karşılaştırabilirim.", game="Saat"),
            t("geometry", TopicArea.GEOMETRY, "Geometrik Şekiller", "Kenar ve köşeleri incele.",
                "Şekilleri kenar ve köşelerine göre ayırabilirim.",
                "Günlük nesneleri geometrik şekillerle eşleştirebilirim.", game="Geometri Kalesi"),
            t("data", TopicArea.DATA, "Veri Düzenleme", "Basit tablolar oluştur.",
                "Verileri gruplara ayırabilirim.",
                "Tablodan basit karşılaştırmalar yapabilirim.")
        )
        3 -> listOf(
            t("place", TopicArea.NUMBERS, "Doğal Sayılar", "Sayıları çözümle ve sırala.",
                "Sayıların basamak değerlerini gösterebilirim.",
                "Sayıları karşılaştırıp sıralayabilirim.", game="Sayı Avı"),
            t("rhythm", TopicArea.NUMBERS, "Ritmik Sayma ve Örüntü", "Kuralları keşfet.",
                "Ritmik sayma kuralını açıklayabilirim.",
                "Eksik örüntü terimini bulabilirim.", pilot="count_3,count_4", game="Örüntüler"),
            t("operations", TopicArea.OPERATIONS, "Dört İşleme Giriş", "İşlemleri modellerle anla.",
                "Çarpmayı eş gruplarla açıklayabilirim.",
                "Bir işlemi başka yöntemle kontrol edebilirim.", pilot="multiply", game="Dört İşlem"),
            t("fractions", TopicArea.NUMBERS, "Kesirleri Tanıyalım", "Parçaları bütünle ilişkilendir.",
                "Kesri bir şekil üzerinde gösterebilirim.",
                "Eş parçaları doğru belirleyebilirim.", game="Kesirler"),
            t("geometry", TopicArea.GEOMETRY, "Şekiller ve Ölçme", "Geometriyi günlük hayatta kullan.",
                "Geometrik şekilleri özellikleriyle ayırabilirim.",
                "Basit ölçme problemlerini modelleyebilirim.", game="Geometri Kalesi"),
            t("time", TopicArea.GEOMETRY, "Zaman ve Para", "Günlük yaşam problemleri.",
                "Saatleri günlük yaşamda kullanabilirim.",
                "Alışverişte para miktarlarını karşılaştırabilirim.", game="Pazar Simülatörü"),
            t("data", TopicArea.DATA, "Veri Okuma", "Tablo ve grafikleri yorumla.",
                "Bir tablodaki verileri karşılaştırabilirim.",
                "Grafikten basit çıkarım yapabilirim.")
        )
        4 -> listOf(
            t("place", TopicArea.NUMBERS, "Doğal Sayılar", "Büyük sayıları çözümle.",
                "Basamak ve sayı değerlerini ayırabilirim.",
                "Sayıları çözümleyip karşılaştırabilirim.", game="Sayı Avı"),
            t("operations", TopicArea.OPERATIONS, "Dört İşlem Problemleri", "Birden fazla adımla düşün.",
                "Probleme uygun işlemleri seçebilirim.",
                "Çarpmayı gruplama ile açıklayabilirim.", pilot="multiply", game="Dört İşlem"),
            t("patterns", TopicArea.NUMBERS, "Sayı Örüntüleri", "Sayıların kuralını bul.",
                "Ritmik örüntünün kuralını belirleyebilirim.",
                "Eksik terimleri tamamlayabilirim.", pilot="count_3,count_4", game="Örüntüler"),
            t("fractions", TopicArea.NUMBERS, "Kesirler", "Kesirleri göster ve karşılaştır.",
                "Kesirleri modellerle gösterebilirim.",
                "Basit kesirleri karşılaştırabilirim.", game="Kesirler"),
            t("geometry", TopicArea.GEOMETRY, "Geometri ve Çevre", "Açı ve kenarları incele.",
                "Şekilleri özelliklerine göre sınıflandırabilirim.",
                "Temel çevre hesabını açıklayabilirim.", game="Geometri Kalesi"),
            t("measure", TopicArea.GEOMETRY, "Ölçme ve Zaman", "Günlük hayatın ölçülerini kullan.",
                "Uygun uzunluk ve zaman birimini seçebilirim.",
                "Ölçme problemleri çözebilirim.", game="Saat"),
            t("data", TopicArea.DATA, "Tablo ve Grafikler", "Veriyi oku ve yorumla.",
                "Bir veri tablosunu açıklayabilirim.",
                "Grafikten uygun çıkarımlar yapabilirim.")
        )
        else -> error("Only grades 1–4 belong in the primary topic catalogue")
    }
}
