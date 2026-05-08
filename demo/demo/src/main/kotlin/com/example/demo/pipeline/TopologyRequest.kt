package com.example.demo.pipeline

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import kotlin.collections.forEach



data class TopologyRequest(
    val sensors: List<SensorInput>,
    val adjacency: Map<String, List<String>>


){
    fun toDomain(): NetworkTopology {

        val sensorMap = sensors.associate {
            it.id to Sensor(it.id, it.groupId,it.x,it.y, it.desiredDutyCycle, it.tolerance)
        }

        val vertex = mutableMapOf<Sensor, MutableList<Sensor>>()

        sensorMap.values.forEach {
            vertex[it] = mutableListOf()
        }

        adjacency.forEach { (fromId, neighborsIds) ->

            val from = sensorMap[fromId]
                ?: error("Sensor $fromId não existe")

            neighborsIds.forEach { toId ->
                val to = sensorMap[toId]
                    ?: error("Sensor $toId não existe")

                vertex[from]!!.add(to)
            }
        }

        return NetworkTopology(vertex)
    }



}






