package com.example.demo.optimizer.heuristics

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor

fun nullRisk(topology: NetworkTopology,sensor: Sensor, assignment: MutableMap<Sensor, Double>):Int{
    val assig=topology.neighbors(sensor).count{it in assignment}
    val dregre=topology.neighbors(sensor).size

    return  assig * 2 +dregre
}