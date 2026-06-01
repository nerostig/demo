import React from 'react'
import { Trash2 } from 'lucide-react'
import { Label } from '@radix-ui/react-label'
import { Input } from '../ui/input'
import { Button } from '../ui/button'
import { Slider } from '../ui/slider'
import type { SensorNode } from './NetworkCanvas'

/* ---------- Props ---------- */

interface NodePropertiesProps {
    node: SensorNode | null
    onUpdate: (nodeId: string, updates: Partial<SensorNode>) => void
    onDelete: (nodeId: string) => void
}

/* ---------- Component ---------- */

export default function NodeProperties({
                                           node,
                                           onUpdate,
                                           onDelete,
                                       }: NodePropertiesProps) {
    if (!node) {
        return (
            <div className="text-sm text-muted-foreground text-center py-8">
                Selecione um sensor para editar as propriedades
            </div>
        )
    }

    const dutyPercent = Math.round(node.desiredDutyCycle)

    return (
        <div className="space-y-4">
            {/* HEADER */}
            <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold">
                    Sensor: {node.id}
                </h3>

                <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 text-destructive"
                    onClick={() => onDelete(node.id)}
                >
                    <Trash2 className="w-4 h-4" />
                </Button>
            </div>

            {/* DUTY CYCLE */}
            <div className="space-y-2">
                <Label className="text-xs">
                    Duty cycle desejado: {dutyPercent}%
                </Label>

                <Slider
                    value={[dutyPercent]}
                    min={1}
                    max={100}
                    step={1}
                    onValueChange={([v]) =>
                        onUpdate(node.id, {
                            desiredDutyCycle: v
                        })
                    }
                />
            </div>

            {/* TOLERÂNCIA */}
            <div className="space-y-2">
                <Label className="text-xs">Tolerância</Label>
                <Input
                    type="number"
                    step="0.01"
                    min="0"
                    value={node.tolerance}
                    onChange={e =>
                        onUpdate(node.id, {
                            tolerance: Number(e.target.value),
                        })
                    }
                    className="h-8 text-sm"
                />
            </div>

            {/* POSIÇÃO */}
            <div className="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                <div>X: {Math.round(node.x)}</div>
                <div>Y: {Math.round(node.y)}</div>
            </div>
        </div>
    )
}