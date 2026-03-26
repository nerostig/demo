package com.example.demo.pipeline

data class SensorInput(
    val id: String,
    val desiredDutyCycle:Double,
    val tolerance:Double
)