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
enum class CastleMode { FIND, BUILD, SYMMETRY, MEASURE }
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
    val buildTargets: List<CastleShape> = emptyList(),
    val measureAnswer:Int=0,
    val measureMax:Int=0,
    val measureStep:Int=1,
    val measureKind:String="",
    val measureWidth:Int=0,
    val measureHeight:Int=0,
    val measureUnit:String=""
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
    val placed: List<CastleShape> = emptyList(),
    val grade:Int=2,
    val measureValue:Int=0
) {
    val finished: Boolean get() = seed > 0L && room == 4
}

object GeometryCastleEngine {
    const val TASKS_PER_ROOM = 3
    const val TOTAL_TASKS = 12 // old v1/v2 games
    const val PREMIUM_STEPS_PER_ROOM = 5
    const val PREMIUM_TOTAL_TASKS = 20
    fun stepsPerRoom(run:CastleRun)=if(run.contentVersion>=3)5 else 3
    fun totalTasks(run:CastleRun)=if(run.contentVersion>=3)20 else 12
    fun taskIndex(run:CastleRun)=run.room*stepsPerRoom(run)+run.step

    private val legacyShapes = listOf(
        CastleShape.CIRCLE, CastleShape.TRIANGLE, CastleShape.SQUARE,
        CastleShape.RECTANGLE, CastleShape.PENTAGON, CastleShape.HEXAGON
    )

    fun available(tier: LearningTier): List<CastleShape> = when (tier) {
        LearningTier.TEMEL -> legacyShapes.take(4)
        LearningTier.GELISEN -> legacyShapes
        LearningTier.USTALIK -> CastleShape.entries
    }

    /** Grade is a content ceiling; skill tier never unlocks future-grade content. */
    fun available(tier:LearningTier,grade:Int):List<CastleShape>{
        val shapes=when(grade.coerceIn(1,8)){
            1,2->legacyShapes.take(4)
            3,4->legacyShapes
            else->CastleShape.entries
        }
        val cap=when(tier){
            LearningTier.TEMEL->4
            LearningTier.GELISEN->6
            LearningTier.USTALIK->10
        }
        return shapes.take(cap)
    }

    private fun legacyAvailable(tier: LearningTier): List<CastleShape> = when (tier) {
        LearningTier.TEMEL -> legacyShapes.take(4)
        LearningTier.GELISEN -> legacyShapes.take(5)
        LearningTier.USTALIK -> legacyShapes
    }

    /** Legacy generators intentionally remain stable for in-progress v1 checkpoints. */
    fun tasks(seed:Long,tier:LearningTier,weakShape:CastleShape?=null,
        contentVersion:Int=2,grade:Int=2):List<CastleTask> = when {
        contentVersion<=1->legacyTasks(seed,tier,weakShape)
        contentVersion==2->expandedTasks(seed,tier,weakShape)
        else->premiumTasks(seed,tier,grade,weakShape)
    }

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


