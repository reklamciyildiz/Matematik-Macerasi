package com.yigit033.matematikmacarasi.learning

/** Ö1 original planning summaries for middle school.
 * 2026–27: TYMM in grades 5–7; prior programme in grade 8.
 * Current working activities in all these grades are FOUNDATIONAL REVIEW,
 * never misrepresented as fully taught grade-level curriculum.
 */
internal fun secondaryTopics(grade: Int): List<CurriculumTopic> {
    fun t(key: String, area: TopicArea, name: String, desc: String,
          a: String, b: String, game: String? = null) =
        curriculumTopic(grade, key, area, name, desc, a, b, game=game)
    val planned = when (grade) {
        5 -> listOf(
            t("numbers", TopicArea.NUMBERS, "Doğal Sayılar", "Sayıları çözümle ve akıl yürüt.",
                "Doğal sayıları farklı biçimlerde gösterebilirim.",
                "Bir problem için sayısal ilişki kurabilirim.", "Dört İşlem"),
            t("fractions", TopicArea.NUMBERS, "Kesirler ve Ondalıklar", "Farklı sayı gösterimlerini tanı.",
                "Kesrin temsil ettiği miktarı modelleyebilirim.",
                "Kesir ve ondalık gösterimleri karşılaştırabilirim.", "Kesirler"),
            t("operations", TopicArea.OPERATIONS, "İşlemler ve İlişkiler", "İşlemin nedenini açıkla.",
                "Uygun işlem stratejisini seçebilirim.",
                "Sonucun akla uygunluğunu kontrol edebilirim.", "Dört İşlem"),
            t("geometry", TopicArea.GEOMETRY, "Geometrik Şekiller", "Açıları ve şekilleri incele.",
                "Şekilleri temel özellikleriyle ayırt edebilirim.",
                "Şekil ilişkilerini çizimle gösterebilirim.", "Geometri Kalesi"),
            t("measure", TopicArea.GEOMETRY, "Geometrik Nicelikler", "Çevre ve alan ilişkilerini anla.",
                "Bir ölçme probleminde uygun birim seçebilirim.",
                "Çevre ve alanı modeller üzerinden yorumlayabilirim.", "Geometri Kalesi"),
            t("data", TopicArea.DATA, "İstatistiksel Araştırma", "Veri topla ve yorumla.",
                "Bir araştırma sorusu için veri toplayabilirim.",
                "Veri gösterimlerini karşılaştırabilirim."),
            t("chance", TopicArea.DATA, "Veriden Olasılığa", "Belirsiz olayları düşün.",
                "Mümkün olayları belirleyebilirim.",
                "Olayların olabilirliğini gerekçelendirebilirim.")
        )
        6 -> listOf(
            t("numbers", TopicArea.NUMBERS, "Sayılar ve Nicelikler", "Farklı sayı türlerini incele.",
                "Tam sayıların konumunu sayı doğrusunda gösterebilirim.",
                "Kesirli ve ondalık miktarları karşılaştırabilirim.", "Sayı Avı"),
            t("fractions", TopicArea.NUMBERS, "Kesirler, Ondalıklar ve Oran", "Nicelikler arası bağ kur.",
                "Kesirlerle işlemleri modelleyebilirim.",
                "İki niceliğin oranını yorumlayabilirim.", "Kesirler"),
            t("algebra", TopicArea.ALGEBRA, "Cebirsel Düşünme", "Örüntüden kurala ilerle.",
                "Sayı örüntüsünün genel kuralını arayabilirim.",
                "Bilinmeyenli basit durumları ifade edebilirim.", "Örüntüler"),
            t("geometry", TopicArea.GEOMETRY, "Geometrik Şekiller", "Açıları ve çokgenleri incele.",
                "Çokgenleri özelliklerine göre ayırabilirim.",
                "Açı ilişkilerini çizimle açıklayabilirim.", "Geometri Kalesi"),
            t("measure", TopicArea.GEOMETRY, "Alan ve Ölçme", "Alan modelleri kullan.",
                "Alanı farklı birimlerle gösterebilirim.",
                "Ölçme problemlerinde uygun strateji seçebilirim.", "Geometri Kalesi"),
            t("data", TopicArea.DATA, "İstatistik ve Olasılık", "Veriden çıkarım yap.",
                "Veri kümesini uygun biçimde özetleyebilirim.",
                "Olası sonuçları karşılaştırabilirim.")
        )
        7 -> listOf(
            t("numbers", TopicArea.NUMBERS, "Sayılar ve Nicelikler", "Rasyonel sayıları gör.",
                "Rasyonel sayıları sayı doğrusunda gösterebilirim.",
                "Farklı sayı gösterimlerini karşılaştırabilirim.", "Kesirler"),
            t("algebra", TopicArea.ALGEBRA, "Cebirsel İlişkiler", "Değişkenleri ve eşitlikleri çöz.",
                "Bir ilişkiyi tablo ve cebirsel ifadeyle gösterebilirim.",
                "Basit eşitlikleri gerekçeli çözebilirim.", "Örüntüler"),
            t("ratio", TopicArea.OPERATIONS, "Orantısal Düşünme", "İki çokluk arasında ilişki kur.",
                "Oranları karşılaştırabilirim.",
                "Orantısal bir günlük yaşam problemini çözebilirim.", "Pazar Simülatörü"),
            t("transform", TopicArea.GEOMETRY, "Dönüşüm Geometrisi", "Şekilleri taşı ve karşılaştır.",
                "Dönüşen bir şeklin konumunu belirleyebilirim.",
                "Dönüşümde korunan özellikleri açıklayabilirim.", "Geometri Kalesi"),
            t("measure", TopicArea.GEOMETRY, "Geometrik Nicelikler", "Ölçme ilişkilerini incele.",
                "Geometrik nicelikleri uygun birimle gösterebilirim.",
                "Çizim yardımıyla ölçme problemi çözebilirim.", "Geometri Kalesi"),
            t("data", TopicArea.DATA, "İstatistiksel Araştırma", "Veriyi sorgula.",
                "Veri özetlerini karşılaştırabilirim.",
                "Grafikten çıkan bir sonucu gerekçelendirebilirim."),
            t("prob", TopicArea.DATA, "Veriden Olasılığa", "Olası durumları keşfet.",
                "Olası sonuçları listeleyebilirim.",
                "Basit olaylarda belirsizliği açıklayabilirim.")
        )
        8 -> listOf(
            t("factors", TopicArea.NUMBERS, "Çarpanlar ve Katlar", "Bölünebilme ilişkilerini kur.",
                "Bir sayının çarpanlarını ve katlarını bulabilirim.",
                "EBOB–EKOK problemlerini modelleyebilirim."),
            t("powers", TopicArea.NUMBERS, "Üslü ve Köklü İfadeler", "Yeni sayı gösterimlerini anla.",
                "Üslü ifadeleri işlem kurallarıyla çözebilirim.",
                "Karekökün büyüklüğünü tahmin edebilirim."),
            t("algebra", TopicArea.ALGEBRA, "Cebir ve Denklemler", "Bilinmeyeni ve doğrusal ilişkiyi keşfet.",
                "Birinci dereceden denklemleri çözebilirim.",
                "Doğrusal ilişkiyi tablo ve grafikle gösterebilirim."),
            t("geometry", TopicArea.GEOMETRY, "Üçgenler ve Dönüşümler", "Şekillerin ilişkilerini incele.",
                "Üçgenlerin özelliklerinden sonuç çıkarabilirim.",
                "Dönüşümleri koordinat düzleminde gösterebilirim.", "Geometri Kalesi"),
            t("solids", TopicArea.GEOMETRY, "Geometrik Cisimler", "Yüzey ve hacmi görselleştir.",
                "Cisimlerin elemanlarını ayırt edebilirim.",
                "Hacim ve yüzey problemlerini çözebilirim.", "Geometri Kalesi"),
            t("data", TopicArea.DATA, "Veri Analizi ve Olasılık", "Grafikleri ve sonuçları yorumla.",
                "Veriyi uygun grafikle sunabilirim.",
                "Basit bir olayın olasılığını hesaplayabilirim.")
        )
        else -> error("Only grades 5–8 belong in the secondary topic catalogue")
    }
    return planned + curriculumTopic(
        grade, "review", TopicArea.REVIEW, "Önceki Sınıflardan Tekrar",
        "Çarpma ve bölünebilme ile temel tekrar.",
        "Eş grupları çarpmayla modelleyebilirim.",
        "15'in katlarını ayırt edebilirim.",
        lessons = "divisible_15,multiply", game = "Dört İşlem"
    )
}
