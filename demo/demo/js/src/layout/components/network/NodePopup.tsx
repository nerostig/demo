import React, { useEffect, useRef, useState } from 'react'
import { Slider } from '../ui/slider'
import { Input } from '../ui/input'
import { Button } from '../ui/button'
import { Trash2, X } from 'lucide-react'
import type { SensorNode } from './NetworkCanvas'

interface Props {
    node: SensorNode
    canvasSize: { width: number; height: number }
    onUpdate?: (id: string, data: Partial<SensorNode>) => void
    onDelete?: (id: string) => void
    onClose?: () => void
}

/**
 * Popup flutuante sobre o nó
 * Coordenadas relativas ao container do canvas
 */
export default function NodePopup({
                                      node,
                                      canvasSize,
                                      onUpdate,
                                      onDelete,
                                      onClose,
                                  }: Props) {
    const ref = useRef<HTMLDivElement | null>(null)

    /* ================= STATE ================= */

    // dutyCycle em percentagem (UI)
    const [dutyPercent, setDutyPercent] = useState(
        Math.round(node.desiredDutyCycle )
    )

    const [tolerance, setTolerance] = useState(node.tolerance)

    /* ================= SYNC ================= */

    useEffect(() => {
        setDutyPercent(Math.round(node.desiredDutyCycle ))
        setTolerance(node.tolerance)
    }, [node])

    /* ================= CLICK OUTSIDE ================= */

    useEffect(() => {
        const handler = (e: MouseEvent) => {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                onClose?.()
            }
        }
        document.addEventListener('mousedown', handler)
        return () => document.removeEventListener('mousedown', handler)
    }, [onClose])

    /* ================= POSITION ================= */

    const POPUP_W = 240
    const POPUP_H = 220
    const OFFSET = 30

    let left = node.x - POPUP_W / 2
    let top = node.y + OFFSET

    if (left < 8) left = 8
    if (left + POPUP_W > canvasSize.width - 8)
        left = canvasSize.width - POPUP_W - 8

    if (top + POPUP_H > canvasSize.height - 8)
        top = node.y - POPUP_H - OFFSET

    /* ================= RENDER ================= */

    return (
        <div
            ref={ref}
            className="absolute z-20 bg-card border border-border rounded-xl shadow-xl p-4 space-y-3"
            style={{ left, top, width: POPUP_W }}
            onMouseDown={e => e.stopPropagation()}
        >
            {/* HEADER */}
            <div className="flex justify-between items-center">
                <span className="text-xs font-semibold">Sensor</span>
                <div className="flex gap-1">
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={() => {
                            onDelete?.(node.id)
                            onClose?.()
                        }}
                    >
                        <Trash2 className="w-3 h-3" />
                    </Button>
                    <Button
                        variant="ghost"
                        size="icon"
                        className="h-6 w-6"
                        onClick={onClose}
                    >
                        <X className="w-3 h-3" />
                    </Button>
                </div>
            </div>

            {/* DUTY CYCLE */}
            <div className="space-y-1">
                <div className="text-xs flex justify-between">
                    <span>Duty cycle</span>
                    <span className="font-mono">{dutyPercent}%</span>
                </div>

                <Slider
                    value={[dutyPercent]}
                    min={1}
                    max={100}
                    step={1}
                    onValueChange={([v]) => {
                        setDutyPercent(v)
                        onUpdate?.(node.id, {
                            desiredDutyCycle: v /// 100, // 🔴 backend continua 0–1
                        })
                    }}
                />
            </div>

            {/* TOLERANCE */}
            <div className="space-y-1">
                <label className="text-xs">Tolerância</label>
                <Input
                    type="number"
                    step="0.01"
                    min="0"
                    value={tolerance}
                    onChange={e => {
                        const v = Number(e.target.value)
                        setTolerance(v)
                        onUpdate?.(node.id, { tolerance: v })
                    }}
                    className="h-7 text-xs"
                />
            </div>
        </div>
    )
}