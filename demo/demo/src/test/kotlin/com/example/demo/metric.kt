package com.example.demo


import HeuristicConfig
import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.pipeline.PerformanceMetrics
import com.example.demo.pipeline.Schedule
import computeSchedulesOptimized
import generateRandomTopology
import kotlinx.coroutines.Dispatchers
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.io.File



fun saveTopology(topology: NetworkTopology, file: File) {

    val sensors = topology.sensors().joinToString("\n") { s ->
        "${s.id},${s.desiredDutyCycle},${s.tolerance}"
    }

    val edges = topology.sensors().joinToString("\n") { s ->
        val neighbors = topology.neighbors(s)
            .joinToString(",") { it.id }

        "${s.id}:$neighbors"
    }

    file.writeText(
        "# sensors\n$sensors\n\n# edges\n$edges"
    )
}


fun loadTopology(file: File): NetworkTopology {

    val lines = file.readLines()

    val sensorMap = mutableMapOf<String, Sensor>()
    val adjacency = mutableMapOf<String, List<String>>()

    var mode = ""

    for (line in lines) {
        if (line.isBlank()) continue

        when {
            line.startsWith("# sensors") -> mode = "sensors"
            line.startsWith("# edges") -> mode = "edges"

            mode == "sensors" -> {
                val parts = line.split(",")
                val sensor = Sensor(
                    id = parts[0],
                    desiredDutyCycle = parts[1].toDouble(),
                    tolerance = parts[2].toDouble()
                )
                sensorMap[sensor.id] = sensor
            }

            mode == "edges" -> {
                val (id, neigh) = line.split(":")
                val neighbors = if (neigh.isBlank()) emptyList()
                else neigh.split(",")

                adjacency[id] = neighbors
            }
        }
    }

    val finalAdjacency: Map<Sensor, List<Sensor>> =
        sensorMap.values.associateWith { sensor ->
            adjacency[sensor.id]
                ?.mapNotNull { sensorMap[it] }
                ?: emptyList()
        }

    return NetworkTopology(finalAdjacency)
}

fun main() {
    val topology = generateRandomTopology(
        sensorCount = 40,
        maxNeighbors = 4,
        dutyCycleRange = 5.0..40.0,
        tolerance = 0.5,
        allCoprime = false)

    val file = File(
        """C:\Users\Utilizador\Desktop\PF\demo\demo\src\test\kotlin\com\example\demo\topology.txt"""
    )

    saveTopology(topology,file)

}



object BenchmarkLogger {

    private val file = File(
        """C:\Users\Utilizador\Desktop\PF\demo\demo\src\test\kotlin\com\example\corrotinas2.txt"""
    )
    @Synchronized
    fun log(
        title: String,
        timeMs: Long,
        memoryKb: Long,
        nulls: Int
    ) {
        file.appendText(
            """
            
            === $title ===
            Time: $timeMs ms
            Memory: $memoryKb KB
            Nulls: $nulls
            """.trimIndent() + "\n"
        )
    }
}
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class NogoodLearningBenchmark() {

    private val file = File(
        "C:\\Users" +
                "\\Utilizador\\Desktop\\PF\\demo\\demo\\src" +
                "\\test\\kotlin\\com\\example\\demo\\teste1.txt"
    )








    private lateinit var topology: NetworkTopology



    @Setup(Level.Trial)
    fun setup() {
        topology = loadTopology(
            File("C:\\Users\\Utilizador\\Desktop\\PF\\demo\\demo\\src\\test\\kotlin\\com\\example\\demo\\topology.txt")
        )

    }





    private fun printResult(
        title: String,
        result: Pair<List<Schedule>, PerformanceMetrics>
    ) {
        val memoryUsed = maxOf(0L, result.second.memoryUsedKb)

        println("\n=== $title ===")
        println("Time: ${result.second.executionTimeMs} ms")
        println("Memory: $memoryUsed KB")
        println("Nulls: ${result.first.count { it.parameter == null }}")

        BenchmarkLogger.log(
            title = title,
            timeMs = result.second.executionTimeMs,
            memoryKb = memoryUsed,
            nulls = result.first.count { it.parameter == null }
        )
    }
//
    @Benchmark
    fun withoutNogood(): Pair<List<Schedule>, PerformanceMetrics> {
        println("sensor 1 ${topology.sensors().first().id}")

        val tolerancias = topology.sensors()
            .joinToString(", ") { it.desiredDutyCycle.toString() }

        println("Tolerâncias dos sensores: $tolerancias")

        val result = computeSchedulesOptimized(
            topology,
            HeuristicConfig(useNogoodLearning = true , useparelism = false)
        )

        printResult("WITHOUT NOGOOD", result)



        return result
    }
val config = HeuristicConfig(
    dispatcher = Dispatchers.Default.limitedParallelism(8)
)

    @Benchmark
    fun withNogood(): Pair<List<Schedule>, PerformanceMetrics> {
        println("sensor 1 ${topology.sensors().first().id}")

        val tolerancias = topology.sensors()
            .joinToString(", ") { it.desiredDutyCycle.toString() }

        println("Tolerâncias dos sensores: $tolerancias")

      //  val parallelism = 7


        val result = computeSchedulesOptimized(
            topology,
            HeuristicConfig(useNogoodLearning = true, useparelism = true)
        )




        printResult("WITH NOGOOD", result)


        return result
    }

//    @Benchmark
//    fun withNogoodsemPareleimo (): Pair<List<Schedule>, PerformanceMetrics> {
//        println("sensor 1 ${topology.sensors().first().id}")
//
//        val tolerancias = topology.sensors()
//            .joinToString(", ") { it.desiredDutyCycle.toString() }
//
//        println("Tolerâncias dos sensores: $tolerancias")
//
//
//
//        val result = computeSchedulesOptimized(
//            topology,
//            HeuristicConfig(useNogoodLearning = true, useparelism = false)
//        )
//
//
//
//
//        printResult("WITH NOGOOD SEM pralismo ", result)
//
//
//        return result
//    }



}

