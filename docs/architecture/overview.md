# Visao Geral da Arquitetura

O MedTrack Mobile e um aplicativo Android nativo em Kotlin para acompanhamento de medicacao, validacao por camera/OCR, notificacoes e sincronizacao com backend.

## Arquitetura adotada

O projeto segue uma arquitetura em camadas, com separacao proxima de MVVM e Clean Architecture pragmatica:

- `ui`: telas em Jetpack Compose, componentes, navegacao e ViewModels.
- `domain`: modelos de dominio, regras de negocio simples, services e use cases.
- `data`: persistencia local, rede, DTOs, repositories, sessao.
- `di`: injeção de depnedências.
- `utils`: utilitarios transversais, notificacoes, conectividade e excecoes.

## Fluxo principal de dados

```text
Tela Compose
  -> ViewModel
  -> Repository
  -> Room / Retrofit / WorkManager / Camera service
  -> Mapper
  -> Domain model
  -> ViewModel state
  -> Tela Compose
```

## Principios

- A UI nao deve acessar Retrofit, Room ou SharedPreferences diretamente.
- Repositories coordenam dados locais, remotos e regras de persistencia.
- Modelos de dominio sao os objetos preferidos para consumo pela aplicacao.
- DTOs ficam restritos a integracao remota.
- Entities ficam restritas a persistencia Room.
- Mappers fazem a traducao entre DTO, Entity e Domain.
- Operacoes de IO devem executar fora da Main Thread, usando coroutines.
- Hilt e a fonte padrao para injecao de dependencias.

## Pacotes principais

```text
app/src/main/java/com/example/piec_1/
├── core/
│   └── config/
├── data/
│   ├── local/
│   ├── remote/
│   ├── repository/
│   └── session/
├── di/
├── domain/
│   ├── model/
│   ├── service/
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
