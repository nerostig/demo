package com.example.demo.pipeline

import com.example.demo.domain.Sensor
import kotlin.collections.map


data class Schedule(
    val sensor: Sensor,
    val parameter: DutyCycleParameter?
)

data class DutyCycleParameter(
    val value: Double
)


data class TopologyScheduleResponse(
    //val id:Int,
    val sensors: List<SensorResultOutput>,
    val adjacency: Map<String, List<String>>,
    val message: String? = null,
    val performance: PerformanceMetrics?


)

data class PerformanceMetrics(
    val executionTimeMs: Long,
    val memoryUsedKb: Long
)

data class ScheduledTopologyOutput(
    val id:Int,
    val name:String?,
    val sensors: List<SensorResultOutput>,
    val adjacency: Map<String, List<String>>,
    val performance: PerformanceMetrics?

)


