package com.example.demo

interface Transaction {
    val topologyRepository: TopologyRepository
    fun rollback()

}