package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/** No floating point arithmetic is used for actual monetary transactions. */
data class MarketGood(val id: Int, val name: String, val emoji: String, val priceKurus: Int)
data class MarketLine(val productId: Int, val quantity: Int)
data class MarketScenario(
    val location: String,
    val hint: String,
    val products: List<MarketGood>,
    val needed: List<MarketLine>,
    val budgetKurus: Int,
    /** Four possible calculated cart totals, exactly one correct. */
    val payOptionsKurus: List<Int>,
    val changeOptionsKurus: List<Int>,
    /** The fixed banknote amount handed to the cashier for the change task. */
    val cashierPayKurus: Int,
    val bonus: MarketBonus? = null
) {
    fun requiredTotal(): Int = needed.sumOf { line ->
        (products.firstOrNull { it.id == line.productId }?.priceKurus ?: 0) * line.quantity
    }
}
enum class MarketAdventurePhase { WELCOME, SHOP, PAYMENT, CHANGE, COMPLETE, BONUS }
enum class MarketBonusKind { DISCOUNT, COMPARE, PROFIT }
data class MarketBonus(
    val kind: MarketBonusKind, val title: String, val prompt: String,
    val explanation: String, val correctKurus: Int, val choicesKurus: List<Int>
)
data class MarketAdventureRun(
    val seed: Long = 0L,
    val tier: LearningTier = LearningTier.TEMEL,
    val visit: Int = 0,
    val phase: MarketAdventurePhase = MarketAdventurePhase.WELCOME,
    val basket: List<MarketLine> = emptyList(),
    val paidKurus: Int = 0,
    val errors: Int = 0,
    val hints: Int = 0,
    val attempts: Int = 0,
    val perfectDecisions: Int = 0,
    val taskHadError: Boolean = false,
    val taskHadHint: Boolean = false,
    val completedRuns: Int = 0,
    val contentVersion: Int = 1
) {
    val finished get() = phase == MarketAdventurePhase.COMPLETE
}
/**
 * Three market visits, each: exact shopping list, sufficient payment and exact change.
 * Currency amounts are stored as integer kuruş, and all tasks are reproducible by seed.
 */
object MarketAdventureEngine {
    const val VISITS = 3
    const val DECISIONS = 9
    const val PREMIUM_VISITS = 6
    const val PREMIUM_DECISIONS = 21
    fun visitCount(run: MarketAdventureRun) =
        if(run.contentVersion>=2) PREMIUM_VISITS else VISITS
    fun decisionCount(run: MarketAdventureRun) =
        if(run.contentVersion>=2) PREMIUM_DECISIONS else DECISIONS

    private data class Template(val name: String, val hint: String,
        val goods: List<Triple<String,String,Int>>)
    private val templates = listOf(
        Template("Güneşli Manav", "Taze meyvelerle alışveriş listeni tamamla.",
            listOf(Triple("Elma","🍎",12),Triple("Muz","🍌",15),
                Triple("Armut","🍐",9),Triple("Portakal","🍊",8),
                Triple("Çilek","🍓",18),Triple("Üzüm","🍇",14))),
        Template("Mahalle Fırını", "Ekmek ve atıştırmalıkları hesapla.",
            listOf(Triple("Ekmek","🥖",10),Triple("Simit","🥨",12),
                Triple("Kurabiye","🍪",14),Triple("Kek","🧁",16),
                Triple("Poğaça","🥐",13),Triple("Sandviç","🥪",20))),
        Template("Oyuncak Atölyesi", "İhtiyacını seç, bütçeni koru.",
            listOf(Triple("Top","⚽",14),Triple("Yapboz","🧩",18),
                Triple("Uçurtma","🪁",16),Triple("Boya","🖍️",12),
                Triple("Kitap","📚",15),Triple("Robot","🤖",20)))
    )

