import React, { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import {Card, CardContent, CardHeader, CardTitle} from "../layout/components/ui/card";
import {Badge} from "../layout/components/ui/badge";
import { Plus, Network, Radio, Activity, Zap } from "lucide-react"
import { motion } from "framer-motion"
import {Button} from "../layout/components/ui/button";

type SensorResultOutput = {
    id: string
    dutyCycleParameter: number | null
}

type ScheduledTopologyOutput = {
    id: number
    sensors: SensorResultOutput[]
    adjacency: Record<string, string[]>
}

const fadeUp = {
    initial: { opacity: 0, y: 20 },
    animate: { opacity: 1, y: 0 },
}

export default function Dashboard() {
    const [networks, setNetworks] = useState<ScheduledTopologyOutput[]>([])
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        fetch("/api/topology")
            .then(res => {
                if (!res.ok) throw new Error("Failed to load networks")
                return res.json()
            })
            .then(data => setNetworks(data))
            .catch(err => console.error(err))
            .finally(() => setIsLoading(false))
    }, [])

    /* ====== STATS ====== */

    const totalNetworks = networks.length

    const totalSensors = networks.reduce(
        (sum, n) => sum + n.sensors.length,
        0
    )

    const unassignedNetworks = networks.filter(n =>
        n.sensors.some(s => s.dutyCycleParameter === null)
    ).length

    const stats = [
        {
            label: "Redes Criadas",
            value: totalNetworks,
            icon: Network,
            color: "text-primary",
        },
        {
            label: "Redes com Sensores em Falta",
            value: unassignedNetworks,
            icon: Activity,
            color: "text-accent",
        },
        {
            label: "Total de Sensores",
            value: totalSensors,
            icon: Radio,
            color: "text-chart-3",
        },
    ]

    return (
        <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-8">
            {/* Hero */}
            <motion.div {...fadeUp} transition={{ duration: 0.5 }}>
                <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-4">
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight">
                            Planeamento de Ciclo de Trabalho
                        </h1>
                        <p className="text-muted-foreground mt-1.5">
                            Gestão de políticas de duty cycling para redes de sensores
                        </p>
                    </div>
                    <Link to="/editor/new">
                        <Button className="gap-2">
                            <Plus className="w-4 h-4" />
                            Nova Rede
                        </Button>
                    </Link>
                </div>
            </motion.div>

            {/* Stats */}
            <motion.div
                {...fadeUp}
                transition={{ duration: 0.5, delay: 0.1 }}
                className="grid grid-cols-1 sm:grid-cols-3 gap-4"
            >
                {stats.map(s => (
                    <Card key={s.label}>
                        <CardContent className="p-5">
                            <div className="flex items-center justify-between">
                                <div>
                                    <p className="text-xs text-muted-foreground uppercase tracking-wide">
                                        {s.label}
                                    </p>
                                    <p className="text-2xl font-bold mt-1 font-mono">
                                        {s.value}
                                    </p>
                                </div>
                                <div className="w-10 h-10 rounded-lg bg-muted flex items-center justify-center">
                                    <s.icon className={`w-5 h-5 ${s.color}`} />
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </motion.div>

            {/* Recent Networks */}
            <motion.div {...fadeUp} transition={{ duration: 0.5, delay: 0.2 }}>
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between">
                        <CardTitle className="text-base">
                            Redes Recentes
                        </CardTitle>
                        <Link
                            to="/networks"
                            className="text-xs text-primary hover:underline"
                        >
                            Ver todas
                        </Link>
                    </CardHeader>
                    <CardContent>
                        {isLoading ? (
                            <div className="flex items-center justify-center py-12">
                                <div className="w-6 h-6 border-2 border-muted border-t-primary rounded-full animate-spin" />
                            </div>
                        ) : networks.length === 0 ? (
                            <div className="text-center py-12">
                                <Zap className="w-10 h-10 text-muted-foreground/30 mx-auto mb-3" />
                                <p className="text-sm text-muted-foreground">
                                    Nenhuma rede criada ainda.
                                </p>
                                <Link to="/editor/new">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        className="mt-3 gap-2"
                                    >
                                        <Plus className="w-3 h-3" />
                                        Criar primeira rede
                                    </Button>
                                </Link>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {networks.slice(0, 5).map(net => {
                                    const hasUnassigned = net.sensors.some(
                                        s => s.dutyCycleParameter === null
                                    )

                                    return (
                                        <Link
                                            key={net.id}
                                            to={`/editor/${net.id}`}
                                            className="flex items-center justify-between p-3 rounded-lg hover:bg-muted/50 transition-colors group"
                                        >
                                            <div className="flex items-center gap-3">
                                                <div className="w-8 h-8 rounded-md bg-primary/10 flex items-center justify-center">
                                                    <Network className="w-4 h-4 text-primary" />
                                                </div>
                                                <div>
                                                    <p className="text-sm font-medium group-hover:text-primary">
                                                        Rede #{net.id}
                                                    </p>
                                                    <p className="text-xs text-muted-foreground">
                                                        {net.sensors.length} sensores ·{" "}
                                                        {Object.keys(net.adjacency).length} nós
                                                    </p>
                                                </div>
                                            </div>

                                            <Badge
                                                variant={
                                                    hasUnassigned
                                                        ? "secondary"
                                                        : "default"
                                                }
                                                className="text-xs"
                                            >
                                                {hasUnassigned
                                                    ? "Incompleta"
                                                    : "Planeada"}
                                            </Badge>
                                        </Link>
                                    )
                                })}
                            </div>
                        )}
                    </CardContent>
                </Card>
            </motion.div>
        </div>
    )
}