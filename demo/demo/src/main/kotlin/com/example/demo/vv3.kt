////import kotlin.math.*
////
////// ===================== MODELO =====================
////data class Sensor(
////    val id: String,
////    val desiredDutyCycle: Double,
////    val tolerance: Double = 2.0
////)
////
////data class DutyCycleParameter(val value: Double)
////
////class NetworkTopology(private val adjacency: Map<Sensor, List<Sensor>>) {
////    fun sensors(): List<Sensor> = adjacency.keys.toList()
////    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
////}
////
////
////// ===================== DOMÍNIO =====================
////fun generateCandidates(sensor: Sensor): List<Int> {
////    val min = ceil(sensor.desiredDutyCycle - sensor.tolerance).toInt()
////    val max = floor(sensor.desiredDutyCycle + sensor.tolerance).toInt()
////    return (min..max).toList()
////}
////
////
////// ===================== OTIMIZADOR =====================
////class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {
////
////    private var bestCost = Double.MAX_VALUE
////    private var bestAssignment: Map<Sensor, Double>? = null
////
////    fun optimize(): Map<Sensor, Double>? {
////        val sensors = topology.sensors().sortedBy { it.id }
////        for (startSensor in sensors) {
////            val domains = sensors.associateWith { generateCandidates(it).toMutableList() }.toMutableMap()
////            val assignment = mutableMapOf<Sensor, Double>()
////            buildTree(startSensor, assignment, domains, 0.0, null)
////        }
////        return bestAssignment
////    }
////
////    private fun buildTree(
////        sensor: Sensor,
////        assignment: MutableMap<Sensor, Double>,
////        domains: MutableMap<Sensor, MutableList<Int>>,
////        currentCost: Double,
////        parentNode: TreeNode?
////    ) {
////        val domain = domains[sensor] ?: return
////
////        for (intValue in domain.toList()) {
////            val value = intValue.toDouble()
////            val incrementalCost = abs(value - sensor.desiredDutyCycle)
////            val newCost = currentCost + incrementalCost
////
////            // ===== Branch & Bound: poda por custo =====
////            if (newCost >= bestCost) continue
////
////            val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
////            assignment[sensor] = value
////            var valid = true
////
////            // Forward checking para vizinhos
////            for (neighbor in topology.neighbors(sensor)) {
////                if (assignment.containsKey(neighbor)) {
////                    if (!areCoprimePercentages(value, assignment[neighbor]!!)) {
////                        valid = false
////                        break
////                    }
////                } else {
////                    newDomains[neighbor]?.removeIf { !areCoprimePercentages(value, it.toDouble()) }
////                    if (newDomains[neighbor].isNullOrEmpty()) {
////                        valid = false
////                        break
////                    }
////                }
////            }
////
////            if (valid) {
////                val node = TreeNode(sensor, value)
////                parentNode?.children?.add(node)
////
////                // Próximo sensor: vizinhos ainda não atribuídos
////                val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
////                if (unassignedNeighbors.isNotEmpty()) {
////                    for (neighbor in unassignedNeighbors) {
////                        buildTree(neighbor, assignment, newDomains, newCost, node)
////                    }
////                } else {
////                    // Todos atribuídos ou sem vizinhos não atribuídos
////                    if (assignment.size == topology.sensors().size) {
////                        if (newCost < bestCost) {
////                            bestCost = newCost
////                            bestAssignment = assignment.toMap()
////                        }
////                    } else {
////                        // Sensores isolados restantes
////                        val nextSensor = topology.sensors().firstOrNull { it !in assignment }
////                        if (nextSensor != null) {
////                            buildTree(nextSensor, assignment, newDomains, newCost, node)
////                        }
////                    }
////                }
////            }
////
////            assignment.remove(sensor) // backtrack
////        }
////    }
////}
////
////// ===================== API =====================
////fun computeSchedulesOptimizedd(topology: NetworkTopology): List<Schedule> {
////    val optimizer = DutyCycleTreeOptimizer(topology)
////    val solution = optimizer.optimize()
////    return topology.sensors().map { sensor ->
////        val value = solution?.get(sensor)
////        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
////    }
////}
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
//import com.example.demo.domain.NetworkTopology
//import com.example.demo.domain.Sensor
//import com.example.demo.pipeline.DutyCycleParameter
//import com.example.demo.pipeline.Schedule
//import kotlin.math.abs
//import kotlin.math.round
//import kotlin.math.sqrt
//
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
//// =====================  =====================
//fun generateCandidatesff(sensor: Sensor, step: Double = 0.05): List<Double> {
//    val min = sensor.desiredDutyCycle - sensor.tolerance
//    val max = (sensor.desiredDutyCycle + sensor.tolerance)
//    val values = mutableListOf<Double>()
//    var v = min
//    while (v <= max ) {
//        values.add(v)
//        v += 1
//    }
//    // println("$sensor->${values} ->${(100/values.first()).toInt()}")
//    return values
//}
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
//// ===================== =====================
//data class TreeNode(
//    val sensor: Sensor,
//    val value: Double,
//    val children: MutableList<TreeNode> = mutableListOf()
//)
//
//// =====================  =====================
//
//
//
//class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {
//
//    private var bestCost = 0.0
//
//    private var bestNullCount = Int.MAX_VALUE
//    private var bestAssignment: Map<Sensor, Double?>? = null
//
//
//
//
//
//    fun optimize(): Map<Sensor, Double?>? {
//        val sensors = topology.sensors().sortedBy { it.id }
//
//        for (startSensor in sensors) {
//            println("start ${startSensor.id}")
//            val domains = sensors.associateWith { generateCandidates(it, step).toMutableList() }
//                .toMutableMap()
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
//    private fun evaluateSolution(
//        assignment: Map<Sensor, Double>,
//        cost: Double
//    ) {
//        val sensors = topology.sensors()
//        val nullCount = sensors.size - assignment.size
//
//        println(">> Avaliar solução parcial")
//        println("   Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}")
//        println("   Nulls = $nullCount | Custo = $cost")
//        println("   Melhor até agora: nulls=$bestNullCount custo=$bestCost")
//
//        val better =
//            (nullCount < bestNullCount )|| (nullCount == bestNullCount && cost < bestCost)
//
//        println("   E melhor? -> $better")
//
//        // if (better) {
//        bestNullCount = nullCount
//        bestCost = cost
//        bestAssignment = sensors.associateWith { assignment[it] }
//
//        println(
//            ">>> ** NOVA MELHOR SOLUÇaO **\n" +
//                    "    nulls=$bestNullCount custo=$bestCost\n" +
//                    "    solução=${bestAssignment!!.map { "${it.key.id}=${it.value}" }}\n"
//        )
//        // }
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
//
//    private fun buildTreecc(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
//        val sensor = node.sensor
//        val domain = domains[sensor] ?: return
//
//        println("\n--> Explorar sensor ${sensor.id}")
//        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//        for (period in domain.toList()) {
//            val percentage = round(100.0 / period)
//            assignment[sensor] = percentage
//            node.percentage = percentage
//
//            var valid = true
//            for (neighbor in topology.neighbors(sensor)) {
//                if (assignment.containsKey(neighbor)) {
//                    val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)
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
//            val newCost = currentCost + abs(percentage - sensor.desiredDutyCycle)
//            evaluateSolution(assignment, newCost)
//
//            val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
//
//            if (unassignedNeighbors.isNotEmpty()) {
//                for (neighborSensor in unassignedNeighbors) {
//                    val childNode = Node(sensor = neighborSensor, parent = node)
//
//                    node.children.add(childNode)
//
//                    buildTree(childNode, domains, assignment, newCost)
//                }
//            } else {
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
//                            buildTree(siblingNode, domains, assignment, newCost)
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
//
//    private fun buildTree(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
//        val sensor = node.sensor
//        val domain = domains[sensor] ?: return
//
//        println("\n--> Explorar sensor ${sensor.id}")
//        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")
//
//        val sortedDomain = domain.sortedBy { period ->
//            val percentage = round(100.0 / period)
//            topology.neighbors(sensor).count { neighbor ->
//                neighbor !in assignment && !areCoprimePercentages(percentage, assignment[neighbor] ?: 0.0)
//            }
//        }
//
//
//        for (period in domain.toList()) {
//            val percentage = round(100.0 / period)
//            assignment[sensor] = percentage
//            node.percentage = percentage
//
//            var valid = true
//            for (neighbor in topology.neighbors(sensor)) {
//                if (assignment.containsKey(neighbor)) {
//                    val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)
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
//            val newCost = currentCost + abs(percentage - sensor.desiredDutyCycle)
//
//            val error = percentage - sensor.desiredDutyCycle
//            //val newCost = currentCost + (error * error)   // ∑ erro²
//
//            val rmsPartial = sqrt(newCost / assignment.size.toDouble())
//
//            val nullCount = topology.sensors().size - assignment.size
//            println("antigo -> $bestCost , novo -> $newCost")
//
//
//
//            if (newCost >= bestCost && bestCost!=0.0) {
//                println("eeeeeeeeeeeeeeeedcccc")
//                assignment.remove(sensor)
//                node.percentage = null
//                continue
//            }
//
//            evaluateSolution(assignment, rmsPartial)
//
//            val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
//
////            val unassignedNeighbors = topology.neighbors(sensor)
////                .filter { it !in assignment }
////                .sortedBy { domains[it]?.size ?: Int.MAX_VALUE }
//
//            if (unassignedNeighbors.isNotEmpty()) {
//                for (neighborSensor in unassignedNeighbors) {
//                    val childNode = Node(sensor = neighborSensor, parent = node)
//
//                    node.children.add(childNode)
//
//                    buildTree(childNode, domains, assignment, newCost)
//                }
//            } else {
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
//                            buildTree(siblingNode, domains, assignment, newCost)
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
//
//}
//
//
//
//
//
//
//// =====================  =====================
//fun computeSchedulesOptimized(topology: NetworkTopology): List<Schedule> {
//    val optimizer = DutyCycleTreeOptimizer(topology)
//    val solution = optimizer.optimize()
//    return topology.sensors().map { sensor ->
//        val value = solution?.get(sensor)
//        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
//    }
//}
//fun main() {
//    println("${(100/15).toInt()}")
//    // ===================== SENSORES =====================
//    val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 0.0) // 100/20 = 5
//    val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 0.0) // 100/25 = 4
//    val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 0.0) // 100/15 ≈ 6
//    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.0) // 100/10 = 10
//    //val D = Sensor("D", desiredDutyCycle = 9.09, tolerance = 0.0)  // período ≈ 11 → coprimo com A e C
//    val E = Sensor("E", desiredDutyCycle = 9.0, tolerance = 0.0)  // 100/9 ≈ 11
//
//    // ===================== TOPOLOGIA =====================
//    val topology = NetworkTopology(
//        mapOf(
//            A to listOf(B, C),
//            B to listOf(A, C, E),
//            C to listOf(A, B, D),
//            D to listOf(C, E),
//            E to listOf(B, D)
//        )
//    )
//
//    val topology2 = NetworkTopology(
//        mapOf(
//            A to listOf(B, D),
//            B to listOf(A, C, E),
//            C to listOf(B, D),
//            D to listOf(A, E),
//            E to listOf(B)
//        )
//    )
//
//    // ===================== OTIMIZAÇÃO =====================
//    val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
//    val solution = optimizer.optimize()
//
//    // ===================== RESULTADOS =====================
//    println("=== SOLUÇÃO ÓTIMA ===")
//    solution?.forEach { (sensor, value) ->
//        println("Sensor ${sensor.id}: DutyCycle = $value%")
//    }
//
//
//}