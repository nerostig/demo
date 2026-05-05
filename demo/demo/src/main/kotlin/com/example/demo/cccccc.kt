/*
import React, {useCallback, useEffect, useState} from 'react'
import { useNavigate, useParams } from "react-router-dom";

import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Link2, MousePointer, Play } from 'lucide-react'
import { toast } from 'sonner'
import DutyCycleTimeline from "../layout/components/network/DutyCycleTimeline";
import AlgorithmPanel from "../layout/components/network/AlgorithmPanel";
import NodeProperties from "../layout/components/network/NodeProperties";
import NetworkCanvas from "../layout/components/network/NetworkCanvas";
import {Card, CardContent, CardHeader, CardTitle } from "../layout/components/ui/card";
import { Button } from "../layout/components/ui/button";

/* ===================== TYPES ===================== */

export interface SensorNode {
    id: string
    x: number
    y: number
    desiredDutyCycle: number
    tolerance: number
}

export interface Edge {
    source: string
    target: string
}

interface TopologyRequest {
    sensors: {
        id: string
        x: number
        y: number
        desiredDutyCycle: number
        tolerance: number
    }[]
    adjacency: Record<string, string[]>
}

interface TopologyResponseBase {
    id: number
    sensors: {
        id: string
        x: number | null
        y: number | null
        dutyCycleParameter: number | null
    }[]
    adjacency: Record<string, string[]>
    message?: string
}


/* ===================== API ===================== */

async function fetchTopology(id: number): Promise<TopologyResponseBase> {
    const res = await fetch(`/api/topology/${id}`)
    if (!res.ok) throw new Error('Topology not found')
    return res.json()
}

async function runAlgorithm(
id: number | null,
body: TopologyRequest
): Promise<TopologyResponseBase> {
    const res = await fetch(
        id === null ? '/api/topology/implement' : `/api/topology/${id}`,
    {
        method: id === null ? 'POST' : 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    }
    )

    if (!res.ok) throw new Error('Algorithm failed')
    return res.json()
}

/* ===================== COMPONENT ===================== */

