package com.example.demo.optimizer.heuristics

import com.example.demo.domain.NetworkTopology
import com.example.demo.domain.Sensor
import com.example.demo.optimizer.SearchContext
import com.example.demo.optimizer.areCoprimePercentages
import kotlin.math.round

 fun impactOf(
    sensor: Sensor,
    period: Int,
    domains: Map<Sensor, List<Int>>,
    assignment: Map<Sensor, Double>,
    ctx: SearchContext,
    topology: NetworkTopology
): Int {
    val percentage =round(100.0 / period) //round(100.0 / period)
    var impact = 0

    for (neighbor in topology.neighbors(sensor)) {
        if (neighbor in assignment) continue

        val key = Triple(sensor, period, neighbor)
        val cached = ctx.impactCache[key]
        if (cached != null) {
            impact += cached
            continue
        }

        val domain = domains[neighbor] ?: continue
        val sizeAfter = domain.count {
            areCoprimePercentages(percentage, round(100.0 / it), ctx)
        }

        val delta = domain.size - sizeAfter
        ctx.impactCache[key] = delta
        impact += delta
    }
    return impact
}