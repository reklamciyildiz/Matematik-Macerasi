package com.yigit033.matematikmacarasi.games.models.market

import com.yigit033.matematikmacarasi.R
import kotlin.random.Random

/**
 * Market oyunu için seviyeleri ve ürün kataloğunu tanımlayan singleton sınıf
 * Dinamik liste oluşturma özelliği eklenmiştir
 */
object MarketLevels {
    // Random Örneği - Sabit seed ile (tutarlı rastgelelik için)
    private val random = Random(System.currentTimeMillis())
    
    // Ürün kataloğu - Tüm mevcut ürünler
    private val products = listOf(
        // Meyveler
        Product(1, "Elma", 5.50, R.drawable.ic_apple, ProductCategory.FRUIT, true),
        Product(2, "Muz", 7.00, R.drawable.ic_banana, ProductCategory.FRUIT, true),
        Product(3, "Portakal", 4.50, R.drawable.ic_orange, ProductCategory.FRUIT, true),
        Product(4, "Çilek", 16.0, R.drawable.ic_strawberry, ProductCategory.FRUIT, true),
        
        // Fırın ürünleri
        Product(5, "Ekmek", 4.0, R.drawable.ic_bread, ProductCategory.BAKERY),
        Product(6, "Simit", 3.50, R.drawable.ic_simit, ProductCategory.BAKERY),
        Product(7, "Poğaça", 5.0, R.drawable.ic_pastry, ProductCategory.BAKERY),
        
        // Süt ürünleri
        Product(8, "Süt", 12.50, R.drawable.ic_milk, ProductCategory.DAIRY),
        Product(9, "Peynir", 25.50, R.drawable.ic_cheese, ProductCategory.DAIRY, true),
        Product(10, "Yoğurt", 15.0, R.drawable.ic_yogurt, ProductCategory.DAIRY),
        
        // Sebzeler
        Product(11, "Domates", 7.50, R.drawable.ic_tomato, ProductCategory.VEGETABLE, true),
        Product(12, "Patates", 4.50, R.drawable.ic_potato, ProductCategory.VEGETABLE, true),
        Product(13, "Salatalık", 6.50, R.drawable.ic_cucumber, ProductCategory.VEGETABLE, true),
        Product(14, "Havuç", 4.00, R.drawable.ic_carrot, ProductCategory.VEGETABLE, true),
        
        // Atıştırmalıklar
        Product(15, "Çikolata", 8.75, R.drawable.ic_chocolate, ProductCategory.SNACKS),
        Product(16, "Cips", 9.50, R.drawable.ic_chips, ProductCategory.SNACKS),
        Product(17, "Bisküvi", 6.25, R.drawable.ic_cookie, ProductCategory.SNACKS),
        Product(18, "Dondurma", 12.50, R.drawable.ic_ice_cream, ProductCategory.SNACKS)
    )
    
    // Seviye şablonları - Konfigürasyon bilgileri
    private val levelTemplates = listOf(
        // Kolay Seviye - 1
        LevelTemplate(
            productCount = 3, // 3 ürün
            maxWeightItems = 1, // En fazla 1 kilogram ile satılan ürün
            maxTotalPrice = 30.0, // Toplam fiyat en fazla 30₺
            budget = 50.0, // Bütçe: 50₺
            timeInSeconds = 180, // 3 dakika
            name = "Basit Alışveriş", 
            description = "Temel alışveriş becerilerini öğren",
            difficulty = 1,
            categoriesWeights = mapOf(
                ProductCategory.FRUIT to 0.3,
                ProductCategory.BAKERY to 0.3,
                ProductCategory.DAIRY to 0.3,
                ProductCategory.VEGETABLE to 0.1,
                ProductCategory.SNACKS to 0.0
            )
        ),
        
        // Orta Seviye - 2
        LevelTemplate(
            productCount = 4, // 4 ürün
            maxWeightItems = 2, // En fazla 2 kilogram ile satılan ürün
            maxTotalPrice = 40.0, // Toplam fiyat en fazla 40₺
            budget = 60.0, // Bütçe: 60₺
            timeInSeconds = 240, // 4 dakika
            name = "Haftalık Alışveriş", 
            description = "Aile için haftalık alışveriş yapma pratiği",
            difficulty = 2,
            categoriesWeights = mapOf(
                ProductCategory.FRUIT to 0.3,
                ProductCategory.BAKERY to 0.2,
                ProductCategory.DAIRY to 0.2,
                ProductCategory.VEGETABLE to 0.2,
                ProductCategory.SNACKS to 0.1
            )
        ),
        
        // Zor Seviye - 3 
        LevelTemplate(
            productCount = 5, // 5 ürün
            maxWeightItems = 3, // En fazla 3 kilogram ile satılan ürün
            maxTotalPrice = 70.0, // Toplam fiyat en fazla 70₺
            budget = 100.0, // Bütçe: 100₺
            timeInSeconds = 300, // 5 dakika
            name = "Aile Alışverişi", 
            description = "Daha fazla ürün ve kısıtlı bütçe ile zorlu alışveriş",
            difficulty = 3,
            categoriesWeights = mapOf(
                ProductCategory.FRUIT to 0.2,
                ProductCategory.BAKERY to 0.2,
                ProductCategory.DAIRY to 0.2,
                ProductCategory.VEGETABLE to 0.2,
                ProductCategory.SNACKS to 0.2
            )
        ),
        
        // Uzman Seviye - 4
        LevelTemplate(
            productCount = 7, // 7 ürün
            maxWeightItems = 4, // En fazla 4 kilogram ile satılan ürün
            maxTotalPrice = 120.0, // Toplam fiyat en fazla 120₺
            budget = 150.0, // Bütçe: 150₺
            timeInSeconds = 360, // 6 dakika
            name = "Ekonomi Uzmanı", 
            description = "Tüm becerileri kullanarak en verimli alışverişi yap",
            difficulty = 5,
            categoriesWeights = mapOf(
                ProductCategory.FRUIT to 0.3,
                ProductCategory.BAKERY to 0.2,
                ProductCategory.DAIRY to 0.2,
                ProductCategory.VEGETABLE to 0.2,
                ProductCategory.SNACKS to 0.1
            )
        )
    )
    
