package com.example.demo.pipeline

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor

data class TopologyGroupRequest(
    val sensorGroups: List<SensorGroupInput>,
    val links: List<Links>
)


fun TopologyGroupRequest.toDomainGroups(): Pair<NetworkTopology, Map<Sensor, DutyCycleParameter>> {

    val sensorMap = sensorGroups
        .flatMap { group ->
            group.sensorIds.map { id ->
                id to Sensor(id, group.desiredDutyCycle, group.tolerance)
            }
        }
        .associate { it }

    val adjacency = mutableMapOf<Sensor, MutableList<Sensor>>()
    sensorMap.values.forEach { adjacency[it] = mutableListOf() }

    links.forEach { link ->
        val a = sensorMap[link.from]!!
        val b = sensorMap[link.to]!!
        adjacency[a]?.add(b)
        adjacency[b]?.add(a)
    }

    // Map de duty cycles inicial, cada sensor com o do grupo
    val initialDutyCycles = sensorMap.values.associateWith {
        DutyCycleParameter(it.desiredDutyCycle!!)
    }

    return NetworkTopology(adjacency) to initialDutyCycles
}