# CURRENT_TASK.md — Documentação da estrutura do projeto

O principal ponto aqui é: no mobile, arquitetura importa MUITO porque o fluxo de estado, 
persistência local, sincronização e navegação tendem a virar caos rapidamente se não houver 
documentação clara.

Sugiro algo assim:

# Estrutura sugerida

```text
docs/
├── architecture/
│   ├── overview.md
│   ├── data-layer.md
│   ├── domain-layer.md
│   ├── ui-layer.md
│   ├── dependency-injection.md
│   ├── navigation.md
│   ├── offline-first.md
│   ├── sync-strategy.md
│   ├── local-database.md
│   └── api-integration.md
│
├── context/
│   ├── product-context.md
│   ├── technical-context.md
│   ├── conventions.md
│   ├── stack.md
│   └── glossary.md
│
├── tasks/
│   ├── CURRENT_TASK.md
│   ├── TASK_TEMPLATE.md
│   └── archive/
│
├── decisions/
│   ├── adr-001-clean-architecture.md
│   ├── adr-002-room-cache-strategy.md
│   └── adr-003-sync-queue.md
│
├── setup/
│   ├── local-setup.md
│   ├── build-release.md
│   └── apk-generation.md
│
└── README.md
```

# O que cada parte resolve

## `architecture/`

Aqui é visão técnica estrutural.

### `overview.md`

Explica:

* arquitetura geral
* fluxo dos dados
* separação das camadas
* princípios adotados

Exemplo:

* MVVM
* Repository Pattern
* Offline First
* Clean-ish Architecture
* Single Source of Truth

### `data-layer.md`

Explica:

* Room
* Retrofit/Ktor
* DTOs
* Entities
* Mappers
* Repository
* Cache
* Sync

Esse provavelmente será um dos arquivos mais importantes.

### `domain-layer.md`

Aqui você explica:

* models de domínio
* regras de negócio
* use cases (se tiver)
* validações
* separação entre entity/domain/dto

Especialmente importante porque vocês já fizeram:

* `MedicamentoEntity`
* `MedicamentoDomain`
* mappers
* sincronização

### `ui-layer.md`

Explica:

* Compose/XML
* ViewModels
* StateFlow
* UI State
* eventos
* padrão de telas

Você pode até documentar:

* Loading/Error/Success pattern
* Snackbar handling
* navegação
* lifecycle

### `dependency-injection.md`

Eu tiraria da pasta `data`.

DI normalmente é transversal ao projeto inteiro.

Esse arquivo explica:

* Hilt/Koin
* módulos
* providers
* singleton strategy
* escopo de ViewModel

### `offline-first.md`

Esse aqui é MUITO diferencial.

Porque mobile sério quase sempre sofre com:

* conexão ruim
* sincronização
* inconsistência local/remota

Documenta:

* comportamento offline
* prioridade local/remota
* filas de sync
* retry
* conflitos

Isso ajuda MUITO o Codex.

### `sync-strategy.md`

Se vocês possuem:

* confirmação sincronizada
* flags locais
* fila de envio

documenta tudo.

Especialmente:

* quando sincroniza
* quem dispara
* como trata erro
* como evita duplicidade

# `context/`

Aqui é mais “contexto mental do projeto”.

### `technical-context.md`

Algo como:

```md
- Kotlin
- Jetpack Compose
- Room
- Retrofit
- Coroutines
- StateFlow
- Hilt
- MVVM
```

E:

* padrões adotados
* convenções
* princípios

### `conventions.md`

Esse arquivo ajuda absurdamente IA.

Exemplo:

```md
# Convenções

- DTO -> usado apenas na camada de rede
- Entity -> persistência Room
- Domain -> consumo da aplicação
- Repository -> única fonte de dados
- ViewModel -> sem regra pesada
- Mapper -> extensão Kotlin
```

Isso evita que o Codex “quebre” a arquitetura.

# `tasks/`

Separar tasks foi uma ótima decisão sua.

Eu manteria:

* `CURRENT_TASK.md`
* template
* histórico arquivado

Isso deixa o agente contextualizado sem poluir o contexto principal.


# Algo MUITO interessante que pode ser incluido

## ADRs (`decisions/`)

Isso é nível projeto profissional.

ADR = Architecture Decision Record.

Exemplo:

```text
adr-001-use-room-as-single-source-of-truth.md
```

Você documenta:

* problema
* decisão
* consequência

Exemplo:

```md
# Contexto

Precisamos suportar uso offline.

# Decisão

Room será a fonte principal de verdade.

# Consequências

- UI nunca acessa Retrofit diretamente
- sincronização obrigatória
- cache persistente
```

Isso ajuda:

* onboarding
* manutenção
* IA
* futuras refatorações

---

# Minha sugestão principal

Eu faria:

## Separação:

* architecture/
* context/
* tasks/
* decisions/

Porque isso escala MUITO bem.

Especialmente no teu caso:

* backend
* mobile
* IA/model training
* sincronização
* segurança
* offline-first
