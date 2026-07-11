import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.optimizer.areCoprimePercentagess
import com.example.demo.optimizer.generateCandidates
import kotlin.random.Random

fun main() {

    val topology = generateRandomTopology(
        sensorCount = 30,
        maxNeighbors = 5,
        dutyCycleRange = 5.0..40.0,
        tolerance = 1.0,
        allCoprime = false
    )

    val tolerancias = topology.sensors().joinToString(", ") { it.tolerance.toString() }
    println("Tolerâncias dos sensores: $tolerancias")



    val optimizer = DutyCycleTreeOptimizer(topology)

    val result = optimizer.optimize()

    println("Resultado:")
    result?.forEach { (sensor, value) ->
        println("${sensor.id} = $value")
    }
}

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

    val sensors = (0 until sensorCount).map { i ->
        Sensor(
            id = "S$i",
            desiredDutyCycle = random
                .nextInt(dutyCycleRange.start.toInt(), dutyCycleRange.endInclusive.toInt() + 1)
                .toDouble(),
            tolerance = tolerance,
            groupid = null
        )
    }

    val adjacency = sensors.associateWith { mutableSetOf<Sensor>() }

    for (sensor in sensors) {
        val possibleNeighbors = sensors.filter { it != sensor }
        val neighborCount = random.nextInt(1, maxNeighbors + 1)
        val chosen = mutableListOf<Sensor>()

        for (neighborCandidate in possibleNeighbors.shuffled(random)) {
            if (chosen.size >= neighborCount) break

            val sensorDomain = generateCandidates(sensor)
            val neighborDomain = generateCandidates(neighborCandidate)

            val hasCoprime = if (allCoprime) {
                sensorDomain.all { sVal ->
                    neighborDomain.any { nVal -> areCoprimePercentagess(sVal.toDouble(), nVal.toDouble()) }
                }
            } else {
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

    val finalAdjacency = adjacency.mapValues { it.value.toList() }

    return NetworkTopology(finalAdjacency)
}


