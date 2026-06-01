package com.example.demo.repository

import com.example.demo.domain.ScheduledNetworkTopology


interface TopologyRepository {

    fun save (ScheduledNetworkTopology: ScheduledNetworkTopology):Int

    fun findById(id: Int): ScheduledNetworkTopology?
    fun update(id: Int, topology: ScheduledNetworkTopology):ScheduledNetworkTopology

    fun findAll():  Map<Int, ScheduledNetworkTopology>
    fun delete(id: Int)

}