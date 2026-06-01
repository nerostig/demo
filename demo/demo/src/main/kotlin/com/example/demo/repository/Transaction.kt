package com.example.demo.repository

interface Transaction {
    val topologyRepository: TopologyRepository
    fun rollback()

}