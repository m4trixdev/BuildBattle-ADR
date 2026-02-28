# BuildBattle

Minigame de Build Battle feito para eventos da ADR Studios

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
![Platform](https://img.shields.io/badge/Platform-Paper-blue)
![Language](https://img.shields.io/badge/Linguagem-Java-orange)
![Java Version](https://img.shields.io/badge/Java-21-red)

## 📋 Funcionalidades

- 🏗️ **Sistema de Plots** — Scaneamento automático de plots por tipo de bloco em uma área configurável
- 🎨 **Construção por Tema** — Tema anunciado no início da fase de construção
- 🗳️ **Avaliação por OP** — Apenas jogadores com OP avaliam as construções com itens de nota (1 a 5)
- 🎮 **Gamemode Automático** — OPs ficam em CREATIVE durante a avaliação; jogadores ficam em ADVENTURE
- 🔒 **Proteção de Plot** — Cada jogador só pode construir dentro do seu próprio plot
- 🚫 **Blocos Protegidos** — BARRIER e QUARTZ_BLOCK são indestrutíveis e não podem ser colocados por jogadores
- 🗑️ **Deleção de Construção** — OPs podem deletar construções durante a avaliação com o item Barrier
- 📊 **Scoreboard Dinâmica** — Atualiza em tempo real com estado, tema, tempo e pontuações
- 🏆 **Ranking Final** — Anuncia automaticamente o Top 3 ao fim do evento
- 💾 **Persistência de Dados** — Spawn, material do plot e área salvos em `data.yml`

## 🚀 Instalação

### Requisitos

- Java 21+
- Paper 1.21.1+
- Gradle 8+

### Passos de Instalação

1. Baixe o arquivo `BuildBattle-1.0.0.jar` dos releases
2. Coloque o JAR na pasta `plugins` do seu servidor
3. Reinicie o servidor
4. Configure o `config.yml` em `plugins/BuildBattle/`
5. Use os comandos `/bb` para configurar o evento

### Build Manual

```bash
# Clone o repositório
git clone https://github.com/m4trixdev/BuildBattle.git
cd BuildBattle

# Compilar e gerar JAR
./gradlew shadowJar
```

O JAR gerado estará em `build/libs/BuildBattle-1.0.0.jar`

## ⚙️ Configuração

### config.yml

```yaml
event:
  build-time: 1800      # Duração da fase de construção em segundos
  vote-time: 8          # Tempo por plot durante a votação em segundos
  plot-size: 25         # Tamanho do plot (raio a partir do centro)

protection:
  block-tnt: true       # Impedir colocação de TNT
  block-lava: true      # Impedir colocação de lava
  block-water: true     # Impedir colocação de água

scoreboard:
  enabled: true
  title: "&6Build Battle"
  lines:
    - "&7Estado: &f%state%"
    - "&7Tema: &f%theme%"
    - "&7Tempo: &f%time%"
    - "&7Plots: &f%plots%"

messages:
  prefix: "&6[BB]&r "
  phase1-start: "&aJogadores teleportados para os plots!"
  phase2-start: "&aConstrução iniciada! Tema: &f%theme%"
  phase2-title: "&a&lConstrução Iniciada!"
  phase2-subtitle: "&7Tema: &f%theme%"
  phase3-start: "&eVotação iniciada!"
  next-plot: "&eAvaliando (%i%/%total%): &f%player%"
  vote-ok: "&aVoto registrado: &f%score% pontos"
  already-voted: "&cVocê já votou neste plot."
  own-plot: "&cVocê não pode votar no próprio plot."
  no-permission: "&cSem permissão."
  deleted: "&cConstrução de &f%player% &cdeletada."
  voting-done: "&aVotação encerrada! Use /bb start 4 para anunciar o resultado."
  phase4-start: "&6&lResultado Final:"
  top1: "&6#1 &f%player% &7- &e%pts% pts"
  top2: "&7#2 &f%player% &7- &e%pts% pts"
  top3: "&c#3 &f%player% &7- &e%pts% pts"
  spawn-set: "&aSpawn salvo."
  stopped: "&cEvento encerrado."
  outside: "&cFique dentro do seu plot!"
  time-up: "&c&lTempo esgotado!"
```

## 🎮 Comandos

#### `/bb set spawn`
Define o spawn do evento na sua posição atual.

**Permissão:** `buildbattle.admin`

---

#### `/bb set spawnblock`
Olhe para um bloco e registre seu material como marcador de plot. Todos os blocos desse tipo encontrados na área definida serão tratados como centros de plot.

**Permissão:** `buildbattle.admin`

---

#### `/bb set area <pos1|pos2>`
Define os dois cantos da área de escaneamento de plots.

**Permissão:** `buildbattle.admin`

**Exemplo:**
```
/bb set area pos1
/bb set area pos2
```

---

#### `/bb start 1 [tema]`
Escaneia a área, distribui os jogadores nos plots e os teleporta. O tema é opcional e pode ser informado após o número da fase.

**Permissão:** `buildbattle.admin`

---

#### `/bb start 2`
Inicia a fase de construção com o timer ativo e libera o CREATIVE para os jogadores.

**Permissão:** `buildbattle.admin`

---

#### `/bb start 3`
Inicia a fase de votação. Os OPs ficam em CREATIVE com os itens de avaliação; demais jogadores ficam em ADVENTURE sem itens.

**Permissão:** `buildbattle.admin`

---

#### `/bb start 4`
Finaliza a votação, calcula os pontos e anuncia o Top 3.

**Permissão:** `buildbattle.admin`

---

#### `/bb stop`
Para o evento imediatamente em qualquer fase, restaura os inventários e teleporta todos ao spawn.

**Permissão:** `buildbattle.admin`

---

#### `/bb info`
Exibe informações do evento atual: estado, tema, participantes, material do plot, área, spawn e pontuações.

**Permissão:** `buildbattle.admin`

---

#### `/bb reload`
Recarrega o `config.yml` sem reiniciar o servidor.

**Permissão:** `buildbattle.admin`

## 🔑 Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `buildbattle.admin` | Acesso total ao plugin e avaliação de construções | OP |

## 🎯 Mecânicas de Jogo

### Sistema de Plots

O admin define um tipo de bloco com `/bb set spawnblock` e uma área com `/bb set area`. Ao iniciar a Fase 1, o plugin varre toda a área bloco a bloco procurando aquele material específico, numera cada ocorrência como um plot (#1, #2, #3...) e distribui aleatoriamente um plot para cada jogador online. O bloco marcador é protegido e não pode ser quebrado por nenhum jogador.

### Fases do Evento

| Fase | Comando | Descrição |
|---|---|---|
| Fase 1 | `/bb start 1 [tema]` | Escaneia plots, distribui e teleporta jogadores |
| Fase 2 | `/bb start 2` | Inicia construção com timer, jogadores em CREATIVE |
| Fase 3 | `/bb start 3` | Votação pelos OPs, jogadores em ADVENTURE |
| Fase 4 | `/bb start 4` | Anuncia ranking e encerra o evento |

### Proteção de Plot

Durante a fase de construção, cada jogador só pode colocar e quebrar blocos dentro do seu próprio plot. Blocos colocados por um jogador são rastreados individualmente, garantindo que ele só quebre o que ele mesmo colocou. Admins com `buildbattle.admin` têm bypass completo.

### Blocos Protegidos

BARRIER e QUARTZ_BLOCK são completamente indestrugíveis por qualquer jogador em qualquer momento, e não podem ser colocados durante o evento. Isso garante que os marcadores de plot e limitadores de área nunca sejam removidos acidentalmente.

### Avaliação

Apenas jogadores com OP podem avaliar. Ao iniciar a Fase 3, o plugin teleporta todos para cada plot sequencialmente. OPs recebem 6 itens de avaliação no inventário:

| Item | Nota |
|---|---|
| Lã Vermelha | 1 ponto |
| Lã Laranja | 2 pontos |
| Lã Amarela | 3 pontos |
| Lã Verde-Limão | 4 pontos |
| Diamante | 5 pontos |
| Barrier | Deletar construção |

Cada avaliador só pode votar uma vez por plot. O dono do plot não pode votar na própria construção.

### Ranking Final

Ao executar `/bb start 4`, as pontuações de todos os plots são somadas e o Top 3 é anunciado no chat para todos os jogadores.

### Scoreboard Dinâmica

Atualiza automaticamente exibindo o estado atual do evento, o tema, o tempo restante de construção e as pontuações durante a votação.

## 🏗️ Arquitetura

### Estrutura de Pacotes

```
br.com.m4trixdev
├── Main.java
├── command/
│   └── BBCommand.java
├── config/
│   └── ConfigManager.java
├── data/
│   └── DataManager.java
├── listener/
│   ├── BlockListener.java
│   └── PlayerListener.java
├── manager/
│   ├── EventManager.java
│   ├── BBScoreboardManager.java
│   ├── PlotManager.java
│   ├── ScoreManager.java
│   └── VoteManager.java
├── model/
│   ├── EventState.java
│   ├── Plot.java
│   └── VoteItem.java
└── util/
    ├── ColorUtil.java
    └── LocationUtil.java
```

### Componentes Principais

```
Main
├── ConfigManager        → Carregamento e gestão de configurações
├── DataManager          → Persistência em data.yml
├── PlotManager          → Escaneamento, distribuição e proteção de plots
├── VoteManager          → Registro de votos por plot
├── ScoreManager         → Cálculo e ranking de pontuações
├── BBScoreboardManager  → Scoreboard dinâmica
├── EventManager         → Toda a lógica e fases do evento
└── Listeners
    ├── BlockListener    → Quebra, colocação e proteção de blocos
    └── PlayerListener   → Movimentação, interação, votação e inventário
```

### Estados do Evento

| Estado | Descrição |
|---|---|
| `WAITING` | Aguardando início |
| `PHASE_1` | Jogadores teleportados, aguardando início da construção |
| `BUILDING` | Fase de construção com timer ativo |
| `VOTING` | Fase de avaliação pelos OPs |
| `ENDED` | Evento encerrado |

## 🛠️ Build

### Requisitos

- JDK 21
- Gradle 8+

### Comandos

```bash
# Compilar
./gradlew compileJava

# Gerar JAR final
./gradlew shadowJar

# Limpar build
./gradlew clean
```

O JAR estará em `build/libs/BuildBattle-1.0.0.jar`

## 🐛 Solução de Problemas

### Evento não inicia

- Verifique se o spawn foi definido com `/bb set spawn`
- Verifique se o material do plot foi definido com `/bb set spawnblock`
- Verifique se a área foi definida com `/bb set area pos1` e `/bb set area pos2`
- Confirme que há jogadores suficientes online
- Verifique se há blocos do material configurado dentro da área definida
- Use `/bb info` para checar o estado atual das configurações

### Nenhum plot encontrado

- Confirme que os blocos do material definido existem fisicamente dentro da área configurada
- Verifique se pos1 e pos2 estão no mesmo mundo
- Use `/bb info` para checar o material e a área registrados
- Redefina o spawnblock olhando diretamente para um dos blocos de plot com `/bb set spawnblock`

### Jogador conseguindo sair do plot

- Verifique se o `plot-size` no `config.yml` corresponde ao tamanho real dos plots no mapa
- O valor de `plot-size` representa o raio em blocos a partir do centro do plot

### Scoreboard não aparece

- Verifique se `scoreboard.enabled: true` no `config.yml`
- Certifique-se de que o plugin não conflita com outros plugins de scoreboard
- Use `/bb reload` para forçar o recarregamento das configurações

### Configuração não carrega

- Verifique se o `config.yml` é um YAML válido (indentação e caracteres especiais)
- Apague o `config.yml` e reinicie o servidor para regenerar o padrão
- Revise o console por erros de parsing

## 📄 Licença

Este projeto está licenciado sob a Licença MIT.

## 👨‍💻 Autor

**M4trixDev**

- GitHub: [@m4trixdev](https://github.com/m4trixdev)

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:
- Reportar bugs
- Sugerir novas funcionalidades
- Enviar pull requests
- Melhorar a documentação

## 📞 Suporte

- Issues: [GitHub Issues](https://github.com/m4trixdev/BuildBattle/issues)
- Discussões: [GitHub Discussions](https://github.com/m4trixdev/BuildBattle/discussions)

## 🎮 Servidores Compatíveis

- Paper 1.21.1+
- Qualquer servidor rodando Paper API 1.21+

## ⚠️ Limitações Conhecidas

- Dados de votos e pontuações são armazenados em memória e limpos ao reiniciar o servidor
- Configurações de spawn, material e área persistem via `data.yml`
- Algumas funcionalidades podem conflitar com plugins que modificam a scoreboard do jogador
- Requer Paper 1.21.1+ (não compatível com versões anteriores ou Bukkit puro)

---

Feito com ❤️ para eventos da ADR Studios
