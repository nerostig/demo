package com.example.demo.domain

import com.example.demo.pipeline.LinkOutput
import com.example.demo.pipeline.Schedule
import com.example.demo.pipeline.SensorResultOutput
import com.example.demo.pipeline.TopologyScheduleResponse
import org.springframework.stereotype.Component


@Component
class TopologyPlanner {

    fun applySchedules(
        topology: NetworkTopology,
        schedules: List<Schedule>
    ): ScheduledNetworkTopology {

        val dutyCycles = schedules.associate {
            it.sensor to it.parameter!!.value
        }

        return ScheduledNetworkTopology(
            adjacency = topology.adjacency,
            dutyCycles = dutyCycles
        )
    }
}
fun ScheduledNetworkTopology.toResponse(): TopologyScheduleResponse {

    val sensors = dutyCycles.map {
        SensorResultOutput(
            id = it.key.id,
            dutyCycleParameter = it.value
        )
    }

    val links = adjacency.flatMap { (from, neighbors) ->
        neighbors.map { to -> LinkOutput(from.id, to.id) }
    }.distinct()

    return TopologyScheduleResponse(sensors, links)
}