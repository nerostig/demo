import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt

// ===================== MODELO =====================

import kotlin.random.Random

fun generateRandomTopology(
    sensorCount: Int,
    maxNeighbors: Int,
    dutyCycleRange: ClosedFloatingPointRange<Double> = 5.0..40.0,
    tolerance: Double = 0.0,
    step: Double = 5.0,
    seed: Int? = null,
    allCoprime: Boolean = true
): NetworkTopology {
    val random = seed?.let { Random(it) } ?: Random.Default

    // 1️ Criar sensores com domínio inicial
    val sensors = (0 until sensorCount).map { i ->
        Sensor(
            id = "S$i",
            desiredDutyCycle = random
                .nextInt(dutyCycleRange.start.toInt(), dutyCycleRange.endInclusive.toInt() + 1)
                .toDouble(),
            tolerance = tolerance
        )
    }

    // 2️ Criar adjacência vazia
    val adjacency = sensors.associateWith { mutableSetOf<Sensor>() }

    // 3⃣ Criar ligações aleatórias garantindo coprimalidade
    for (sensor in sensors) {
        val possibleNeighbors = sensors.filter { it != sensor }
        val neighborCount = random.nextInt(1, maxNeighbors + 1)
        val chosen = mutableListOf<Sensor>()

        for (neighborCandidate in possibleNeighbors.shuffled(random)) {
            if (chosen.size >= neighborCount) break

            val sensorDomain = generateCandidates(sensor)
            val neighborDomain = generateCandidates(neighborCandidate)

            val hasCoprime = if (allCoprime) {
                // Todos os valores do sensor devem ter pelo menos um coprimo no vizinho
                sensorDomain.all { sVal ->
                    neighborDomain.any { nVal -> areCoprimePercentagess(sVal.toDouble(), nVal.toDouble()) }
                }
            } else {
                // Apenas um valor coprimo com o vizinho é suficiente
                sensorDomain.any { sVal ->
                    neighborDomain.any { nVal -> areCoprimePercentagess(sVal.toDouble(), nVal.toDouble()) }
                }
            }

            if (hasCoprime) {
                chosen.add(neighborCandidate)
                adjacency[sensor]!!.add(neighborCandidate)
                adjacency[neighborCandidate]!!.add(sensor)
            }
        }
    }

    // 4️ Converter para Map<Sensor, List<Sensor>>
    val finalAdjacency = adjacency.mapValues { it.value.toList() }

    return NetworkTopology(finalAdjacency)
}


fun generateRandomTopology2(
    sensorCount: Int,
    maxNeighbors: Int,
    dutyCycleRange: ClosedFloatingPointRange<Double> = 5.0..40.0,
    tolerance: Double = 0.0,
    seed: Int? = null
): NetworkTopology {

    val random = seed?.let { Random(it) } ?: Random.Default

    // 1️⃣ Criar sensores
    val sensors = (0 until sensorCount).map { i ->
        Sensor(
            id = "S$i",
            desiredDutyCycle = random
                .nextInt(
                    dutyCycleRange.start.toInt(),
                    dutyCycleRange.endInclusive.toInt() + 1
                )
                .toDouble(),
            tolerance = tolerance
        )
    }

    // 2️⃣ Criar adjacência vazia
    val adjacency = sensors.associateWith { mutableSetOf<Sensor>() }

    // 3️⃣ Criar ligações aleatórias (grafo não dirigido)
    for (sensor in sensors) {
        val possibleNeighbors = sensors
            .filter { it != sensor && adjacency[it]!!.size < maxNeighbors }

        val remaining = maxNeighbors - adjacency[sensor]!!.size
        if (remaining <= 0) continue

        val neighborsToAdd = random.nextInt(0, remaining + 1)

        for (neighbor in possibleNeighbors.shuffled(random).take(neighborsToAdd)) {
            adjacency[sensor]!!.add(neighbor)
            adjacency[neighbor]!!.add(sensor)
        }
    }

    // 4️⃣ Converter para estrutura imutável
    val finalAdjacency = adjacency.mapValues { it.value.toList() }

    return NetworkTopology(finalAdjacency)
}
//data class Sensor(
//    val id: String,
//    val desiredDutyCycle: Double,
//    val tolerance: Double = 2.0
//)
//
//data class DutyCycleParameter(val value: Double)
//data class Schedule(val sensor: Sensor, val parameter: DutyCycleParameter?)
//
//class NetworkTopology(private val adjacency: Map<Sensor, List<Sensor>>) {
//    fun sensors(): List<Sensor> = adjacency.keys.toList()
//    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
//}


