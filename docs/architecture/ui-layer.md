# Camada de UI

A camada de UI vive em `app/src/main/java/com/medtrack/mobile/ui`.

Ela usa Jetpack Compose, ViewModel, StateFlow e Navigation Compose.

## Estrutura

```text
ui/
├── components/
├── navigation/
├── screen/
│   └── viewmodel/
└── theme/
```

## Telas

As telas principais ficam em `ui/screen`:

- `TelaInicial`
- `TelaLogin`
- `TelaPrincipal`
- `TelaCamera`
- `TelaConfirmacao`
- `TelaEsqueciSenha`
- `TelaRedefinirSenha`

## ViewModels

ViewModels ficam em `ui/screen/viewmodel` e sao anotadas com `@HiltViewModel`.

ViewModels existentes:

- `LoginViewModel`
- `CameraViewModel`
- `MedicamentoViewModel`

Responsabilidades esperadas:

- Expor estado observavel para a tela.
- Chamar casos de uso injetados.
- Traduzir excecoes em mensagens de UI.
- Evitar regra de negocio pesada.

## Estado da UI

Os fluxos principais usam UDF: cada ViewModel expoe um unico `StateFlow<UiState>` imutavel,
recebe acoes por `Intent` e publica efeitos unicos por `SharedFlow`. Compose coleta estado com
`collectAsStateWithLifecycle`; navegacao e dialogos nao sao representados por booleanos persistentes.

O binding da CameraX pertence ao controlador lifecycle-aware usado pela tela. O ViewModel da camera
mantem apenas referencias de imagem, selecao da dose e estado de processamento.

Padrao atual observavel:

- `Idle`
- `Loading`
- `Success`
- `Error`

## Componentes

Componentes reutilizaveis ficam em `ui/components`.

Exemplos:

- cards de conteudo e status
- dialogos
- entrada de texto
- lista de horarios
- overlay de camera

Componentes devem receber estado e callbacks por parametro, sem acessar repository ou DAO diretamente.
