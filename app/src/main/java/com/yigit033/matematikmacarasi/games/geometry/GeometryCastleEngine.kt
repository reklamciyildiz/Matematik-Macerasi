package com.yigit033.matematikmacarasi.games.geometry

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

enum class CastleRoom(val title: String, val instruction: String) {
    COURTYARD("Şekil Avlusu", "Şekilleri özelliklerine göre keşfet"),
    BRIDGE("Köprü Atölyesi", "Doğru taş parçalarıyla köprüyü tamamla"),
    MIRROR("Ayna Kulesi", "Vitrayın eksik simetrisini oluştur"),
    CROWN("Taç Salonu", "Öğrendiklerinle kaleyi aydınlat")
}
enum class CastleShape(val display: String, val sides: Int) {
    CIRCLE("Çember", 0), TRIANGLE("Üçgen", 3), SQUARE("Kare", 4),
    RECTANGLE("Dikdörtgen", 4), PENTAGON("Beşgen", 5), HEXAGON("Altıgen", 6),
    RHOMBUS("Eşkenar dörtgen", 4), PARALLELOGRAM("Paralelkenar", 4),
    TRAPEZOID("Yamuk", 4), OCTAGON("Sekizgen", 8)
}
enum class CastleMode { FIND, BUILD, SYMMETRY }
data class CastleTask(
    val mode: CastleMode,
    val prompt: String,
    val options: List<CastleShape>,
    val target: CastleShape,
    val gridSize: Int,
    /** left half indexed by row * (gridSize/2) + col */
    val leftMask: Int,
    val skill: String,
    val explanation: String,
    /** Multiple silhouette sockets. Empty on legacy runs means one target. */
    val buildTargets: List<CastleShape> = emptyList()
) {
    val requiredPieces: List<CastleShape> get() =
        if (mode != CastleMode.BUILD) emptyList()
        else if (buildTargets.isNotEmpty()) buildTargets else listOf(target)
}
data class CastleRun(
    val seed: Long = 0L,
    val tier: LearningTier = LearningTier.TEMEL,
    val room: Int = 0,
    val step: Int = 0,
    val selected: CastleShape? = null,
    val solved: Boolean = false,
    val focusShape: CastleShape? = null,
    val mirrorMask: Int = 0,
    val attempts: Int = 0,
    val errors: Int = 0,
    val hints: Int = 0,
    val firstTryCorrect: Int = 0,
    val hasErrorInTask: Boolean = false,
    val hasHintInTask: Boolean = false,
    val completedRuns: Int = 0,
    /** Existing checkpoints have version 1; only new runs use the expanded catalogue. */
    val contentVersion: Int = 1,
    val placed: List<CastleShape> = emptyList()
) {
    val finished: Boolean get() = seed > 0L && room == 4
}

object GeometryCastleEngine {
    const val TASKS_PER_ROOM = 3
    const val TOTAL_TASKS = 12

    private val legacyShapes = listOf(
        CastleShape.CIRCLE, CastleShape.TRIANGLE, CastleShape.SQUARE,
        CastleShape.RECTANGLE, CastleShape.PENTAGON, CastleShape.HEXAGON
    )

    fun available(tier: LearningTier): List<CastleShape> = when (tier) {
        LearningTier.TEMEL -> legacyShapes.take(4)
        LearningTier.GELISEN -> legacyShapes
        LearningTier.USTALIK -> CastleShape.entries
    }

    private fun legacyAvailable(tier: LearningTier): List<CastleShape> = when (tier) {
        LearningTier.TEMEL -> legacyShapes.take(4)
        LearningTier.GELISEN -> legacyShapes.take(5)
        LearningTier.USTALIK -> legacyShapes
    }

    /** Legacy generators intentionally remain stable for in-progress v1 checkpoints. */
    fun tasks(seed: Long, tier: LearningTier,
        weakShape: CastleShape? = null, contentVersion: Int = 2): List<CastleTask> =
        if (contentVersion <= 1) legacyTasks(seed, tier, weakShape)
        else expandedTasks(seed, tier, weakShape)

