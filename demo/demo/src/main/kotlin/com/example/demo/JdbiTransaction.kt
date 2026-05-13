package com.example.demo

import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val topologyRepository: TopologyRepository = JdbiTopologyRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}