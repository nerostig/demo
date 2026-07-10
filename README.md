Projecto criado por 
André Carrilho 49420 Grupo 45


Guia de Utilização da Aplicação de Planeamento de Ciclo de Trabalho
1. Autenticação
Ao iniciar a aplicação, é apresentada a página de Login, onde o utilizador deve introduzir:
•	Username 
•	Password 
Em seguida, deve selecionar o botão Login para aceder à aplicação.
Caso ainda não possua uma conta, deverá selecionar a opção "Não tens conta?” e carregar no botão “Registar", sendo redirecionado para a página de registo.
Registo de Utilizador
Na página de registo, o utilizador deverá preencher:
•	Username 
•	Password 
Por exemplo:
•	Username: Rita 
•	Password: password123 
Após selecionar o botão Registar, a aplicação apresenta a mensagem:
"Registo realizado com sucesso!"
De seguida, o utilizador é automaticamente redirecionado para a página de Login, onde poderá introduzir as credenciais acabadas de criar e autenticar-se na aplicação.
2.	Acesso à Aplicação Dashboard Inicial
Ao aceder à aplicação, o utilizador é direcionado para o Dashboard, que apresenta uma visão geral do sistema, incluindo:

•	Número de redes de sensores criadas
•	Número total de sensores registados
•	Número de redes incompletas ou não planeadas
O Dashboard disponibiliza ainda o botão “Nova Rede”, que permite criar uma nova topologia.


3.	Criação de uma Nova Rede

Ao selecionar “Nova Rede”, o utilizador é redirecionado para o Editor de Rede. Adição de Sensores
Os sensores são adicionados ao clicar no canvas. Cada sensor possui:

•	Identificador (ex: S1, S2, …)
•	Parâmetro de duty cycle (ajustável)
•	Tolerância (valor configurável)

Criação de Ligações

Para criar ligações entre sensores:

•	Ativar o modo de ligação
•	Selecionar dois sensores no canvas

Agrupamento de Sensores (opcional)

•	Ativar o modo de grupo
•	Selecionar dois ou mais sensores
•	Criar grupo

Sensores pertencentes ao mesmo grupo partilham configurações comuns.


4.	Edição de uma Rede Existente

A partir do Dashboard, o utilizador pode selecionar uma rede previamente criada. No Editor de Rede é possível:
•	Mover sensores livremente no canvas
•	Alterar parâmetros individuais (duty cycle e tolerância)
•	Gerir grupos de sensores


5.	Execução do Algoritmo de Planeamento

No Editor de Rede, o utilizador pode executar o algoritmo através do botão “Executar”.
O algoritmo realiza:

•	Atribuição de duty cycles a cada sensor
•	Verificação de restrições entre sensores adjacentes
•	Geração de uma solução válida (total ou parcial)

Resultados

Os resultados são apresentados na componente Duty Cycle Timeline, incluindo:

•	Duty cycle atribuído a cada sensor
•	Métricas de desempenho (tempo de execução e memória utilizada)
•	Mensagens de estado da execução


6.	Exportação e Importação de Dados Exportação
A aplicação permite exportar a topologia nos seguintes formatos:

•	JSON (persistência da estrutura da rede)
•	SVG (representação vetorial)
•	PNG (imagem estática do canvas)

Importação

É possível importar topologias previamente guardadas em formato:

•	JSON
•	SVG


7.	Gestão de Redes

No Dashboard é possível:

•	Visualizar redes existentes
•	Consultar número de sensores e ligações
•	Aceder ao editor de cada rede
•	Eliminar redes



8.	Considerações Importantes

•	A rede deve estar conectada para garantir execução correta do algoritmo
•	A definição adequada de duty cycle e tolerância influencia diretamente a qualidade da solução


9.	Fluxo de Utilização Recomendado

1.	Criar uma nova rede
2.	Adicionar sensores ao canvas
3.	Definir ligações entre sensores
4.	Configurar parâmetros (duty cycle e tolerância)
5.	Executar o algoritmo de planeamento
6.	Visualizar resultados
7.	Guardar ou exportar a topologia