    private fun legacyTasks(seed: Long, tier: LearningTier,
        weakShape: CastleShape?): List<CastleTask> {
        require(seed > 0)
        val rand = Random(seed xor 0x53631L)
        val shapes = legacyAvailable(tier)
        fun options(target: CastleShape): List<CastleShape> {
            val extras = shapes.filter { it != target }.shuffled(rand).take(3)
            return (extras + target).shuffled(rand)
        }
        fun shapeTask(mode: CastleMode, index: Int,
            forced: CastleShape? = null): CastleTask {
            val target = forced?.takeIf { it in shapes } ?:
                shapes[(index + rand.nextInt(shapes.size)) % shapes.size]
            val prompt = if(mode == CastleMode.FIND) when(target) {
                CastleShape.CIRCLE -> "Köşesi olmayan yuvarlak şekli bul."
                CastleShape.TRIANGLE -> "Üç kenarı olan şekli bul."
                CastleShape.SQUARE -> "Dört eşit kenarlı kareyi bul."
                CastleShape.RECTANGLE -> "Karşılıklı kenarları eşit, kare olmayan şekli bul."
                CastleShape.PENTAGON -> "Beş kenarlı şekli bul."
                CastleShape.HEXAGON -> "Altı kenarlı şekli bul."
                else -> "Köşeleri ve kenarları kontrol et."
            } else "Köprünün boş yuvasındaki siluete uyan taşı seç ve yerleştir."
            val explanation = if(target == CastleShape.CIRCLE)
                "Yuvarlak çemberin köşesi yoktur."
            else if(target == CastleShape.SQUARE)
                "Karenin dört kenarı eşit uzunluktadır."
            else if(target == CastleShape.RECTANGLE)
                "Dikdörtgenin karşılıklı kenarları aynı uzunluktadır."
            else "${target.display} şeklinin ${target.sides} kenarı vardır."
            return CastleTask(mode,prompt,options(target),target,4,0,
                if(mode == CastleMode.FIND) "geometry_classify" else "geometry_compose",
                explanation)
        }
        fun mirrorTask(index: Int): CastleTask {
            val size = if(tier == LearningTier.USTALIK) 6 else 4
            val half = size / 2
            var mask = 0
            val count = if(tier == LearningTier.TEMEL) 1 else if(tier == LearningTier.GELISEN) 2 else 4
            val cells = (0 until size * half).shuffled(rand).take(count)
            for (cell in cells) mask = mask or (1 shl cell)
            return CastleTask(CastleMode.SYMMETRY,
                if(tier == LearningTier.TEMEL)
                    "Sol vitraydaki parlayan karonun sağdaki eşini bul."
                else "Ayna çizgisine göre eksik karoları yerleştir.",
                emptyList(),CastleShape.SQUARE,size,mask,
                if(tier == LearningTier.TEMEL) "geometry_symmetry_recognize"
                    else "geometry_symmetry_build",
                "Her parça ayna çizgisine aynı uzaklıkta olmalı.")
        }
        val courtyard = (0..2).map { shapeTask(CastleMode.FIND,it,
            if(it == 0) weakShape else null) }
        val bridge = (0..2).map { shapeTask(CastleMode.BUILD,it) }
        val mirror = (0..2).map { mirrorTask(it) }
        // Finale distinct tasks from three learned mechanics.
        val crown = listOf(shapeTask(CastleMode.FIND,4),
            shapeTask(CastleMode.BUILD,5),mirrorTask(6))
        return courtyard + bridge + mirror + crown
    }

