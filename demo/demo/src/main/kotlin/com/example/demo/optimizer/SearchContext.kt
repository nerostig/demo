package com.example.demo.optimizer

import com.example.demo.domain.Sensor



class SearchContext(val globalNogoods: GlobalNogoodStore) {
    val coprimeCache = mutableMapOf<Pair<Double, Double>, Boolean>()
    val impactCache  = mutableMapOf<Triple<Sensor, Int, Sensor>, Int>()
    val localNogoodIndex = HashMap<Sensor, MutableList<Nogood>>()

    fun addLocalNogood(nogood: Nogood) {
        nogood.assignments.keys.forEach { s ->
            val list = localNogoodIndex.getOrPut(s) { mutableListOf() }
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

























