import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.optimizer.GlobalNogoodStore
import com.example.demo.optimizer.Nogood
import com.example.demo.optimizer.SearchContext
import com.example.demo.optimizer.areCoprimePercentages
import com.example.demo.optimizer.generateCandidates
import com.example.demo.optimizer.heuristics.impactOf
import com.example.demo.optimizer.heuristics.nullRisk
import com.example.demo.optimizer.heuristics.saturation
import com.example.demo.optimizer.violatesNogood
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.PerformanceMetrics
import com.example.demo.pipeline.Schedule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt

data class HeuristicConfig(
    val useMRV: Boolean = true,
    val useDSatur: Boolean = true,
    val useNullRisk: Boolean = true,
    val useImpact: Boolean = true,
    val useNogoodLearning: Boolean = true,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val useparelism: Boolean=true

)



// ===================== ÁRVORE DE DECISÃO =====================

class DutyCycleTreeOptimizer(
    val topology: NetworkTopology,
    private val config: HeuristicConfig = HeuristicConfig()) {

    private var bestCost = Double.MAX_VALUE

    private val globalNogoods = GlobalNogoodStore()
    private lateinit var orderedSensorsPair: List<Pair<Sensor, List<Sensor>>>

    private var bestNullCount = Int.MAX_VALUE
    private var bestAssignment: Map<Sensor, Double?>? = null
    private val bestLock = Any()


    fun optimize(): Map<Sensor, Double?>? = runBlocking {


        val sensors = topology.sensors()
            .sortedWith(
                compareBy<Sensor> { generateCandidates(it).size }
                    .thenBy { topology.neighbors(it).size }
            )

        val domains = sensors
            .associateWith { generateCandidates(it).toMutableList() }
            .toMutableMap()

        orderedSensorsPair = topology.sensors()
            .map { sensor ->
                val orderedNeighbors = topology.neighbors(sensor)
                    .sortedWith(
                        compareBy<Sensor> { neighbor ->
                            domains[neighbor]?.size ?: Int.MAX_VALUE
                        }.thenByDescending { neighbor ->
                            topology.neighbors(neighbor).size
                        }
                    )
                sensor to orderedNeighbors
            }
            .sortedWith(
                compareBy<Pair<Sensor, List<Sensor>>> {
                    domains[it.first]?.size ?: Int.MAX_VALUE
                }.thenByDescending {
                    it.second.size
                }
            )


        sensors
            .map { startSensor ->

                async(config.dispatcher) {

                    val ctx = SearchContext(globalNogoods)
                    val assignment = mutableMapOf<Sensor, Double>()

                    buildTree(
                        sensor = startSensor,
                        domains = domains,
                        assignment = assignment,
                        currentCost = 0.0,
                        countSensor = 1,
                        ctx = ctx
                    )
                }
            }
            .awaitAll()


        bestAssignment
    }

    private fun tryUpdate(
        assignment: Map<Sensor, Double>,
        cost: Double
    ) {
        val rms = sqrt(cost / assignment.size)
        val nulls = topology.sensors().size - assignment.size

        synchronized(bestLock) {
            val better =
                (nulls < bestNullCount) ||
                        (nulls == bestNullCount && rms < bestCost)

            if (better) {
                bestNullCount = nulls
                bestCost = rms
                bestAssignment = topology.sensors().associateWith { assignment[it] }
            }
        }
    }



    private fun buildTree(
        sensor: Sensor,
        domains: MutableMap<Sensor, MutableList<Int>>,
        assignment: MutableMap<Sensor, Double>,
        currentCost: Double,
        countSensor: Int,
        ctx: SearchContext

    ) {


        val domain = domains[sensor] ?: return



        val sortedDomain = domain.sortedWith(
            compareBy<Int> { period ->
                if (config.useImpact)
                    impactOf(sensor, period, domains, assignment, ctx, topology)
                else 0.0
            }.thenBy { period ->
                abs(round(100.0 / period) - sensor.desiredDutyCycle)
            }
        )

        for (period in sortedDomain) {
            val percentage = round(100.0 / period)//round(100.0 / period)
            assignment[sensor] = percentage






            if (config.useNogoodLearning && violatesNogood(sensor,assignment, ctx)) {
                assignment.remove(sensor)
                continue
            }



            val error = percentage - sensor.desiredDutyCycle
            val newSquaredCost = currentCost + (error * error)
            val rmsPartial = sqrt(newSquaredCost / assignment.size.toDouble())


            if (bestAssignment != null && bestNullCount == 0 && rmsPartial > bestCost) {
                assignment.remove(sensor)
                continue
            }


            // Verificação de coprimalidade local
            val valid = topology.neighbors(sensor)
                .filter { it in assignment }
                .all { neighbor ->
                    assignment[neighbor]!=percentage
                    &&(areCoprimePercentages(percentage, assignment[neighbor]!!,ctx))
                }

            if (!valid) {

                val conflict = topology.neighbors(sensor)
                    .filter { it in assignment }
                    .associateWith { assignment[it]!! }
                    .toMutableMap()

                conflict[sensor] = percentage

                val nogood = Nogood(conflict)


                if (config.useNogoodLearning ) {


                    //  Aprendizagem local

                    ctx.addLocalNogood(nogood)
                    //  Aprendizagem global

                    globalNogoods.add(nogood)



                }

                assignment.remove(sensor)
                continue
            }





            tryUpdate(assignment, newSquaredCost)






            val nextSensors = orderedSensorsPair
                .first { it.first == sensor }
                .second
                .asSequence()
                .filter { it !in assignment }
                .sortedWith(
                    compareByDescending<Sensor> { s ->
                        if (config.useDSatur) saturation(topology,s, assignment) else 0.0   //  DSatur
                    }.thenBy { s ->
                        if (config.useMRV) domains[s]?.size ?: Int.MAX_VALUE else 0 // MRV
                    }.thenByDescending { s ->
                        topology.neighbors(s).size //
                    }.thenByDescending { s ->
                        if (config.useNullRisk) nullRisk(topology,s, assignment) else 0//
                    }
                )
                .toList()



            if (nextSensors.isNotEmpty()) {

                for (next in nextSensors) {

                    buildTree(next, domains, assignment, newSquaredCost, countSensor + 1,ctx)
                }


            } else if (nextSensors.isEmpty() && countSensor != topology.sensors().size) {
                return
            }



            assignment.remove(sensor)
        }
    }







}

 fun usedMemoryKb(): Long {
    val rt = Runtime.getRuntime()
    return (rt.totalMemory() - rt.freeMemory()) / 1024
}

// ===================== API =====================
fun computeSchedulesOptimized(topology: NetworkTopology, config: HeuristicConfig = HeuristicConfig()): Pair<List<Schedule>, PerformanceMetrics> {


    val memBefore = usedMemoryKb()
    val start = System.nanoTime()


    val optimizer = DutyCycleTreeOptimizer(topology,config)
    val solution = optimizer.optimize()

    val end = System.nanoTime()
    val memAfter = usedMemoryKb()

    val metrics = PerformanceMetrics(
        executionTimeMs = (end - start) / 1_000_000,
        memoryUsedKb = memAfter - memBefore
    )


    val schedules= topology.sensors().map { sensor ->
        val value = solution?.get(sensor)
        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
    }

    return Pair(schedules,metrics)


}








