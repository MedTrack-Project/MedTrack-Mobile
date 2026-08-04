# Contexto Tecnico

## Stack

- Kotlin
- Android nativo
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- LiveData
- Coroutines
- Hilt
- Room
- Retrofit
- OkHttp
- Gson
- CameraX
- WorkManager
- AlarmManager

## Build

- Gradle Kotlin DSL
- Android Gradle Plugin configurado por version catalog
- KSP para Room e Hilt
- Java/Kotlin target 21
- `compileSdk` 37
- `minSdk` 26
- `targetSdk` 36

## Modulos

Atualmente o projeto tem um modulo principal:

- `app`

## Estado atual da arquitetura

O projeto ja possui separacao em `data`, `di`, `domain`, `ui` e `utils`.

Alguns pontos ainda sao pragmaticos e podem evoluir:

- DI possui o pacote transversal `di`, usado como composition root.
- `StateFlow<UiState>` imutavel e intents formam o padrao de estado dos fluxos principais.
- Adapters de infraestrutura Android concentram WorkManager e CameraX fora do domínio.
- A estrategia offline existe para scan e deve ser expandida para confirmacoes.
