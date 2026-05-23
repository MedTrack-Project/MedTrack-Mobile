# Navegacao

A navegacao do app fica em `ui/navigation`.

## Arquivos

- `AppRoutes.kt`: nomes de rotas e helpers.
- `AppNavigation.kt`: grafo de navegacao Compose.
- `NavigationManager.kt`: estado compartilhado usado para navegar a partir de notificacoes ou intents.

## Rotas atuais

- inicial
- login
- principal
- camera
- confirmacao
- esqueci senha
- redefinir senha
- camera por notificacao com argumentos `medicamentoId` e `horario`

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
NotificationReceiver
  -> MainActivity
  -> AppRoutes.cameraDeepLink(...)
  -> TelaCamera
```

### Scan offline processado

```text
ScanUpload
  -> notificacao OPEN_CONFIRMATION
  -> MainActivity.processIntent
  -> NavigationManager
  -> AppNavigation
  -> TelaConfirmacao
```

## Convencoes

- Centralizar novas rotas em `AppRoutes`.
- Evitar strings de rota soltas dentro das telas.
- Preferir callbacks de navegacao passados pela tela pai.
- Evitar que componentes reutilizaveis conhecam `NavController`.
