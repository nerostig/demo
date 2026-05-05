

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





















//import com.example.demo.domain.NetworkTopology
//import com.example.demo.domain.Sensor
//import com.example.demo.pipeline.DutyCycleParameter
//import com.example.demo.pipeline.Schedule
//import kotlin.math.abs
//import kotlin.math.round
//import kotlin.math.sqrt
//
//// ===================== MODELO =====================
//
//import kotlin.random.Random
//
//fun generateRandomTopology(
//    sensorCount: Int,
//    maxNeighbors: Int,
//    dutyCycleRange: ClosedFloatingPointRange<Double> = 5.0..40.0,
//    tolerance: Double = 0.0,
//    seed: Int? = null
//): NetworkTopology {
//
//    val random = seed?.let { Random(it) } ?: Random.Default
//
//    // 1️⃣ Criar sensores
//    val sensors = (0 until sensorCount).map { i ->
//        Sensor(
//            id = "S$i",
//            desiredDutyCycle = random
//                .nextInt(
//                    dutyCycleRange.start.toInt(),
//                    dutyCycleRange.endInclusive.toInt() + 1
//                )
//                .toDouble(),
//            tolerance = tolerance
//        )
//    }
//
//    // 2️⃣ Criar adjacência vazia
//    val adjacency = sensors.associateWith { mutableSetOf<Sensor>() }
//
//    // 3️⃣ Criar ligações aleatórias
//    for (sensor in sensors) {
//        val possibleNeighbors = sensors.filter { it != sensor }
//        val neighborCount = random.nextInt(1, maxNeighbors + 1)
//
//        val chosen = possibleNeighbors.shuffled(random)
//            .take(neighborCount)
//
//        for (neighbor in chosen) {
//            adjacency[sensor]!!.add(neighbor)
//            adjacency[neighbor]!!.add(sensor) // grafo não-direcionado
//        }
//    }
//
//    // 4️⃣ Converter para Map<Sensor, List<Sensor>>
//    val finalAdjacency = adjacency.mapValues { it.value.toList() }
//
//    return NetworkTopology(finalAdjacency)
//}
//
////data class Sensor(
////    val id: String,
////    val desiredDutyCycle: Double,
////    val tolerance: Double = 2.0
////)
////
////data class DutyCycleParameter(val value: Double)
////data class Schedule(val sensor: Sensor, val parameter: DutyCycleParameter?)
////
////class NetworkTopology(private val adjacency: Map<Sensor, List<Sensor>>) {
////    fun sensors(): List<Sensor> = adjacency.keys.toList()
////    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
////}
//
//// ===================== MATEMÁTICA =====================
//fun gcd(a: Int, b: Int): Int {
//    //println("$a")
//    //println("$b")
//    var x = abs(a)
//    var y = abs(b)
//    while (y != 0) {
//        val t = y
//        y = x % y
//        x = t
//    }
//    return x
//}
//
//fun areCoprime(a: Int, b: Int): Boolean = gcd(a, b) == 1
//fun dutyCycleToPeriod(dc: Double): Int = (100.0 / dc).toInt()
//fun areCoprimePercentages(a: Double, b: Double): Boolean {
//    // println("avvv ->$a")
//    //println("b vvv->$b")
//
//
//    return areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))
//
//
//}
//
//// ===================== DOMÍNIO =====================
//
//
//fun generateCandidates(sensor: Sensor, step: Double = 0.05): List<Int> {
//    // val min = (100 / (sensor.desiredDutyCycle - sensor.tolerance)).toInt()
//    //val max = (100 / (sensor.desiredDutyCycle + sensor.tolerance)).toInt()
//    val min = (100 / (sensor.desiredDutyCycle + sensor.tolerance)).toInt()
//    val max = (100 / (sensor.desiredDutyCycle - sensor.tolerance)).toInt()
//    val values = mutableListOf<Int>()
//    var v = min
//    while (v <= max) {
//        values.add(v)
//        v += 1
//    }
//    return values
//}
//
//
//// ===================== ÁRVORE DE DECISÃO =====================
//data class TreeNode(
//    val sensor: Sensor,
//    val value: Double,
//    val children: MutableList<TreeNode> = mutableListOf()
//)
//
//// ===================== OTIMIZADOR =====================
//
//class LargeDutyCycleOptimizer(
//    private val topology: NetworkTopology,
//    private val step: Double = 0.05,
//    private val maxSubtreeSize: Int = 2
//) {
//
//    // ===================== RESULTADOS GLOBAIS =====================
//    private val globalAssignments = mutableMapOf<Sensor, Double?>()
//
//    // ===================== OTIMIZAÇÃO =====================
//    fun optimize(): MutableMap<Sensor, Double?> {
//
//        // 1. Dividir topologia em subárvores
//        val subtrees = buildSubtrees(topology.sensors(), maxSubtreeSize)
//
//        println("subtrees = $subtrees")
//
//        // 2. Resolver cada subárvore incrementalmente
//        for (subtree in subtrees) {
//
//            // --- sensores já resolvidos que ligam a esta subárvore
//            val fixedAssignments = subtree.connectors
//                .filter { it in globalAssignments }
//                .associateWith { globalAssignments[it]!! }
//
//            // --- subtopologia:
//            //     - sensores da subárvore
//            //     - ligações internas
//            //     - ligações aos conectores (fixos)
//            val subTopology = NetworkTopology(
//                subtree.sensors.associateWith { sensor ->
//                    topology.neighbors(sensor)
//                        .filter { it in subtree.sensors || it in fixedAssignments }
//                }
//            )
//
//            println("subTopology sensors = ${subTopology.sensors()}")
//            println("fixed = $fixedAssignments")
//
//            // 3. Otimizar subárvore com restrições fixas
//            val subOptimizer = DutyCycleTreeOptimizer(
//                topology = subTopology,
//                step = step,
//                fixedAssignments = fixedAssignments
//            )
//
//            val partialSolution = subOptimizer.optimize() ?: emptyMap()
//            println("partialSolution $partialSolution")
//
//            // 4. Guardar resultados globais
//            for ((sensor, value) in partialSolution) {
//                globalAssignments[sensor] = value
//            }
//        }
//
//
//        return globalAssignments
//    }
//
//    // ===================== CRIAR SUBÁRVORES =====================
//    private fun buildSubtrees(
//        sensors: Set<Sensor>,
//        maxSize: Int
//    ): List<Subtree> {
//
//        val visited = mutableSetOf<Sensor>()
//        val subtrees = mutableListOf<Subtree>()
//
//        for (start in sensors) {
//            if (start in visited) continue
//
//            // -------- FASE 1: BFS limitado --------
//            val component = mutableSetOf<Sensor>()
//            val queue = ArrayDeque<Sensor>()
//            queue.add(start)
//
//            while (queue.isNotEmpty() && component.size < maxSize) {
//                val s = queue.removeFirst()
//                if (s in visited) continue
//
//                visited.add(s)
//                component.add(s)
//
//                for (n in topology.neighbors(s)) {
//                    if (n !in visited && component.size < maxSize) {
//                        queue.add(n)
//                    }
//                }
//            }
//
//            // -------- FASE 2: conectores --------
//            val connectors = mutableSetOf<Sensor>()
//            for (s in component) {
//                for (n in topology.neighbors(s)) {
//                    if (n !in component) {
//                        connectors.add(n)
//                    }
//                }
//            }
//
//            subtrees.add(Subtree(component, connectors))
//        }
//
//        return subtrees
//    }
//
//    // ===================== SUBÁRVORE =====================
//    data class Subtree(
//        val sensors: Set<Sensor>,
//        val connectors: Set<Sensor>
//    )
//}
//class DutyCycleTreeOptimizer
//    (private val topology: NetworkTopology
//     , private val step: Double = 0.05
//     , private val fixedAssignments: Map<Sensor, Double> = emptyMap()) {
//
//    private var bestCost = Double.MAX_VALUE
//
//    private var bestNullCount = Int.MAX_VALUE
//    private var bestAssignment: Map<Sensor, Double?>? = null
//
//
//
//    fun optimize(): Map<Sensor, Double?>? {
//
//        val assignment = fixedAssignments.toMutableMap()
//
//
//        val domains = topology.sensors()
//            .filter { it !in fixedAssignments }
//            .associateWith { generateCandidates(it, step).toMutableList() }
//            .toMutableMap()
//
//        val sensors = topology.sensors()
//            .filter { it !in fixedAssignments }
//            .sortedWith(
//                compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
//                    .thenBy { topology.neighbors(it).size }
//            )
//
//        for (startSensor in sensors) {
//            println("start ${startSensor.id}")
//            println(sensors)
//            val rootNode = Node(sensor = startSensor, parent = null)
//            buildTree(rootNode, domains, assignment, 0.0)
//        }
//
//        return bestAssignment
//    }
//
//
//    fun optimizebb(): Map<Sensor, Double?>? {
//
//        // Gera os domínios iniciais para cada sensor
//        val domains = topology.sensors().associateWith { generateCandidates(it, step).toMutableList() }
//            .toMutableMap()
//
//        val sensors = topology.sensors()
//            .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE }
//                .thenBy { topology.neighbors(it).size })
//        // val sensors = topology.sensors().sortedBy { topology.neighbors(it).size }
//
//
//        for (startSensor in sensors) {
//            println("start ${startSensor.id}")
//            println(sensors)
//
////            val domains =
////            sensors.associateWith { generateCandidates(it, step).toMutableList() } .toMutableMap()
//
//
//            val rootNode = Node(sensor = startSensor, parent = null)
//            buildTree(rootNode, domains, mutableMapOf(), 0.0)
//        }
//
//        return bestAssignment
//    }
//
//
//
//
//    private fun evaluateSolution(
//        assignment: Map<Sensor, Double>,
//        cost: Double
//    ) {
//        val sensors = topology.sensors()
//
//        val rmsCost = sqrt(cost / assignment.size.toDouble())
//
//        val nullCount = sensors.size - assignment.size
//
//        //val better=rmsCost < bestCost
//
//        println(">> Avaliar solução parcial")
//        println("bestAssignment -> $bestAssignment")
//        println("   Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}")
//        println("   | Custo = $cost")
//        println("   Melhor até agora:custo=$bestCost")
//
//        val better =
//            (nullCount < bestNullCount )|| (nullCount == bestNullCount && rmsCost < bestCost)
//
//        println("   É melhor? -> $better")
//
//        if (better) {
//            bestNullCount = nullCount
//            bestCost = rmsCost
//            bestAssignment = sensors.associateWith { assignment[it] }
//
//            println(
//                ">>> ** NOVA MELHOR SOLUÇÃO **\n" +
//                        "    nulls=$bestNullCount custo=$bestCost\n" +
//                        "    solução=${bestAssignment!!.map { "${it.key.id}=${it.value}" }}\n"
//            )
//        }
//    }
//
//
//
//    class Node(
//        val sensor: Sensor,
//        val parent: Node? = null,
//        val children: MutableList<Node> = mutableListOf(),
//        var percentage: Double? = null
//    )
//    private fun buildTree(
//        node: Node,
//        domains: MutableMap<Sensor, MutableList<Int>>,
//        assignment: MutableMap<Sensor, Double>,
//        currentCost: Double
//    ) {
//        val sensor = node.sensor
//        // val domain = domains[sensor] ?: return
//
//        //if (sensor in fixedAssignments) return
//        val domain = domains[sensor] ?: return
//
//        println("\n--> Explorar sensor ${sensor.id}")
//        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//        // Evitar clonagem completa desnecessária, só backup dos vizinhos
//        val backup = mutableMapOf<Sensor, List<Int>>()
//
//        // MRV + Degree heurística
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
//
//        for (period in sortedDomain) {
//            val percentage = round(100.0 / period)
//            assignment[sensor] = percentage
//            node.percentage = percentage
//
//            val error = percentage - sensor.desiredDutyCycle
//            val newSquaredCost = currentCost + (error * error)
//            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())
//
//            // Poda pelo melhor custo conhecido
//            if (rmsPartial > bestCost) {
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//            // Verificação local coprimalidade
//            val valid = topology.neighbors(sensor)
//                .filter { it in assignment }
//                .all { neighbor -> areCoprimePercentages(percentage, assignment[neighbor]!!) }
//
//            if (!valid) {
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
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
//            } else {
//                // Backtracking em cadeia local (somente quando não há vizinhos)
//                var currentNode: Node? = node
//                while (currentNode?.parent != null) {
//                    val siblings = topology.neighbors(currentNode.parent!!.sensor)
//                        .filter { it !in assignment && it != currentNode.sensor }
//
//                    if (siblings.isNotEmpty()) {
//                        for (siblingSensor in siblings) {
//                            val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)
//                            currentNode.parent.children.add(siblingNode)
//                            buildTree(siblingNode, domains, assignment, newSquaredCost)
//                        }
//                        break
//                    }
//                    currentNode = currentNode.parent
//                }
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
//    /*
//        private fun buildTreecc(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
//            val sensor = node.sensor
//            val domain = domains[sensor] ?: return
//
//            println("\n--> Explorar sensor ${sensor.id}")
//            println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//
//
//            val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
//    //        val sortedDomain = domain.sortedWith(compareByDescending<Int> { period ->
//    //            val percentage = round(100.0 / period)
//    //            // Conta quantas opções compatíveis sobram para os vizinhos
//    //            topology.neighbors(sensor)
//    //                .filter { it !in assignment }
//    //                .sumOf { neighbor ->
//    //                    val neighborDomain = newDomains[neighbor] ?: emptyList()
//    //                    neighborDomain.count { neighborValue ->
//    //                        areCoprimePercentages(percentage, round(100.0 / neighborValue))
//    //                    }
//    //                }
//    //        }.thenBy { it }) // quebra empates preferindo period menor
//            val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
//                val percentage = round(100.0 / period)
//                // conta quantos valores nos vizinhos seriam removidos
//                topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .sumOf { neighbor ->
//                        domains[neighbor]!!.count { neighborValue ->
//                            !areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                        }
//                    }
//            })
//
//            for (period in sortedDomain /*domain.toList()*/) {
//                val percentage = round(100.0 / period)
//                assignment[sensor] = percentage
//                node.percentage = percentage
//
//                val error = percentage - sensor.desiredDutyCycle
//                val newSquaredCost = currentCost + (error * error)
//                val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())
//
//
//                if (/*newCost*/ rmsPartial > bestCost ) {
//
//                    println("Deistiu $rmsPartial ; $bestCost  | $ ")
//                    assignment.remove(sensor)
//                    node.percentage = null
//                    continue
//                }
//
//
//                // Verificação local coprimalidade
//                var valid = true
//                for (neighbor in topology.neighbors(sensor)) {
//                    if (assignment.containsKey(neighbor)) {
//                        val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)
//
//
//
//                        if (!coprime) {
//                            valid = false
//                            break
//                        }
//                    }
//                }
//                if (!valid) {
//                    assignment.remove(sensor)
//                    node.percentage = null
//                    continue
//                }
//
//                topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .forEach { neighbor ->
//                        newDomains[neighbor] = newDomains[neighbor]!!
//                            .filter { neighborValue ->
//                                areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                            }.toMutableList()
//                        println("newDomains $newDomains")
//                    }
//
//                evaluateSolution(assignment, newSquaredCost)
//
//                //val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
//    //
//    //            val unassignedNeighbors = topology.neighbors(sensor)
//    //                .filter { it !in assignment }
//    //                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }
//
//                val unassignedNeighbors = topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
//                        .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree
//
//
//                if (unassignedNeighbors.isNotEmpty()) {
//
//                    for (neighborSensor in unassignedNeighbors) {
//                        val childNode = Node(sensor = neighborSensor, parent = node)
//
//                        node.children.add(childNode)
//
//
//                        buildTree(childNode, /*domains*/newDomains, assignment, newSquaredCost)
//                    }
//                } else {
//                    // Backtracking em cadeia usando node.parent
//                    var currentNode: Node? = node
//                    while (currentNode?.parent != null) {
//
//                        val siblings = topology.neighbors(currentNode.parent!!.sensor)
//                            .filter { it !in assignment && it != currentNode.sensor }
//
//                        if (siblings.isNotEmpty()) {
//                            for (siblingSensor in siblings) {
//                                val siblingNode = Node(sensor = siblingSensor, parent = currentNode.parent)
//
//                                currentNode.parent.children.add(siblingNode)
//
//                                buildTree(siblingNode, /*domains*/newDomains, assignment, newSquaredCost)
//                            }
//                            break
//                        }
//                        currentNode = currentNode.parent
//                    }
//                }
//
//                assignment.remove(sensor)
//                node.percentage = null
//            }
//        }
//
//        private fun buildTreecc(
//            node: Node,
//            domains: MutableMap<Sensor, MutableList<Int>>,
//            assignment: MutableMap<Sensor, Double>,
//            currentCost: Double
//        ) {
//            val sensor = node.sensor
//            val domain = domains[sensor] ?: return
//
//            println("\n--> Explorar sensor ${sensor.id}")
//            println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//            // Ordenar domínios priorizando opções que dão mais compatibilidade com vizinhos
//    //        val sortedDomain = domain.sortedWith(compareByDescending<Int> { period ->
//    //            val percentage = round(100.0 / period)
//    //            topology.neighbors(sensor)
//    //                .filter { it !in assignment }
//    //                .sumOf { neighbor ->
//    //                    val neighborDomain = domains[neighbor] ?: emptyList()
//    //                    neighborDomain.count { neighborValue ->
//    //                        areCoprimePercentages(percentage, round(100.0 / neighborValue))
//    //                    }
//    //                }
//    //        }.thenBy { it }) // desempate preferindo período menor
//
//            val sortedDomain = domain.sortedWith(compareBy<Int> { period ->
//                val percentage = round(100.0 / period)
//                // conta quantos valores nos vizinhos seriam removidos
//                topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .sumOf { neighbor ->
//                        domains[neighbor]!!.count { neighborValue ->
//                            !areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                        }
//                    }
//            })
//
//            for (period in sortedDomain) {
//                val percentage = round(100.0 / period)
//                assignment[sensor] = percentage
//                node.percentage = percentage
//
//                // custo parcial
//                val error = percentage - sensor.desiredDutyCycle
//                val newSquaredCost = currentCost + (error * error)
//                val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())
//
//                // poda pelo melhor custo conhecido
//                if (rmsPartial > bestCost) {
//                    assignment.remove(sensor)
//                    node.percentage = null
//                    continue
//                }
//
//                // verificação coprimalidade local
//                val valid = topology.neighbors(sensor)
//                    .filter { it in assignment }
//                    .all { neighbor -> areCoprimePercentages(percentage, assignment[neighbor]!!) }
//
//                if (!valid) {
//                    assignment.remove(sensor)
//                    node.percentage = null
//                    continue
//                }
//
//                // Forward-checking: atualizar domínios de vizinhos não atribuídos
//                val backup = mutableMapOf<Sensor, List<Int>>()
//                topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .forEach { neighbor ->
//                        backup[neighbor] = domains[neighbor]!!.toList() // guardar estado antigo
//                        domains[neighbor] = domains[neighbor]!!
//                            .filter { neighborValue ->
//                                areCoprimePercentages(percentage, round(100.0 / neighborValue))
//                            }.toMutableList()
//                    }
//
//                // Avaliar solução parcial
//                evaluateSolution(assignment, newSquaredCost)
//
//                // Explorar vizinhos não atribuídos
//    //            val unassignedNeighbors = topology.neighbors(sensor)
//    //                .filter { it !in assignment }
//    //                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }
//                val unassignedNeighbors = topology.neighbors(sensor)
//                    .filter { it !in assignment }
//                    .sortedWith(compareBy<Sensor> { domains[it]?.size ?: Int.MAX_VALUE } // MRV
//                        .thenByDescending { topology.neighbors(it).count { n -> n !in assignment } }) // Degree
//
//                for (neighborSensor in unassignedNeighbors) {
//                    val childNode = Node(sensor = neighborSensor, parent = node)
//                    node.children.add(childNode)
//                    buildTree(childNode, domains, assignment, newSquaredCost)
//                }
//
//                // rollback domínios após explorar filhos
//                backup.forEach { (neighbor, oldDomain) ->
//                    domains[neighbor] = oldDomain.toMutableList()
//                }
//
//                // remover atribuição atual e limpar nó
//                assignment.remove(sensor)
//                node.percentage = null
//            }
//        }
//        */
//}
//
//
//
//// ===================== API =====================
//fun computeSchedulesOptimized(topology: NetworkTopology): List<Schedule> {
//    val optimizer = DutyCycleTreeOptimizer(topology)
//    val solution = optimizer.optimize()
//    return topology.sensors().map { sensor ->
//        val value = solution?.get(sensor)
//        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
//    }
//}
//fun main() {
//
//
//
//    println("${(100/15).toInt()}")
//    // ===================== SENSORES =====================
//    val A = Sensor("A", desiredDutyCycle = 21.0, tolerance = 0.0) // 100/20 = 5
//    val B = Sensor("B", desiredDutyCycle = 22.0, tolerance = 0.0) // 100/25 = 4
//    val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 0.0) // 100/15 ≈ 6
//    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.0) // 100/10 = 10
//    val E = Sensor("E", desiredDutyCycle = 9.0, tolerance = 0.0)  // 100/9 ≈ 11
//
//    // ===================== TOPOLOGIA =====================
//    val topology = NetworkTopology(
//        mapOf(
//            A to listOf(B,C),
//            B to listOf(A,C,E),
//            C to listOf(A, B, D),
//            D to listOf(C, E),
//            E to listOf(B,D)
//        )
//    )
//
//    val optimizer = LargeDutyCycleOptimizer(topology, step = 1.0)
//    val solution = optimizer.optimize()
//
//    solution.forEach { (sensor, duty) ->
//        println("Sensor ${sensor.id}: DutyCycle = $duty%")
//    }
//
//
//
//    // ===================== OTIMIZAÇÃO =====================
////    val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
////    val solution = optimizer.optimize()
////
////    // ===================== RESULTADOS =====================
////    println("=== SOLUÇÃO ÓTIMA ===")
////    solution?.forEach { (sensor, value) ->
////        println("Sensor ${sensor.id}: DutyCycle = $value%")
////    }
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
//
////            var forwardCheckFailed = false
////            for (neighbor in topology.neighbors(sensor)) {
////                if (neighbor !in assignment) {
////                    val neighborDomain = newDomains[neighbor]!!
////                    neighborDomain.removeIf { value ->
////                        !areCoprimePercentages(percentage, round(100.0 / value))
////                    }
////                    if (neighborDomain.isEmpty()) {
////                        forwardCheckFailed = true
////                        break
////                    }
////                }
////            }
////            if (forwardCheckFailed) {
////                assignment.remove(sensor)
////                node.percentage = null
////                continue
////            }
//
//
////            topology.neighbors(sensor)
////                .filter { it !in assignment }
////                .forEach { neighbor ->
////                    newDomains[neighbor] = newDomains[neighbor]!!
////                        .filter { neighborValue ->
////                            areCoprimePercentages(percentage, round(100.0 / neighborValue))
////                        }.toMutableList()
////                    if (newDomains[neighbor]!!.isEmpty()) valid = false
////                }
////            if (!valid) {
////                assignment.remove(sensor)
////                node.percentage = null
////                continue
////            }