    /** Distinct silhouettes in each room; all prompts rely on measurable geometry. */
    private fun expandedTasks(seed: Long, tier: LearningTier,
        weakShape: CastleShape?): List<CastleTask> {
        require(seed > 0)
        val random = Random(seed xor 0x7F302L)
        val pool = available(tier)
        val shuffled = pool.shuffled(random)
        val priority = if (weakShape != null && weakShape in pool) listOf(weakShape) +
            shuffled.filterNot { it == weakShape } else shuffled
        val targets = (0 until 8).map { priority[it % priority.size] }
        fun choices(required: List<CastleShape>): List<CastleShape> =
            (required + pool.filterNot { it in required }.shuffled(random)
                .take(4 - required.size)).shuffled(random)

        fun shapeTask(mode: CastleMode, target: CastleShape): CastleTask {
            val prompt = if (mode == CastleMode.BUILD)
                "Köprünün eksik siluetlerine uygun parçaları sırayla yerleştir."
            else when (target) {
                CastleShape.CIRCLE -> "Köşesi ve düz kenarı olmayan çemberi bul."
                CastleShape.TRIANGLE -> "Üç düz kenarı olan şekli bul."
                CastleShape.SQUARE -> "Dört eşit kenarı ve dört dik açısı olan kareyi bul."
                CastleShape.RECTANGLE -> "Dört dik açısı olan, iki uzun iki kısa kenarlı dikdörtgeni bul."
                CastleShape.PENTAGON -> "Beş düz kenarlı çokgeni bul."
                CastleShape.HEXAGON -> "Altı düz kenarlı çokgeni bul."
                CastleShape.RHOMBUS -> "Dört kenarı eşit, ancak dik açısı olmayan eğik dörtgeni bul."
                CastleShape.PARALLELOGRAM ->
                    "İki çift paralel kenarı olan, dik açısı olmayan ve kenarları eşit olmayan dörtgeni bul."
                CastleShape.TRAPEZOID ->
                    "Yalnız bir çift karşılıklı kenarı paralel olan dörtgeni bul."
                CastleShape.OCTAGON -> "Sekiz düz kenarı olan çokgeni bul."
            }
            val explanation = when (target) {
                CastleShape.CIRCLE -> "Çember bir eğri çizgidir; düz kenarı ve köşesi yoktur."
                CastleShape.TRIANGLE -> "Üçgenin üç düz kenarı ve üç köşesi vardır."
                CastleShape.SQUARE -> "Karenin dört kenarı eşittir ve dört açısı diktir."
                CastleShape.RECTANGLE -> "Dikdörtgenin bütün açıları diktir; burada iki kenar uzun, ikisi kısadır."
                CastleShape.RHOMBUS -> "Bu eşkenar dörtgenin dört kenarı eşittir, açıları dik değildir."
                CastleShape.PARALLELOGRAM -> "Paralelkenarın karşılıklı kenarları paralel ve eşit uzunluktadır."
                CastleShape.TRAPEZOID -> "Bu yamuğun yalnızca bir çift karşılıklı kenarı paraleldir."
                else -> "${target.display} ${target.sides} kenarlı bir çokgendir."
            }
            val pieceCount = when (tier) {
                LearningTier.TEMEL -> 1
                LearningTier.GELISEN -> 2
                LearningTier.USTALIK -> 3
            }
            val pieces = if (mode == CastleMode.BUILD)
                listOf(target) + pool.filterNot { it == target }
                    .shuffled(random).take(pieceCount - 1)
            else emptyList()
            return CastleTask(mode, prompt,
                choices(if (pieces.isEmpty()) listOf(target) else pieces),
                target, 4, 0,
                if (mode == CastleMode.FIND) "geometry_classify" else "geometry_compose",
                explanation, pieces)
        }
        fun mirrorTask(index: Int): CastleTask {
            val grid = if (tier == LearningTier.USTALIK) 6 else 4
            val cells = grid * (grid / 2)
            val filled = when (tier) {
                LearningTier.TEMEL -> 1 + index % 2
                LearningTier.GELISEN -> 2 + index % 2
                LearningTier.USTALIK -> 3 + index % 3
            }
            val mask = (0 until cells).shuffled(random).take(filled)
                .fold(0) { bits, cell -> bits or (1 shl cell) }
            return CastleTask(CastleMode.SYMMETRY,
                if (tier == LearningTier.TEMEL)
                    "Sol vitrayın sağdaki ayna görüntüsünü tamamla."
                else "Kareleri düşey ayna çizgisine göre tamamla.",
                emptyList(), CastleShape.SQUARE, grid, mask,
                if (tier == LearningTier.TEMEL) "geometry_symmetry_recognize"
                else "geometry_symmetry_build",
                "Karşılıklı kareler ayna çizgisine eşit uzaklıktadır.")
        }
        return listOf(
            shapeTask(CastleMode.FIND, targets[0]),
            shapeTask(CastleMode.FIND, targets[1]),
            shapeTask(CastleMode.FIND, targets[2]),
            shapeTask(CastleMode.BUILD, targets[3]),
            shapeTask(CastleMode.BUILD, targets[4]),
            shapeTask(CastleMode.BUILD, targets[5]),
            mirrorTask(0), mirrorTask(1), mirrorTask(2),
            shapeTask(CastleMode.FIND, targets[6]),
            shapeTask(CastleMode.BUILD, targets[7]),
            mirrorTask(3)
        )
    }