    /** New 20-task world; legacy v1/v2 generated tasks remain bit-for-bit unchanged. */
    private fun premiumTasks(seed:Long,tier:LearningTier,grade:Int,
        weakShape:CastleShape?):List<CastleTask> {
        require(seed>0 && grade in 1..8)
        val rng=Random(seed xor 0x72931L)
        val pool=available(tier,grade)
        val priorities=if(weakShape in pool) listOfNotNull(weakShape)+
            pool.filterNot{it==weakShape}.shuffled(rng) else pool.shuffled(rng)
        fun target(i:Int)=priorities[i%priorities.size]
        fun picks(needed:List<CastleShape>):List<CastleShape> {
            val a=needed.distinct()
            return (a+pool.filterNot{it in a}.shuffled(rng)
                .take((4-a.size).coerceAtLeast(0))).shuffled(rng)
        }
        fun shape(mode:CastleMode,index:Int):CastleTask {
            val chosen=target(index)
            val count=when(tier){
                LearningTier.TEMEL->if(grade<=2)1 else 2
                LearningTier.GELISEN->2
                LearningTier.USTALIK->if(grade<=2)2 else 3
            }
            val pieces=if(mode==CastleMode.BUILD)
                listOf(chosen)+pool.filterNot{it==chosen}.shuffled(rng).take(count-1)
                else emptyList()
            val prompt=if(mode==CastleMode.BUILD)
                "Köprüdeki şekil siluetlerini taşlarla sırayla doldur."
                else when(chosen){
                    CastleShape.CIRCLE->"Düz kenarı ve köşesi olmayan çemberi bul."
                    CastleShape.TRIANGLE->"3 köşesi olan üçgeni bul."
                    CastleShape.SQUARE->"4 eşit kenarı ve 4 dik açısı olan kareyi bul."
                    CastleShape.RECTANGLE->"4 dik açılı, iki uzun iki kısa kenarlı dikdörtgeni bul."
                    CastleShape.PENTAGON->"5 köşeli çokgeni bul."
                    CastleShape.HEXAGON->"6 köşeli çokgeni bul."
                    CastleShape.OCTAGON->"8 köşeli çokgeni bul."
                    CastleShape.RHOMBUS->"Dört eşit kenarlı eğik dörtgeni bul."
                    CastleShape.PARALLELOGRAM->"Karşılıklı kenarları paralel olan eğik dörtgeni bul."
                    CastleShape.TRAPEZOID->"Yalnızca bir çift paralel kenarı olan yamuğu bul."
                }
            val explanation=when(chosen){
                CastleShape.CIRCLE->"Çemberin düz kenarı ve köşesi yoktur."
                CastleShape.SQUARE->"Karenin dört kenarı eşit ve dört açısı diktir."
                CastleShape.RECTANGLE->"Dikdörtgenin dört açısı diktir ve karşı kenarları eşittir."
                CastleShape.RHOMBUS->"Eşkenar dörtgenin dört kenarı eşittir."
                CastleShape.PARALLELOGRAM->"Paralelkenarın karşılıklı kenarları paraleldir."
                CastleShape.TRAPEZOID->"Bu yamukta yalnızca bir çift karşılıklı kenar paraleldir."
                else->chosen.display+" şeklinin "+chosen.sides+" kenarı vardır."
            }
            return CastleTask(mode,prompt,picks(if(pieces.isEmpty())
                listOf(chosen) else pieces),chosen,4,0,
                if(mode==CastleMode.FIND)"geometry_classify" else "geometry_compose",
                explanation,pieces)
        }
        fun mirror(index:Int):CastleTask {
            val grid=if(grade>=5 && tier==LearningTier.USTALIK)6 else 4
            val cells=grid*(grid/2)
            val count=when(tier){
                LearningTier.TEMEL->1+index%2
                LearningTier.GELISEN->2+index%3
                LearningTier.USTALIK->3+index%4
            }.coerceAtMost(cells-1)
            val mask=(0 until cells).shuffled(rng).take(count)
                .fold(0){acc,cell->acc or (1 shl cell)}
            return CastleTask(CastleMode.SYMMETRY,
                "Sol vitraydaki kristallerin sağdaki ayna görüntüsünü oluştur.",
                emptyList(),CastleShape.SQUARE,grid,mask,
                if(tier==LearningTier.TEMEL)"geometry_symmetry_recognize"
                else "geometry_symmetry_build",
                "Eksenin iki tarafındaki kareler aynı uzaklıkta eşleşir.")
        }
        fun measure(index:Int):CastleTask {
            val kind=when{
                grade<=2->"edges"
                grade==3->"vertices"
                grade==4->"perimeter"
                grade==5->if(index%2==0)"area" else "perimeter"
                else->when(index%3){
                    0->"angle"
                    1->"triangle_area"
                    else->"perimeter"
                }
            }
            val w=if(grade<=4)rng.nextInt(2,7) else rng.nextInt(3,11)
            val h=if(grade<=4)rng.nextInt(2,7) else rng.nextInt(3,10)
            val target=if(kind=="edges"||kind=="vertices")
                pool.filter{it.sides>0}.random(rng)
                else if(kind=="triangle_area")CastleShape.TRIANGLE
                else CastleShape.RECTANGLE
            val angle=listOf(30,45,60,90,120,135,150).random(rng)
            val answer=when(kind){
                "edges","vertices"->target.sides
                "perimeter"->2*(w+h)
                "area"->w*h
                "triangle_area"->w*h
                else->angle
            }
            val prompt=when(kind){
                "edges"->"Taşın düz kenarlarını say. Kristal sayacını doğru sonuca getir."
                "vertices"->"Çokgenin köşelerini sayıp sayacı ayarla."
                "perimeter"->"Kapının çevresini ölç: kenarlar "+w+" ve "+h+" birim."
                "area"->"Bahçenin alanını hesapla: kenarlar "+w+" ve "+h+" birim."
                "triangle_area"->"Üçgenin alanı kaç birimkare? Taban "+
                    (2*w)+", yükseklik "+h+"."
                else->"Pusulanın gösterdiği açıyı derece olarak belirle."
            }
            val explanation=when(kind){
                "edges","vertices"->target.display+" şeklinin "+target.sides+
                    " düz kenarı ve köşesi vardır."
                "perimeter"->"Çevre = 2 × ("+w+" + "+h+") = "+answer+" birim."
                "area"->"Alan = "+w+" × "+h+" = "+answer+" birimkare."
                "triangle_area"->"Üçgen alanı = ("+(2*w)+" × "+h+
                    ") / 2 = "+answer+" birimkare."
                else->answer.toString()+"° açı "+
                    (if(answer<90)"dar" else if(answer==90)"dik" else "geniş")+
                    " açıdır."
            }
            val unit=if(kind=="angle")"°"
                else if(kind=="area"||kind=="triangle_area")"birim²"
                else if(kind=="perimeter")"birim" else "tane"
            val skill=when(kind){
                "edges","vertices"->"geometry_properties"
                "perimeter"->"geometry_perimeter"
                "area","triangle_area"->"geometry_area"
                else->"geometry_angle"
            }
            return CastleTask(CastleMode.MEASURE,prompt,emptyList(),target,
                4,0,skill,explanation,emptyList(),answer,
                if(kind=="angle")180 else maxOf(answer+10,30).coerceAtMost(220),
                if(kind=="angle")15 else 1,kind,w,h,unit)
        }
        return listOf(
            shape(CastleMode.FIND,0),shape(CastleMode.FIND,1),measure(2),
            shape(CastleMode.FIND,3),shape(CastleMode.FIND,4),
            shape(CastleMode.BUILD,5),shape(CastleMode.BUILD,6),
            shape(CastleMode.BUILD,7),measure(8),shape(CastleMode.BUILD,9),
            mirror(0),mirror(1),mirror(2),mirror(3),mirror(4),
            shape(CastleMode.FIND,15),measure(16),shape(CastleMode.BUILD,17),
            measure(18),mirror(5))
    }

