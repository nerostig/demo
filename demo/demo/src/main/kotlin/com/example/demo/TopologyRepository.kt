package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.Sensor


interface TopologyRepository {

    fun save (ScheduledNetworkTopology: ScheduledNetworkTopology)

    fun findById(id: Int): ScheduledNetworkTopology?

    fun findAll(): List<ScheduledNetworkTopology>
}