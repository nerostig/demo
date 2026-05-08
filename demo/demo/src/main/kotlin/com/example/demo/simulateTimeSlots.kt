package com.example.demo

import com.example.demo.domain.ScheduledNetworkTopology
import com.example.demo.domain.SimulationOutput
import com.example.demo.domain.SlotResult
import com.example.demo.pipeline.SensorResultOutput
import kotlin.math.absoluteValue

fun simulateTimeSlotsd(
    topology: ScheduledNetworkTopology,
    totalSlots: Int
): SimulationOutput {

    val sensors = topology.dutyCycles.keys.toList()

    val periods = topology.dutyCycles.mapValues { (_, dc) ->
        (100.0 / dc!!).toInt()
    }

    val slots = mutableListOf<SlotResult>()

    for (slot in 0 until totalSlots) {

        val active = sensors.filter { sensor ->
            val period = periods[sensor]!!
            slot % period == 0
        }.map { it.id }

        slots.add(
            SlotResult(
                slot = slot,
                activeSensors = active
            )
        )
    }

    return SimulationOutput(
        slots = slots,
        totalSlots = totalSlots
    )
}

fun simulateTimeSlots2(
    topology: ScheduledNetworkTopology,
    totalSlots: Int
): SimulationOutput {

    val sensors = topology.dutyCycles.keys.toList()

    val periods = topology.dutyCycles
        .filterValues { it != null }
        .mapValues { (_, dc) ->
            (100.0 / dc!!).toInt()
        }

    val slots = mutableListOf<SlotResult>()

    for (slot in 0 until totalSlots) {

        val active = sensors.filter { sensor ->

            val dc = topology.dutyCycles[sensor]

            if (dc == null) return@filter false

            val period = periods[sensor] ?: return@filter false

            slot % period == 0
        }.map { it.id }

        slots.add(
            SlotResult(
                slot = slot,
                activeSensors = active
            )
        )
    }

    return SimulationOutput(
        slots = slots,
        totalSlots = totalSlots
    )
}

fun simulateTimeSlots(
    topology: ScheduledNetworkTopology,
    totalSlots: Int
): SimulationOutput {

    val sensors = topology.dutyCycles.keys.toList()

    val periods = topology.dutyCycles
        .filterValues { it != null }
        .mapValues { (_, dc) ->
            (100.0 / dc!!).toInt()
        }

    val offsets = sensors.associateWith { sensor ->
        (sensor.id.hashCode().absoluteValue % (periods[sensor] ?: 1))
    }

    val slots = mutableListOf<SlotResult>()

    for (slot in 0 until totalSlots) {

        val active = sensors.filter { sensor ->

            val dc = topology.dutyCycles[sensor]
            if (dc == null) return@filter false

            val period = periods[sensor] ?: return@filter false
            val offset = offsets[sensor] ?: 0

            ((slot - offset) % period) == 0
        }.map { it.id }

        slots.add(
            SlotResult(
                slot = slot,
                activeSensors = active
            )
        )
    }

    return SimulationOutput(
        slots = slots,
        totalSlots = totalSlots
    )
}