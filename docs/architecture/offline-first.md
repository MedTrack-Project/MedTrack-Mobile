# Offline First

O MedTrack Mobile ja possui partes do comportamento offline, especialmente para captura de scans 
quando nao ha conectividade.

## Objetivo

Garantir que o usuario consiga continuar fluxos essenciais mesmo com conexao ruim, intermitente 
ou indisponivel.

## Comportamento atual

### Dados de usuario e medicamentos

No login/sincronizacao, `LoginUseCase` autentica e solicita ao `MedicationRepository` a atualizacao de
medicamentos no backend, salva em Room e agenda notificacoes.

### Scan offline

Quando a camera detecta que o app esta offline:

```text
CameraViewModel
  -> QueueOfflineScanUseCase
  -> ScanRepository.enqueue
  -> ScanQueueDao.insert
  -> WorkManager agenda ScanUpload
```

O item e salvo em `scan_queue` com status `PENDENTE`.

### Processamento posterior

`ScanUpload` executa `ProcessOfflineScanQueueUseCase`, que busca scans pendentes e solicita o reenvio
quando o WorkManager executa o job. O worker notifica o usuario quando o medicamento e processado.

## Fonte de verdade

A diretriz do projeto e tratar Room como fonte local de verdade para dados necessarios ao uso do app.

Para novas features:

- UI le primeiro do banco local quando fizer sentido.
- Sincronizacao remota atualiza o banco local.
- Estados pendentes devem ser persistidos antes da tentativa remota.

## Pontos a evoluir

- Fila de sincronizacao para confirmacoes de medicamento quando a API falhar.
- Estados padronizados para `PENDENTE`, `ENVIANDO`, `CONCLUIDO` e `ERRO`.
- Politica explicita de retry e backoff.
- Estrategia de conflito entre local e remoto.
