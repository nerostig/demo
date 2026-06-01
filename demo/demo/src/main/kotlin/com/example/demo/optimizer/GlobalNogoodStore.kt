package com.example.demo.optimizer

import com.example.demo.domain.Sensor


data class Nogood(
    val assignments: Map<Sensor, Double>
)

// ===================== OTIMIZADOR =====================



class GlobalNogoodStore {

    private val nogoods = mutableListOf<Nogood>()
    private val lock = Any()

    fun add(nogood: Nogood) {
        synchronized(lock) {
            if (nogoods.none { it.assignments == nogood.assignments }) {
                nogoods.add(nogood)
            }
        }
    }

    fun snapshot(): List<Nogood> {
        synchronized(lock) {
            return nogoods.toList()
        }
    }
}


 fun violatesNogood(
    assignment: Map<Sensor, Double>,
    ctx: SearchContext
): Boolean {

    for (nogood in ctx.localNogoods) {
        if (nogood.assignments.all { (s, v) -> assignment[s] == v }) {
            return true
        }
    }

    for (nogood in ctx.globalNogoods.snapshot()) {
        if (nogood.assignments.all { (s, v) -> assignment[s] == v }) {
            return true
        }
    }

    return false
}