export default function NetworkEditor() {
    const { id } = useParams<{ id: string }>()
    const isNew = id === 'new'
    const topologyId = isNew ? null : Number(id)

    const navigate = useNavigate()

    const [nodes, setNodes] = useState<SensorNode[]>([])
    const [edges, setEdges] = useState<Edge[]>([])
    const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null)
    const [edgeMode, setEdgeMode] = useState(false)
    const [edgeStart, setEdgeStart] = useState<string | null>(null)

    const [result, setResult] = useState<TopologyResponseBase | null>(null)
    const [isRunning, setIsRunning] = useState(false)



    const { data, isLoading, error } = useQuery<TopologyResponseBase>({
        queryKey: ['topology', topologyId],
        enabled: topologyId !== null,
        queryFn: () => fetchTopology(topologyId!),
    });

    useEffect(() => {
        if (!data) return

        setNodes(
            data.sensors.map(s => ({
                id: s.id,
                x: s.x ?? 0,
                y: s.y ?? 0,
                desiredDutyCycle: s.dutyCycleParameter ?? 0.5,
                tolerance: 0.1,
            }))
        )

        const rebuiltEdges: Edge[] = []
        Object.entries(data.adjacency).forEach(([src, targets]) => {
            targets.forEach(tgt => {
                if (src < tgt) {
                    rebuiltEdges.push({ source: src, target: tgt })
                }
            })
        })

        setEdges(rebuiltEdges)
        setResult(data)
    }, [data])

    /* ---------- Canvas handlers ---------- */

    const handleAddNode = useCallback((x: number, y: number) => {
        const id = `S${Date.now()}`
                setNodes(prev => [
        ...prev,
        { id, x, y, desiredDutyCycle: 0.5, tolerance: 0.1 },
        ])
        setSelectedNodeId(id)
    }, [])

    const handleMoveNode = useCallback((id: string, x: number, y: number) => {
        setNodes(prev =>
        prev.map(n => (n.id === id ? { ...n, x, y } : n))
        )
    }, [])

    const handleSelectNode = useCallback(
            (id: string) => {
        if (!edgeMode) {
            setSelectedNodeId(id)
            return
        }

        if (!edgeStart) {
            setEdgeStart(id)
        } else if (edgeStart !== id) {
            setEdges(prev => [...prev, { source: edgeStart, target: id }])
            setEdgeStart(null)
        }
    },
    [edgeMode, edgeStart]
    )

    const handleUpdateNode = useCallback(
            (id: string, data: Partial<SensorNode>) => {
        setNodes(prev =>
        prev.map(n => (n.id === id ? { ...n, ...data } : n))
        )
    },
    []
    )

    const handleDeleteNode = useCallback((id: string) => {
        setNodes(prev => prev.filter(n => n.id !== id))
        setEdges(prev => prev.filter(e => e.source !== id && e.target !== id))
        setSelectedNodeId(null)
    }, [])

    /* ---------- Run algorithm ---------- */

    const mutation = useMutation({
        mutationFn: (body: TopologyRequest) => runAlgorithm(topologyId, body),
        onSuccess: (data: TopologyResponseBase) => {
        setResult(data)
        toast.success(data.message ?? 'Algoritmo executado')
        if (isNew && topologyId === null) {
            //navigate('/networks')
        }
    },
        onError: () => toast.error('Erro ao executar algoritmo'),
        onSettled: () => setIsRunning(false),
    })

    const handleRun = () => {
        if (nodes.length === 0) {
            toast.error('Adicione sensores primeiro')
            return
        }

        setIsRunning(true)

        const adjacency: Record<string, string[]> = {}
        nodes.forEach(n => (adjacency[n.id] = []))
        edges.forEach(e => {
            adjacency[e.source].push(e.target)
            adjacency[e.target].push(e.source)
        })

        mutation.mutate({
            sensors: nodes.map(n => ({
            id: n.id,
            x: Math.round(n.x),
            y: Math.round(n.y),
            desiredDutyCycle: n.desiredDutyCycle,
            tolerance: n.tolerance,
        })),
            adjacency,
        })
    }

    const selectedNode = nodes.find(n => n.id === selectedNodeId) ?? null

    /* ---------- Render ---------- */

    return (
    <div className="p-4 max-w-[1600px] mx-auto space-y-4">
    <div className="flex items-center justify-between">
    <Button
    variant="ghost"
    size="icon"
    onClick={() => navigate('/networks')}
    >
    <ArrowLeft className="w-4 h-4" />
    </Button>

    <div className="flex gap-2">
    <Button
    variant={edgeMode ? 'default' : 'outline'}
    size="sm"
    onClick={() => {
        setEdgeMode(!edgeMode)
        setEdgeStart(null)
    }}
    >
    {edgeMode ? <Link2 /> : <MousePointer />}
    </Button>

    <Button size="sm" onClick={handleRun} disabled={isRunning}>
    <Play className="w-4 h-4 mr-1" />
    Executar
    </Button>
    </div>
    </div>

    <div className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4">
    <Card>
    <CardContent className="p-0 h-[500px] overflow-hidden">
    <NetworkCanvas
    nodes={nodes}
    edges={edges}
    onAddNode={handleAddNode}
    onMoveNode={handleMoveNode}
    onSelectNode={handleSelectNode}
    onUpdateNode={handleUpdateNode}
    onDeleteNode={handleDeleteNode}
    selectedNodeId={selectedNodeId}
    edgeMode={edgeMode}
    edgeStart={edgeStart}
    />
    </CardContent>
    </Card>

    <div className="space-y-4">
    <Card>
    <CardHeader>
    <CardTitle className="text-sm">
    Sensor
    </CardTitle>
    </CardHeader>
    <CardContent>
    <NodeProperties
    node={selectedNode}
    onUpdate={handleUpdateNode}
    onDelete={handleDeleteNode}
    />
    </CardContent>
    </Card>

    <AlgorithmPanel
    isRunning={isRunning}
    hasResult={!!result}
    onRun={handleRun}
    />
    </div>
    </div>

    <DutyCycleTimeline
    assignments={result?.sensors}
    message={result?.message}

    />
    </div>
    )
}
*/
















































