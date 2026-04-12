package com.example.demo.domain


class NetworkTopology(
    val adjacency: Map<Sensor, List<Sensor>>
) {

    fun neighbors(sensor:Sensor): List<Sensor> {
        return adjacency[sensor] ?: emptyList()
    }

    fun sensors(): Set<Sensor> {
        return adjacency.keys
    }

    fun subgraph(nodes: Set<Sensor>): NetworkTopology {
        val subAdj = nodes.associateWith { s ->
            adjacency[s]?.filter { it in nodes } ?: emptyList()
        }
        return NetworkTopology(subAdj)
    }

    fun connectedComponents(): List<Set<Sensor>> {
        val visited = mutableSetOf<Sensor>()
        val components = mutableListOf<Set<Sensor>>()

        for (sensor in sensors()) {
            if (sensor in visited) continue

            val stack = ArrayDeque<Sensor>()
            val component = mutableSetOf<Sensor>()

            stack.add(sensor)
            visited.add(sensor)

            while (stack.isNotEmpty()) {
                val current = stack.removeLast()
                component.add(current)

                for (n in neighbors(current)) {
                    if (n !in visited) {
                        visited.add(n)
                        stack.add(n)
                    }
                }
            }
            components.add(component)
        }
        return components
    }
}