package com.example.demo.domain

class Sensor(
    val id: String,
    val desiredDutyCycle: Double,
    val tolerance: Double = 0.0
)
{
    override fun toString(): String = id

}