//package com.example.demo
//
//import com.example.demo.domain.NetworkTopology
//import com.example.demo.domain.Sensor
//import com.example.demo.pipeline.DutyCycleParameter
//import com.example.demo.pipeline.Schedule
//import kotlin.collections.component1
//
//
//fun percentageToPeriod(percent: Int): Int {
//    require(percent in 1..100) { "Percentagem inválida: $percent" }
//    return 100 / gcd(percent, 100)
//}
//
//fun percentageRangeToPeriods(
//    desired: Int,
//    tolerance: Int
//): IntRange {
//
//    val minP = (desired - tolerance).coerceAtLeast(0)
//    val maxP = (desired + tolerance).coerceAtMost(100)
//
//    val minPeriod = percentageToPeriod(maxP)
//    val maxPeriod = percentageToPeriod(minP)
//
//    return minPeriod..maxPeriod
//}
//
//fun assignParametersToSensorsPercenatge(
//    topology: NetworkTopology,
//    colors: Map<Sensor, Int>,
//    defaultTolerance: Int = 2
//): Map<String, DutyCycleParameter?> {
//
//    val valuesPerSensor = mutableMapOf<Sensor, DutyCycleParameter?>()
//
//    val sensorsOrdered =
//        colors.entries
//            .sortedWith(compareBy({ it.value }, { it.key.id }))
//            .map { it.key }
//
//    for (sensor in sensorsOrdered) {
//
//        val neighborParameters =
//            topology.neighbors(sensor)
//                .filter { colors[it] != colors[sensor] }
//                .mapNotNull { valuesPerSensor[it]?.value }
//
//        val tolerance = sensor.tolerance ?: defaultTolerance
//        val desiredPercent = sensor.desiredDutyCycle
//
//        val periodRange =
//            percentageRangeToPeriods(desiredPercent, tolerance)
//
//        var assignedPeriod: Int? = null
//
//        // 1️⃣ tentar dentro da tolerância
//        for (candidatePeriod in periodRange) {
//            if (neighborParameters.all { areCoprime(candidatePeriod, it) }) {
//                assignedPeriod = candidatePeriod
//                break
//            }
//        }
//
//        // 2️⃣ fallback: reutilizar período da mesma cor
//        if (assignedPeriod == null) {
//
//            val sameColorPeriods =
//                valuesPerSensor
//                    .filterKeys { colors[it] == colors[sensor] }
//                    .mapNotNull { it.value?.value }
//
//            val fallback =
//                sameColorPeriods.firstOrNull { candidate ->
//                    neighborParameters.all { areCoprime(candidate, it) }
//                }
//
//            if (fallback != null) {
//                assignedPeriod = fallback
//            }
//        }
//
//        valuesPerSensor[sensor] =
//            assignedPeriod?.let { DutyCycleParameter(it) }
//    }
//
//    return valuesPerSensor.mapKeys { it.key.id }
//}
//
//fun gcd(a: Int, b: Int): Int {
//    var x = kotlin.math.abs(a)
//    var y = kotlin.math.abs(b)
//
//    while (y != 0) {
//        val temp = y
//        y = x % y
//        x = temp
//    }
//
//    return x
//}
//
//
//fun areCoprime(a: Int, b: Int): Boolean {
//    return gcd(a, b) == 1
//}
//
//
//fun colorGraph(topology: NetworkTopology): Map<Sensor, Int> {
//    val colors = mutableMapOf<Sensor, Int>()
//
//    val sensorsOrdered = topology
//        .sensors()
//        .sortedByDescending { topology.neighbors(it).size }
//
//    for (sensor in sensorsOrdered) {
//
//        val neighborColors = topology
//            .neighbors(sensor)
//            .mapNotNull { colors[it] }
//            .toSet()
//
//        var color = 1
//        while (neighborColors.contains(color)) {
//            color++
//        }
//
//        colors[sensor] = color
//    }
////    println("Cores -> ${colors.forEach {
////        i->
////        println(i.value)
////        println(i.key.id)
////        println("wwww")
////    }}")
//
//    return colors
//}
//
//
//
//fun assignParametersToSensors(
//    topology: NetworkTopology,
//    colors: Map<Sensor, Int>,
//    defaultTolerance: Int = 2
//): Map<String, DutyCycleParameter?> {
//
//    val valuesPerSensor = mutableMapOf<Sensor, DutyCycleParameter?>()
//
//    // sensores ordenados por cor (heurística)
//    val sensorsOrdered =
//        colors.entries
//            .sortedWith(compareBy({ it.value }, { it.key.id }))
//            .map { it.key }
//
//    for (sensor in sensorsOrdered) {
//
//        // duty cycles dos vizinhos reais (outras cores)
//        val neighborParameters =
//            topology.neighbors(sensor)
//                .filter { colors[it] != colors[sensor] }
//                .mapNotNull { valuesPerSensor[it]?.value }
//
//        val tolerance = sensor.tolerance ?: defaultTolerance
//        val minValue = sensor.desiredDutyCycle - tolerance
//        val maxValue = sensor.desiredDutyCycle + tolerance
//
//        println(
//            "Sensor ${sensor.id} vizinhos=${topology.neighbors(sensor).map { it.id }} " +
//                    "intervalo=[$minValue,$maxValue]"
//        )
//
//        var assignedValue: Int? = null
//
//        // 1️⃣ tentar dentro da tolerância
//        for (candidate in minValue..maxValue) {
//            if (neighborParameters.all { areCoprime(candidate, it) }) {
//                assignedValue = candidate
//                println("  -> atribuído $candidate (tolerância)")
//                break
//            }
//        }
//
//        // 2️⃣ fallback: reutilizar valor da MESMA cor
//        if (assignedValue == null) {
//
//            val sameColorValues =
//                valuesPerSensor
//                    .filterKeys { colors[it] == colors[sensor] }
//                    .mapNotNull { it.value?.value }
//
//            val fallback =
//                sameColorValues.firstOrNull { candidate ->
//                    neighborParameters.all { areCoprime(candidate, it) }
//                }
//
//            if (fallback != null) {
//                assignedValue = fallback
//                println("  -> fallback mesma cor: $fallback")
//            }
//        }
//
//        // 3️⃣ atribuir (ou null)
//        if (assignedValue != null) {
//            valuesPerSensor[sensor] = DutyCycleParameter(assignedValue)
//        } else {
//            valuesPerSensor[sensor] = null
//            println("  -> NÃO FOI POSSÍVEL ATRIBUIR (null)")
//        }
//    }
//
//    return valuesPerSensor.mapKeys { it.key.id }
//}
//
//fun computeSchedules(
//    topology: NetworkTopology
//): List<Schedule> {
//
//    val colors = colorGraph(topology)
//    val colorParameters = assignParametersToSensors(topology, colors)
//
//    return colors
//        .mapNotNull { (sensor) ->
//            val param = colorParameters[sensor.id]
//            println(param)
//            if (param != null) {
//                Schedule(sensor, param)
//            } else {
//
//                Schedule(sensor, null)
//            }
//        }
//}
////fun computeSchedules(
////    topology: NetworkTopology
////): List<Schedule> {
////
////    val colors = colorGraph(topology)
////
////    val colorParameters = assignParametersToSensors(topology, colors)
////
////    return colors.map { (sensor) ->
////        Schedule(
////            sensor,
////            colorParameters[sensor.id]!!
////        )
////    }
////}
//
//
//
//
//fun main() {
//    println("eeee")
//
//    val A = Sensor("A", desiredDutyCycle = 16, tolerance = 2)
//    val B = Sensor("B", desiredDutyCycle = 16, tolerance = 2)
//    val C = Sensor("C", desiredDutyCycle = 18, tolerance = 2)
//    val D = Sensor("D", desiredDutyCycle = 80, tolerance = 2)
//    val E = Sensor("E", desiredDutyCycle = 20, tolerance = 2)
//    val F = Sensor("F", desiredDutyCycle = 18, tolerance = 2)
//
//    val topology = NetworkTopology(
//
//        mapOf(
//            A to listOf(B, D),
//            B to listOf(A, C, E),
//            C to listOf(B, E),
//            D to listOf(A, F, E),
//            E to listOf(B, C, D, F),
//            F to listOf(D, E)
//        )
//    )
//
//    val schedules = computeSchedules(topology)
//
//    println("Resultado final:")
//    schedules
//        .sortedBy { it.sensor.id }
//        .forEach {
//            println("Sensor ${it.sensor.id} -> duty cycle = ${it.parameter?.value}")
//        }
//}
////fun assignParametersToSensorscc(
////    topology: NetworkTopology,
////    colors: Map<Sensor, Int>,
////    defaultTolerance: Int = 2
////): Map< String, DutyCycleParameter? > {
////
////    val colorAdj = buildColorAdjacency(topology, colors)
////
////    val valuesPerSensor=mutableMapOf<Sensor,DutyCycleParameter>()
////
////    val valuePerColor = mutableMapOf<Int, MutableList<Int>>()
////
////    val colorsOrdered = colors.values.toSet().sorted()
////
////    val finalSensorsParameters = mutableMapOf<String, DutyCycleParameter>()
////
////
////    for (color in colorsOrdered) {
////
////        val sensorsOfColor = colors
////            .filterValues { it == color }
////            .keys
////
////        println("vvvvvvvv Color $color -> ${sensorsOfColor.map { it.id }}")
////
////        val neighborColors = colorAdj[color] ?: emptySet()
////        println("vizinho cor $neighborColors")
////
////
////        //val neighborParameters = neighborColors.flatMap { valuePerColor[it]?:emptyList() }
////
////
////        val valuesOfthisColor=mutableListOf<Int>()
////
////        val neighborParameters =
////            neighborColors.flatMap { valuePerColor[it] ?: emptyList() } +
////                    valuesOfthisColor
////
////
////        for (sensor in sensorsOfColor) {
////            val tolerance = sensor.tolerance ?: defaultTolerance
////            val minValue = sensor.desiredDutyCycle - tolerance
////            val maxValue = sensor.desiredDutyCycle + tolerance
////
////            println("Processando sensor ${sensor.id} -> intervalo [$minValue, $maxValue]")
////
////            var candidate = minValue
////            var assigned = false
////
////            while (candidate <= maxValue) {
////                println("vizinho $neighborParameters")
////                val valid = neighborParameters.all { areCoprime(candidate, it) }
////
////                if (valid) {
////                    valuesPerSensor[sensor]= DutyCycleParameter(candidate)
////
////                    valuesOfthisColor.add(candidate)
////
////                    assigned = true
////
////                    println("  Sensor ${sensor.id} atribuído candidato válido $candidate")
////
////                    break
////                }
////                candidate++
////            }
////            if (!assigned) {
////                val fallback = sensor.desiredDutyCycle
////
////                valuesPerSensor[sensor] = DutyCycleParameter(fallback)
////                valuesOfthisColor.add(fallback)
////                println("  Sensor ${sensor.id} sem candidato -> usa fallback $fallback")
////            }
////        }
////        valuePerColor[color]=valuesOfthisColor
////
////    }
////    valuesPerSensor.forEach { finalSensorsParameters[it.key.id]=it.value }
////
////    return finalSensorsParameters
////}
////
////fun buildColorAdjacency(
////    topology: NetworkTopology,
////    colors: Map<Sensor, Int>
////): Map<Int, Set<Int>> {
////
////    val adjacency = mutableMapOf<Int, MutableSet<Int>>()
////
////    for ((sensor, neighbors) in topology.adjacency) {
////
////        val colorA = colors[sensor]!!
////
////        for (neighbor in neighbors) {
////
////            val colorB = colors[neighbor]!!
////
////            if (colorA != colorB) {
////
////                adjacency
////                    .computeIfAbsent(colorA) { mutableSetOf() }
////                    .add(colorB)
////
////                adjacency
////                    .computeIfAbsent(colorB) { mutableSetOf() }
////                    .add(colorA)
////            }
////        }
////    }
////
////    return adjacency
////}
////
////
////
////
////fun assignParametersToSensorsvvvvv(
////    topology: NetworkTopology,
////    colors: Map<Sensor, Int>,
////    defaultTolerance: Int = 2
////): Map<String, DutyCycleParameter> {
////
////    val colorAdj = buildColorAdjacency(topology, colors)
////
////    val valuesPerSensor = mutableMapOf<Sensor, DutyCycleParameter>()
////    val valuePerColor = mutableMapOf<Int, MutableList<Int>>()
////
////    val colorsOrdered = colors.values.toSet().sorted()
////
////    for (color in colorsOrdered) {
////
////        val sensorsOfColor = colors
////            .filterValues { it == color }
////            .keys
////
////        println("vvvvvvvv Color $color -> ${sensorsOfColor.map { it.id }}")
////
////        val neighborColors = colorAdj[color] ?: emptySet()
////
////        // 🔴 APENAS sensores vizinhos (outras cores)
////        val neighborParameters =
////            neighborColors.flatMap { valuePerColor[it] ?: emptyList() }
////
////        val valuesOfThisColor = mutableListOf<Int>()
////
////        for (sensor in sensorsOfColor) {
////
////            val tolerance = sensor.tolerance ?: defaultTolerance
////            val minValue = sensor.desiredDutyCycle - tolerance
////            val maxValue = sensor.desiredDutyCycle + tolerance
////
////            println("Processando sensor ${sensor.id} -> intervalo [$minValue, $maxValue]")
////
////            var assigned = false
////
////            // 1️⃣ tentar dentro da tolerância
////            for (candidate in minValue..maxValue) {
////
////                val valid = neighborParameters.all {
////                    areCoprime(candidate, it)
////                }
////
////                if (valid) {
////                    valuesPerSensor[sensor] = DutyCycleParameter(candidate)
////                    valuesOfThisColor.add(candidate)
////                    println("  Sensor ${sensor.id} usa $candidate (tolerância)")
////                    assigned = true
////                    break
////                }
////            }
////
////            // 2️⃣ fallback: usar valor da mesma cor
////            if (!assigned) {
////                val fallback = valuesOfThisColor.firstOrNull {
////                    neighborParameters.all { n -> areCoprime(it, n) }
////                }
////
////                if (fallback != null) {
////                    valuesPerSensor[sensor] = DutyCycleParameter(fallback)
////                    valuesOfThisColor.add(fallback)
////                    println("  Sensor ${sensor.id} fallback -> $fallback")
////                    assigned = true
////                }
////            }
////
////            // 3️⃣ erro final
////            if (!assigned) {
////                throw RuntimeException(
////                    "Não foi possível atribuir duty cycle ao sensor ${sensor.id}"
////                )
////            }
////        }
////
////        valuePerColor[color] = valuesOfThisColor
////    }
////
////    return valuesPerSensor.mapKeys { it.key.id }
////}