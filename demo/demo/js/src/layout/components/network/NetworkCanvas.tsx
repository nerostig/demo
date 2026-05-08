import React, { useRef, useEffect, useCallback, useState } from 'react'
import NodePopup from './NodePopup'

/* ================= TYPES ================= */

export interface SensorNode {
    id: string
    x: number
    y: number
    desiredDutyCycle: number
    tolerance: number
    label?: string
}

export interface Edge {
    source: string
    target: string
}

export interface ColorAssignment {
    node_id: string
    color: number
}

/* ================= CONFIG ================= */

const NODE_RADIUS = 20

const PALETTE = [
    '#0ea5e9', '#a855f7', '#22c55e', '#f59e0b', '#ef4444',
    '#06b6d4', '#ec4899', '#84cc16', '#f97316', '#6366f1',
    '#14b8a6', '#e11d48', '#eab308', '#8b5cf6', '#10b981',
]

/* ================= PROPS ================= */

interface Props {
    nodes: SensorNode[]
    edges: Edge[]
    colorAssignments?: ColorAssignment[]
    onAddNode?: (x: number, y: number) => void
    onMoveNode?: (id: string, x: number, y: number) => void
    onSelectNode?: (id: string) => void
    onUpdateNode?: (id: string, data: Partial<SensorNode>) => void
    onDeleteNode?: (id: string) => void
    selectedNodeId?: string | null
    edgeMode?: boolean
    edgeStart?: string | null
    interactive?: boolean
}

/* ================= COMPONENT ================= */