    fun task(run: CastleRun): CastleTask? =
        if (run.seed <= 0 || run.room !in 0..3 || run.step !in 0..2) null
        else tasks(run.seed, run.tier, run.focusShape, run.contentVersion)[run.room * 3 + run.step]

    fun symmetricRightMask(left: Int, size: Int): Int {
        require(size in listOf(4,6))
        val half = size/2
        var mask = 0
        for (row in 0 until size) for (col in 0 until half) {
            val bit = row * half + col
            if ((left and (1 shl bit)) != 0) {
                val mirroredCol = half - col - 1
                mask = mask or (1 shl (row * half + mirroredCol))
            }
        }
        return mask
    }

    fun toggleMirror(mask: Int, row: Int, rightCol: Int, size: Int): Int {
        if(size !in listOf(4,6) || row !in 0 until size || rightCol !in 0 until size/2) return mask
        return mask xor (1 shl (row * (size/2) + rightCol))
    }

    fun isCorrect(task: CastleTask, shape: CastleShape?, mask: Int): Boolean =
        when(task.mode) {
            CastleMode.FIND,CastleMode.BUILD -> shape == task.target
            CastleMode.SYMMETRY -> mask == symmetricRightMask(task.leftMask, task.gridSize)
        }

    fun valid(run: CastleRun): Boolean =
        run.seed >= 0 && run.room in 0..4 && run.step in 0..2 &&
            run.attempts >= 0 && run.errors in 0..run.attempts &&
            run.hints >= 0 && run.firstTryCorrect in 0..TOTAL_TASKS &&
            run.completedRuns >= 0 &&
            (run.seed > 0 || (run.room == 0 && run.step == 0)) &&
            run.mirrorMask >= 0 && (run.mirrorMask ushr 18) == 0 &&
            run.contentVersion in 1..2 && run.placed.size <= 3 &&
            run.placed.all { it in CastleShape.entries }

    fun answer(run: CastleRun, correct: Boolean): CastleRun {
        if(!valid(run) || run.seed <= 0 || run.room == 4) return run
        return run.copy(
            attempts=run.attempts+1,
            errors=run.errors + if(correct) 0 else 1,
            hasErrorInTask=run.hasErrorInTask || !correct
        )
    }
    fun hint(run: CastleRun): CastleRun =
        if(!valid(run) || run.seed <= 0 || run.room == 4 || run.hasHintInTask) run
        else run.copy(hints=run.hints+1,hasHintInTask=true)

    fun advance(run: CastleRun): CastleRun {
        if(!valid(run) || run.seed <= 0 || run.room == 4 || !run.solved) return run
        val completed = run.room == 3 && run.step == 2
        val nextStep = if(run.step == 2) 0 else run.step + 1
        val nextRoom = if(run.step == 2) run.room+1 else run.room
        return run.copy(room=nextRoom,step=nextStep,selected=null,solved=false,mirrorMask=0,
            placed=emptyList(),
            firstTryCorrect=run.firstTryCorrect+
                if(!run.hasHintInTask && !run.hasErrorInTask) 1 else 0,
            hasErrorInTask=false,hasHintInTask=false,
            completedRuns=run.completedRuns+if(completed) 1 else 0)
    }
}
