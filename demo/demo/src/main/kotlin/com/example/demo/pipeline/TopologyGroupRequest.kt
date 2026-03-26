package com.example.demo.pipeline

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor

data class TopologyGroupRequest(
    val sensorGroups: List<SensorGroupInput>,
    val links: List<Links>
)


fun TopologyGroupRequest.toDomainGroups(): NetworkTopology {

    // Criar sensores únicos a partir dos grupos
    val sensorMap: Map<String, Sensor> =
        sensorGroups
            .flatMap { group ->
                group.sensorIds.map { id ->
                    id to Sensor(
                        id = id,
                        desiredDutyCycle = group.desiredDutyCycle,
                        tolerance = group.tolerance
                    )
                }
            }
            .toMap()

    // Inicializar lista de adjacências
    val adjacency: MutableMap<Sensor, MutableList<Sensor>> = mutableMapOf()
    sensorMap.values.forEach { sensor ->
        adjacency[sensor] = mutableListOf()
    }

    // Criar ligações bidirecionais
    links.forEach { link ->
        val fromSensor = sensorMap[link.from]
            ?: error("Sensor '${link.from}' não existe")
        val toSensor = sensorMap[link.to]
            ?: error("Sensor '${link.to}' não existe")

        adjacency[fromSensor]!!.add(toSensor)
        adjacency[toSensor]!!.add(fromSensor)
    }

    return NetworkTopology(adjacency)
}