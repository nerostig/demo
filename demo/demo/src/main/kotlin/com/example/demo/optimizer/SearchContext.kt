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

























//    fun addLocalNogood(nogood: Nogood) {
//        nogood.assignments.keys.forEach { s ->
//            localNogoodIndex.getOrPut(s) { mutableListOf() }.add(nogood)
//        }
//    }

//// 2
//    fun addLocalNogood(nogood: Nogood) {
//        nogood.assignments.keys.forEach { s ->
//            val list = localNogoodIndex.getOrPut(s) { mutableListOf() }
//            // insere ordenado por tamanho crescente
//            val idx = list.indexOfFirst { it.assignments.size > nogood.assignments.size }
//            if (idx == -1) list.add(nogood) else list.add(idx, nogood)
//        }
//    }


//class SearchContextdd(
//    var globalNogoods: GlobalNogoodStore,
//    var coprimeCache: Map<Pair<Double, Double>, Boolean> = emptyMap(),
//    var impactCache: Map<Triple<Sensor, Int, Sensor>, Int> = emptyMap(),
//    var localNogoods: List<Nogood> = emptyList()
//) {
//
//    fun copyWithCoprime(key: Pair<Double, Double>, value: Boolean): SearchContext {
//        return SearchContextdd(
//            globalNogoods = globalNogoods,
//            coprimeCache = coprimeCache + (key to value),
//            impactCache = impactCache,
//            localNogoods = localNogoods
//        )
//    }
//}
