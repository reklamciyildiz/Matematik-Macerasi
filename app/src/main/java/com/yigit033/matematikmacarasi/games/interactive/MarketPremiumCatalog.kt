package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/**
 * S3.1.2: ten distinct shops, twelve catalogued products each (120 total).
 * All monetary quantities are integer kuruş. The player sees six products per
 * visit, sampled deterministically from twelve; a new run presents a new shelf.
 */
object MarketPremiumCatalog {
    data class Shop(
        val title: String, val emoji: String, val learning: String,
        val products: List<MarketGood>, val isPro: Boolean
    )
    private data class Item(val label: String,val emoji: String,val tl: Int,val unit: String="adet")

    private fun shop(index: Int, title: String, emoji: String,
        learning: String, isPro: Boolean, items: List<Item>): Shop {
        require(items.size==12)
        return Shop(title,emoji,learning,items.mapIndexed { i,x ->
            MarketGood(index*100+i+1,x.label,x.emoji,x.tl*100,x.unit)
        },isPro)
    }
    private fun x(name:String,icon:String,tl:Int,unit:String="adet")=
        Item(name,icon,tl,unit)

    val shops: List<Shop> = listOf(
        shop(0,"Güneşli Manav","🍎","Tartı, kilogram ve gram",false,listOf(
            x("Elma","🍎",18,"kg"),x("Muz","🍌",23,"kg"),x("Armut","🍐",19,"kg"),
            x("Portakal","🍊",14,"kg"),x("Çilek","🍓",36,"kg"),x("Üzüm","🍇",28,"kg"),
            x("Domates","🍅",17,"kg"),x("Salatalık","🥒",12,"kg"),
            x("Patates","🥔",11,"kg"),x("Havuç","🥕",10,"kg"),
            x("Limon","🍋",20,"kg"),x("Biber","🫑",26,"kg"))),
        shop(1,"Mahalle Fırını","🥖","Çoklu ürün ve litre hesabı",false,listOf(
            x("Ekmek","🥖",13),x("Simit","🥨",12),x("Poğaça","🥐",16),
            x("Kurabiye","🍪",14),x("Kek","🧁",20),x("Sandviç","🥪",27),
            x("Açma","🥯",15),x("Tost","🥪",25),x("Börek","🥟",22),
            x("Çörek","🍞",18),x("Kraker","🍘",9),x("Muffin","🧁",24))),
        shop(2,"Oyuncak Atölyesi","🧸","Harçlık planlama",false,listOf(
            x("Oyuncak Top","⚽",28),x("Yapboz","🧩",34),x("Uçurtma","🪁",25),
            x("Boya Kalemi","🖍️",19),x("Kitap","📚",27),x("Robot","🤖",45),
            x("Oyuncak Ayı","🧸",39),x("Blok Seti","🧱",36),x("Balon","🎈",8),
            x("Oyuncak Araba","🚗",31),x("Tren","🚂",43),x("Kukla","🎭",29))),
        shop(3,"Renkli Kırtasiye","📒","Etiket ve indirim hesabı",true,listOf(
            x("Defter","📒",24),x("Kalem","✏️",8),x("Silgi","🧽",7),
            x("Cetvel","📏",12),x("Suluboya","🎨",36),x("Hikâye Kitabı","📗",32),
            x("Kalemtıraş","✂️",11),x("Ajanda","📔",27),x("Dosya","📁",19),
            x("Fırça","🖌️",16),x("Sticker","⭐",9),x("Çanta","🎒",48))),
        shop(4,"Giyim Sokağı","👕","Fiyat karşılaştırması",true,listOf(
            x("Tişört","👕",44),x("Şapka","🧢",31),x("Çorap","🧦",15),
            x("Atkı","🧣",28),x("Eldiven","🧤",23),x("Ayakkabı","👟",65),
            x("Mont","🧥",78),x("Pantolon","👖",59),x("Elbise","👗",69),
            x("Gözlük","🕶️",38),x("Bere","🎩",26),x("Terlik","🩴",24))),
        shop(5,"Spor Dükkânı","⚽","Bütçeyle takım ekipmanı alma",true,listOf(
            x("Futbol Topu","⚽",51),x("Basketbol","🏀",48),x("Tenis Topu","🎾",21),
            x("Raket","🏸",60),x("Atlama İpi","🪢",25),x("Mat","🧘",47),
            x("Sulık","🥤",19),x("Krampon","👟",74),x("Kask","🪖",58),
            x("Düdük","📣",12),x("Madalya","🏅",16),x("Forma","👕",46))),
        shop(6,"Evcil Dostlar","🐾","Birim fiyat ve karşılaştırma",true,listOf(
            x("Mama","🥣",35,"kg"),x("Kum","🪣",27,"kg"),x("Mama Kabı","🥣",22),
            x("Oyuncak Fare","🐭",14),x("Tasma","🦮",26),x("Fırça","🪮",18),
            x("Yatak","🧺",64),x("Su Kabı","💧",20),x("Top","🎾",12),
            x("Yuvalık","🏠",69),x("Tarak","🪮",15),x("Ödül Maması","🦴",21))),
        shop(7,"Mahalle Bakkalı","🏬","Paket ve miktar hesabı",true,listOf(
            x("Süt","🥛",17,"litre"),x("Su","💧",8,"litre"),x("Peynir","🧀",38),
            x("Yumurta","🥚",29),x("Pirinç","🍚",31,"kg"),x("Makarna","🍝",22),
            x("Yoğurt","🥣",24,"kg"),x("Çay","🫖",35),x("Bal","🍯",45),
            x("Reçel","🍓",32),x("Tuz","🧂",12),x("Zeytin","🫒",30))),
        shop(8,"Kafe ve Pastane","☕","Fiyat ve paket kombinasyonu",true,listOf(
            x("Limonata","🍋",26),x("Meyve Suyu","🧃",19),x("Süt","🥛",17),
            x("Kakao","☕",23),x("Kurabiye","🍪",15),x("Dondurma","🍦",28),
            x("Kek Dilimi","🍰",31),x("Tost","🥪",35),x("Pasta","🎂",63),
            x("Çay","🫖",12),x("Waffle","🧇",44),x("Smoothie","🥤",39))),
        shop(9,"Benim Tezgâhım","🏪","Stok satın alma, fiyat ve kâr",true,listOf(
            x("Elma Sepeti","🍎",25),x("Çiçek","💐",29),x("Oyuncak","🧸",41),
            x("Meyve Suyu","🧃",19),x("Kurabiye","🍪",18),x("Hediye","🎁",39),
            x("Defter","📒",22),x("Kavanoz","🫙",24),x("Simit","🥨",13),
            x("Top","⚽",32),x("Çikolata","🍫",16),x("Kupa","🏆",43)))
    )

