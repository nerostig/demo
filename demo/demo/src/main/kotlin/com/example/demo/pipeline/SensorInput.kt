package com.example.demo.pipeline

data class SensorInput(
    val id: String,
    val desiredDutyCycle:Int,
    val tolerance:Int
)