// ===================== MATEMÁTICA =====================
fun gcd(a: Int, b: Int): Int {
    //println("$a")
    //println("$b")
    var x = abs(a)
    var y = abs(b)
    while (y != 0) {
        val t = y
        y = x % y
        x = t
    }
    return x
}

fun areCoprime(a: Int, b: Int): Boolean = gcd(a, b) == 1
fun dutyCycleToPeriod(dc: Double): Int = (100.0 / dc).toInt()
fun areCoprimePercentagess(a: Double, b: Double): Boolean {
    // println("avvv ->$a")
    //println("b vvv->$b")


    return areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))


}

// ===================== DOMÍNIO =====================


fun generateCandidatesdd(sensor: Sensor, step: Double = 0.05): List<Int> {
   // val min = (100 / (sensor.desiredDutyCycle - sensor.tolerance)).toInt()
    //val max = (100 / (sensor.desiredDutyCycle + sensor.tolerance)).toInt()
    val min = (100 / (sensor.desiredDutyCycle + sensor.tolerance)).toInt()
    val max = (100 / (sensor.desiredDutyCycle - sensor.tolerance)).toInt()
    val values = mutableListOf<Int>()
    var v = min
    while (v <= max) {
        values.add(v)
        v += 1
    }
    return values
}
fun generateCandidates(sensor: Sensor): List<Int> {
    val minPeriod = kotlin.math.ceil(
        100.0 / (sensor.desiredDutyCycle + sensor.tolerance)
    ).toInt()

    val maxPeriod = kotlin.math.ceil(
        100.0 / (sensor.desiredDutyCycle - sensor.tolerance)
    ).toInt()
    println("${sensor.id }> $minPeriod e $maxPeriod")

    return (minPeriod..maxPeriod).toList()
}



// ===================== ÁRVORE DE DECISÃO =====================
data class TreeNode(
    val sensor: Sensor,
    val value: Double,
    val children: MutableList<TreeNode> = mutableListOf()
)

data class Nogood(
    val assignments: Map<Sensor, Double>
)
fun luby(i: Int): Int {
    var k = 1
    while ((1 shl k) - 1 < i) k++
    if (i == (1 shl k) - 1) return 1 shl (k - 1)
    return luby(i - (1 shl (k - 1)) + 1)
}
// ===================== OTIMIZADOR =====================
 class SearchContext(val globalNogoods: GlobalNogoodStore){
                          //  val cutoff: Int) {

    var decisions  = 0
    val coprimeCache = mutableMapOf<Pair<Double, Double>, Boolean>()
    val impactCache = mutableMapOf<Triple<Sensor, Int, Sensor>, Int>()
    val localNogoods  = mutableListOf<Nogood>()

}


class GlobalNogoodStore {

    // Lista thread-safe
    private val nogoods = mutableListOf<Nogood>()
    private val lock = Any()

    fun add(nogood: Nogood) {
        synchronized(lock) {
            // Evita duplicados triviais
            if (nogoods.none { it.assignments == nogood.assignments }) {
                nogoods.add(nogood)
            }
        }
    }

    fun snapshot(): List<Nogood> {
        synchronized(lock) {
            return nogoods.toList()
        }
    }
}

