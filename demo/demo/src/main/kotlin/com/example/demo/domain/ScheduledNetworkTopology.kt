package com.example.demo.domain

class ScheduledNetworkTopology(
    val adjacency: Map<Sensor, List<Sensor>>,
    val dutyCycles: Map<Sensor, Double>
)