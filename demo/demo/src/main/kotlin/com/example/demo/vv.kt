import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.Schedule
import kotlin.math.abs
import kotlin.math.round

// ===================== MODELO =====================
//data class Sensor(
//    val id: String,
//    val desiredDutyCycle: Double,
//    val tolerance: Double = 2.0
//)
//
//data class DutyCycleParameter(val value: Double)
//data class Schedule(val sensor: Sensor, val parameter: DutyCycleParameter?)

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
fun areCoprimePercentages(a: Double, b: Double): Boolean {
    // println("avvv ->$a")
    //println("b vvv->$b")


    return areCoprime(dutyCycleToPeriod(a), dutyCycleToPeriod(b))


}

// ===================== DOMÍNIO =====================
fun generateCandidatesff(sensor: Sensor, step: Double = 0.05): List<Double> {
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

fun generateCandidates(sensor: Sensor, step: Double = 0.05): List<Int> {
    val min = (100 / (sensor.desiredDutyCycle - sensor.tolerance)).toInt()
    val max = (100 / (sensor.desiredDutyCycle + sensor.tolerance)).toInt()
    val values = mutableListOf<Int>()
    var v = min
    while (v <= max) {
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
    // private var bestAssignment: Map<Sensor, Double>? = null

    private var bestNullCount = Int.MAX_VALUE
    private var bestAssignment: Map<Sensor, Double?>? = null

    val parentMap = mutableMapOf<Sensor, Sensor?>()



    /*
        fun optimizedd(): Map<Sensor, Double>? {
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

        private fun buildTreedd(
            sensor: Sensor,
            assignment: MutableMap<Sensor, Double>,
            domains: MutableMap<Sensor, MutableList<Int>>,
            currentCost: Double
        ) {
            val domain = domains[sensor] ?: return

            for (value in domain.toList()) {

                val percentage = round(100.0 / value)   // converte para duty cycle em %

                assignment[sensor] = percentage//value
                println("Tentando sensor ${sensor.id} = $value  e percentagem $percentage")
                println("Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}\n")

                var valid = true

                // Verificação de vizinhos já atribuídos
                for (neighbor in topology.neighbors(sensor)) {
                    if (assignment.containsKey(neighbor)) {
                        val coprime = areCoprimePercentages(percentage, assignment[neighbor]!!)
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
                        val hasCompatible = neighborDomain.any { areCoprimePercentages(percentage, round(100.0 / it)) }
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

        */


//    fun optimize(): Map<Sensor, Double?>? {
//        val sensors = topology.sensors().sortedBy { it.id }
//
//        for (startSensor in sensors) {
//            println("start $startSensor")
//            val domains = sensors.associateWith {
//                generateCandidates(it, step).toMutableList()
//            }.toMutableMap()
//
//            buildTree(startSensor,parent = null, mutableMapOf(), domains, 0.0)
//        }
//        return bestAssignment
//    }

    fun optimize(): Map<Sensor, Double?>? {
        val sensors = topology.sensors().sortedBy { it.id }

        for (startSensor in sensors) {
            println("start ${startSensor.id}")
            val domains = sensors.associateWith { generateCandidates(it, step).toMutableList() }
                .toMutableMap()

            val rootNode = Node(sensor = startSensor, parent = null)
            buildTree(rootNode, domains, mutableMapOf(), 0.0)
        }

        return bestAssignment
    }


    /*
    fun optimize(): Map<Sensor, Double?>? {
        val sensors = topology.sensors().sortedBy { it.id }

        for (startSensor in sensors) {
            println("start ${startSensor.id}")

            val domains = sensors.associateWith {
                generateCandidates(it, step).toMutableList()
            }.toMutableMap()

            val assignment = mutableMapOf<Sensor, Double>()
            val parentMap = mutableMapOf<Sensor, Sensor?>()

            parentMap[startSensor] = null

            buildTree(
                sensor = startSensor,
                assignment = assignment,
                domains = domains,
                parentMap = parentMap,
                currentCost = 0.0
            )
        }

        return bestAssignment
    }
    */


    private fun evaluateSolution(
        assignment: Map<Sensor, Double>,
        cost: Double
    ) {
        val sensors = topology.sensors()
        val nullCount = sensors.size - assignment.size

        println(">> Avaliar solução parcial")
        println("   Assignment atual: ${assignment.map { "${it.key.id}=${it.value}" }}")
        println("   Nulls = $nullCount | Custo = $cost")
        println("   Melhor até agora: nulls=$bestNullCount custo=$bestCost")

        val better =
            (nullCount < bestNullCount )|| (nullCount == bestNullCount && cost < bestCost)

        println("   É melhor? -> $better")

        if (better) {
            bestNullCount = nullCount
            bestCost = cost
            bestAssignment = sensors.associateWith { assignment[it] }

            println(
                ">>> ** NOVA MELHOR SOLUÇÃO **\n" +
                        "    nulls=$bestNullCount custo=$bestCost\n" +
                        "    solução=${bestAssignment!!.map { "${it.key.id}=${it.value}" }}\n"
            )
        }
    }


    /*
    private fun buildTree(
        sensor: Sensor,
        assignment: MutableMap<Sensor, Double>,
        domains: MutableMap<Sensor, MutableList<Int>>,
        parentMap: MutableMap<Sensor, Sensor?>,
        currentCost: Double
    ) {
        val domain = domains[sensor] ?: return

        println("\n--> Explorar sensor ${sensor.id}")
        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")

        for (period in domain.toList()) {

            val percentage = round(100.0 / period)
            println("    Tentativa: ${sensor.id} = $percentage% (período=$period)")

            assignment[sensor] = percentage

            // ---------- verificação local ----------
            var valid = true
            for (neighbor in topology.neighbors(sensor)) {
                if (assignment.containsKey(neighbor)) {
                    val coprime =
                        areCoprimePercentages(percentage, assignment[neighbor]!!)
                    println(
                        "      Verificar coprimalidade com ${neighbor.id}=${assignment[neighbor]} -> $coprime"
                    )
                    if (!coprime) {
                        println("      ❌ Falha coprimalidade")
                        valid = false
                        break
                    }
                }
            }

            if (!valid) {
                assignment.remove(sensor)
                continue
            }

            val newCost = currentCost + abs(percentage - sensor.desiredDutyCycle)
            println("      ✔ Valor válido | custo acumulado=$newCost")

            evaluateSolution(assignment, newCost)

            // ---------- expansão normal ----------
            val unassignedNeighbors =
                topology.neighbors(sensor).filter { it !in assignment }

            if (unassignedNeighbors.isNotEmpty()) {
                println(
                    "      Vizinhos não atribuídos: ${unassignedNeighbors.map { it.id }}"
                )

                for (neighbor in unassignedNeighbors) {
                    parentMap[neighbor] = sensor
                    buildTree(
                        sensor = neighbor,
                        assignment = assignment,
                        domains = domains,
                        parentMap = parentMap,
                        currentCost = newCost
                    )
                }
            }
            // ---------- BACKTRACKING EM CADEIA ----------
            else {
                if(assignment.size!=topology.sensors().size){
                println("      Sensor ${sensor.id} sem mais vizinhos locais")
                var current: Sensor? = sensor
                var parent: Sensor? = parentMap[current]

                println("pai $parent de current $current")


//                var current: Sensor? = sensor
//                var parent: Sensor? = parentMap[current]
//
                while (parent != null) {
                    println("pai $parent de current $current")

                    val siblings =
                        topology.neighbors(parent)
                            .filter { it !in assignment && it != current }

                    if (siblings.isNotEmpty()) {
                        println("   Subida até ${parent.id}, explorar irmãos ${siblings.map { it.id }}")

                        for (sibling in siblings) {
                            parentMap[sibling] = parent
                            buildTree(
                                sensor = sibling,
                                assignment = assignment,
                                domains = domains,
                                parentMap = parentMap,
                                currentCost = newCost
                            )
                        }
                        break
                    }

                    // continua a subir
                    current = parent
                    parent = parentMap[current]
                }

                if (parent == null) {
                    println("      ⚠ Subida completa até à raiz — sem mais expansão")
                }
            }
            }

            println("    Backtrack: remover ${sensor.id}")
            assignment.remove(sensor)
        }
        //println("    Backtrack: remover ${sensor.id}")

        //assignment.remove(sensor)

    }

*/

    class Node(
        val sensor: Sensor,
        val parent: Node? = null,
        val children: MutableList<Node> = mutableListOf(),
        var percentage: Double? = null
    )

    private fun buildTree(node: Node, domains: MutableMap<Sensor, MutableList<Int>>, assignment: MutableMap<Sensor, Double>, currentCost: Double) {
        val sensor = node.sensor
        val domain = domains[sensor] ?: return

        println("\n--> Explorar sensor ${sensor.id}")
        println("    Assignment à entrada: ${assignment.map { "${it.key.id}=${it.value}" }}")

        for (period in domain.toList()) {
            val percentage = round(100.0 / period)
            assignment[sensor] = percentage
            node.percentage = percentage

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

            val newCost = currentCost + abs(percentage - sensor.desiredDutyCycle)
            evaluateSolution(assignment, newCost)

            val unassignedNeighbors = topology.neighbors(sensor).filter { it !in assignment }

            if (unassignedNeighbors.isNotEmpty()) {
                for (neighborSensor in unassignedNeighbors) {
                    val childNode = Node(sensor = neighborSensor, parent = node)
                    node.children.add(childNode)
                    buildTree(childNode, domains, assignment, newCost)
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
                            buildTree(siblingNode, domains, assignment, newCost)
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



}




//                println("      ✔ Sensor folha atingido (${sensor.id}), regressar")
//                val nextSensor =
//                    topology.sensors().firstOrNull { it !in assignment }
//                if (nextSensor != null) {
//                    println("      Próximo sensor global: ${nextSensor.id}")
//                    buildTree(nextSensor, topology.neighbors(nextSensor).first(), assignment, domains, newCost)
//                } else {
//                    println("      ⚠ Não há mais sensores para explorar")
//                }


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
            C to listOf(B, D),
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