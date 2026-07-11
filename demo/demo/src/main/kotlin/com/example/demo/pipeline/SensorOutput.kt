package com.example.demo.pipeline



data class SensorResultOutput(
    val id: String,
    val grouId:String?,
    val x:Int?,
    val y:Int?,
    val dutyCycleParameter: Double?
)


