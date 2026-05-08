package com.example.demo.domain

data class Sensor(
    val id: String,
    val groupid:String?=null,
    val x:Int?=null,
    val y :Int?=null,
    val desiredDutyCycle: Double,
    val tolerance: Double = 0.0
)
{
    override fun toString(): String = id

}



