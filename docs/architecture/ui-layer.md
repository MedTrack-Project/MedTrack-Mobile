# Camada de UI

A camada de UI vive em `app/src/main/java/com/medtrack/mobile/ui`.

Ela usa Jetpack Compose, ViewModel, LiveData e Navigation Compose.

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
- Chamar repositories ou services injetados.
- Traduzir excecoes em mensagens de UI.
- Evitar regra de negocio pesada.

## Estado da UI

O projeto usa `LiveData` em ViewModels atuais. Quando novas telas forem criadas, manter o padrao local ou migrar de forma planejada para `StateFlow`, evitando misturar estilos sem necessidade.

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
