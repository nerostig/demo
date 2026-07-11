package com.example.demo.pipeline


import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.Sensor



fun TopologySaveRequest.toDomain(): ScheduledNetworkTopology {

    val sensors = sensors.map {
        Sensor(
            id = it.id,
            groupid = it.groupId,
            x = it.x,
            y = it.y,
            desiredDutyCycle = it.desiredDutyCycle,
            tolerance = it.tolerance
        )
    }

    val sensorById = sensors.associateBy { it.id }

    val domainAdjacency: Map<Sensor, List<Sensor>> =
        adjacency.map { (sourceId, targetIds) ->

            val sourceSensor =
                sensorById[sourceId]
                    ?: throw IllegalArgumentException("Sensor $sourceId não existe")

            val targetSensors =
                targetIds.map { targetId ->
                    sensorById[targetId]
                        ?: throw IllegalArgumentException("Sensor $targetId não existe")
                }

            sourceSensor to targetSensors
        }.toMap()

    val dutyCycles: Map<Sensor, Double?> =
        sensors.associateWith { it.desiredDutyCycle }

    return ScheduledNetworkTopology(
        name= this.name,
        adjacency = domainAdjacency,
        dutyCycles = dutyCycles
    )
}