private fun violatesNogood(
    assignment: Map<Sensor, Double>,
    ctx: SearchContext
): Boolean {

    // 1 Nogoods locais (rápidos)
    for (nogood in ctx.localNogoods) {
        if (nogood.assignments.all { (s, v) -> assignment[s] == v }) {
            return true
        }
    }

    //  Nogoods globais (snapshot para evitar lock longo)
    for (nogood in ctx.globalNogoods.snapshot()) {
        if (nogood.assignments.all { (s, v) -> assignment[s] == v }) {
            return true
        }
    }

    return false
}
fun areCoprimePercentages(
    p1: Double,
    p2: Double,
    ctx: SearchContext
): Boolean {
    val key = Pair(p1, p2)
    return ctx.coprimeCache.getOrPut(key) {
        areCoprime(dutyCycleToPeriod(p1), dutyCycleToPeriod(p2))
    }
}

class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {

    private var bestCost = Double.MAX_VALUE
    private val coprimeCache = mutableMapOf<Pair<Double, Double>, Boolean>()

    private val globalNogoods = GlobalNogoodStore()


    private val impactCache =
        mutableMapOf<Triple<Sensor, Int, Sensor>, Int>()


    private lateinit var orderedSensors: List<Sensor>
    private lateinit var orderedSensorsPair: List<Pair<Sensor, List<Sensor>>>



    private var bestNullCount = Int.MAX_VALUE
    private var bestAssignment: Map<Sensor, Double?>? = null
    private val bestLock = Any()
//    private fun areCoprimePercentages(p1: Double, p2: Double): Boolean {
//        val key = Pair(p1, p2)
//        return coprimeCache.getOrPut(key) {
//            val period1 = dutyCycleToPeriod(p1)
//            val period2 = dutyCycleToPeriod(p2)
//            areCoprime(period1, period2)
//        }
//    }







fun optimize(): Map<Sensor, Double?>? = runBlocking {

    val sensors = topology.sensors()
        .sortedWith(
            compareBy<Sensor> { generateCandidates(it).size }
                .thenBy { topology.neighbors(it).size }
        )

   // val threads=mutableListOf<Thread>()
    val domains = sensors
        .associateWith { generateCandidates(it).toMutableList() }
        .toMutableMap()


    //

//    orderedSensors = topology.sensors()
//        .sortedWith(
//            compareBy<Sensor> { domains2[it]?.size ?: Int.MAX_VALUE }
//                .thenByDescending { topology.neighbors(it).size }
//        )


    orderedSensorsPair = topology.sensors()
        .map { sensor ->

            val orderedNeighbors = topology.neighbors(sensor)
                .sortedWith(
                    compareBy<Sensor> { neighbor ->
                        domains[neighbor]?.size ?: Int.MAX_VALUE
                    }.thenByDescending { neighbor ->
                        topology.neighbors(neighbor).size
                    }
                )

            sensor to orderedNeighbors
        }
        .sortedWith(
            compareBy<Pair<Sensor, List<Sensor>>> {
                domains[it.first]?.size ?: Int.MAX_VALUE
            }.thenByDescending {
                it.second.size
            }
        )


    val baseCutoff = 2_000   // ajustável
    val maxRestarts = 30

    //for (restart in 1..maxRestarts) {

      //  val lubyFactor = luby(restart)
       // val cutoff = baseCutoff * lubyFactor


        sensors.map { startSensor ->
            async(Dispatchers.Default) {

                val ctx = SearchContext(globalNogoods)
                    //cutoff)

//                val domains = sensors
//                    .associateWith { generateCandidates(it).toMutableList() }
//                    .toMutableMap()

                val assignment = mutableMapOf<Sensor, Double>()

                buildTree(
                    sensor = startSensor,
                    domains = domains,
                    assignment = assignment,
                    currentCost = 0.0,
                    countSensor = 1,
                    ctx = ctx
                )
            }
        }.awaitAll()
   // }



    // resultado global protegido
    bestAssignment
}




