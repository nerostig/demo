import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import kotlin.random.Random

fun main() {

    val topology = generateRandomTopology(
        sensorCount = 30,
        maxNeighbors = 4,
        dutyCycleRange = 5.0..40.0,
        tolerance = 1.0,
        allCoprime = true// opcional, para testes reproduzíveis
    )
//
//    Exception in thread "main" java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects
//    topology.sensors().forEach { sensor ->
//        val neighbors = topology.neighbors(sensor)
//            .joinToString(", ") { it.id }
//
//        println("${sensor.id} -> [$neighbors] ->${sensor.desiredDutyCycle} sensortolerance-> ${sensor.tolerance}")
//    }


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

    // 1️ Criar sensores com domínio inicial
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


