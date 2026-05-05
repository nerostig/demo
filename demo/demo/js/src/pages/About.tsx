import React from 'react';
import { Radio, GitBranch, Palette, Clock, Zap, Shield } from 'lucide-react';
import {Card, CardContent, CardHeader, CardTitle} from "../layout/components/ui/card";

const features = [
    {
        icon: Radio,
        title: 'Redes de Sensores',
        description: 'Modelo visual de topologias de redes de sensores sem fios com relações de vizinhança.',
    },
    {
        icon: Palette,
        title: 'Coloração de Grafos',
        description: 'Algoritmo de coloração por backtracking em árvore geradora, com restrição de coprimalidade entre vizinhos.',
    },
    {
        icon: GitBranch,
        title: 'Árvore Geradora',
        description: 'Construção automática de spanning tree via BFS para ordenação dos nós no backtracking.',
    },
    {
        icon: Clock,
        title: 'Ciclo de Trabalho',
        description: 'Planeamento automático de slots de atividade/inatividade para redução do consumo energético.',
    },
    {
        icon: Zap,
        title: 'Avaliação de Desempenho',
        description: 'Métricas de tempo de execução, nós visitados e número de backtrackings do algoritmo.',
    },
    {
        icon: Shield,
        title: 'Restrição de Coprimalidade',
        description: 'Vizinhos na rede recebem cores (slots) coprimos, evitando colisões de comunicação.',
    },
];

export default function About() {
    return (
        <div className="p-6 lg:p-8 max-w-4xl mx-auto space-y-8">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Sobre a Aplicação</h1>
                <p className="text-muted-foreground mt-2 leading-relaxed">
                    Esta aplicação realiza o planeamento automático da política de ciclo de trabalho
                    (duty cycling) de cada sensor de uma rede, utilizando coloração de grafos com
                    backtracking em árvore geradora.
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle className="text-base">Como Funciona</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4 text-sm text-muted-foreground leading-relaxed">
                    <p>
                        <strong className="text-foreground">1. Modelação da Rede:</strong> Defina a topologia da rede
                        adicionando sensores (nós) e as suas relações de vizinhança (arestas) no editor visual.
                    </p>
                    <p>
                        <strong className="text-foreground">2. Configuração:</strong> Para cada sensor, defina o
                        percentual de tempo de atividade desejado. Configure o número total de slots do ciclo.
                    </p>
                    <p>
                        <strong className="text-foreground">3. Coloração:</strong> O algoritmo percorre a árvore geradora
                        (BFS) e atribui cores (representando slots) usando backtracking. A restrição é que vizinhos
                        devem receber valores coprimos (GCD = 1), garantindo que não transmitam simultaneamente.
                    </p>
                    <p>
                        <strong className="text-foreground">4. Resultado:</strong> Cada sensor recebe um slot de ativação
                        no ciclo de trabalho. O cronograma visual mostra quando cada sensor está ativo ou inativo.
                    </p>
                </CardContent>
            </Card>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {features.map(f => (
                    <Card key={f.title}>
                        <CardContent className="p-5 flex items-start gap-3">
                            <div className="w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center flex-shrink-0">
                                <f.icon className="w-4 h-4 text-primary" />
                            </div>
                            <div>
                                <h3 className="text-sm font-semibold">{f.title}</h3>
                                <p className="text-xs text-muted-foreground mt-0.5 leading-relaxed">{f.description}</p>
                            </div>
                        </CardContent>
                    </Card>
                ))}
            </div>

            <Card>
                <CardHeader>
                    <CardTitle className="text-base">Referências</CardTitle>
                </CardHeader>
                <CardContent className="text-sm text-muted-foreground">
                    <p>
                        Carrano, R. C., Passos, D., Magalhaes, L. C., & Albuquerque, C. V. (2013).
                        Survey and taxonomy of duty cycling mechanisms in wireless sensor networks.
                        <em> IEEE Communications Surveys & Tutorials</em>, 16(1), 181-194.
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}