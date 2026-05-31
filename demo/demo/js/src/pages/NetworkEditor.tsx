import React, {useCallback, useEffect, useRef, useState} from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ArrowLeft, Link2, MousePointer, Play, Layers } from 'lucide-react'
import { toast } from 'sonner'

import DutyCycleTimeline from '../layout/components/network/DutyCycleTimeline'
import AlgorithmPanel from '../layout/components/network/AlgorithmPanel'
import NodeProperties from '../layout/components/network/NodeProperties'
import NetworkCanvas from '../layout/components/network/NetworkCanvas'
import { Card, CardContent, CardHeader, CardTitle } from '../layout/components/ui/card'
import { Button } from '../layout/components/ui/button'
import TopologyIO from "../layout/components/network/TopologyIO";

/* ===================== TYPES ===================== */
interface PerformanceMetrics {
    executionTimeMs: number
    memoryUsedKb: number
}
interface TopologySaveRequest {
    id?: number
    name?: string
    sensors: {
        id: string
        x: number
        y: number
        desiredDutyCycle: number
        tolerance: number
        groupId?: string
    }[]
    adjacency: Record<string, string[]>
}
export interface SensorNode {
    id: string
    x: number
    y: number
    desiredDutyCycle: number
    tolerance: number
    groupId?: string
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
    name?: string
    sensors: {
        id: string
        x: number | null
        y: number | null
        dutyCycleParameter: number | null
        grouId?: string
    }[]
    adjacency: Record<string, string[]>
    message?: string,
    performance?: PerformanceMetrics
}

/* ===================== API ===================== */

async function saveTopology(id: number | null, body: TopologySaveRequest) {
    const url = id === null
        ? '/api/topology'
        : `/api/topology/${id}`

    const method = id === null ? 'POST' : 'PUT'

    const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    })

    if (!res.ok) throw new Error('Save failed')

    return res.json()
}

async function fetchTopology(id: number): Promise<TopologyResponseBase> {
    const res = await fetch(`/api/topology/${id}`)
    if (!res.ok) throw new Error('Topology not found')
    return res.json()
}

