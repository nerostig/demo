package com.example.demo.optimizer

import com.example.demo.domain.Sensor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


data class Nogood(
    val assignments: Map<Sensor, Double>
)

class GlobalNogoodStore {
    private val index = ConcurrentHashMap<Sensor, CopyOnWriteArrayList<Nogood>>()
    private val locks = ConcurrentHashMap<Sensor, Any>()




    private fun lockFor(sensor: Sensor): Any =
        locks.computeIfAbsent(sensor) { Any() }


    fun add(nogood: Nogood) {
        nogood.assignments.keys.forEach { sensor ->
            val list = index.getOrPut(sensor) { CopyOnWriteArrayList() }
            synchronized(lockFor(sensor)) {

                list.removeIf { existing ->
                    nogood.assignments.all { (k, v) -> existing.assignments[k] == v }
                }

                val subsumed = list.any { existing ->
                    existing.assignments.all { (k, v) -> nogood.assignments[k] == v }
                }
                if (!subsumed) {

                    val idx = list.indexOfFirst { it.assignments.size > nogood.assignments.size }
                    if (idx == -1) list.add(nogood) else list.add(idx, nogood)
                }
            }
        }
    }



    fun getFor(sensor: Sensor): List<Nogood> =
        index[sensor] ?: emptyList()




}


fun violatesNogood(sensor: Sensor, assignment: Map<Sensor, Double>, ctx: SearchContext): Boolean {

    val assignmentSize = assignment.size

    // locais


    ctx.localNogoodIndex[sensor]?.forEach { nogood ->
        if (nogood.assignments.size <= assignmentSize &&
            nogood.assignments.all { (s, v) -> assignment[s] == v }) return true
    }
    // globais


    ctx.globalNogoods.getFor(sensor).forEach { nogood ->
        if (nogood.assignments.size <= assignmentSize &&
            nogood.assignments.all { (s, v) -> assignment[s] == v }) return true
    }
    return false
}

