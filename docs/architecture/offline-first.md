# Offline First

## Scan offline

Fotos aguardando a API de IA são persistidas em `scan_queue` antes do agendamento. Cada item possui
uma chave SHA-256 do conteúdo, índice único e um estado fechado:

- `PENDING`: aguardando execução;
- `PROCESSING`: claim atômico adquirido por um Worker;
- `UPLOADED`: resposta recebida, antes dos efeitos locais finais;
- `COMPLETED`: notificação publicada; arquivo aguardando ou concluindo limpeza;
- `RETRY`: falha transitória;
- `FAILED`: falha permanente ou limite de tentativas.

O trabalho `offline_scan_upload` é único (`ExistingWorkPolicy.KEEP`), exige qualquer conexão
`CONNECTED` e usa backoff exponencial. Cinco tentativas são o limite. Rede e `5xx` são transitórios;
sessão inválida, `4xx`, payload inválido e medicamento não detectado são permanentes.

Antes do upload, o repository faz um claim condicional. Processamentos abandonados há mais de 15
minutos retornam a `RETRY`. Assim, múltiplos Workers não enviam o mesmo registro simultaneamente.

## Notificação e limpeza

A notificação de resultado usa ID e referência determinísticos por scan. Uma nova execução substitui
a anterior em vez de duplicá-la. A versão pública da notificação não contém medicamento ou dado
clínico.

Depois da notificação, o registro vai para `COMPLETED`. O arquivo é removido e, somente após remoção
confirmada, o registro é apagado. Se o sistema de arquivos negar a exclusão, o registro permanece e
a próxima execução tenta a limpeza novamente.

## WorkManager e Hilt

Workers usam `@HiltWorker`/`HiltWorkerFactory`; o initializer padrão foi removido do Manifest conforme
a integração oficial. Nenhum Worker usa EntryPoint manual.

Referências: [WorkManager — retry e backoff](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work) e [Hilt com WorkManager](https://developer.android.com/training/dependency-injection/hilt-jetpack).