    private val extraTemplates = listOf(
        Template("Renkli Kırtasiye","Alışveriş yap, indirimli fiyatı hesapla.",
            listOf(Triple("Defter","📒",15),Triple("Kalem","✏️",8),
                Triple("Silgi","🧽",5),Triple("Cetvel","📏",12),
                Triple("Boya","🖍️",20),Triple("Kitap","📗",25))),
        Template("Giyim Sokağı","Aynı ürünün iki fiyatını karşılaştır.",
            listOf(Triple("Tişört","👕",30),Triple("Şapka","🧢",25),
                Triple("Çorap","🧦",12),Triple("Atkı","🧣",20),
                Triple("Eldiven","🧤",18),Triple("Ayakkabı","👟",45))),
        Template("Benim Tezgâhım","Kendi satışını yap, kârı hesapla.",
            listOf(Triple("Elma Sepeti","🍎",22),Triple("Çiçek","💐",27),
                Triple("Oyuncak","🧸",35),Triple("Meyve Suyu","🧃",13),
                Triple("Kurabiye","🍪",18),Triple("Hediye","🎁",32)))
    )

    private fun bonusFor(visit: Int, goods: List<MarketGood>, random: Random): MarketBonus? {
        if(visit<3) return null
        val first=goods[0]
        val kind=when(visit) {
            3 -> MarketBonusKind.DISCOUNT
            4 -> MarketBonusKind.COMPARE
            else -> MarketBonusKind.PROFIT
        }
        val title:String
        val prompt:String
        val explanation:String
        val correct:Int
        when(kind) {
            MarketBonusKind.DISCOUNT -> {
                correct=first.priceKurus*9/10
                title="Etiket İndirimi"
                prompt="${first.name} normalde ${format(first.priceKurus)}. %10 indirimli fiyatı kaç TL?"
                explanation="Önce fiyatın onda birini bul, sonra normal fiyattan çıkar."
            }
            MarketBonusKind.COMPARE -> {
                correct=200
                title="Akıllı Fiyat Karşılaştırma"
                prompt="${first.name} A tezgâhında ${format(first.priceKurus)}, " +
                    "B tezgâhında ${format(first.priceKurus-200)}. " +
                    "Ucuz tezgâhtan alırsan kaç TL tasarruf edersin?"
                explanation="Aynı ürünün iki fiyatını karşılaştır; büyük fiyattan küçüğü çıkar."
            }
            MarketBonusKind.PROFIT -> {
                val cost=first.priceKurus
                val sale=cost+500+random.nextInt(1,4)*100
                correct=sale-cost
                title="Kendi Tezgâhın"
                prompt="${first.name} ürününü ${format(cost)} maliyetle aldın, " +
                    "${format(sale)} fiyatla sattın. Kârın kaç TL?"
                explanation="Kâr = satış fiyatı − maliyet. Para üstüyle karıştırma."
            }
        }
        val choices=linkedSetOf(correct,correct+100,correct+200,
            (correct-100).coerceAtLeast(0),correct+500)
        return MarketBonus(kind,title,prompt,explanation,correct,
            choices.take(4).shuffled(random))
    }

    /** Only the best 1–3-star result is awarded by StarManager's high-water policy. */
    fun starsFor(run: MarketAdventureRun): Int = if(!run.finished) 0 else when {
        run.perfectDecisions >= (if(run.contentVersion>=2) 18 else 8) -> 3
        run.perfectDecisions >= (if(run.contentVersion>=2) 12 else 5) -> 2
        else -> 1
    }

    fun format(kurus: Int): String {
        val value = kurus.coerceAtLeast(0)
        val lira = value / 100
        val rest = value % 100
        return if(rest==0) "${lira} TL" else "${lira},${rest.toString().padStart(2,'0')} TL"
    }

