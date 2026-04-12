import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.Schedule
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.sqrt

// ===================== MODELO =====================

import kotlin.random.Random

fun generateRandomTopology2(
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


fun generateRandomTopology(
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
        val domains = topology.sensors().associateWith { generateCandidates(it).toMutableList() }
            .toMutableMap()

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



        //for (startSensor in sensors) {
            println("start ${startSensor.id}")
            println(sensors)

//            val domains =
//            sensors.associateWith { generateCandidates(it, step).toMutableList() } .toMutableMap()


            val rootNode = Node(startSensor, parent = null)
            buildTree(rootNode, domains, mutableMapOf(), 0.0)
        //}

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



    class Node(
        val sensor: Sensor,
        val parent: Node? = null,
        val children: MutableList<Node> = mutableListOf(),
        var percentage: Double? = null
    )

    private fun impactOf(
        sensor: Sensor,
        period: Int,
        domains: Map<Sensor, MutableList<Int>>,
        assignment: Map<Sensor, Double>
    ): Int {
        val percentage = round(100.0 / period)
        var impact = 0

        for (neighbor in topology.neighbors(sensor)) {
            if (neighbor in assignment) continue

            val domainBefore = domains[neighbor] ?: continue
            val sizeBefore = domainBefore.size

            val sizeAfter = domainBefore.count { neighborValue ->
                areCoprimePercentages(
                    percentage,
                    round(100.0 / neighborValue)
                )
            }

            impact += (sizeBefore - sizeAfter)
        }
        return impact
    }



    private fun buildTree(
        node: Node,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double
    ) {
        val sensor = node.sensor
        val domain = domains[sensor] ?: return

//        println("\n--> Explorar sensor ${sensor.id}")
//        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")

        val backup = mutableMapOf<Sensor, List<Int>>()

        // MRV + Degree heurística
//        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
//            val percentage = round(100.0 / period)
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sumOf { neighbor ->
//                    domains[neighbor]!!.count { neighborValue ->
//                        !areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                    }
//                }
//        })

        val sortedDomain = domain.sortedBy { period ->
            impactOf(sensor, period, domains, assignment)
        }

        for (period in sortedDomain) {

            val percentage = round(100.0 / period)

            assignment[sensor] = percentage
            node.percentage = percentage

            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())

//            println("bestCost $bestCost")
//            println("rmsPartial $rmsPartial")
            // Poda pelo melhor custo conhecido
            val sensors = topology.sensors()


            val nullCount = sensors.size - assignment.size
            if (nullCount == bestNullCount && rmsPartial > bestCost) {

//                if (sensor.id=="D"){
//                    println(" ccc custo $period ")
//
//                    println("pior custo")
//                }

                assignment.remove(sensor)
                node.percentage = null
                continue
            }




            // Verificação local coprimalidade
            val valid = topology.neighbors(sensor)
                .filter { it in assignment }
                .all { neighbor -> areCoprimePercentages(percentage, assignment[neighbor]!!) }
//            if (sensor.id=="D"){
//                println(" valid $valid ")}

            if (!valid) {

                assignment.remove(sensor)
                node.percentage = null
                continue
            }


            // Forward-checking: atualizar apenas vizinhos não atribuídos
            topology.neighbors(sensor)
                .filter { it !in assignment }
                .forEach { neighbor ->
                    backup[neighbor] = domains[neighbor]!!.toList() // backup
                    domains[neighbor] = domains[neighbor]!!
                        .filter { neighborValue ->
                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
                        }.toMutableList()
                }





            evaluateSolution(assignment, newSquaredCost)

            // Explorar vizinhos não atribuídos com MRV + Degree
            val unassignedNeighbors = topology.neighbors(sensor)
                .filter { it !in assignment }
                .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
                    .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree

            if (unassignedNeighbors.isNotEmpty()) {
                for (neighborSensor in unassignedNeighbors) {
                    val childNode = Node(sensor = neighborSensor, parent = node)
                    node.children.add(childNode)
                    buildTree(childNode, domains, assignment, newSquaredCost)
                }
            } else {
                // Backtracking em cadeia local (somente quando não há vizinhos)
                var currentNode: Node? = node
                while (currentNode?.parent != null) {
                    val siblings = topology.neighbors(currentNode.parent!!.sensor)
                        .filter { it !in assignment && it != currentNode.sensor }

                    if (siblings.isNotEmpty()) {
                        for (siblingSensor in siblings) {
                            val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)
                            currentNode.parent.children.add(siblingNode)
                            buildTree(siblingNode, domains, assignment, newSquaredCost)
                        }
                        break
                    }
                    currentNode = currentNode.parent
                }
            }

            // Rollback domínios dos vizinhos
            backup.forEach { (neighbor, oldDomain) ->
                domains[neighbor] = oldDomain.toMutableList()
            }

            // Remover atribuição atual e limpar nó
            assignment.remove(sensor)
            node.percentage = null
        }
    }




    private fun buildTreecc(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
        val sensor = node.sensor
        val domain = domains[sensor] ?: return

        println("\n--> Explorar sensor ${sensor.id}")
        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")



        val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
//        val sortedDomain = domain.sortedWith(compareByDescending<Int> { period ->
//            val percentage = round(100.0 / period)
//            // Conta quantas opções compatíveis sobram para os vizinhos
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sumOf { neighbor ->
//                    val neighborDomain = newDomains[neighbor] ?: emptyList()
//                    neighborDomain.count { neighborValue ->
//                        areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                    }
//                }
//        }.thenBy { it }) // quebra empates preferindo period menor
        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
            val percentage = round(100.0 / period)
            // conta quantos valores nos vizinhos seriam removidos
            topology.neighbors(sensor)
                .filter { it !in assignment }
                .sumOf { neighbor ->
                    domains[neighbor]!!.count { neighborValue ->
                        !areCoprimePercentages(percentage, round(100.0 / neighborValue))
                    }
                }
        })

        for (period in sortedDomain /*domain.toList()*/) {
            val percentage = round(100.0 / period)
            assignment[sensor] = percentage
            node.percentage = percentage

            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())


            if (/*newCost*/ rmsPartial > bestCost ) {

               // println("Deistiu $rmsPartial ; $bestCost  | $ ")
                assignment.remove(sensor)
                node.percentage = null
                continue
            }


            // Verificação local coprimalidade
            var valid = true
            for (neighbor in topology.neighbors(sensor)) {
                if (assignment.containsKey(neighbor)) {
                    val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)



                    if (!coprime) {
                        valid = false
                        break
                    }
                }
            }
            if (!valid) {
                assignment.remove(sensor)
                node.percentage = null
                continue
            }

            topology.neighbors(sensor)
                .filter { it !in assignment }
                .forEach { neighbor ->
                    newDomains[neighbor] = newDomains[neighbor]!!
                        .filter { neighborValue ->
                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
                        }.toMutableList()
                  //  println("newDomains $newDomains")
                }

            evaluateSolution(assignment, newSquaredCost)

            //val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
