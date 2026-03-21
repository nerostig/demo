package com.example.demo.pipeline

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import kotlin.collections.forEach



data class TopologyRequest(
    val sensors: List<SensorInput>,
    val links:List<Links>

){
    fun toDomain(): NetworkTopology {
        val sensorMap=
            sensors.associate{it.id to Sensor(it.id,it.desiredDutyCycle,it.tolerance)
            }

        val vertex= mutableMapOf<Sensor, MutableList<Sensor>>()

        sensorMap.values.forEach {
            vertex[it]=mutableListOf()
        }

        links.forEach {
            val a = sensorMap[it.from]!!
            val b =sensorMap[it.to]!!

            vertex[a]?.add(b)
            vertex[b]?.add(a)

        }

        return NetworkTopology(vertex)


    }



}






