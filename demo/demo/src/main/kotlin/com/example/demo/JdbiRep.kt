package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import org.springframework.stereotype.Component

@Component
class local(): TopologyRepository{
    private val storage = mutableMapOf<Int, ScheduledNetworkTopology>()
    private var nextId = (storage.keys.maxOrNull() ?: 0) + 1


    override fun delete(id: Int) {
        storage.remove(id)

    }

    override fun save(topology: ScheduledNetworkTopology):Int {
        val id = nextId++
        storage[id] = topology
        return id
    }

    override fun findById(id: Int): ScheduledNetworkTopology? {
        return storage[id]
    }


    override fun update(id: Int, topology: ScheduledNetworkTopology) {
        if (!storage.containsKey(id)) {
            throw TopologyNotFoundException(id)
        }
        storage[id] = topology
    }

    override fun findAll(): Map<Int, ScheduledNetworkTopology> =
        storage.toMap()
}

