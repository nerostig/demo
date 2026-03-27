import kotlin.math.abs
import kotlin.math.round

// ===================== MODELO =====================
data class Sensor(
    val id: String,
    val desiredDutyCycle: Double,
    val tolerance: Double = 2.0
)

data class DutyCycleParameter(val value: Double)
data class Schedule(val sensor: Sensor, val parameter: DutyCycleParameter?)

class NetworkTopology(private val adjacency: Map<Sensor, List<Sensor>>) {
    fun sensors(): List<Sensor> = adjacency.keys.toList()
    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
}

// ===================== MATEMÁTICA =====================
fun gcd(a: Int, b: Int): Int {
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
fun dutyCycleToPeriod(dc: Double): Int = round(100.0 / dc).toInt()
fun areCoprimePercentages(a: Double, b: Double): Boolean = areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))

// ===================== DOMÍNIO =====================
fun generateCandidates(sensor: Sensor, step: Double = 0.05): List<Double> {
    val min = sensor.desiredDutyCycle - sensor.tolerance
    val max = (sensor.desiredDutyCycle + sensor.tolerance)
    val values = mutableListOf<Double>()
    var v = min
    while (v <= max ) {
        values.add(v)
        v += 1
    }
    return values
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
    private var bestAssignment: Map<Sensor, Double>? = null

    fun optimize(): Map<Sensor, Double>? {
        val sensors = topology.sensors().sortedBy { it.id }
        // tenta cada sensor inicial
        for (startSensor in sensors) {
            val domains = sensors.associateWith { generateCandidates(it, step).toMutableList() }.toMutableMap()
            val assignment = mutableMapOf<Sensor, Double>()
            buildTree(startSensor, assignment, domains, 0.0, null)
        }
        return bestAssignment
    }

    private fun buildTree(
        sensor: Sensor,
        assignment: MutableMap<Sensor, Double>,
        domains: MutableMap<Sensor, MutableList<Double>>,
        currentCost: Double,
        parentNode: TreeNode?
    ) {
        val domain = domains[sensor] ?: return

        for (value in domain.toList()) {
            val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
            assignment[sensor] = value
            var valid = true

            // forward checking para vizinhos
            for (neighbor in topology.neighbors(sensor)) {
                if (assignment.containsKey(neighbor)) {
                    if (!areCoprimePercentages(value, assignment[neighbor]!!)) {
                        valid = false
                        break
                    }
                } else {
                    newDomains[neighbor]?.removeIf { !areCoprimePercentages(value, it) }
                    if (newDomains[neighbor].isNullOrEmpty()) {
                        valid = false
                        break
                    }
                }
            }

            if (valid) {
                val node = TreeNode(sensor, value)
                parentNode?.children?.add(node)

                // próximo sensor não atribuído: escolha heurística -> vizinhos ainda não atribuídos
                val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
                if (unassignedNeighbors.isNotEmpty()) {
                    for (neighbor in unassignedNeighbors) {
                        buildTree(neighbor, assignment, newDomains, currentCost + abs(value - sensor.desiredDutyCycle), node)
                    }
                } else {
                    // todos atribuídos ou sem vizinhos não atribuídos, verifica se todos sensores têm valor
                    if (assignment.size == topology.sensors().size) {
                        val totalCost = currentCost + abs(value - sensor.desiredDutyCycle)
                        if (totalCost < bestCost) {
                            bestCost = totalCost
                            bestAssignment = assignment.toMap()
                        }
                    } else {
                        // ainda há sensores isolados, pega o próximo sensor não atribuído da lista
                        val nextSensor = topology.sensors().firstOrNull { it !in assignment }
                        if (nextSensor != null) {
                            buildTree(nextSensor, assignment, newDomains, currentCost + abs(value - sensor.desiredDutyCycle), node)
                        }
                    }
                }
            }

            assignment.remove(sensor) // backtrack
        }
    }
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