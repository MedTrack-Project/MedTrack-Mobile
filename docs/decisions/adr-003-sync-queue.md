# ADR-003: Fila local para operacoes offline

## Status

Aceita parcialmente.

## Contexto

Scans podem acontecer sem conexao disponivel. O app precisa guardar a captura e tentar 
processa-la depois.

## Decisao

Usar uma fila local em Room para operacoes offline, com processamento posterior por WorkManager.

A implementacao atual aplica isso a scans por meio de:

- `ScanQueueItem`
- `ScanQueueDao`
- `QueueOfflineScanUseCase` e `ProcessOfflineScanQueueUseCase`
- contratos `ScanRepository` e `OfflineScanRepository` no dominio
- implementacao `ScanRepository.enqueue` em `data`
- `ScanUpload`

## Consequencias

- O usuario pode capturar imagem offline.
- O processamento pode acontecer depois, quando houver rede adequada.
- O app precisa manter estados claros de fila.
- Confirmacoes de medicamento ainda devem evoluir para estrategia equivalente quando a API falhar.
