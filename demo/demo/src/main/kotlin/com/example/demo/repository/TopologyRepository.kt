package com.example.demo.repository

import com.example.demo.domain.ScheduledNetworkTopology


interface TopologyRepository {

    fun save (ScheduledNetworkTopology: ScheduledNetworkTopology,userId: Int):Int

    fun findById(id: Int,userId: Int): ScheduledNetworkTopology?
    fun update(id: Int, topology: ScheduledNetworkTopology,userId: Int):ScheduledNetworkTopology

    fun findAll(userId: Int):  Map<Int, ScheduledNetworkTopology>
    fun delete(id: Int)

}