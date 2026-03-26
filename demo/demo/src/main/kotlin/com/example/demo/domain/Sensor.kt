package com.example.demo.domain

class Sensor(
    val id: String,
    val desiredDutyCycle: Double,
    val tolerance: Double? = null
)
{
    override fun toString(): String = id

}