    private fun evaluateSolution(
        assignment: Map<Sensor, Double>,
        cost: Double
    ) {
        val sensors = topology.sensors()

        val rmsCost = sqrt(cost / assignment.size.toDouble())

        val nullCount = sensors.size - assignment.size

        //val better=rmsCost < bestCost

//        println(">> Avaliar solução parcial")
//        println("bestAssignment -> $bestAssignment")
//        println("   Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}")
//        println("   | Custo = $cost")
//        println("   Melhor até agora:custo=$bestCost")

        val better =
           (nullCount < bestNullCount )|| (nullCount == bestNullCount && rmsCost < bestCost)

//        println("   É melhor? -> $better")

        if (better) {
            bestNullCount = nullCount
            bestCost = rmsCost
            bestAssignment = sensors.associateWith { assignment[it] }

//            println(
//                ">>> ** NOVA MELHOR SOLUÇÃO **\n" +
//                        "    nulls=$bestNullCount custo=$bestCost\n" +
//                        "    solução=${bestAssignment!!.map { "${it.key.id}=${it.value}" }}\n"
//            )
        }
    }





/*
    private fun impactOf(
        sensor: Sensor,
        period: Int,
        domains: Map<Sensor, List<Int>>,
        assignment: Map<Sensor, Double>
    ): Int {

        val percentage = round(100.0 / period)
        var impact = 0

        for (neighbor in topology.neighbors(sensor)) {

            if (neighbor in assignment) continue

            val key = Triple(sensor, period, neighbor)
            val cached = impactCache[key]
            if (cached != null) {
                impact += cached
                continue
            }

            val domainBefore = domains[neighbor] ?: continue
            val sizeBefore = domainBefore.size

            val sizeAfter = domainBefore.count { neighborValue ->
                areCoprimePercentages(
                    percentage,
                    round(100.0 / neighborValue)
                )
            }

            val delta = sizeBefore - sizeAfter
            impactCache[key] = delta
            impact += delta
        }

        return impact
    }
    */
private fun impactOf(
    sensor: Sensor,
    period: Int,
    domains: Map<Sensor, List<Int>>,
    assignment: Map<Sensor, Double>,
    ctx: SearchContext
): Int {
    val percentage =ceil(100.0 / period) //round(100.0 / period)
    var impact = 0

    for (neighbor in topology.neighbors(sensor)) {
        if (neighbor in assignment) continue

        val key = Triple(sensor, period, neighbor)
        val cached = ctx.impactCache[key]
        if (cached != null) {
            impact += cached
            continue
        }

        val domain = domains[neighbor] ?: continue
        val sizeAfter = domain.count {
            areCoprimePercentages(percentage, round(100.0 / it), ctx)
        }

        val delta = domain.size - sizeAfter
        ctx.impactCache[key] = delta
        impact += delta
    }
    return impact
}

    private fun tryUpdate(
        assignment: Map<Sensor, Double>,
        cost: Double
    ) {
        val rms = sqrt(cost / assignment.size)
        val nulls = topology.sensors().size - assignment.size

        synchronized(bestLock) {
            val better =
                (nulls < bestNullCount) ||
                        (nulls == bestNullCount && rms < bestCost)

            if (better) {
                bestNullCount = nulls
                bestCost = rms
                bestAssignment = topology.sensors().associateWith { assignment[it] }
            }
        }
    }


    private fun backupDomains(domains: MutableMap<Sensor, MutableList<Int>>, backup: MutableMap<Sensor, List<Int>>) {
        for ((sensor, domain) in domains) {
            backup[sensor] = domain.toList()
        }
    }

    private fun restoreDomains(domains: MutableMap<Sensor, MutableList<Int>>, backup: Map<Sensor, List<Int>>) {
        for ((sensor, values) in backup) {
            domains[sensor] = values.toMutableList()
        }
    }

