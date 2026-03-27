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
fun areCoprimePercentages(a: Double, b: Double): Boolean {
   // println("avvv ->$a")
    //println("b vvv->$b")


    return areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))


}

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
   // println("$sensor->${values} ->${(100/values.first()).toInt()}")
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
            println("startSensor ->$startSensor")
            val domains = sensors.associateWith { generateCandidates(it, step).toMutableList() }.toMutableMap()
            val assignment = mutableMapOf<Sensor, Double>()
            buildTree(startSensor, assignment, domains, 0.0) //null)
        }
        return bestAssignment
    }

    fun buildTresse(
        sensor: Sensor,
        assignment: MutableMap<Sensor, Double>,
        domains: MutableMap<Sensor, MutableList<Double>>,
        currentCost: Double,
       // parentNode: TreeNode?
    ) {
       /// if(currentCost>bestCost)return

        val domain = domains[sensor] ?: return

        for (value in domain.toList()) {
            val newDomains = domains.mapValues { it.value.toMutableList() }.toMutableMap()
            assignment[sensor] = value
            var valid = true

           // println("$domains")

            println("Tentando sensor ${sensor.id} = $value")
            println("Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}\n")
            //println("vizinhos ->${topology.neighbors(sensor)}")
            
            for (neighbor in topology.neighbors(sensor)) { //C->A
                println(" sensor Atual ${sensor.id} e vizinho ${neighbor.id}")

                if (assignment.containsKey(neighbor)) {
                    val result2 = areCoprimePercentages(value, assignment[neighbor]!!)

                    println(
                        " coprimalidade MAs com um vizinho com valor Já defenido: " +
                                "SensorAtual=${sensor.id} (value=$value) ↔ " +
                                "Vizinho DEFENIDO=${neighbor.id} (candidate=${assignment[neighbor]!!}) => $result2"
                    )
                    if (!areCoprimePercentages(value, assignment[neighbor]!!)) {
                        valid = false
                        break
                    }

                } else {
                    /*
                    val remainingNeigbors=(topology.neighbors(sensor)).filter { assignment.containsKey(neighbor)==false }.size

                        var countNOTValids = 0

                        newDomains.filter{it.key !in assignment.keys}[neighbor]?.removeIf {
                             val result = areCoprimePercentages(value, it)

                        println(
                            " coprimalidade: " +
                                    "SensorAtual=${sensor.id} (value=$value) ↔ " +
                                    "Vizinho=${neighbor.id} (candidate=$it) => $result"
                       )
                            //B->15
                            if (!areCoprimePercentages(value, it)) {
                                countNOTValids += 1
                            }
                            !areCoprimePercentages(value, it)

                        }
                        if (countNOTValids == remainingNeigbors) { //B=(C e E)   B->C
                            valid = false
                            break
                        }
                    */
                    val hasAtLeastOneValid = newDomains[neighbor]!!
                        .any { areCoprimePercentages(value, it) }

                    println(
                        "Vizinho ${neighbor.id} ainda livre → " +
                                "existe valor compatível? $hasAtLeastOneValid"
                    )

                    if (!hasAtLeastOneValid) {
                        valid = false
                        break
                    }

                }
            }

            if (valid) {
               // val node = TreeNode(sensor, value)
               // parentNode?.children?.add(node)

                // próximo sensor não atribuído: escolha heurística -> vizinhos ainda não atribuídos
                val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
                if (unassignedNeighbors.isNotEmpty()) {
                    for (neighbor in unassignedNeighbors) {
                        buildTree(neighbor, assignment, domains, currentCost + abs(value - sensor.desiredDutyCycle), )
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
                            buildTree(nextSensor, assignment, newDomains, currentCost + abs(value - sensor.desiredDutyCycle), )
                        }
                    }
                }
            }

            assignment.remove(sensor) // backtrack
        }
    }

    private fun buildTree(
        sensor: Sensor,
        assignment: MutableMap<Sensor, Double>,
        domains: MutableMap<Sensor, MutableList<Double>>,
        currentCost: Double
    ) {
        val domain = domains[sensor] ?: return

        for (value in domain.toList()) {
            assignment[sensor] = value
            println("Tentando sensor ${sensor.id} = $value")
            println("Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}\n")

            var valid = true

            // Verificação de vizinhos já atribuídos
            for (neighbor in topology.neighbors(sensor)) {
                if (assignment.containsKey(neighbor)) {
                    val coprime = areCoprimePercentages(value, assignment[neighbor]!!)
                    println("  Coprimalidade com vizinho já definido ${neighbor.id}=${assignment[neighbor]} → $coprime")
                    if (!coprime) valid = false
                }
            }

            if (!valid) {
                assignment.remove(sensor)
                continue
            }

            // Verificação de vizinhos ainda não atribuídos
            val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }
            var allNeighborsImpossible = false
            if (unassignedNeighbors.isNotEmpty()) {
                allNeighborsImpossible = unassignedNeighbors.all { neighbor ->
                    val neighborDomain = domains[neighbor]!!
                    val hasCompatible = neighborDomain.any { areCoprimePercentages(value, it) }
                    println("  Vizinho ${neighbor.id} ainda livre → existe valor compatível? $hasCompatible")
                    !hasCompatible
                }
            }

            if (allNeighborsImpossible) {
                println("  Nenhum vizinho livre pode aceitar este valor. Volta atrás.")
                assignment.remove(sensor)
                continue
            }

            // Próximos sensores a explorar: vizinhos não atribuídos
            if (unassignedNeighbors.isNotEmpty()) {
                for (neighbor in unassignedNeighbors) {
                    buildTree(neighbor, assignment, domains, currentCost + abs(value - sensor.desiredDutyCycle))
                }
            } else {
                // Se todos atribuídos, verificar custo total
                if (assignment.size == topology.sensors().size) {
                    val totalCost = currentCost + abs(value - sensor.desiredDutyCycle)
                    if (totalCost < bestCost) {
                        bestCost = totalCost
                        bestAssignment = assignment.toMap()
                        println("** Novo melhor assignment encontrado: ${bestAssignment!!.map { "${it.key.id}=${it.value}" }} com custo $bestCost **\n")
                    }
                } else {
                    // Atribuir próximo sensor ainda não usado
                    val nextSensor = topology.sensors().firstOrNull { it !in assignment }
                    if (nextSensor != null) {
                        buildTree(nextSensor, assignment, domains, currentCost + abs(value - sensor.desiredDutyCycle))
                    }
                }
            }

            // Backtrack
            assignment.remove(sensor)
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
fun main() {
    println("${(100/15).toInt()}")
    // ===================== SENSORES =====================
    val A = Sensor("A", desiredDutyCycle = 20.0, tolerance = 0.0) // 100/20 = 5
    val B = Sensor("B", desiredDutyCycle = 25.0, tolerance = 0.0) // 100/25 = 4
    val C = Sensor("C", desiredDutyCycle = 14.0, tolerance = 0.0) // 100/15 ≈ 6
    val D = Sensor("D", desiredDutyCycle = 10.0, tolerance = 0.0) // 100/10 = 10
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

    // ===================== OTIMIZAÇÃO =====================
    val optimizer = DutyCycleTreeOptimizer(topology, step = 1.0)
    val solution = optimizer.optimize()

    // ===================== RESULTADOS =====================
    println("=== SOLUÇÃO ÓTIMA ===")
    solution?.forEach { (sensor, value) ->
        println("Sensor ${sensor.id}: DutyCycle = $value%")
    }


}