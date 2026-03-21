package com.example.demo.domain

class Sensor(
    val id: String,
    val desiredDutyCycle:Int,
    val tolerance: Int? = null
)
{
    override fun toString(): String = id

}