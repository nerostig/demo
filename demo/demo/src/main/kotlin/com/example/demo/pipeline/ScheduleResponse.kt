package com.example.demo.pipeline

import com.example.demo.domain.Sensor
import kotlin.collections.map


data class Schedule(
    val sensor: Sensor,
    val parameter: DutyCycleParameter?
)

data class DutyCycleParameter(
    val value: Int
)


data class TopologyScheduleResponse(
    val sensors: List<SensorResultOutput>,
    val links: List<LinkOutput>
) {
    companion object {

//        fun from(
//            schedules: List<Schedule>,
//            originalRequest: TopologyRequest
//        ): TopologyScheduleResponse {
//
//            val sensorsOutput = schedules.map {
//                SensorResultOutput(
//                    id = it.sensor.id,
//                    dutyCycleParameter = it.parameter.value
//                )
//            }
//
//            val linksOutput = originalRequest.links.map {
//                LinkOutput(it.from, it.to)
//            }
//
//            return TopologyScheduleResponse(
//                sensors = sensorsOutput,
//                links = linksOutput
//            )
//        }
    }
}

