package com.example.demo.domain

data class SlotResult(
    val slot: Int,
    val activeSensors: List<String>
)

data class SimulationOutput(
    val slots: List<SlotResult>,
    val totalSlots: Int
)