    // Mevcut seviyeler - Başlangıçta oluşturulmuş listeler (default liste)
    // Uygulama açıldığında bu listeler gösterilir, ancak seviye seçildiğinde dinamik olarak oluşturulurlar
    val levels = levelTemplates.map { template ->
        generateShoppingListFromTemplate(template)
    }
    
    /**
     * Tüm ürünleri döndüren fonksiyon
     * @return Ürün listesi
     */
    fun getAllProducts(): List<Product> = products
    
    /**
     * Belirli bir kategorideki ürünleri döndüren fonksiyon
     * @param category Ürün kategorisi
     * @return Filtrelenmiş ürün listesi
     */
    fun getProductsByCategory(category: ProductCategory): List<Product> {
        return products.filter { it.category == category }
    }
    
    /**
     * ID'ye göre ürün bulan fonksiyon
     * @param id Ürün ID'si
     * @return Bulunan ürün veya null
     */
    fun getProductById(id: Int): Product? {
        return products.find { it.id == id }
    }
    
    /**
     * Belirli bir seviye indeksi için yeni bir dinamik alışveriş listesi oluşturur
     * @param levelIndex Seviye indeksi (0-3)
     * @return Yeni oluşturulmuş dinamik alışveriş listesi
     */
    fun generateDynamicShoppingList(levelIndex: Int): ShoppingList {
        try {
            val safeIndex = levelIndex.coerceIn(0, levelTemplates.size - 1)
            return generateShoppingListFromTemplate(levelTemplates[safeIndex])
        } catch (e: Exception) {
            e.printStackTrace()
            // Hata durumunda statik listeden seviyeyi döndür
            val safeIndex = levelIndex.coerceIn(0, levels.size - 1)
            return levels[safeIndex]
        }
    }
    