    fun nullRisk(sensor: Sensor, assignment: MutableMap<Sensor, Double>):Int{
        val assig=topology.neighbors(sensor).count{it in assignment}
        val dregre=topology.neighbors(sensor).size

        return  assig * 2 +dregre
    }
    private fun saturation(sensor: Sensor, assignment: Map<Sensor, Double>): Int {
        return topology.neighbors(sensor)
            .mapNotNull { assignment[it] }
            .distinct()
            .size
    }

    private fun buildTree(
        sensor: Sensor,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double,
        countSensor: Int,
        ctx: SearchContext

    ) {
//        ctx.decisions++
//        if (ctx.decisions > ctx.cutoff) {
//            return
//        }

        val domain = domains[sensor] ?: return

        // Backup dos domínios antes da propagação
        val originalDomains = mutableMapOf<Sensor, List<Int>>()
        backupDomains(domains, originalDomains)

        // Ordena valores com base em impacto local e desvio
//        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
//            val percentage = round(100.0 / period)
//           topology.neighbors(sensor)
//                .filter { it !in assignment }
//               .sumOf { neighbor ->
//                    domains[neighbor]!!.count { neighborValue ->
//                       !areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                    }
//                }
//       })

//        val sortedDomain = domain.sortedBy { period ->
//            val percentage = round(100.0 / period)
//            abs(percentage - sensor.desiredDutyCycle)
//        }


//        val sortedDomain = domain.sortedWith(
//            compareBy<Int> { period ->
//                val penalty = impactOf(sensor, period, domains, assignment,ctx)
//                val deviation = abs(round(100.0 / period) - sensor.desiredDutyCycle)
//                penalty + deviation.toInt()
//            }
//        )

        val sortedDomain = domain.sortedWith(
            compareBy<Int> { period ->
                impactOf(sensor, period, domains, assignment, ctx)
            }.thenBy { period ->
                abs(round(100.0 / period) - sensor.desiredDutyCycle)
            }
        )




        for (period in sortedDomain) {
            val percentage = ceil(100.0 / period)//round(100.0 / period)
            assignment[sensor] = percentage



            if (violatesNogood(assignment, ctx)) {
                assignment.remove(sensor)
                continue
            }



            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())

            val nullCount = topology.sensors().size - assignment.size

            if (bestAssignment != null && bestNullCount == 0 && rmsPartial > bestCost) {
                assignment.remove(sensor)
                continue
            }

            // Verificação de coprimalidade local
            val valid = topology.neighbors(sensor)
                .filter { it in assignment }
                .all { neighbor ->
                    assignment[neighbor]!=percentage
                    &&(areCoprimePercentages(percentage, assignment[neighbor]!!,ctx))
                }

            if (!valid) {

                val conflict = topology.neighbors(sensor)
                    .filter { it in assignment }
                    .associateWith { assignment[it]!! }
                    .toMutableMap()

                conflict[sensor] = percentage

                val nogood = Nogood(conflict)

                //  Aprendizagem local
                ctx.localNogoods.add(nogood)

                //  Aprendizagem global
                globalNogoods.add(nogood)

                assignment.remove(sensor)
                continue
            }



            //evaluateSolution(assignment, newSquaredCost)
            tryUpdate(assignment, newSquaredCost)


//            val nextSensors = topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sortedWith(
//                    compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
//                        .thenByDescending {
//                            topology.neighbors(it).count { n -> n !in assignment }
//                        }
//                )

//            val nextSensors = orderedSensorsPair
//                .first { it.first == sensor }
//                .second
//                .filter { it !in assignment }

//
//            val nextSensors = orderedSensorsPair
//                .first { it.first == sensor }
//                .second
//                .asSequence()
//                .filter { it !in assignment }
//                .sortedWith(compareBy<Sensor> {
//                    s-> -nullRisk(s,assignment)
//                }).toList()

            val nextSensors = orderedSensorsPair
                .first { it.first == sensor }
                .second
                .asSequence()
                .filter { it !in assignment }
                .sortedWith(
                    compareByDescending<Sensor> { s ->
                        saturation(s, assignment)   //  DSatur
                    }.thenBy { s ->
                        domains[s]?.size ?: Int.MAX_VALUE // MRV
                    }.thenByDescending { s ->
                        topology.neighbors(s).size //
                    }.thenByDescending { s ->
                        nullRisk(s, assignment) //
                    }
                )
                .toList()



            if (nextSensors.isNotEmpty()) {

                for (next in nextSensors) {

                    buildTree(next, domains, assignment, newSquaredCost, countSensor + 1,ctx)
                }


            } else if (nextSensors.isEmpty() && countSensor != topology.sensors().size) {
                return
            }

            // Rollback de domínios
            restoreDomains(domains, originalDomains)
            assignment.remove(sensor)
        }
    }







}



