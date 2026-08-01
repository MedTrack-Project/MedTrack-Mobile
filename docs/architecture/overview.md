# Visao Geral da Arquitetura

O MedTrack Mobile e um aplicativo Android nativo em Kotlin para acompanhamento de medicacao, validacao por camera/OCR, notificacoes e sincronizacao com backend.

## Arquitetura adotada

O projeto segue uma arquitetura em camadas, com separacao proxima de MVVM e Clean Architecture pragmatica:

- `ui`: telas em Jetpack Compose, componentes, navegacao e ViewModels.
- `domain`: modelos, erros tipados, contratos e casos de uso sem dependencias Android.
- `data`: persistencia local, rede, DTOs, repositories, sessao.
- `di`: injeção de depnedências.
- `utils`: adaptadores Android ainda compartilhados, notificacoes e conectividade.

## Fluxo principal de dados

```text
Tela Compose
  -> ViewModel
  -> Use case / contrato de dominio
  -> Repository de data
  -> Room / Retrofit / WorkManager / Camera service
  -> Mapper
  -> Domain model
  -> ViewModel state
  -> Tela Compose
```

## Principios

- A UI nao deve acessar Retrofit, Room ou SharedPreferences diretamente.
- ViewModels dependem de casos de uso ou interfaces, nunca de repositories concretos de `data`.
- Domain nao importa Android, Compose, Room, Retrofit, OkHttp ou `data`.
- Repositories coordenam dados locais, remotos e regras de persistencia.
- Modelos de dominio sao os objetos preferidos para consumo pela aplicacao.
- DTOs ficam restritos a integracao remota.
- Entities ficam restritas a persistencia Room.
- Mappers fazem a traducao entre DTO, Entity e Domain.
- Operacoes de IO devem executar fora da Main Thread, usando coroutines.
- Hilt e a fonte padrao para injecao de dependencias.

## Pacotes principais

```text
app/src/main/java/com/medtrack/mobile/
├── core/
│   └── config/
├── data/
│   ├── local/
│   ├── camera/
│   ├── mapper/
│   ├── remote/
│   ├── repository/
│   ├── session/
│   └── worker/
├── di/
├── domain/
│   ├── model/
│   ├── repository/
│   ├── service/
│   ├── time/
│   └── usecase/
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screen/
│   └── theme/
└── utils/
```

## Entrada da aplicacao

- `MedTrackApp` habilita Hilt com `@HiltAndroidApp`.
- `MainActivity` e o ponto de entrada, processa intents de notificacao, solicita permissoes e renderiza `AppNavigation`.
- `AppNavigation` concentra as rotas e instancia ViewModels com Hilt.
