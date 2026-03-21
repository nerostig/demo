package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import org.springframework.stereotype.Component

@Component
class local(): TopologyRepository{
    private val storage = mutableMapOf<Int, ScheduledNetworkTopology>()
    private var nextId = (storage.keys.maxOrNull() ?: 0) + 1
    override fun save(topology: ScheduledNetworkTopology) {
        val id = nextId++
        storage[id] = topology
    }

    override fun findById(id: Int): ScheduledNetworkTopology? {
        return storage[id]
    }

    override fun findAll(): List<ScheduledNetworkTopology> {
        return storage.values.toList()
    }


}