// ===================== API =====================
fun computeSchedulesOptimized(topology: NetworkTopology): List<Schedule> {

    println("=== TOPOLOGIA RECEBIDA ===")

    for (sensor in topology.sensors()) {
        val neighbors = topology.neighbors(sensor)
        println(
            "Sensor ${sensor.id} -> vizinhos: ${
                neighbors.joinToString { it.id }
            }"
        )
    }

    println("=========================")

    val optimizer = DutyCycleTreeOptimizer(topology)
    val solution = optimizer.optimize()



    println("=== SOLUÇÃO ÓTIMA ===")
    solution?.forEach { (sensor, value) ->
        println("Sensor ${sensor.id}: DutyCycle = $value%")
    }
    return topology.sensors().map { sensor ->
        val value = solution?.get(sensor)
        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
    }
}
fun main() {




    println("${(100/15).toInt()}")
    // ===================== SENSORES =====================
    val A = Sensor("A", desiredDutyCycle = 57.0, tolerance = 0.0) // 100/20 = 5
    val B = Sensor("B", desiredDutyCycle = 13.0, tolerance = 0.0) // 100/25 = 4
    val C = Sensor("C", desiredDutyCycle = 15.0, tolerance = 0.0) // 100/15 ≈ 6
    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.0) // 100/10 = 10
    //val D = Sensor("D", desiredDutyCycle = 9.09, tolerance = 0.0)  // período ≈ 11 → coprimo com A e C
    val E = Sensor("E", desiredDutyCycle = 9.0, tolerance = 0.0)  // 100/9 ≈ 11

    // ===================== TOPOLOGIA =====================
    val topology = NetworkTopology(
        mapOf(
            A to listOf(B),
            B to listOf(A),
           // C to listOf(A, B, D),
           // D to listOf(C, E),
            //E to listOf(B, D)
        )
    )

    val topology2 = NetworkTopology(
        mapOf(
            A to listOf(B, D),
            B to listOf(A, C, E),
            C to listOf(B),
            D to listOf(A, E),
            E to listOf(B)
        )
    )

    // ===================== OTIMIZAÇÃO =====================
    val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
    val solution = optimizer.optimize()

    // ===================== RESULTADOS =====================
    println("=== SOLUÇÃO ÓTIMA ===")
    solution?.forEach { (sensor, value) ->
        println("Sensor ${sensor.id}: DutyCycle = $value%")
    }



}












//            var forwardCheckFailed = false
//            for (neighbor in topology.neighbors(sensor)) {
//                if (neighbor !in assignment) {
//                    val neighborDomain = newDomains[neighbor]!!
//                    neighborDomain.removeIf { value ->
//                        !areCoprimePercentages(percentage, round(100.0 / value))
//                    }
//                    if (neighborDomain.isEmpty()) {
//                        forwardCheckFailed = true
//                        break
//                    }
//                }
//            }
//            if (forwardCheckFailed) {
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }


//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .forEach { neighbor ->
//                    newDomains[neighbor] = newDomains[neighbor]!!
//                        .filter { neighborValue ->
//                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                        }.toMutableList()
//                    if (newDomains[neighbor]!!.isEmpty()) valid = false
//                }
//            if (!valid) {
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }