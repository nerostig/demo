//import com.example.demo.domain.NetworkTopology
//import com.example.demo.domain.Sensor
//import com.example.demo.pipeline.DutyCycleParameter
//import com.example.demo.pipeline.Schedule
//import kotlin.math.pow
//
//data class Fraction(val numerator: Int, val denominator: Int)
//
//fun gcd(a: Int, b: Int): Int {
//    var x = kotlin.math.abs(a)
//    var y = kotlin.math.abs(b)
//    println(x)
//    println(y)
//
//
//    while (y != 0) {
//        val temp = y
//        y = x % y
//        x = temp
//    }
//
//    return x
//}
//
//fun areCoprime(a: Int, b: Int): Boolean {
//    return gcd(a, b) == 1
//}
//
//
//fun dutyCycleToPeriod(dc: Double): Int {
//    require(dc > 0) { "Duty cycle deve ser positivo" }
//    return kotlin.math.round(100.0 / dc).toInt()
//}
//
//fun areCoprimePercentages(p1: Double, p2: Double): Boolean {
//    val period1 = dutyCycleToPeriod(p1)
//    val period2 = dutyCycleToPeriod(p2)
//    return areCoprime(period1, period2)
//}
//
//fun colorGraph(topology: NetworkTopology): Map<Sensor, Int> {
//    val colors = mutableMapOf<Sensor, Int>()
//
//    val sensorsOrdered = topology
//        .sensors()
//        .sortedByDescending { topology.neighbors(it).size }
//
//    for (sensor in sensorsOrdered) {
//        val neighborColors = topology
//            .neighbors(sensor)
//            .mapNotNull { colors[it] }
//            .toSet()
//
//        var color = 1
//        while (neighborColors.contains(color)) {
//            color++
//        }
//
//        colors[sensor] = color
//    }
//
//    return colors
//}
//
//fun assignParametersToSensors(
//    topology: NetworkTopology,
//    colors: Map<Sensor, Int>,
//    defaultTolerance: Double = 2.0,
//    step: Double = 0.01
//): Map<String, DutyCycleParameter?> {
//
//    val valuesPerSensor = mutableMapOf<Sensor, DutyCycleParameter?>()
//
//    val sensorsOrdered =
//        colors.entries
//            .sortedWith(compareBy({ it.value }, { it.key.id }))
//            .map { it.key }
//
//    for (sensor in sensorsOrdered) {
//
//        val neighborParameters =
//            topology.neighbors(sensor)
//                .filter { colors[it] != colors[sensor] }
//                .mapNotNull { valuesPerSensor[it]?.value?.toDouble() }
//
//        val tolerance = sensor.tolerance ?: defaultTolerance
//        val minValue = sensor.desiredDutyCycle - tolerance
//        val maxValue = sensor.desiredDutyCycle + tolerance
//
//        println(
//            "Sensor ${sensor.id} vizinhos=${topology.neighbors(sensor).map { it.id }} " +
//                    "intervalo=[$minValue,$maxValue]"
//        )
//
//        var assignedValue: Double? = null
//
//        var candidate = minValue
//        while (candidate <= maxValue + 1e-9) {
//            if (neighborParameters.all { areCoprimePercentages(candidate, it) }) {
//                assignedValue = candidate
//                println("  -> atribuído $candidate (tolerância)")
//                break
//            }
//            candidate += step
//        }
//
//        if (assignedValue == null) {
//
//            val sameColorValues =
//                valuesPerSensor
//                    .filterKeys { colors[it] == colors[sensor] }
//                    .mapNotNull { it.value?.value?.toDouble() }
//
//            val fallback =
//                sameColorValues.firstOrNull { candidateValue ->
//                    neighborParameters.all { areCoprimePercentages(candidateValue, it) }
//                }
//
//            if (fallback != null) {
//                assignedValue = fallback
//                println("  -> fallback mesma cor: $fallback")
//            }
//        }
//
//        if (assignedValue != null) {
//            valuesPerSensor[sensor] = DutyCycleParameter(assignedValue)
//        } else {
//            valuesPerSensor[sensor] = null
//            println("  -null)")
//        }
//    }
//
//    return valuesPerSensor.mapKeys { it.key.id }
//}
//
//fun computeScheduless(
//    topology: NetworkTopology
//): List<Schedule> {
//
//    val colors = colorGraph(topology)
//    val colorParameters = assignParametersToSensors(topology, colors)
//
//    return colors
//        .mapNotNull { (sensor) ->
//            val param = colorParameters[sensor.id]
//            println(param)
//            if (param != null) {
//                Schedule(sensor, param)
//            } else {
//                Schedule(sensor, null)
//            }
//        }
//}
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
//
//// ===================== Classes de suporte =====================
//
////class Sensor(
////    val id: String,
////    val desiredDutyCycle: Double,
////    val tolerance: Double? = null
////) {
////    override fun toString(): String = id
////}
////
////class DutyCycleParameter(val value: Double)
////
////class Schedule(val sensor: Sensor, val parameter: DutyCycleParameter?)
////
////class NetworkTopology(
////    val adjacency: Map<Sensor, List<Sensor>>
////) {
////    fun neighbors(sensor: Sensor): List<Sensor> = adjacency[sensor] ?: emptyList()
////    fun sensors(): Set<Sensor> = adjacency.keys
////}
////
////class ScheduledNetworkTopology(
////    val adjacency: Map<Sensor, List<Sensor>>,
////    val dutyCycles: Map<Sensor, Double>
////)