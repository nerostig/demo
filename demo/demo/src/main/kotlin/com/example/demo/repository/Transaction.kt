package com.example.demo.repository

interface Transaction {
    val topologyRepository: TopologyRepository
    val userRepository: UserRepository
    fun rollback()

}