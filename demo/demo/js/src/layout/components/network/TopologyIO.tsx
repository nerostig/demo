import React, { useRef } from 'react'
import { Button } from '../ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card'
import { Upload, Download, FileText, Image } from 'lucide-react'
import { toast } from 'sonner'
import html2canvas from 'html2canvas'

export default function TopologyIO({ name, description, nodes, edges, onImport }) {
    const fileInputRef = useRef<HTMLInputElement>(null)
    const svgInputRef = useRef<HTMLInputElement>(null)

    /* ================= EXPORT JSON ================= */
    const handleExportJSON = () => {
        const data = { name, description, nodes, edges }

        const blob = new Blob([JSON.stringify(data, null, 2)], {
            type: 'application/json'
        })

        const url = URL.createObjectURL(blob)

        const a = document.createElement('a')
        a.href = url
        a.download = `${name || 'topologia'}.json`
        a.click()

        URL.revokeObjectURL(url)

        toast.success('JSON exportado!')
    }

    /* ================= EXPORT PDF (imagem do canvas) ================= */
    const handleExportPDF = async () => {
        const canvasElement = document.querySelector('canvas') as HTMLElement

        if (!canvasElement) {
            toast.error('Canvas não encontrado')
            return
        }

        const canvas = await html2canvas(canvasElement, {
            backgroundColor: '#ffffff',
            scale: 2,
        })

        const imgData = canvas.toDataURL('image/png')

        const link = document.createElement('a')
        link.href = imgData
        link.download = `${name || 'topologia'}.png`
        link.click()

        toast.success('Imagem exportada (PDF substituído por PNG)')
    }

    /* ================= EXPORT SVG ================= */
    const handleExportSVG = () => {
        const svgNodes = nodes.map(n => `
        <g data-node="true"
           data-id="${n.id}"
           data-x="${n.x}"
           data-y="${n.y}"
           data-dc="${n.desiredDutyCycle}"
        >
            <circle cx="${n.x}" cy="${n.y}" r="10" fill="blue" />
            <text x="${n.x + 12}" y="${n.y + 4}" font-size="10">${n.id}</text>
        </g>
    `).join('\n')

        const svgEdges = edges.map(e => {
            const a = nodes.find(n => n.id === e.source)
            const b = nodes.find(n => n.id === e.target)
            if (!a || !b) return ''

            return `
            <line
                data-edge="true"
                data-source="${e.source}"
                data-target="${e.target}"
                x1="${a.x}" y1="${a.y}"
                x2="${b.x}" y2="${b.y}"
                stroke="black"
            />
        `
        }).join('\n')

        const svg = `
<svg xmlns="http://www.w3.org/2000/svg" width="1000" height="800">
    ${svgEdges}
    ${svgNodes}
</svg>
`

        const blob = new Blob([svg], { type: 'image/svg+xml' })
        const url = URL.createObjectURL(blob)

        const a = document.createElement('a')
        a.href = url
        a.download = `${name || 'topologia'}.svg`
        a.click()

        URL.revokeObjectURL(url)
    }
    /* ================= IMPORT JSON ================= */
    const handleImportJSON = (e) => {
        const file = e.target.files?.[0]
        if (!file) return

        const reader = new FileReader()

        reader.onload = (ev) => {
            try {
                const data = JSON.parse(ev.target?.result as string)

                if (!Array.isArray(data.nodes) || !Array.isArray(data.edges)) {
                    toast.error('JSON inválido')
                    return
                }

                onImport(data)
                toast.success('JSON importado!')
            } catch {
                toast.error('Erro ao ler JSON')
            }
        }

        reader.readAsText(file)
        e.target.value = ''
    }

    /* ================= IMPORT SVG ================= */
    const handleImportSVG = async (e) => {
        const file = e.target.files?.[0]
        if (!file) return

        const text = await file.text()

        const parser = new DOMParser()
        const svgDoc = parser.parseFromString(text, 'image/svg+xml')

        const nodesEl = svgDoc.querySelectorAll('[data-node="true"]')
        const edgesEl = svgDoc.querySelectorAll('[data-edge="true"]')

        const nodesParsed = Array.from(nodesEl).map((el: any) => ({
            id: el.getAttribute('data-id'),
            x: Number(el.getAttribute('data-x')),
            y: Number(el.getAttribute('data-y')),
            desiredDutyCycle: Number(el.getAttribute('data-dc') || 0.5),
            tolerance: 0.1,
        }))

        const edgesParsed = Array.from(edgesEl).map((el: any) => ({
            source: el.getAttribute('data-source'),
            target: el.getAttribute('data-target'),
        }))

        onImport({
            name: file.name.replace('.svg', ''),
            description: '',
            nodes: nodesParsed,
            edges: edgesParsed,
        })

        toast.success('SVG importado!')
    }
    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-sm">
                    Importar / Exportar
                </CardTitle>
            </CardHeader>

            <CardContent className="space-y-2">

                {/* INPUTS */}
                <input
                    ref={fileInputRef}
                    type="file"
                    accept=".json"
                    hidden
                    onChange={handleImportJSON}
                />

                <input
                    ref={svgInputRef}
                    type="file"
                    accept=".svg"
                    hidden
                    onChange={handleImportSVG}
                />

                {/* BUTTONS */}
                <Button onClick={() => fileInputRef.current?.click()} variant="outline">
                    <Upload className="w-4 h-4 mr-2" />
                    Importar JSON
                </Button>

                <Button onClick={() => svgInputRef.current?.click()} variant="outline">
                    <FileText className="w-4 h-4 mr-2" />
                    Importar SVG
                </Button>

                <Button onClick={handleExportJSON} variant="outline">
                    <Download className="w-4 h-4 mr-2" />
                    Exportar JSON
                </Button>

                <Button onClick={handleExportSVG} variant="outline">
                    <FileText className="w-4 h-4 mr-2" />
                    Exportar SVG
                </Button>

                <Button onClick={handleExportPDF} variant="outline">
                    <Image className="w-4 h-4 mr-2" />
                    Exportar imagem
                </Button>

                <p className="text-xs text-muted-foreground">
                    JSON = dados | SVG = editável | PNG = visual
                </p>

            </CardContent>
        </Card>
    )
}