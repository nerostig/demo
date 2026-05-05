package com.example.demo.domain

class Sensor(
    val id: String,
    val x:Int?=null,
    val y :Int?=null,
    val desiredDutyCycle: Double,
    val tolerance: Double = 0.0
)
{
    override fun toString(): String = id

}



/*


import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.Schedule
import java.util.LinkedList
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.pow
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


fun generateCandidates(sensor: Sensor, step: Double = 0.05): List<Int> {
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
fun generateCandidatesdd(sensor: Sensor): List<Int> {
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

// ===================== OTIMIZADOR =====================


class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {

    private var bestCost = Double.MAX_VALUE
    private val coprimeCache = mutableMapOf<Pair<Double, Double>, Boolean>()

    private val impactCache =
        mutableMapOf<Triple<Sensor, Int, Sensor>, Int>()

    private lateinit var orderedSensors: List<Sensor>

    private var bestNullCount = Int.MAX_VALUE
    private var bestAssignment: Map<Sensor, Double?>? = null

    private fun areCoprimePercentages(p1: Double, p2: Double): Boolean {
        val key = Pair(p1, p2)
        return coprimeCache.getOrPut(key) {
            val period1 = dutyCycleToPeriod(p1)
            val period2 = dutyCycleToPeriod(p2)
            areCoprime(period1, period2)
        }
    }







    fun optimize(): Map<Sensor, Double?>? {

        // Gera os domínios iniciais para cada sensor
//        val domains = topology.sensors().associateWith { generateCandidates(it).toMutableList() }
//            .toMutableMap()
        val domains = topology.sensors()
            .associateWith { generateCandidates(it).toMutableList() }
            .toMutableMap()

        orderedSensors = topology.sensors()
            .sortedWith(
                compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
                    .thenByDescending { topology.neighbors(it).size }
            )

        val sensors = topology.sensors()
            .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
                .thenBy { topology.neighbors(it).size })
        // val sensors = topology.sensors().sortedBy { topology.neighbors(it).size }

        val startSensor = topology.sensors()
            .minWithOrNull(
                compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
                    .thenBy { topology.neighbors(it).size }
            )
            ?: return null
//        println("startSensor $startSensor")
//        println("sensors $sensors")



        for (startSensor in sensors) {
            println("start ${startSensor.id}")
            println(sensors)

//            val domains =
//            sensors.associateWith { generateCandidates(it, step).toMutableList() } .toMutableMap()


            // val rootNode = Node(startSensor, parent = null)
            buildTree(startSensor, domains, mutableMapOf(), 0.0,1)
        }

        return bestAssignment
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

    private fun revise(x: Sensor, y: Sensor, domains: MutableMap<Sensor, MutableList<Int>>): Boolean {
        var revised = false
        val xDomain = domains[x]!!
        val yDomain = domains[y]!!

        xDomain.removeAll { xVal ->
            !yDomain.any { yVal ->
                areCoprimePercentages(round(100.0 / xVal), round(100.0 / yVal))
            }
        }

        revised = xDomain.size < domains[x]!!.size
        return revised
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




    private fun estimateFutureCost(
        assignment: Map<Sensor, Double>,
        domains: Map<Sensor, List<Int>>
    ): Double {

        var estimate = 0.0

        for (sensor in topology.sensors()) {

            // apenas sensores ainda não atribuídos
            if (sensor !in assignment) {

                val domain = domains[sensor] ?: continue
                if (domain.isEmpty()) {
                    // domínio vazio → ramo impossível
                    return Double.POSITIVE_INFINITY
                }

                // melhor erro possível para este sensor
                val minSquaredError = domain.minOf { period ->
                    val percentage = round(100.0 / period)
                    val error = percentage - sensor.desiredDutyCycle
                    error * error
                }

                estimate += minSquaredError
            }
        }

        return estimate
    }
    private fun buildTree(
        sensor: Sensor,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double,
        countSensor: Int
    ) {
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


        val sortedDomain = domain.sortedWith(
            compareBy<Int> { period ->
                val penalty = impactOf(sensor, period, domains, assignment)
                val deviation = abs(round(100.0 / period) - sensor.desiredDutyCycle)
                penalty + deviation.toInt()
            }
        )




        for (period in sortedDomain) {
            val percentage = round(100.0 / period)
            assignment[sensor] = percentage

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
                            &&(areCoprimePercentages(percentage, assignment[neighbor]!!))
                }

            if (!valid) {
                assignment.remove(sensor)
                continue
            }

            // Propagação de restrições (AC-3)
//            val queue = LinkedList<Pair<Sensor, Sensor>>()
//            topology.neighbors(sensor).forEach { neighbor -> queue.add(Pair(sensor, neighbor)) }
//
//            while (queue.isNotEmpty()) {
//                val (x, y) = queue.poll()
//                if (revise(x, y, domains)) {
//                    if (domains[x]!!.size == 0) {
//                        restoreDomains(domains, originalDomains)
//                        assignment.remove(sensor)
//                        continue
//                    }
//                    topology.neighbors(x).forEach { z -> queue.add(Pair(x, z)) }
//                }
//            }

            evaluateSolution(assignment, newSquaredCost)

            //          escolher próximo sensor (MRV + degree)
            val nextSensors = topology.neighbors(sensor)
                .filter { it !in assignment }
                .sortedWith(
                    compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
                        .thenByDescending {
                            topology.neighbors(it).count { n -> n !in assignment }
                        }
                )

            if (nextSensors.isNotEmpty()) {

                for (next in nextSensors) {

                    buildTree(next, domains, assignment, newSquaredCost, countSensor + 1)
                }


            } else if (nextSensors.isEmpty() && countSensor != topology.sensors().size) {
                return
            }

            // Rollback de domínios
            restoreDomains(domains, originalDomains)
            assignment.remove(sensor)
        }
    }


    private fun buildTree2(
        sensor: Sensor,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double,
        countSensor:Int
    ) {
        val domain = domains[sensor] ?: return

        //    val sortedDomain = domain.sortedBy { period ->
        //        impactOf(sensor, period, domains, assignment)
        //    }
        val sortedDomain = domain.sortedWith(
            compareBy<Int> { period ->
                val penalty = impactOf(sensor, period, domains, assignment)
                val deviation = abs(round(100.0 / period) - sensor.desiredDutyCycle)
                penalty + deviation.toInt()
            }
        )




        println("\n--> Explorar sensor ${sensor.id}")
        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")


        for (period in sortedDomain) {

            val percentage = round(100.0 / period)
            assignment[sensor] = percentage

            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())

            val nullCount = topology.sensors().size - assignment.size

            //  PODA por melhor solução conhecida
            if ( bestAssignment!=null &&
                ( ( bestNullCount==0 && rmsPartial > bestCost ))) {
                println("pior")
                assignment.remove(sensor)
                continue
            }

            //  restrição local (coprimalidade)
            val valid = topology.neighbors(sensor)
                .filter { it in assignment }
                .all { neighbor ->
                    areCoprimePercentages(percentage, assignment[neighbor]!!)
                }

            if (!valid) {
                assignment.remove(sensor)
                continue
            }

            //  FORWARD CHECKING
            val backup = mutableMapOf<Sensor, List<Int>>()


            topology.neighbors(sensor)
                .filter { it !in assignment }
                .forEach { neighbor ->
                    backup[neighbor] = domains[neighbor]!!.toList()

                    domains[neighbor] = domains[neighbor]!!
                        .filter { neighborValue ->
                            areCoprimePercentages(
                                percentage,
                                round(100.0 / neighborValue)
                            )
                        }.toMutableList()
                }


            //  avaliar solução parcial
            evaluateSolution(assignment, newSquaredCost)

            //          escolher próximo sensor (MRV + degree)
            //        val nextSensors = topology.neighbors(sensor)
            //            .filter { it !in assignment }
            //            .sortedWith(
            //                compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
            //                    .thenByDescending {
            //                        topology.neighbors(it).count { n -> n !in assignment }
            //                    }
            //            )

            val nextSensors = orderedSensors
                .filter { it !in assignment }
                .filter { it in topology.neighbors(sensor) }





            if (nextSensors.isNotEmpty()) {
                for (next in nextSensors) {
                    buildTree(next, domains, assignment, newSquaredCost,countSensor+1)
                }
            }
            else if (nextSensors.isEmpty() && countSensor!=topology.sensors().size){
                println(" tamnaho ${topology.sensors().size} e count $countSensor ")

                println("olhar para trás de ${sensor.id}")
                return
            }

            //  rollback domínios
            backup.forEach { (neighbor, oldDomain) ->
                domains[neighbor] = oldDomain.toMutableList()
            }

            //  backtrack
            assignment.remove(sensor)
        }
    }



    /*
        private fun buildTree(
            node: Node,
            domains: Map<Sensor, List<Int>>,
            assignment: Map<Sensor, Double>,
            currentCost: Double
        ) {
            val sensor = node.sensor
            val domain = domains[sensor] ?: return

            val sortedDomain = domain.sortedBy { period ->
                impactOf(sensor, period, domains, assignment)
            }
    //        println("\n--> Explorar sensor ${sensor.id}")
    //        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")

            for (period in sortedDomain) {

                val percentage = round(100.0 / period)

                val newAssignment = assignment + (sensor to percentage)

                val error = percentage - sensor.desiredDutyCycle
                val newSquaredCost = currentCost + (error * error)
                val rmsPartial = sqrt(newSquaredCost / newAssignment.size.toDouble())

                val sensors = topology.sensors()
                val nullCount = sensors.size - newAssignment.size

                // poda por melhor solução
                if (nullCount == bestNullCount && rmsPartial > bestCost) {
                    continue
                }

                // verificação local de coprimalidade
                val valid = topology.neighbors(sensor)
                    .filter { it in assignment }
                    .all { neighbor ->
                        areCoprimePercentages(percentage, assignment[neighbor]!!)
                    }

                if (!valid) continue

                // forward-checking IMUTÁVEL (SEM backup)
                val newDomains = domains.mapValues { (s, dom) ->
                    if (s in newAssignment || s !in topology.neighbors(sensor)) dom
                    else dom.filter { neighborValue ->
                        areCoprimePercentages(
                            percentage,
                            round(100.0 / neighborValue)
                        )
                    }
                }

                node.percentage = percentage
                evaluateSolution(newAssignment, newSquaredCost)

                val unassignedNeighbors = topology.neighbors(sensor)
                    .filter { it !in newAssignment }
                    .sortedWith(
                        compareBy<Sensor> { newDomains[it]?.size ?: Int.MAX_VALUE }
                            .thenByDescending {
                                topology.neighbors(it).count { n -> n !in newAssignment }
                            }
                    )

                if (unassignedNeighbors.isNotEmpty()) {
                    for (neighborSensor in unassignedNeighbors) {
                        val childNode = Node(sensor = neighborSensor, parent = node)
                        node.children.add(childNode)

                        buildTree(
                            node = childNode,
                            domains = newDomains,
                            assignment = newAssignment,
                            currentCost = newSquaredCost
                        )
                    }
                }

                node.percentage = null
            }
        }
        */