async function runAlgorithm(
    id: number | null,
    body: TopologyRequest
): Promise<TopologyResponseBase> {

    const res = await fetch('/api/topology/planning', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
    })

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

    const [isSaving, setIsSaving] = useState(false)

    const [groupMode, setGroupMode] = useState(false)
    const [selectedNodes, setSelectedNodes] = useState<string[]>([])

    const savingRef = useRef(false)

    const [topologyName, setTopologyName] = useState<string>('')

    const [result, setResult] = useState<TopologyResponseBase | null>(null)
    const [isRunning, setIsRunning] = useState(false)

    /* ---------- Load topology ---------- */

    const { data } = useQuery({
        queryKey: ['topology', topologyId],
        enabled: topologyId !== null,
        queryFn: () => fetchTopology(topologyId!),
    })
    /*
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
                if (src < tgt) rebuiltEdges.push({ source: src, target: tgt })
            })
        })

        setEdges(rebuiltEdges)
        setResult(data)
    }, [data])

    */
    useEffect(() => {
        if (!data) return

        setTopologyName(data.name ?? '')
        setNodes(
            data.sensors.map(s => ({
                id: s.id,
                x: s.x ?? 0,
                y: s.y ?? 0,
                desiredDutyCycle: s.dutyCycleParameter ?? 0.5,
                tolerance: 0.1,
                groupId: s.grouId ?? undefined,
            }))
        )

        const rebuiltEdges: Edge[] = []
        const seen = new Set<string>()

        Object.entries(data.adjacency).forEach(([src, targets]) => {
            targets.forEach(tgt => {
                const key = [src, tgt].sort().join('|')

                if (!seen.has(key)) {
                    seen.add(key)
                    rebuiltEdges.push({
                        source: src,
                        target: tgt
                    })
                }
            })
        })

        setEdges(rebuiltEdges)
        setResult(data)
    }, [data])




    /* ---------- Node handlers ---------- */


    function nextId(nodes: { id: string }[]): string {
        const used = new Set<number>()

        for (const n of nodes) {
            const match = n.id.match(/^S(\d+)$/)
            if (match) {
                used.add(Number(match[1]))
            }
        }

        let i = 1
        while (used.has(i)) {
            i++
        }

        return `S${i}`
    }
    /*
    const handleAddNode = useCallback((x: number, y: number) => {
        //const id = `S${Date.now()}`
        const id = nextId()
        setNodes(prev => [...prev, { id, x, y, desiredDutyCycle: 0.5, tolerance: 0.1 }])
        setSelectedNodeId(id)
        setSelectedNodes([id])
    }, [])

    */

    const saveMutation = useMutation({
        mutationFn: (body: TopologySaveRequest) => saveTopology(topologyId,body),

        onSuccess: () => {
            toast.success('Topologia guardada com sucesso')
        },

        onError: () => {
            toast.error('Erro ao guardar topologia')
        },

        onSettled: () => {
            setIsSaving(false)
            savingRef.current = false
        },
    })
    const buildAdjacency = () => {
        const adjacency: Record<string, string[]> = {}

        nodes.forEach(n => (adjacency[n.id] = []))

        edges.forEach(e => {
            adjacency[e.source].push(e.target)
            adjacency[e.target].push(e.source)
        })

        return adjacency
    }
    const handleSave = () => {
        // bloqueio imediato (sync, não depende do React state)
        if (savingRef.current || saveMutation.isPending) return

        if (nodes.length === 0) {
            toast.error('Adicione sensores primeiro')
            return
        }

        savingRef.current = true
        setIsSaving(true)

        saveMutation.mutate({
            id: topologyId ?? undefined,
            name: topologyName || undefined,
            sensors: nodes.map(n => ({
                id: n.id,
                x: Math.round(n.x),
                y: Math.round(n.y),
                desiredDutyCycle: n.desiredDutyCycle,
                tolerance: n.tolerance,
                groupId: n.groupId,
            })),
            adjacency: buildAdjacency(),
        })
    }

    const handleAddNode = useCallback((x: number, y: number) => {
        setNodes(prev => {
            const id = nextId(prev)

            setSelectedNodeId(id)
            setSelectedNodes([id])

            return [
                ...prev,
                { id, x, y, desiredDutyCycle: 0.5, tolerance: 0.1 }
            ]
        })
    }, [])

    const handleMoveNode = useCallback((id: string, x: number, y: number) => {
        setNodes(prev => prev.map(n => (n.id === id ? { ...n, x, y } : n)))
    }, [])

    const handleSelectNode = useCallback(
        (id: string) => {
            if (edgeMode) {
                if (!edgeStart) setEdgeStart(id)
                else if (edgeStart !== id) {
                    setEdges(prev => [...prev, { source: edgeStart, target: id }])
                    setEdgeStart(null)
                }
                return
            }

            if (!groupMode) {
                setSelectedNodeId(id)
                setSelectedNodes([id])
                return
            }

            setSelectedNodes(prev =>
                prev.includes(id) ? prev.filter(n => n !== id) : [...prev, id]
            )
            setSelectedNodeId(id)
        },
        [edgeMode, edgeStart, groupMode]
    )

    /* ---------- GROUP LOGIC ---------- */

    function nextGroupId(nodes: { groupId?: string }[]): string {
        const used = new Set<number>()

        for (const n of nodes) {
            if (!n.groupId) continue
            const match = n.groupId.match(/^G(\d+)$/)
            if (match) {
                used.add(Number(match[1]))
            }
        }

        let i = 1
        while (used.has(i)) {
            i++
        }

        return `G${i}`
    }

    const createGroupFromSelection = () => {
        if (selectedNodes.length < 2) {
            toast.error('Selecione pelo menos 2 sensores')
            return
        }

        //const groupId = `G${Date.now()}`
        const  groupId =nextGroupId(nodes)

            setNodes(prev =>
            prev.map(n =>
                selectedNodes.includes(n.id)
                    ? { ...n, groupId }
                    : n
            )
        )

        toast.success(`Grupo criado (${selectedNodes.length} sensores)`)
    }

    const handleUpdateNode = useCallback(
        (id: string, data: Partial<SensorNode>) => {
            setNodes(prev => {
                const base = prev.find(n => n.id === id)
                if (!base) return prev

                if (base.groupId) {
                    return prev.map(n =>
                        n.groupId === base.groupId ? { ...n, ...data } : n
                    )
                }

                return prev.map(n => (n.id === id ? { ...n, ...data } : n))
            })
        },
        []
    )

    const handleDeleteNode = useCallback((id: string) => {
        setNodes(prev => prev.filter(n => n.id !== id))
        setEdges(prev => prev.filter(e => e.source !== id && e.target !== id))
        setSelectedNodeId(null)
        setSelectedNodes([])
    }, [])

    /* ---------- Run algorithm ---------- */

    const mutation = useMutation({
        mutationFn: (body: TopologyRequest) => runAlgorithm(topologyId, body),
        onSuccess: data => {
            setResult(data)
            toast.success(data.message ?? 'Algoritmo executado')
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

    const selectedNode =
        nodes.find(n => n.id === selectedNodeId) ?? null

    /* ---------- Render ---------- */
    const groups = nodes.reduce<Record<string, SensorNode[]>>((acc, node) => {
        if (!node.groupId) return acc
        acc[node.groupId] = acc[node.groupId] || []
        acc[node.groupId].push(node)
        return acc
    }, {})

    const ungrouped = nodes.filter(n => !n.groupId)
    return (
        <div className="p-4 max-w-[1600px] mx-auto space-y-4">
            <div className="flex justify-between">
                <Button variant="ghost" size="icon" onClick={() => navigate('/networks')}>
                    <ArrowLeft />
                </Button>

                <TopologyIO
                    name={`topology-${topologyId ?? 'new'}`}
                    description=""
                    nodes={nodes}
                    edges={edges}
                    onImport={({ name, description, nodes, edges }) => {
                        setNodes(nodes)
                        setEdges(edges)
                        setSelectedNodeId(null)
                        setSelectedNodes([])
                        setResult(null)

                        toast.success('Topologia importada')
                    }}
                />
                <input
                    className="border rounded px-2 py-1 text-sm"
                    placeholder="Nome da topologia"
                    value={topologyName}
                    onChange={(e) => setTopologyName(e.target.value)}
                />

                <div className="flex gap-2">
                    <Button
                        size="sm"
                        variant={groupMode ? 'default' : 'outline'}
                        onClick={() => {
                            setGroupMode(!groupMode)
                            setSelectedNodes([])
                        }}
                    >
                        <Layers className="w-4 h-4 mr-1" />
                        Grupo
                    </Button>

                    {groupMode && (
                        <Button size="sm" onClick={createGroupFromSelection}>
                            Criar grupo
                        </Button>
                    )}

                    <Button
                        size="sm"
                        variant={edgeMode ? 'default' : 'outline'}
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
                    <Button
                        size="sm"
                        onClick={handleSave}
                        disabled={isSaving || saveMutation.isPending}
                    >
                        Save
                    </Button>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4">
                <Card>
                    <CardContent className="p-0 h-[500px]">
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

                <Card>
                    <CardHeader>
                        <CardTitle className="text-sm">Sensor</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <NodeProperties
                            node={selectedNode}
                            onUpdate={handleUpdateNode}
                            onDelete={handleDeleteNode}
                        />
                    </CardContent>
                </Card>
            </div>
            <Card>
                <CardHeader>
                    <CardTitle className="text-sm flex items-center gap-2">
                        <Layers className="w-4 h-4" />
                        Grupos de Sensores
                    </CardTitle>
                </CardHeader>

                <CardContent className="space-y-4">
                    {Object.keys(groups).length === 0 && ungrouped.length === 0 && (
                        <p className="text-sm text-muted-foreground">
                            Nenhum sensor criado ainda
                        </p>
                    )}

                    {/* Grupos */}
                    {Object.entries(groups).map(([groupId, sensors]) => (
                        <div key={groupId} className="border rounded-lg p-3">
                            <div className="text-xs font-semibold mb-2">
                                Grupo {groupId}
                            </div>

                            <div className="flex flex-wrap gap-2">
                                {sensors.map(s => (
                                    <span
                                        key={s.id}
                                        className="px-2 py-1 rounded-md bg-muted text-xs font-mono"
                                    >
                            {s.id}
                        </span>
                                ))}
                            </div>
                        </div>
                    ))}

                    {/* Sensores sem grupo */}
                    {ungrouped.length > 0 && (
                        <div className="border rounded-lg p-3">
                            <div className="text-xs font-semibold mb-2">
                                Sensores sem grupo
                            </div>

                            <div className="flex flex-wrap gap-2">
                                {ungrouped.map(s => (
                                    <span
                                        key={s.id}
                                        className="px-2 py-1 rounded-md bg-muted text-xs font-mono"
                                    >
                            {s.id}
                        </span>
                                ))}
                            </div>
                        </div>
                    )}
                </CardContent>
            </Card>

            <DutyCycleTimeline
                assignments={result?.sensors}
                message={result?.message}
                performance={result?.performance}

            />
        </div>
    )
}