package com.example.demo.pipeline

data class SensorOutput(
    val Id:String,
    val dutyCycleFinal:Int
)

data class SensorResultOutput(
    val id: String,
    val dutyCycleParameter: Double?
)

data class LinkOutput(
    val from: String,
    val to: String
)

//data class TopologyScheduleResponse(
//    val sensors: List<SensorResultOutput>,
//    val links: List<LinkOutput>
//)