    val productCount:Int get()=shops.sumOf { it.products.size }
    const val FREE_SHOPS=3

    fun scenarios(seed:Long,tier:LearningTier):List<MarketScenario> {
        require(seed>0L)
        val random=Random(seed xor 0x77121L)
        return shops.mapIndexed { index,shop ->
            val shelves=shop.products.shuffled(random).take(6).mapIndexed { i,item ->
                val price=when(tier) {
                    LearningTier.TEMEL -> item.priceKurus
                    LearningTier.GELISEN -> item.priceKurus * 2
                    LearningTier.USTALIK -> item.priceKurus * 3 + if(i%2==0) 50 else 0
                }
                item.copy(priceKurus=price)
            }
            val targetCount=when(tier) {
                LearningTier.TEMEL -> 2
                LearningTier.GELISEN -> 3
                LearningTier.USTALIK -> 4
            }
            val needed=shelves.shuffled(random).take(targetCount).map { good ->
                MarketLine(good.id,if(tier==LearningTier.TEMEL) 1 else 1+random.nextInt(2))
            }
            val total=needed.sumOf { line ->
                shelves.first { it.id==line.productId }.priceKurus*line.quantity
            }
            val budget=total+when(tier) {
                LearningTier.TEMEL -> 1500
                LearningTier.GELISEN -> 3000
                LearningTier.USTALIK -> 7000
            }
            // Choose from real denominations. One or more notes of this denomination.
            val note=when(tier) {
                LearningTier.TEMEL -> 5000
                LearningTier.GELISEN -> 10000
                LearningTier.USTALIK -> 20000
            }
            val paid=(total/note+1)*note
            val paymentOptions=options(total,
                listOf(total-100,total+100,total-50,paid),random)
            val change=paid-total
            val changeOptions=options(change,
                listOf(change-100,change+100,change-50,change+50),random)
            MarketScenario(shop.title,shop.learning,shelves,needed,budget,
                paymentOptions,changeOptions,paid,
                bonus=bonus(index,tier,shelves,random))
        }
    }

    private fun options(correct:Int,wrong:List<Int>,random:Random):List<Int> {
        val values=linkedSetOf(correct)
        wrong.filter { it>=0 }.forEach { values.add(it) }
        var delta=100
        while(values.size<4) {values.add(correct+delta);delta+=100}
        return values.take(4).shuffled(random)
    }

