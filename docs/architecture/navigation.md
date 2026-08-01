# Navegacao

A navegacao do app fica em `ui/navigation`.

## Arquivos

- `AppRoutes.kt`: nomes de rotas e helpers.
- `AppNavigation.kt`: grafo de navegacao Compose.
- `NavigationManager.kt`: canal consumivel usado para navegar a partir de notificacoes ou intents.
- `AppIntentContract.kt`: acoes e chaves de Intent centralizadas e validadas.

## Rotas atuais

- inicial
- login
- principal
- camera
- confirmacao
- esqueci senha
- redefinir senha
- dose com argumentos validados `medicamentoId`, `data` e `horario`

## Fluxos importantes

### Login

```text
TelaInicial -> TelaLogin -> TelaPrincipal
```

### Scan online

```text
TelaPrincipal -> TelaCamera -> CameraViewModel -> ScanRepository -> TelaConfirmacao
```

### Notificacao de medicamento

```text
NotificationReceiver -> deep link tipado de dose -> TelaDoseHorario
```

### Scan offline processado

```text
ScanUpload
  -> persistencia de resultado por UUID
  -> notificacao ACTION_OPEN_CONFIRMATION com a referencia
  -> MainActivity.processIntent
  -> NavigationManager
  -> AppNavigation
  -> TelaConfirmacao
```

## Convencoes

- Centralizar novas rotas em `AppRoutes`.
- Validar argumentos com o tipo de rota antes de renderizar uma tela.
- Transportar dados grandes/sensiveis por referencia persistida de uso unico, nunca como JSON em extras.
- Evitar strings de rota soltas dentro das telas.
- Preferir callbacks de navegacao passados pela tela pai.
- Evitar que componentes reutilizaveis conhecam `NavController`.
