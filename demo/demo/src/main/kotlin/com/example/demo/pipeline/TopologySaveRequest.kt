package com.example.demo.pipeline

data class TopologySaveRequest(
    val id: Int?,
    val sensors: List<SensorSaveInput>,
    val adjacency: Map<String, List<String>>
)

data class SensorSaveInput(
    val id: String,
    val x: Int,
    val y: Int,
    val desiredDutyCycle: Double,
    val tolerance: Double,
    val groupId: String? = null
)