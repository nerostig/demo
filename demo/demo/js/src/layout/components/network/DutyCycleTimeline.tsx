import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card"

/* ---------- Types ---------- */

interface PerformanceMetrics {
    executionTimeMs: number
    memoryUsedKb: number
}

interface SensorAssignment {
    id: string
    x: number | null
    y: number | null
    dutyCycleParameter: number | null
}

interface Props {
    assignments?: SensorAssignment[]
    message?: string | null,
    performance?: PerformanceMetrics
}

/* ---------- Component ---------- */

export default function DutyCycleTimeline({
                                              assignments,
                                              message,
                                              performance
                                          }: Props) {
    if (!assignments || assignments.length === 0) return null

    return (
        <Card>
            <CardHeader className="pb-3 space-y-1">
                <CardTitle className="text-sm font-semibold">
                    Duty Cycle por Sensor
                </CardTitle>

                {/* MESSAGE  */}
                {message && (
                    <p className="text-xs text-muted-foreground">
                        {message}
                    </p>
                )}

                {/* MÉTRICAS DO ALGORITMO */}
                {performance && (
                    <div className="grid grid-cols-2 gap-4 pt-2 text-xs">
                        <div>
                            <span className="text-muted-foreground">Tempo execução:</span>
                            <span className="ml-1 font-mono">
                {performance.executionTimeMs} ms
            </span>
                        </div>

                        <div>
                            <span className="text-muted-foreground">Memória:</span>
                            <span className="ml-1 font-mono">
                {performance.memoryUsedKb} KB
            </span>
                        </div>
                    </div>
                )}
            </CardHeader>

            <CardContent className="space-y-3">

                {/* Header */}
                <div className="grid grid-cols-3 text-xs text-muted-foreground font-medium">
                    <div>Sensor</div>
                    <div>Duty Cycle</div>
                    <div>Visualização</div>
                </div>

                {/* Rows */}
                {assignments.map(sensor => {
                    const value = sensor.dutyCycleParameter
                    const percent = value !== null
                        ? Math.round(value )
                        : 0

                    return (
                        <div
                            key={sensor.id}
                            className="grid grid-cols-3 items-center gap-2 text-sm"
                        >
                            {/* Sensor ID */}
                            <div className="font-mono text-xs">
                                {sensor.id}
                            </div>

                            {/* Value */}
                            <div className="font-mono text-xs">
                                {value !== null ? `${percent}%` : 'N/A'}
                            </div>

                            {/* Bar */}
                            <div className="h-2 w-full bg-muted rounded relative overflow-hidden">
                                <div
                                    className="h-full bg-cyan-500 transition-all"
                                    style={{
                                        width: `${percent}%`,
                                    }}
                                />
                            </div>
                        </div>
                    )
                })}

            </CardContent>
        </Card>
    )
}