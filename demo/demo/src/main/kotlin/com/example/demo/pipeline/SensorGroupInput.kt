package com.example.demo.pipeline

data class SensorGroupInput(
    val sensorIds: List<String>,
    val desiredDutyCycle: Int,
    val tolerance: Int
)