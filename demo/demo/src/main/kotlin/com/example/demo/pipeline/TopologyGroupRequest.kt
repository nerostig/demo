package com.example.demo.pipeline

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor

data class TopologyGroupRequest(
    val sensorGroups: List<SensorGroupInput>,
    val adjacency: Map<String, List<String>>
)


fun TopologyGroupRequest.toDomainGroups(): NetworkTopology {

//    val sensorMap = sensorGroups
//        .flatMap { group ->
//            group.sensorIds.map { id ->
//                id to Sensor(id, group.desiredDutyCycle, group.tolerance)
//            }
//        }
//        .toMap()


    val sensorMap = sensorGroups
        .flatMap { group ->
            group.sensors.map { s ->
                s.id to Sensor(
                    id = s.id,
                    desiredDutyCycle = group.desiredDutyCycle,
                    tolerance = group.tolerance,
                    x = s.x,
                    y = s.y
                )
            }
        }
        .toMap()

    val adjacencyMap: MutableMap<Sensor, MutableList<Sensor>> = mutableMapOf()

    sensorMap.values.forEach {
        adjacencyMap[it] = mutableListOf()
    }

    adjacency.forEach { (fromId, neighbors) ->

        val from = sensorMap[fromId]
            ?: error("Sensor '$fromId' não existe")

        neighbors.forEach { toId ->
            val to = sensorMap[toId]
                ?: error("Sensor '$toId' não existe")

            adjacencyMap[from]!!.add(to)
        }
    }

    return NetworkTopology(adjacencyMap)
}