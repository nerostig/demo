package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.Sensor


interface TopologyRepository {

    fun save (ScheduledNetworkTopology: ScheduledNetworkTopology):Int

    fun findById(id: Int): ScheduledNetworkTopology?
    fun update(id: Int, topology: ScheduledNetworkTopology)

    fun findAll():  Map<Int, ScheduledNetworkTopology>
    fun delete(id: Int)

}