    fun scenarios(seed: Long, tier: LearningTier, contentVersion: Int = 1): List<MarketScenario> {
        require(seed>0)
        val r = Random(seed xor 0x7315B4L)
        val selected=if(contentVersion>=2) templates+extraTemplates else templates
        return selected.mapIndexed { visit, template ->
            val multiplier = when(tier) {
                LearningTier.TEMEL -> 1
                LearningTier.GELISEN -> 2
                LearningTier.USTALIK -> 3
            }
            val goods = template.goods.shuffled(r).take(4).mapIndexed { index,g ->
                val fractional = if(tier==LearningTier.USTALIK && (index+visit)%2==0) 50 else 0
                MarketGood(visit*10+index+1,g.first,g.second,
                    g.third*multiplier*100+fractional)
            }
            val required = when(tier) {
                LearningTier.TEMEL -> 2
                LearningTier.GELISEN -> 2
                LearningTier.USTALIK -> 3
            }
            val needed = goods.shuffled(r).take(required).map { g ->
                MarketLine(g.id,if(tier==LearningTier.TEMEL) 1 else 1+r.nextInt(2))
            }
            val total = needed.sumOf { line ->
                goods.first { it.id==line.productId }.priceKurus*line.quantity
            }
            val budget = total + (when(tier) {
                LearningTier.TEMEL -> 1000
                LearningTier.GELISEN -> 3000
                LearningTier.USTALIK -> 5000
            })
            // Cash is a realistic rounded banknote amount, not total+an arbitrary margin.
            val banknoteKurus = when(tier) {
                LearningTier.TEMEL -> 5000
                LearningTier.GELISEN -> 10000
                LearningTier.USTALIK -> 20000
            }
            val pay = (total / banknoteKurus + 1) * banknoteKurus
            val choices = listOf(total-100, total-50, total, pay).filter { it>0 }
                .distinct().toMutableList()
            var x=total+100
            while(choices.size<4) {if(x !in choices) choices.add(x);x+=100}
            val correctChange = pay-total
            val wrongChange = linkedSetOf(correctChange)
            for(offset in listOf(-100,100,-50,50,-200,200,-500,500)) {
                if(correctChange+offset>=0) wrongChange.add(correctChange+offset)
            }
            MarketScenario(template.name,template.hint,goods,needed,budget,
                choices.take(4).shuffled(r),
                wrongChange.take(4).shuffled(r),pay,
                if(contentVersion>=2) bonusFor(visit,goods,r) else null)
        }
    }

    fun scenario(run: MarketAdventureRun): MarketScenario? =
        if(run.seed>0 && run.visit in 0 until visitCount(run))
            scenarios(run.seed,run.tier,run.contentVersion)[run.visit] else null

    fun amount(run: MarketAdventureRun, scenario: MarketScenario): Int =
        run.basket.sumOf { line ->
            (scenario.products.firstOrNull { it.id==line.productId }?.priceKurus ?: 0)*line.quantity
        }

    fun basketExact(run: MarketAdventureRun, scenario: MarketScenario): Boolean {
        val actual=run.basket.filter { it.quantity>0 }.associate { it.productId to it.quantity }
        val required=scenario.needed.associate { it.productId to it.quantity }
        return actual==required && amount(run,scenario)<=scenario.budgetKurus
    }

    fun valid(run: MarketAdventureRun): Boolean {
        if(run.seed<0L || run.contentVersion !in 1..2 || run.visit !in 0..visitCount(run) ||
            run.attempts<0 || run.errors !in 0..run.attempts ||
            run.hints<0 || run.perfectDecisions !in 0..decisionCount(run) ||
            run.paidKurus<0 || run.completedRuns<0 ||
            run.basket.size>4 || run.basket.map { it.productId }.distinct().size!=run.basket.size ||
            run.basket.any { it.productId<=0 || it.quantity !in 1..3 } ||
            (run.seed==0L && (run.visit!=0 || run.phase!=MarketAdventurePhase.WELCOME)) ||
            ((run.phase==MarketAdventurePhase.COMPLETE)!=(run.visit==visitCount(run))) ||
            (run.phase==MarketAdventurePhase.WELCOME && run.visit!=0)) return false
        if(run.seed==0L) return run.basket.isEmpty()
        if(run.phase==MarketAdventurePhase.COMPLETE)
            return run.basket.isEmpty() && run.paidKurus==0
        val scene=scenario(run)?:return false
        if(run.basket.any { line -> scene.products.none { it.id==line.productId } })
            return false
        if(run.phase==MarketAdventurePhase.SHOP)
            return run.paidKurus==0
        if(run.phase==MarketAdventurePhase.BONUS && scene.bonus==null) return false
        return basketExact(run,scene) &&
            (run.phase!=MarketAdventurePhase.CHANGE ||
                run.paidKurus==scene.cashierPayKurus)
    }

