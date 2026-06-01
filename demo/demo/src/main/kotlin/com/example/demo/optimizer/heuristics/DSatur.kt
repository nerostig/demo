package com.example.demo.optimizer.heuristics

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor

 fun saturation(topology: NetworkTopology,sensor: Sensor, assignment: Map<Sensor, Double>): Int {
    return topology.neighbors(sensor)
        .mapNotNull { assignment[it] }
        .distinct()
        .size
}
