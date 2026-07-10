package com.example.demo.repository_jdbi

import com.example.demo.repository.TopologyRepository
import com.example.demo.repository.Transaction
import com.example.demo.repository.JdbiTopologyRepository
import com.example.demo.repository.JdbiUserRepository
import com.example.demo.repository.UserRepository
import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val topologyRepository: TopologyRepository = JdbiTopologyRepository(handle)
    override val userRepository: UserRepository = JdbiUserRepository(handle)


    override fun rollback() {
        handle.rollback()
    }
}