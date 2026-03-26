package com.example.demo.pipeline

data class SensorGroupInput(
    val sensorIds: List<String>,
    val desiredDutyCycle: Double,
    val tolerance: Double
)