    fun task(run: CastleRun): CastleTask? =
        if (run.seed <= 0 || run.room !in 0..3 || run.step !in 0 until stepsPerRoom(run)) null
        else tasks(run.seed,run.tier,run.focusShape,run.contentVersion,run.grade)
            .getOrNull(taskIndex(run))

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

    /** The build socket sequence is verified by geometry, never by answer labels. */
    fun isCorrectPiece(task: CastleTask, slot: Int, shape: CastleShape?): Boolean =
        task.mode == CastleMode.BUILD && slot in task.requiredPieces.indices &&
            task.requiredPieces[slot] == shape

    fun isCorrect(task:CastleTask,shape:CastleShape?,mask:Int,
        number:Int=0):Boolean=when(task.mode){
        CastleMode.FIND,CastleMode.BUILD->shape==task.target
        CastleMode.SYMMETRY->mask==symmetricRightMask(task.leftMask,task.gridSize)
        CastleMode.MEASURE->number==task.measureAnswer
    }

    fun valid(run: CastleRun): Boolean {
        if (run.seed < 0 || run.room !in 0..4 || run.step !in 0 until stepsPerRoom(run) ||
            run.attempts < 0 || run.errors !in 0..run.attempts ||
            run.hints < 0 || run.firstTryCorrect !in 0..totalTasks(run) ||
            run.completedRuns < 0 || (run.seed == 0L &&
                (run.room != 0 || run.step != 0)) ||
            run.mirrorMask < 0 || (run.mirrorMask ushr 18) != 0 ||
            run.contentVersion !in 1..3 || run.placed.size > 3 ||
            run.grade !in 1..8 || run.measureValue<0) return false

        if (run.contentVersion >= 2 && run.seed > 0L && run.room in 0..3) {
            val current = task(run) ?: return false
            if (current.mode == CastleMode.BUILD) {
                // A resumed game cannot fabricate completed sockets or skip a piece.
                if (run.placed != current.requiredPieces.take(run.placed.size)) return false
                if (run.solved && run.placed.size != current.requiredPieces.size)
                    return false
            } else if (run.placed.isNotEmpty()) return false
            if(current.mode==CastleMode.MEASURE){
                if(run.measureValue>current.measureMax ||
                    run.measureValue%current.measureStep!=0)return false
            }else if(run.measureValue!=0)return false
            if(run.contentVersion>=3 && run.solved){
                val right=when(current.mode){
                    CastleMode.FIND->run.selected==current.target
                    CastleMode.BUILD->run.placed==current.requiredPieces
                    CastleMode.SYMMETRY->run.mirrorMask==
                        symmetricRightMask(current.leftMask,current.gridSize)
                    CastleMode.MEASURE->run.measureValue==current.measureAnswer
                }
                if(!right)return false
            }
        } else if (run.placed.isNotEmpty() || run.measureValue!=0) return false
        return true
    }

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
        val completed = run.room == 3 && run.step == stepsPerRoom(run)-1
        val nextStep = if(run.step == stepsPerRoom(run)-1) 0 else run.step + 1
        val nextRoom = if(run.step == stepsPerRoom(run)-1) run.room+1 else run.room
        return run.copy(room=nextRoom,step=nextStep,selected=null,solved=false,mirrorMask=0,
            measureValue=0,
            placed=emptyList(),
            firstTryCorrect=run.firstTryCorrect+
                if(!run.hasHintInTask && !run.hasErrorInTask) 1 else 0,
            hasErrorInTask=false,hasHintInTask=false,
            completedRuns=run.completedRuns+if(completed) 1 else 0)
    }
}
