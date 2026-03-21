package com.example.demo.domain


class NetworkTopology(
    val adjacency: Map<Sensor, List<Sensor>>
) {

    fun neighbors(sensor:Sensor): List<Sensor> {
        return adjacency[sensor] ?: emptyList()
    }

    fun sensors(): Set<Sensor> {
        return adjacency.keys
    }
}