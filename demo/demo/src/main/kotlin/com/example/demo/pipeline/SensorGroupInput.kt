package com.example.demo.pipeline

data class SensorGroupInput(
    val sensors: List<SensorInputGroup>,
// val sensorIds: List<String>,
    val desiredDutyCycle: Double,
    val tolerance: Double
)

data class SensorInputGroup(
    val id: String,
    val x: Int,
    val y: Int
)