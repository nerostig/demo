//package com.example.demo
//
//import com.example.demo.domain.NetworkTopology
//import com.example.demo.domain.Sensor
//import com.example.demo.pipeline.DutyCycleParameter
//import com.example.demo.pipeline.Schedule
//
//fun computeSchedulesForGroups(
//    topology: NetworkTopology,
//    baseParameters: Map<Sensor, DutyCycleParameter>,
//    defaultTolerance: Int = 2
//): List<Schedule> {
//
//    // 1️⃣ Coloração dos sensores (heurística)
//    val colors = colorGraph(topology)
//
//    // 2️⃣ Map de duty cycles atribuídos
//    val valuesPerSensor = mutableMapOf<Sensor, DutyCycleParameter?>()
//
//    // 3️⃣ Sensores ordenados por cor e id (heurística)
//    val sensorsOrdered = colors.entries
//        .sortedWith(compareBy({ it.value }, { it.key.id }))
//        .map { it.key }
//
//    for (sensor in sensorsOrdered) {
//
//        // duty cycles de vizinhos de cores diferentes
//        val neighborParameters = topology.neighbors(sensor)
//            .filter { colors[it] != colors[sensor] }  // apenas cores diferentes
//            .mapNotNull { valuesPerSensor[it]?.value }
//
//        val desiredValue = baseParameters[sensor]?.value ?: sensor.desiredDutyCycle
//        val tolerance = sensor.tolerance ?: defaultTolerance
//        val minValue = desiredValue - tolerance
//        val maxValue = desiredValue + tolerance
//
//        var assignedValue: Int? = null
//
//        // 1️⃣ tentar dentro da tolerância
//        for (candidate in minValue..maxValue) {
//            if (neighborParameters.all { areCoprime(candidate, it) }) {
//                assignedValue = candidate
//                break
//            }
//        }
//
//        // 2️⃣ fallback: usar valor de outro sensor da mesma cor (coprime com vizinhos)
//        if (assignedValue == null) {
//            val sameColorValues = valuesPerSensor
//                .filterKeys { colors[it] == colors[sensor] }
//                .mapNotNull { it.value?.value }
//
//            val fallback = sameColorValues.firstOrNull { candidate ->
//                neighborParameters.all { areCoprime(candidate, it) }
//            }
//
//            if (fallback != null) {
//                assignedValue = fallback
//            }
//        }
//
//        // 3️⃣ fallback final: valor base do grupo (se compatível com vizinhos)
//        if (assignedValue == null && neighborParameters.all { areCoprime(desiredValue, it) }) {
//            assignedValue = desiredValue
//        }
//
//        // 4️⃣ se mesmo assim falhar -> null
//        valuesPerSensor[sensor] = assignedValue?.let { DutyCycleParameter(it) }
//    }
//
//    // 5️⃣ construir schedules
//    return valuesPerSensor.map { (sensor, param) ->
//        Schedule(sensor, param)
//    }
//}
//
//
////
////import com.example.demo.domain.NetworkTopology
////import com.example.demo.domain.Sensor
////import com.example.demo.pipeline.DutyCycleParameter
////import com.example.demo.pipeline.Schedule
////fun assignParametersToColorscccc(
////    topology: NetworkTopology,
////    colors: Map<Sensor, Int>,
////    defaultTolerance: Int = 2
////): Map<Int, DutyCycleParameter> {
////
////    val colorAdj = buildColorAdjacency(topology, colors)
////    val colorParameters = mutableMapOf<Int, DutyCycleParameter>()
////
////    val colorsOrdered = colors.values.toSet().sorted()
////
////    for (color in colorsOrdered) {
////
////        val sensorsOfColor = colors
////            .filterValues { it == color }
////            .keys
////
////        println("vvvvvvvv Color $color -> ${sensorsOfColor.map { it.id }}")
////
////        val referenceSensor = sensorsOfColor.first()
////        println("${referenceSensor.id}")
////
////        val tolerance = referenceSensor.tolerance ?: defaultTolerance
////
////
////        val minValue = referenceSensor.desiredDutyCycle - tolerance
////        val maxValue = referenceSensor.desiredDutyCycle + tolerance
////
////        val neighborColors = colorAdj[color] ?: emptySet()
////
////        val neighborParameters = neighborColors
////            .mapNotNull { colorParameters[it]?.value }
////
////        var candidate = minValue
////
////        while (true) {
////
////            val valid = neighborParameters.all {
////                areCoprime(candidate, it)
////            }
////
////            if (valid) {
////                colorParameters[color] = DutyCycleParameter(candidate)
////                break
////            }
////
////            candidate++
////
////            if (candidate > maxValue + 10000) {
////                throw RuntimeException("Não foi possível encontrar parâmetro coprimo")
////            }
////        }
////    }
////
////    return colorParameters
////}
////
////
////fun assignParametersToColorsForGroups(
////    topology: com.example.demo.domain.NetworkTopology,
////    colors: Map<com.example.demo.domain.Sensor, Int>,
////    baseParameters: Map<com.example.demo.domain.Sensor, com.example.demo.pipeline.DutyCycleParameter>,
////    defaultTolerance: Int = 2
////): Map<Int, com.example.demo.pipeline.DutyCycleParameter> {
////
////    val colorAdj = buildColorAdjacency(topology, colors)
////    val colorParameters = mutableMapOf<Int, com.example.demo.pipeline.DutyCycleParameter>()
////
////    val colorsOrdered = colors.values.toSet().sorted()
////
////    for (color in colorsOrdered) {
////
////        val sensorsOfColor = colors
////            .filterValues { it == color }
////            .keys
////
////        val referenceSensor = sensorsOfColor.first()
////
////        val baseValue =
////            baseParameters[referenceSensor]?.value
////                ?: referenceSensor.desiredDutyCycle
////
////        val tolerance = referenceSensor.tolerance ?: defaultTolerance
////
////        val minValue = baseValue - tolerance
////        val maxValue = baseValue + tolerance
////
////        val neighborColors = colorAdj[color] ?: emptySet()
////        val neighborParameters =
////            neighborColors.mapNotNull { colorParameters[it]?.value }
////
////        var candidate = minValue
////
////        while (true) {
////
////            if (neighborParameters.all { areCoprime(candidate, it) }) {
////                colorParameters[color] = DutyCycleParameter(candidate)
////                break
////            }
////
////            candidate++
////
////            if (candidate > maxValue + 10_000) {
////                throw RuntimeException("Não foi possível encontrar parâmetro coprimo")
////            }
////        }
////    }
////
////    return colorParameters
////}
////
////fun computeSchedulesForGroups(
////    topology: NetworkTopology,
////    baseParameters: Map<Sensor, DutyCycleParameter>
////): List<com.example.demo.pipeline.Schedule> {
////
////    val colors = colorGraph(topology)
////
////    val colorParameters =
////        assignParametersToColorsForGroups(
////            topology,
////            colors,
////            baseParameters
////        )
////
////    return colors.map { (sensor, color) ->
////        Schedule(sensor, colorParameters[color]!!)
////    }
////}