package com.example.demo.domain

import com.example.demo.pipeline.LinkOutput
import com.example.demo.pipeline.PerformanceMetrics
import com.example.demo.pipeline.Schedule
import com.example.demo.pipeline.ScheduledTopologyOutput
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
            it.sensor to it.parameter?.value
        }

        return ScheduledNetworkTopology(
            adjacency = topology.adjacency,
            dutyCycles = dutyCycles
        )
    }
}
fun ScheduledNetworkTopology.toResponse(metrics: PerformanceMetrics?): TopologyScheduleResponse {

    val sensors = dutyCycles.map {
        SensorResultOutput(
            id = it.key.id,
            grouId = it.key.groupid,
            x=it.key.x,
            y=it.key.y,
            dutyCycleParameter = it.value
        )
    }

    val adjacencyOut = adjacency.mapKeys { it.key.id }
        .mapValues { (_, neighbors) ->
            neighbors.map { it.id }
        }

    val unassignedSensors = dutyCycles
        .filter { it.value == null }
        .keys
        .map { it.id }

    val message = if (unassignedSensors.isEmpty()) {
        "All sensors successfully assigned"
    } else {
        "Sensors requiring new values: ${unassignedSensors.joinToString(", ")}"
    }

    return TopologyScheduleResponse(
        //id=id,
        sensors = sensors,
        adjacency = adjacencyOut,
        message = message,
        metrics
    )
}

fun ScheduledNetworkTopology.toOutput(id:Int,metrics: PerformanceMetrics?): ScheduledTopologyOutput {

    val sensors = dutyCycles.map {
        SensorResultOutput(
            id = it.key.id,
            grouId = it.key.groupid,
            x=it.key.x,
            y=it.key.y,
            dutyCycleParameter = it.value
        )
    }

    val adjacencyOut = adjacency
        .mapKeys { it.key.id }
        .mapValues { (_, neighbors) ->
            neighbors.map { it.id }
        }

    return ScheduledTopologyOutput(
        id=id,
        name=this.name,
        sensors = sensors,
        adjacency = adjacencyOut,
        metrics
    )
}