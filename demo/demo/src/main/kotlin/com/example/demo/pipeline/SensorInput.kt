package com.example.demo.pipeline

data class SensorInput(
    val id: String,
    val x:Int?,
    val y:Int?,
    val desiredDutyCycle:Double,
    val tolerance:Double
)