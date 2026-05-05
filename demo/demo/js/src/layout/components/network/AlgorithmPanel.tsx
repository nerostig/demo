import React from 'react';
import {Card, CardContent, CardHeader, CardTitle} from "../ui/card";

import { Play, RotateCcw } from 'lucide-react';
import { Label } from "@radix-ui/react-label";
import {Slider} from "../ui/slider";
import {Button} from "../ui/button";

/* ---------- Props ---------- */

interface AlgorithmPanelProps {
    onRun: () => void
    onReset?: () => void
    isRunning: boolean
    hasResult: boolean
}

/* ---------- Component ---------- */

export default function AlgorithmPanel({
                                           onRun,
                                           onReset,
                                           isRunning,
                                           hasResult,
                                       }: AlgorithmPanelProps) {
    return (
        <Card>
            <CardHeader className="pb-3">
                <CardTitle className="text-sm font-semibold">
                    Algoritmo de Coloração
                </CardTitle>
            </CardHeader>

            <CardContent className="space-y-4">
                {/* (Opcional) parâmetros futuros */}
                <div className="space-y-2">
                    <Label className="text-xs">
                        Parâmetros
                    </Label>

                    <Slider
                        disabled
                        className="opacity-50"
                    />

                    <p className="text-xs text-muted-foreground">
                        Define o número total de intervalos de tempo no ciclo de trabalho.
                    </p>
                </div>

                {/* Botões */}
                <div className="flex gap-2">
                    <Button
                        onClick={onRun}
                        disabled={isRunning}
                        className="flex-1 gap-2"
                    >
                        <Play className="w-4 h-4" />
                        {isRunning ? 'A calcular…' : 'Executar Algoritmo'}
                    </Button>

                    {hasResult && onReset && (
                        <Button
                            variant="outline"
                            onClick={onReset}
                            className="gap-2"
                        >
                            <RotateCcw className="w-4 h-4" />
                        </Button>
                    )}
                </div>

                {/* Info */}
                <div className="text-xs text-muted-foreground space-y-1">
                    <p><strong>Método:</strong> Algoritmo no backend</p>
                    <p><strong>Restrição:</strong> Vizinhos não podem partilhar o mesmo intervalo</p>
                </div>
            </CardContent>
        </Card>
    )
}