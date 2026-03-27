//import kotlin.math.*
//
//// ===================== MODELO =====================
//data class Sensor(
//    val id: String,
//    val desiredDutyCycle: Double,
//    val tolerance: Double = 2.0
//)
//
//data class DutyCycleParameter(val value: Double)
//
//class NetworkTopology(private val adjacency: Map<Sensor, List<Sensor>>) {
//    fun sensors(): List<Sensor> = adjacency.keys.toList()
//    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
//}
//
//
//// ===================== DOMÍNIO =====================
//fun generateCandidates(sensor: Sensor): List<Int> {
//    val min = ceil(sensor.desiredDutyCycle - sensor.tolerance).toInt()
//    val max = floor(sensor.desiredDutyCycle + sensor.tolerance).toInt()
//    return (min..max).toList()
//}
//
//
//// ===================== OTIMIZADOR =====================
//class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {
//
//    private var bestCost = Double.MAX_VALUE
//    private var bestAssignment: Map<Sensor, Double>? = null
//
//    fun optimize(): Map<Sensor, Double>? {
//        val sensors = topology.sensors().sortedBy { it.id }
//        for (startSensor in sensors) {
//            val domains = sensors.associateWith { generateCandidates(it).toMutableList() }.toMutableMap()
//            val assignment = mutableMapOf<Sensor, Double>()
//            buildTree(startSensor, assignment, domains, 0.0, null)
//        }
//        return bestAssignment
//    }
//
//    private fun buildTree(
//        sensor: Sensor,
//        assignment: MutableMap<Sensor, Double>,
//        domains: MutableMap<Sensor, MutableList<Int>>,
//        currentCost: Double,
//        parentNode: TreeNode?
//    ) {
//        val domain = domains[sensor] ?: return
//
//        for (intValue in domain.toList()) {
//            val value = intValue.toDouble()
//            val incrementalCost = abs(value - sensor.desiredDutyCycle)
//            val newCost = currentCost + incrementalCost
//
//            // ===== Branch & Bound: poda por custo =====
//            if (newCost >= bestCost) continue
//
//            val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
//            assignment[sensor] = value
//            var valid = true
//
//            // Forward checking para vizinhos
//            for (neighbor in topology.neighbors(sensor)) {
//                if (assignment.containsKey(neighbor)) {
//                    if (!areCoprimePercentages(value, assignment[neighbor]!!)) {
//                        valid = false
//                        break
//                    }
//                } else {
//                    newDomains[neighbor]?.removeIf { !areCoprimePercentages(value, it.toDouble()) }
//                    if (newDomains[neighbor].isNullOrEmpty()) {
//                        valid = false
//                        break
//                    }
//                }
//            }
//
//            if (valid) {
//                val node = TreeNode(sensor, value)
//                parentNode?.children?.add(node)
//
//                // Próximo sensor: vizinhos ainda não atribuídos
//                val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
//                if (unassignedNeighbors.isNotEmpty()) {
//                    for (neighbor in unassignedNeighbors) {
//                        buildTree(neighbor, assignment, newDomains, newCost, node)
//                    }
//                } else {
//                    // Todos atribuídos ou sem vizinhos não atribuídos
//                    if (assignment.size == topology.sensors().size) {
//                        if (newCost < bestCost) {
//                            bestCost = newCost
//                            bestAssignment = assignment.toMap()
//                        }
//                    } else {
//                        // Sensores isolados restantes
//                        val nextSensor = topology.sensors().firstOrNull { it !in assignment }
//                        if (nextSensor != null) {
//                            buildTree(nextSensor, assignment, newDomains, newCost, node)
//                        }
//                    }
//                }
//            }
//
//            assignment.remove(sensor) // backtrack
//        }
//    }
//}
//
//// ===================== API =====================
//fun computeSchedulesOptimizedd(topology: NetworkTopology): List<Schedule> {
//    val optimizer = DutyCycleTreeOptimizer(topology)
//    val solution = optimizer.optimize()
//    return topology.sensors().map { sensor ->
//        val value = solution?.get(sensor)
//        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
//    }
//}