    private fun bonus(index:Int,tier:LearningTier,
        goods:List<MarketGood>,random:Random):MarketBonus {
        val item=goods.first()
        val lower=tier==LearningTier.TEMEL
        val title:String; val prompt:String;val explanation:String;val correct:Int
        val kind:MarketBonusKind
        when(index) {
            0 -> {
                kind=MarketBonusKind.WEIGHT;title="Tartıyla Öğren"
                val multiplier=if(lower) 2 else if(tier==LearningTier.GELISEN) 1 else 3
                val divisor=if(tier==LearningTier.GELISEN) 2 else 2
                correct=item.priceKurus*multiplier/divisor*(if(lower) 2 else 1)
                val amount=if(lower) "2 kilogram" else if(tier==LearningTier.GELISEN)
                    "500 gram" else "1,5 kilogram"
                prompt="1 kg ${item.name} ${MarketAdventureEngine.format(item.priceKurus)}. " +
                    "$amount kaç TL?"
                explanation="1000 gram 1 kilogramdır. Önce istenen ağırlığı bul."
            }
            1 -> {
                kind=MarketBonusKind.VOLUME;title="Litre Dedektifi"
                val unit=1700; correct=if(tier==LearningTier.USTALIK) 2550
                    else if(lower) 3400 else 5100
                prompt="1 litre süt 17 TL. " +
                    if(tier==LearningTier.USTALIK)
                        "1 litre 500 mililitre kaç TL?" else
                        "${if(lower) 2 else 3} litre kaç TL?"
                explanation="1 litre = 1000 mililitre. Litre fiyatını miktarla çarp."
            }
            2 -> {
                kind=MarketBonusKind.SAVINGS;title="Harçlık Kutusu"
                val allowance=if(lower) 10000 else if(tier==LearningTier.GELISEN) 20000 else 30000
                val spent=if(lower) 6000 else if(tier==LearningTier.GELISEN) 12500 else 18550
                correct=allowance-spent
                prompt="${MarketAdventureEngine.format(allowance)} harçlığından " +
                    "${MarketAdventureEngine.format(spent)} harcarsan ne kadar biriktirirsin?"
                explanation="Biriken tutar = harçlık − harcama."
            }
            3 -> {
                kind=MarketBonusKind.DISCOUNT;title="İndirim Avcısı"
                val discount=if(tier==LearningTier.USTALIK)
                    item.priceKurus/10 else if(tier==LearningTier.GELISEN) 500 else 200
                correct=item.priceKurus-discount
                prompt="${item.name} fiyatı ${MarketAdventureEngine.format(item.priceKurus)}. " +
                    if(tier==LearningTier.USTALIK) "%10 indirimli fiyat kaç TL?" else
                        "${MarketAdventureEngine.format(discount)} indirimden sonra kaç TL?"
                explanation="Etiket fiyatından indirim tutarını çıkar."
            }
            4 -> {
                kind=MarketBonusKind.COMPARE;title="Ucuza Al"
                val saving=if(lower) 200 else if(tier==LearningTier.GELISEN) 500 else 1250
                correct=saving
                prompt="${item.name} bir yerde ${MarketAdventureEngine.format(item.priceKurus)}, " +
                    "diğerinde ${MarketAdventureEngine.format(item.priceKurus-saving)}. " +
                    "Ucuzdan alırsan kaç TL biriktirirsin?"
                explanation="İki etiket arasındaki fark tasarruftur."
            }
            5 -> {
                kind=MarketBonusKind.BUDGET;title="Takım Bütçesi"
                val reserve=if(lower) 2000 else if(tier==LearningTier.GELISEN) 3500 else 5750
                val allowance=item.priceKurus+reserve
                correct=reserve
                prompt="Takıma ${MarketAdventureEngine.format(allowance)} ayrıldı. " +
                    "${item.name} ${MarketAdventureEngine.format(item.priceKurus)}. " +
                    "Alırsan ne kadar kalır?"
                explanation="Bütçeden harcamayı çıkar."
            }
            6 -> {
                kind=MarketBonusKind.UNIT_PRICE;title="Paket Karşılaştırma"
                val one=item.priceKurus
                correct=one*2
                prompt="1 paket ${item.name} ${MarketAdventureEngine.format(one)}. " +
                    "2 paket kaç TL?"
                explanation="Birim fiyatı ikiyle çarp. Paket miktarları eşitse adil karşılaştırılır."
            }
            7 -> {
                kind=MarketBonusKind.BUNDLE;title="Çoklu Paket"
                val one=item.priceKurus
                val count=if(lower)2 else if(tier==LearningTier.GELISEN)3 else 4
                correct=one*count
                prompt="1 ${item.unit} ${item.name} ${MarketAdventureEngine.format(one)}. " +
                    "$count ${item.unit} kaç TL tutar?"
                explanation="Birim fiyat × alınan miktar = toplam tutar."
            }
            8 -> {
                kind=MarketBonusKind.COMPARE;title="Kafe İkramı"
                val other=goods[1]
                correct=item.priceKurus+other.priceKurus
                prompt="Bir ${item.name} ve bir ${other.name} alırsan " +
                    "toplam kaç TL ödersin?"
                explanation="Birinci ürünün fiyatına ikincisini ekle."
            }
            else -> {
                kind=MarketBonusKind.PROFIT;title="Kendi Tezgâhım"
                val margin=(if(lower) 500 else if(tier==LearningTier.GELISEN) 800 else 1250)
                val sale=item.priceKurus+margin;correct=margin
                prompt="${item.name} toptan ${MarketAdventureEngine.format(item.priceKurus)}. " +
                    "${MarketAdventureEngine.format(sale)} fiyatla satarsan kâr kaç TL?"
                explanation="Kâr = satış fiyatı − alış maliyeti."
            }
        }
        return MarketBonus(kind,title,prompt,explanation,correct,
            options(correct,listOf(correct-100,correct+100,correct-200,correct+500),
                random))
    }
}
