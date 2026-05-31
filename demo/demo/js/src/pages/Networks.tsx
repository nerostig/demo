import React from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'

import {Plus, Network, Trash2, Radio, Badge} from 'lucide-react'
import { motion } from 'framer-motion'
import {Button} from "../layout/components/ui/button";
import {Card, CardContent} from "../layout/components/ui/card";

/* =========================
   Tipos vindos do backend
   ========================= */

type SensorResultOutput = {
    id: string
    dutyCycleParameter: number | null
}

type ScheduledTopologyOutput = {
    id: number
    name?: string | null
    sensors: SensorResultOutput[]
    adjacency: Record<string, string[]>
}

/* =========================
   Funções API
   ========================= */

async function fetchTopologies(): Promise<ScheduledTopologyOutput[]> {
    const res = await fetch('/api/topology')

    if (!res.ok) {
        throw new Error('Erro ao carregar topologias')
    }

    return res.json()
}

async function deleteTopology(id: number): Promise<void> {
    const res = await fetch(`/api/topology/${id}`, {
        method: 'DELETE',
    })

    if (!res.ok) {
        throw new Error('Erro ao apagar topologia')
    }
}

/* =========================
   Componente
   ========================= */

export default function Networks() {
    const queryClient = useQueryClient()

    const { data: networks = [], isLoading } = useQuery({
        queryKey: ['topologies'],
        queryFn: fetchTopologies,
    })

    const deleteMutation = useMutation({
        mutationFn: deleteTopology,
        onSuccess: () =>
            queryClient.invalidateQueries({ queryKey: ['topologies'] }),
    })

    return (
        <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight">
                        Redes de Sensores
                    </h1>
                    <p className="text-sm text-muted-foreground mt-0.5">
                        Topologias planeadas
                    </p>
                </div>

                <Link to="/editor/new">
                    <Button className="gap-2">
                        <Plus className="w-4 h-4" />
                        Nova Rede
                    </Button>
                </Link>
            </div>

            {/* Loading */}
            {isLoading ? (
                <div className="flex items-center justify-center py-20">
                    <div className="w-6 h-6 border-2 border-muted border-t-primary rounded-full animate-spin" />
                </div>
            ) : networks.length === 0 ? (
                /* Empty state */
                <Card>
                    <CardContent className="flex flex-col items-center justify-center py-16">
                        <Radio className="w-12 h-12 text-muted-foreground/20 mb-4" />
                        <p className="text-muted-foreground">
                            Nenhuma topologia criada ainda.
                        </p>
                        <Link to="/editor/new">
                            <Button variant="outline" className="mt-4 gap-2">
                                <Plus className="w-4 h-4" />
                                Criar topologia
                            </Button>
                        </Link>
                    </CardContent>
                </Card>
            ) : (
                /* Grid */
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {networks.map((net: ScheduledTopologyOutput, i: number) => {
                        const sensorCount = net.sensors.length

                        const edgeCount3 = Object.values(net.adjacency).reduce<number>(
                            (acc, v) => acc + (v as string[]).length,
                            0
                    )
                        const edgeCount = new Set(
                            Object.entries(net.adjacency).flatMap(([src, targets]) =>
                                targets.map(tgt => [src, tgt].sort().join('|'))
                            )
                        ).size

                        return (
                            <motion.div
                                key={net.id}
                                initial={{ opacity: 0, y: 20 }}
                                animate={{ opacity: 1, y: 0 }}
                                transition={{ delay: i * 0.05 }}
                            >
                                <Card className="group hover:shadow-lg transition-all duration-300 hover:border-primary/20">
                                    <CardContent className="p-5">
                                        <div className="flex items-start justify-between mb-3">
                                            <div className="flex items-center gap-2">
                                                <div className="w-8 h-8 rounded-md bg-primary/10 flex items-center justify-center">
                                                    <Network className="w-4 h-4 text-primary" />
                                                </div>
                                                <div>
                                                    <h3 className="text-sm font-semibold">
                                                        {net.name?.trim()
                                                            ? net.name
                                                            : `Topologia #${net.id}`}
                                                    </h3>
                                                    <Badge className="text-xs mt-0.5">
                                                        Planeada
                                                    </Badge>
                                                </div>
                                            </div>

                                            <Button
                                                variant="ghost"
                                                size="icon"
                                                className="h-7 w-7 text-muted-foreground hover:text-destructive opacity-0 group-hover:opacity-100 transition-opacity"
                                                onClick={(e:React.MouseEvent<HTMLButtonElement>) => {
                                                    e.preventDefault()
                                                    deleteMutation.mutate(net.id)
                                                }}
                                            >
                                                <Trash2 className="w-3.5 h-3.5" />
                                            </Button>
                                        </div>

                                        <div className="flex items-center gap-4 text-xs text-muted-foreground">
                                            <span>{sensorCount} sensores</span>
                                            <span>{edgeCount} ligações</span>
                                        </div>

                                        <Link to={`/editor/${net.id}`}>
                                            <Button
                                                variant="outline"
                                                size="sm"
                                                className="w-full mt-4 text-xs"
                                            >
                                                Abrir Editor
                                            </Button>
                                        </Link>
                                    </CardContent>
                                </Card>
                            </motion.div>
                        )
                    })}
                </div>
            )}
        </div>
    )
}