//    private fun buildTree2(
//        node: Node,
//        domains: MutableMap<Sensor, MutableList<Int>>,
//        assignment: MutableMap<Sensor, Double>,
//        currentCost: Double
//    ) {
//        val sensor = node.sensor
//        val domain = domains[sensor] ?: return
//
////        println("\n--> Explorar sensor ${sensor.id}")
////        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//        val backup = mutableMapOf<Sensor, List<Int>>()
//
//        // MRV + Degree heurística
////        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
////            val percentage = round(100.0 / period)
////            topology.neighbors(sensor)
////                .filter { it !in assignment }
////                .sumOf { neighbor ->
////                    domains[neighbor]!!.count { neighborValue ->
////                        !areCoprimePercentages(percentage, round(100.0 / neighborValue))
////                    }
////                }
////        })
//
//        val sortedDomain = domain.sortedBy { period ->
//            impactOf(sensor, period, domains, assignment)
//        }
//
//        for (period in sortedDomain) {
//
//            val percentage = round(100.0 / period)
//
//            assignment[sensor] = percentage
//            node.percentage = percentage
//
//            val error = percentage - sensor.desiredDutyCycle
//            val newSquaredCost = currentCost + (error * error)
//            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())
//
////            println("bestCost $bestCost")
////            println("rmsPartial $rmsPartial")
//            // Poda pelo melhor custo conhecido
//            val sensors = topology.sensors()
//
//
//            val nullCount = sensors.size - assignment.size
//            if (nullCount == bestNullCount && rmsPartial > bestCost) {
//
////                if (sensor.id=="D"){
////                    println(" ccc custo $period ")
////
////                    println("pior custo")
////                }
//
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//
//
//
//            // Verificação local coprimalidade
//            val valid = topology.neighbors(sensor)
//                .filter { it in assignment }
//                .all { neighbor -> areCoprimePercentages(percentage, assignment[neighbor]!!) }
////            if (sensor.id=="D"){
////                println(" valid $valid ")}
//
//            if (!valid) {
//
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//
//            // Forward-checking: atualizar apenas vizinhos não atribuídos
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .forEach { neighbor ->
//                    backup[neighbor] = domains[neighbor]!!.toList() // backup
//                    domains[neighbor] = domains[neighbor]!!
//                        .filter { neighborValue ->
//                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                        }.toMutableList()
//                }
//
//
//
//
//
//            evaluateSolution(assignment, newSquaredCost)
//
//            // Explorar vizinhos não atribuídos com MRV + Degree
//            val unassignedNeighbors = topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
//                    .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree
//
//            if (unassignedNeighbors.isNotEmpty()) {
//                for (neighborSensor in unassignedNeighbors) {
//                    val childNode = Node(sensor = neighborSensor, parent = node)
//                    node.children.add(childNode)
//                    buildTree(childNode, domains, assignment, newSquaredCost)
//                }
////            } else {
////                // Backtracking em cadeia local (somente quando não há vizinhos)
////                var currentNode: Node? = node
////                while (currentNode?.parent != null) {
////                    val siblings = topology.neighbors(currentNode.parent!!.sensor)
////                        .filter { it !in assignment && it != currentNode.sensor }
////
////                    if (siblings.isNotEmpty()) {
////                        for (siblingSensor in siblings) {
////                            val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)
////                            currentNode.parent.children.add(siblingNode)
////                            buildTree(siblingNode, domains, assignment, newSquaredCost)
////                        }
////                        break
////                    }
////                    currentNode = currentNode.parent
////                }
//            }
//
//            // Rollback domínios dos vizinhos
//            backup.forEach { (neighbor, oldDomain) ->
//                domains[neighbor] = oldDomain.toMutableList()
//            }
//
//            // Remover atribuição atual e limpar nó
//            assignment.remove(sensor)
//            node.percentage = null
//        }
//    }
//
//
//
//
//    private fun buildTreecc(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
//        val sensor = node.sensor
//        val domain = domains[sensor] ?: return
//
//        println("\n--> Explorar sensor ${sensor.id}")
//        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//
//
//        val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
////        val sortedDomain = domain.sortedWith(compareByDescending<Int> { period ->
////            val percentage = round(100.0 / period)
////            // Conta quantas opções compatíveis sobram para os vizinhos
////            topology.neighbors(sensor)
////                .filter { it !in assignment }
////                .sumOf { neighbor ->
////                    val neighborDomain = newDomains[neighbor] ?: emptyList()
////                    neighborDomain.count { neighborValue ->
////                        areCoprimePercentages(percentage, round(100.0 / neighborValue))
////                    }
////                }
////        }.thenBy { it }) // quebra empates preferindo period menor
//        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
//            val percentage = round(100.0 / period)
//            // conta quantos valores nos vizinhos seriam removidos
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sumOf { neighbor ->
//                    domains[neighbor]!!.count { neighborValue ->
//                        !areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                    }
//                }
//        })
//
//        for (period in sortedDomain /*domain.toList()*/) {
//            val percentage = round(100.0 / period)
//            assignment[sensor] = percentage
//            node.percentage = percentage
//
//            val error = percentage - sensor.desiredDutyCycle
//            val newSquaredCost = currentCost + (error * error)
//            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())
//
//
//            if (/*newCost*/ rmsPartial > bestCost ) {
//
//               // println("Deistiu $rmsPartial ; $bestCost  | $ ")
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//
//            // Verificação local coprimalidade
//            var valid = true
//            for (neighbor in topology.neighbors(sensor)) {
//                if (assignment.containsKey(neighbor)) {
//                    val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)
//
//
//
//                    if (!coprime) {
//                        valid = false
//                        break
//                    }
//                }
//            }
//            if (!valid) {
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .forEach { neighbor ->
//                    newDomains[neighbor] = newDomains[neighbor]!!
//                        .filter { neighborValue ->
//                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                        }.toMutableList()
//                  //  println("newDomains $newDomains")
//                }
//
//            evaluateSolution(assignment, newSquaredCost)
//
//            //val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
////
////            val unassignedNeighbors = topology.neighbors(sensor)
////                .filter { it !in assignment }
////                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }
//
//            val unassignedNeighbors = topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
//                    .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree
//
//
//            if (unassignedNeighbors.isNotEmpty()) {
//
//                for (neighborSensor in unassignedNeighbors) {
//                    val childNode = Node(sensor = neighborSensor, parent = node)
//
//                    node.children.add(childNode)
//
//
//                    buildTree(childNode, /*domains*/newDomains, assignment, newSquaredCost)
//                }
//            } else {
//                // Backtracking em cadeia usando node.parent
//                var currentNode: Node? = node
//                while (currentNode?.parent != null) {
//
//                    val siblings = topology.neighbors(currentNode.parent!!.sensor)
//                        .filter { it !in assignment && it != currentNode.sensor }
//
//                    if (siblings.isNotEmpty()) {
//                        for (siblingSensor in siblings) {
//                            val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)
//
//                            currentNode.parent.children.add(siblingNode)
//
//                            buildTree(siblingNode, /*domains*/newDomains, assignment, newSquaredCost)
//                        }
//                        break
//                    }
//                    currentNode = currentNode.parent
//                }
//            }
//
//            assignment.remove(sensor)
//            node.percentage = null
//        }
//    }


}



// ===================== API =====================
fun computeSchedulesOptimized(topology: NetworkTopology): List<Schedule> {
    val optimizer = DutyCycleTreeOptimizer(topology)
    val solution = optimizer.optimize()
    return topology.sensors().map { sensor ->
        val value = solution?.get(sensor)
        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
    }
}
fun main() {



    println("${(100/15).toInt()}")
    // ===================== SENSORES =====================
    val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 0.0) // 100/20 = 5
    val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 0.0) // 100/25 = 4
    val C = Sensor("C", desiredDutyCycle = 15.0, tolerance = 0.0) // 100/15 ≈ 6
    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.0) // 100/10 = 10
    //val D = Sensor("D", desiredDutyCycle = 9.09, tolerance = 0.0)  // período ≈ 11 → coprimo com A e C
    val E = Sensor("E", desiredDutyCycle = 9.0, tolerance = 0.0)  // 100/9 ≈ 11

    // ===================== TOPOLOGIA =====================
    val topology = NetworkTopology(
        mapOf(
            A to listOf(B, C),
            B to listOf(A, C, E),
            C to listOf(A, B, D),
            D to listOf(C, E),
            E to listOf(B, D)
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
*/