    /**
     * Verilen şablona göre dinamik bir alışveriş listesi oluşturur
     * @param template Seviye şablonu
     * @return Şablona uygun rastgele alışveriş listesi
     */
    private fun generateShoppingListFromTemplate(template: LevelTemplate): ShoppingList {
        try {
            // Seçilecek ürünleri tutacak bir liste
            val selectedItems = mutableListOf<ShoppingListItem>()
            
            // Toplam fiyatı takip et
            var totalPrice = 0.0
            
            // Maksimum 10 deneme yap - sonsuz döngüye girmemek için
            var attempts = 0
            val maxAttempts = 10
            
            // Toplam ürün sayısı kadar ürün seç
            while (selectedItems.size < template.productCount && attempts < maxAttempts) {
                attempts++
                
                try {
                    // Kategori seçimi (ağırlıklı olasılık)
                    val category = selectRandomCategory(template.categoriesWeights)
                    
                    // Seçilen kategorideki tüm ürünleri bul
                    val categoryProducts = products.filter { 
                        it.category == category && 
                        // Zaten bu üründen eklemediğimizden emin ol
                        !selectedItems.any { item -> item.product.id == it.id }
                    }
                    
                    if (categoryProducts.isEmpty()) {
                        continue
                    }
                    
                    // Kategoriden rastgele bir ürün seç
                    val product = categoryProducts.random(random)
                    
                    // Ürünün kilogram ile mi satıldığını kontrol et
                    val isWeightItem = product.isByWeight
                    
                    // Miktar hesapla (kilogram ile satılan ürünler için 0.5-3.0 kg arası, diğerleri için 1-3 adet)
                    val quantity = if (isWeightItem) {
                        // Kilogram ile satılan ürünlerin toplam sayısı, şablondaki maksimum sayıyı aşmamalı
                        val currentWeightItems = selectedItems.count { it.product.isByWeight }
                        if (currentWeightItems >= template.maxWeightItems) {
                            continue
                        }
                        (random.nextInt(1, 6) * 0.5) // 0.5, 1.0, 1.5, 2.0, 2.5 kg
                    } else {
                        random.nextInt(1, 4).toDouble() // 1, 2 veya 3 adet
                    }
                    
                    // Bu ürünün toplam fiyatını hesapla
                    val itemPrice = product.price * quantity
                    
                    // Toplam fiyat şablondaki maksimum fiyatı aşmıyorsa ekle
                    if (totalPrice + itemPrice <= template.maxTotalPrice) {
                        selectedItems.add(ShoppingListItem(product, quantity))
                        totalPrice += itemPrice
                    }
                } catch (e: Exception) {
                    // Bir hata oluşursa kaydet ve devam et
                    e.printStackTrace()
                    continue
                }
            }
            
            // Yeterli ürün bulunamadıysa, kalan öğeleri basit bir şekilde ekle
            if (selectedItems.size < template.productCount) {
                try {
                    val remainingCount = template.productCount - selectedItems.size
                    val availableProducts = products.filter { 
                        !selectedItems.any { item -> item.product.id == it.id }
                    }
                    
                    // Rastgele ürünler ekle
                    val productsToAdd = availableProducts.shuffled(random).take(remainingCount)
                    
                    for (product in productsToAdd) {
                        val quantity = if (product.isByWeight) 0.5 else 1.0
                        selectedItems.add(ShoppingListItem(product, quantity))
                    }
                } catch (e: Exception) {
                    // Hata oluşursa en azından mevcut öğelerle devam et
                    e.printStackTrace()
                }
            }
            
            // Şablona göre alışveriş listesi oluştur
            return ShoppingList(
                items = selectedItems,
                budget = template.budget,
                levelName = template.name,
                timeInSeconds = template.timeInSeconds,
                name = template.name,
                description = template.description,
                difficulty = template.difficulty
            )
        } catch (e: Exception) {
            // Ciddi bir hata durumunda varsayılan güvenli bir seviye oluştur
            e.printStackTrace()
            
            // En basit ürünlerden oluşan güvenli bir liste oluştur
            val safeItems = try {
                val safeProducts = products.take(template.productCount.coerceAtMost(3))
                safeProducts.map { product ->
                    ShoppingListItem(product, if (product.isByWeight) 0.5 else 1.0)
                }
            } catch (ex: Exception) {
                // Son çare - boş liste
                emptyList()
            }
            
            return ShoppingList(
                items = safeItems,
                budget = template.budget,
                levelName = template.name,
                timeInSeconds = template.timeInSeconds,
                name = template.name,
                description = template.description,
                difficulty = template.difficulty
            )
        }
    }
    
    /**
     * Belirtilen ağırlıklara göre rastgele kategori seçer
     * @param weights Kategori ağırlıkları (olasılıkları)
     * @return Seçilen rastgele kategori
     */
    private fun selectRandomCategory(weights: Map<ProductCategory, Double>): ProductCategory {
        try {
            val totalWeight = weights.values.sum()
            if (totalWeight <= 0) return ProductCategory.ALL
            
            val randomValue = random.nextDouble() * totalWeight
            var currentWeight = 0.0
            
            for ((category, weight) in weights) {
                currentWeight += weight
                if (randomValue <= currentWeight) {
                    return category
                }
            }
            
            // Varsayılan olarak tüm kategoriler
            return ProductCategory.ALL
        } catch (e: Exception) {
            // Hata durumunda varsayılan kategoriye geri dön
            e.printStackTrace()
            return ProductCategory.ALL
        }
    }
    
    /**
     * Seviye şablonu veri sınıfı - Dinamik liste oluşturmak için kullanılır
     */
    private data class LevelTemplate(
        val productCount: Int,         // Kaç adet ürün olacak
        val maxWeightItems: Int,       // Kaç adet kilogram ile satılan ürün olacak
        val maxTotalPrice: Double,     // Maksimum toplam fiyat
        val budget: Double,            // Bütçe
        val timeInSeconds: Int,        // Zaman sınırı
        val name: String,              // Seviye adı
        val description: String,       // Seviye açıklaması
        val difficulty: Int,           // Zorluk seviyesi (1-5)
        val categoriesWeights: Map<ProductCategory, Double> // Kategori olasılıkları
    )
} 