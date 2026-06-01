import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.optimizer.GlobalNogoodStore
import com.example.demo.optimizer.Nogood
import com.example.demo.optimizer.SearchContext
import com.example.demo.optimizer.areCoprime
import com.example.demo.optimizer.areCoprimePercentages
import com.example.demo.optimizer.dutyCycleToPeriod
import com.example.demo.optimizer.generateCandidates
import com.example.demo.optimizer.heuristics.impactOf
import com.example.demo.optimizer.heuristics.nullRisk
import com.example.demo.optimizer.heuristics.saturation
import com.example.demo.optimizer.violatesNogood
import com.example.demo.pipeline.DutyCycleParameter
import com.example.demo.pipeline.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt





// ===================== ÁRVORE DE DECISÃO =====================


class DutyCycleTreeOptimizer(private val topology: NetworkTopology, private val step: Double = 0.05) {

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






        sensors.map { startSensor ->
            async(Dispatchers.Default) {

                val ctx = SearchContext(globalNogoods)



                val assignment = mutableMapOf<Sensor, Double>()

                buildTree(
                    sensor = startSensor, //sensors.first(),
                    domains = domains,
                    assignment = assignment,
                    currentCost = 0.0,
                    countSensor = 1,
                    ctx = ctx
                )
            }
        }.awaitAll()




    // resultado global protegido
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
                impactOf(sensor, period, domains, assignment, ctx,topology)
            }.thenBy { period ->
                abs(round(100.0 / period) - sensor.desiredDutyCycle)
            }
        )




        for (period in sortedDomain) {
            val percentage = round(100.0 / period)//round(100.0 / period)
            assignment[sensor] = percentage





            if (violatesNogood(assignment, ctx)) {
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

                //  Aprendizagem local
                ctx.localNogoods.add(nogood)

                //  Aprendizagem global
                globalNogoods.add(nogood)

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
                        saturation(topology,s, assignment)   //  DSatur
                    }.thenBy { s ->
                        domains[s]?.size ?: Int.MAX_VALUE // MRV
                    }.thenByDescending { s ->
                        topology.neighbors(s).size //
                    }.thenByDescending { s ->
                        nullRisk(topology,s, assignment) //
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



// ===================== API =====================
fun computeSchedulesOptimized(topology: NetworkTopology): List<Schedule> {



    val optimizer = DutyCycleTreeOptimizer(topology)
    val solution = optimizer.optimize()




    return topology.sensors().map { sensor ->
        val value = solution?.get(sensor)
        if (value != null) Schedule(sensor, DutyCycleParameter(value)) else Schedule(sensor, null)
    }
}