export default function NetworkCanvas({
                                          nodes,
                                          edges,
                                          colorAssignments,
                                          onAddNode,
                                          onMoveNode,
                                          onSelectNode,
                                          onUpdateNode,
                                          onDeleteNode,
                                          selectedNodeId,
                                          edgeMode,
                                          edgeStart,
                                          interactive = true,
                                      }: Props) {

    const canvasRef = useRef<HTMLCanvasElement | null>(null)

    const [dragging, setDragging] = useState<string | null>(null)
    const [dragMoved, setDragMoved] = useState(false)
    const [popupNodeId, setPopupNodeId] = useState<string | null>(null)
    const [canvasSize, setCanvasSize] = useState({ width: 800, height: 500 })

    /* ================= RESIZE ================= */
    /*
    useEffect(() => {
        const container = canvasRef.current?.parentElement
        if (!container) return

        const obs = new ResizeObserver(entries => {
            const { width, height } = entries[0].contentRect
            setCanvasSize({ width, height: Math.max(400, height) })
        })

        obs.observe(container)
        return () => obs.disconnect()
    }, [])
    */

    /* =================  ================= */

    const getNodeColor = useCallback((nodeId: string) => {
        if (!colorAssignments) return null
        const assignment = colorAssignments.find(a => a.node_id === nodeId)
        if (!assignment) return null
        return PALETTE[(assignment.color - 1) % PALETTE.length]
    }, [colorAssignments])

    const getNodeAtPos = (x: number, y: number) =>
        nodes.find(n => Math.hypot(n.x - x, n.y - y) <= NODE_RADIUS)

    const getCanvasPos = (e: React.MouseEvent<HTMLCanvasElement>) => {
        const rect = canvasRef.current!.getBoundingClientRect()
        return {
            x: e.clientX - rect.left,
            y: e.clientY - rect.top,
        }
    }

    /* ================= DRAW ================= */

    const draw = useCallback(() => {
        const canvas = canvasRef.current
        if (!canvas) return

        const ctx = canvas.getContext('2d')
        if (!ctx) return

        const { width, height } = canvasSize

        canvas.width = width * window.devicePixelRatio
        canvas.height = height * window.devicePixelRatio

        ctx.setTransform(1, 0, 0, 1, 0, 0)

        ctx.scale(window.devicePixelRatio, window.devicePixelRatio)

        ctx.fillStyle = 'hsl(222, 42%, 9%)'
        ctx.fillRect(0, 0, width, height)

        // Grid
        ctx.strokeStyle = 'rgba(100, 130, 180, 0.06)';
        ctx.lineWidth = 1;
        for (let x = 0; x < width; x += 40) {
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, height);
            ctx.stroke();
        }
        for (let y = 0; y < height; y += 40) {
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(width, y);
            ctx.stroke();
        }

        /* edges */
        edges.forEach(edge => {
            const source = nodes.find(n => n.id === edge.source)
            const target = nodes.find(n => n.id === edge.target)
            if (!source || !target) return

            ctx.beginPath()
            ctx.strokeStyle = 'rgba(100,160,220,0.3)'
            ctx.lineWidth = 2
            ctx.moveTo(source.x, source.y)
            ctx.lineTo(target.x, target.y)
            ctx.stroke()
        })
        // Edge mode line
        if (edgeMode && edgeStart) {
            const startNode = nodes.find(n => n.id === edgeStart);
            if (startNode) {
                ctx.beginPath();
                ctx.strokeStyle = 'rgba(14, 165, 233, 0.5)';
                ctx.lineWidth = 2;
                ctx.setLineDash([6, 4]);
                ctx.moveTo(startNode.x, startNode.y);
                ctx.lineTo(startNode.x + 40, startNode.y + 40);
                ctx.stroke();
                ctx.setLineDash([]);
            }
        }

        /* nodes */
        nodes.forEach(node => {
            const isSelected = node.id === selectedNodeId
            const color = getNodeColor(node.id)

            const baseColor = color || (isSelected ? '#0ea5e9' : '#475569')
            // Glow
            if (color || isSelected) {
                ctx.beginPath();
                const gradient = ctx.createRadialGradient(node.x, node.y, NODE_RADIUS, node.x, node.y, NODE_RADIUS * 2.5);
                gradient.addColorStop(0, baseColor + '40');
                gradient.addColorStop(1, 'transparent');
                ctx.fillStyle = gradient;
                ctx.arc(node.x, node.y, NODE_RADIUS * 2.5, 0, Math.PI * 2);
                ctx.fill();
            }

            ctx.beginPath()
            ctx.arc(node.x, node.y, NODE_RADIUS, 0, Math.PI * 2)
            ctx.fillStyle = baseColor
            ctx.fill()

            if (isSelected) {
                ctx.strokeStyle = '#fff'
                ctx.lineWidth = 2
                ctx.stroke()
            }

            ctx.fillStyle = '#fff'
            ctx.font = '11px sans-serif'
            ctx.textAlign = 'center'
            ctx.textBaseline = 'middle'
            ctx.fillText(node.label ?? node.id, node.x, node.y)

            // Color number

            if (color) {
                const assignment = colorAssignments?.find(
                    a => a.node_id === node.id
                );

                if (assignment) {
                    ctx.fillStyle = '#fff';
                    ctx.font = 'bold 9px JetBrains Mono, monospace';
                    ctx.fillText(
                        `C${assignment.color}`,
                        node.x,
                        node.y + NODE_RADIUS + 14
                    );
                }
            }

        })
    }, [nodes, edges, canvasSize, selectedNodeId, colorAssignments])

    useEffect(() => {
        draw()
    }, [draw])

    /* ================= EVENTS ================= */

    const handleMouseDown = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!interactive) return

        const pos = getCanvasPos(e)
        const node = getNodeAtPos(pos.x, pos.y)

        if (node) {
            if (edgeMode) {
                onSelectNode?.(node.id)
            } else {
                setDragging(node.id)
                setDragMoved(false)
                onSelectNode?.(node.id)
            }
        } else {
            setPopupNodeId(null)
        }
    }

    const handleMouseMove = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!dragging) return
        setDragMoved(true)

        const pos = getCanvasPos(e)
        onMoveNode?.(dragging, pos.x, pos.y)
    }

    const handleMouseUp = () => {
        if (dragging && !dragMoved) {
            setPopupNodeId(dragging)
        }
        setDragging(null)
        setDragMoved(false)
    }

    const handleDoubleClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
        if (!interactive || edgeMode) return

        const pos = getCanvasPos(e)
        const exists = getNodeAtPos(pos.x, pos.y)

        if (!exists) onAddNode?.(pos.x, pos.y)
    }

    const popupNode = nodes.find(n => n.id === popupNodeId) ?? null

    /* ================= RENDER ================= */

    return (
        <div className="relative w-full h-full min-h-[400px]">
            <canvas
                ref={canvasRef}
                width={canvasSize.width}
                height={canvasSize.height}
                style={{ width: canvasSize.width, height: canvasSize.height }}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onDoubleClick={handleDoubleClick}
                className="rounded-lg cursor-crosshair"
            />

            {/*   */}
            {interactive && nodes.length === 0 && (
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    <p className="text-muted-foreground text-sm">
                        Duplo clique para adicionar sensores · clique para editar
                    </p>
                </div>
            )}

            {popupNode && (
                <NodePopup
                    node={popupNode}
                    canvasSize={canvasSize}
                    onUpdate={onUpdateNode}
                    onDelete={onDeleteNode}
                    onClose={() => setPopupNodeId(null)}
                />
            )}
        </div>
    )
}