//
//            val unassignedNeighbors = topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }

            val unassignedNeighbors = topology.neighbors(sensor)
                .filter { it !in assignment }
                .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
                    .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree


            if (unassignedNeighbors.isNotEmpty()) {

                for (neighborSensor in unassignedNeighbors) {
                    val childNode = Node(sensor = neighborSensor, parent = node)

                    node.children.add(childNode)


                    buildTree(childNode, /*domains*/newDomains, assignment, newSquaredCost)
                }
            } else {
                // Backtracking em cadeia usando node.parent
                var currentNode: Node? = node
                while (currentNode?.parent != null) {

                    val siblings = topology.neighbors(currentNode.parent!!.sensor)
                        .filter { it !in assignment && it != currentNode.sensor }

                    if (siblings.isNotEmpty()) {
                        for (siblingSensor in siblings) {
                            val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)

                            currentNode.parent.children.add(siblingNode)

                            buildTree(siblingNode, /*domains*/newDomains, assignment, newSquaredCost)
                        }
                        break
                    }
                    currentNode = currentNode.parent
                }
            }

            assignment.remove(sensor)
            node.percentage = null
        }
    }

/*
    private fun buildTree(
        node: Node,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double
    ) {
        val sensor = node.sensor
        val domain = domains[sensor] ?: return

        println("\n--> Explorar sensor ${sensor.id}")
        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")

        // Ordenar domínios priorizando opções que dão mais compatibilidade com vizinhos
//        val sortedDomain = domain.sortedWith(compareByDescending<Int> { period ->
//            val percentage = round(100.0 / period)
//            topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sumOf { neighbor ->
//                    val neighborDomain = domains[neighbor] ?: emptyList()
//                    neighborDomain.count { neighborValue ->
//                        areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                    }
//                }
//        }.thenBy { it }) // desempate preferindo período menor

        val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
            val percentage = round(100.0 / period)
            // conta quantos valores nos vizinhos seriam removidos
            topology.neighbors(sensor)
                .filter { it !in assignment }
                .sumOf { neighbor ->
                    domains[neighbor]!!.count { neighborValue ->
                        !areCoprimePercentages(percentage, round(100.0 / neighborValue))
                    }
                }
        })

        for (period in sortedDomain) {
            val percentage = round(100.0 / period)
            assignment[sensor] = percentage
            node.percentage = percentage

            // custo parcial
            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())

            // poda pelo melhor custo conhecido
            if (rmsPartial > bestCost) {
                assignment.remove(sensor)
                node.percentage = null
                continue
            }

            // verificação coprimalidade local
            val valid = topology.neighbors(sensor)
                .filter { it in assignment }
                .all { neighbor -> areCoprimePercentages(percentage, assignment[neighbor]!!) }

            if (!valid) {
                assignment.remove(sensor)
                node.percentage = null
                continue
            }

            // Forward-checking: atualizar domínios de vizinhos não atribuídos
            val backup = mutableMapOf<Sensor, List<Int>>()
            topology.neighbors(sensor)
                .filter { it !in assignment }
                .forEach { neighbor ->
                    backup[neighbor] = domains[neighbor]!!.toList() // guardar estado antigo
                    domains[neighbor] = domains[neighbor]!!
                        .filter { neighborValue ->
                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
                        }.toMutableList()
                }

            // Avaliar solução parcial
            evaluateSolution(assignment, newSquaredCost)

            // Explorar vizinhos não atribuídos
//            val unassignedNeighbors = topology.neighbors(sensor)
//                .filter { it !in assignment }
//                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }
            val unassignedNeighbors = topology.neighbors(sensor)
                .filter { it !in assignment }
                .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
                    .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree

            for (neighborSensor in unassignedNeighbors) {
                val childNode = Node(sensor = neighborSensor, parent = node)
                node.children.add(childNode)
                buildTree(childNode, domains, assignment, newSquaredCost)
            }

            // rollback domínios após explorar filhos
            backup.forEach { (neighbor, oldDomain) ->
                domains[neighbor] = oldDomain.toMutableList()
            }

            // remover atribuição atual e limpar nó
            assignment.remove(sensor)
            node.percentage = null
        }
    }
    */

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