    fun changeQuantity(run: MarketAdventureRun, productId: Int, delta: Int): MarketAdventureRun {
        if(!valid(run) || run.phase!=MarketAdventurePhase.SHOP) return run
        val scene=scenario(run)?:return run
        if(scene.products.none { it.id==productId } || delta !in listOf(-1,1)) return run
        val qty=(run.basket.firstOrNull { it.productId==productId }?.quantity?:0)
        val updated=(qty+delta).coerceIn(0,3)
        return run.copy(basket=(run.basket.filterNot { it.productId==productId }+
            if(updated>0) listOf(MarketLine(productId,updated)) else emptyList()))
    }

    fun checkBasket(run: MarketAdventureRun): MarketAdventureRun {
        if(!valid(run) || run.phase!=MarketAdventurePhase.SHOP) return run
        val scene=scenario(run)?:return run
        val correct=basketExact(run,scene)
        return register(run,correct).copy(
            phase=if(correct) MarketAdventurePhase.PAYMENT else MarketAdventurePhase.SHOP
        )
    }

    fun choosePayment(run: MarketAdventureRun, paid: Int): MarketAdventureRun {
        if(!valid(run) || run.phase!=MarketAdventurePhase.PAYMENT) return run
        val scene=scenario(run)?:return run
        if(paid !in scene.payOptionsKurus) return run
        // Basket total must be calculated first; the cashier later receives a
        // fixed separate banknote, so budgeting and making change stay distinct.
        val correct=paid==scene.requiredTotal()
        return register(run,correct).copy(
            paidKurus=if(correct) scene.cashierPayKurus else 0,
            phase=if(correct) MarketAdventurePhase.CHANGE else MarketAdventurePhase.PAYMENT
        )
    }

    fun chooseChange(run: MarketAdventureRun, change: Int): MarketAdventureRun {
        if(!valid(run) || run.phase!=MarketAdventurePhase.CHANGE) return run
        val scene=scenario(run)?:return run
        if(change !in scene.changeOptionsKurus) return run
        val correct=change==run.paidKurus-amount(run,scene)
        val checked=register(run,correct)
        if(!correct) return checked
        val last=run.visit==VISITS-1
        return checked.copy(
            visit=run.visit+1,
            phase=if(last) MarketAdventurePhase.COMPLETE else MarketAdventurePhase.SHOP,
            basket=emptyList(),paidKurus=0,
            completedRuns=run.completedRuns+(if(last) 1 else 0)
        )
    }

    fun hint(run: MarketAdventureRun): MarketAdventureRun {
        if(!valid(run) || run.phase !in listOf(
                MarketAdventurePhase.SHOP,MarketAdventurePhase.PAYMENT,MarketAdventurePhase.CHANGE)
            || run.taskHadHint) return run
        return run.copy(hints=run.hints+1,taskHadHint=true)
    }

    private fun register(run: MarketAdventureRun,correct: Boolean): MarketAdventureRun {
        val good=if(correct && !run.taskHadError && !run.taskHadHint) 1 else 0
        return run.copy(attempts=run.attempts+1,
            errors=run.errors+(if(correct) 0 else 1),
            perfectDecisions=run.perfectDecisions+good,
            taskHadError=if(correct) false else true,
            taskHadHint=if(correct) false else run.taskHadHint)
    }
}
