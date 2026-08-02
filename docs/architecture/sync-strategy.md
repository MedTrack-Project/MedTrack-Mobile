# Estrategia de Sincronizacao

Este documento descreve a sincronizacao atual e a diretriz para proximas implementacoes.

## Sincronizacao atual

### Login

Depois do login, o app usa o token para buscar:

- usuario
- medicamentos

As duas respostas sao validadas antes de qualquer escrita. Usuario e medicamentos substituem o
snapshot Room em uma unica transacao; o repository le o snapshot persistido como source of truth e
so entao agenda notificacoes. Falha parcial de rede preserva integralmente o cache anterior.

### Confirmacao de medicamento

Ao confirmar um medicamento:

1. O app encontra o medicamento local correspondente ao scan.
2. Verifica se ja existe confirmacao local para o mesmo medicamento, data e horario.
3. Envia a confirmacao para a API.
4. Persiste ou atualiza `ConfirmacaoEntity` como sincronizada em transacao.

Uma falha remota nao cria confirmacao local incorretamente marcada como concluida. O fluxo
offline-first de confirmacoes duraveis permanece planejado para a Etapa 5.

### Scans offline

Scans offline sao persistidos em `scan_queue` e processados depois por `ScanUpload`.

## Diretriz

Toda sincronizacao deve seguir este formato:

```text
persistir localmente
  -> tentar remoto
  -> marcar sincronizado
  -> manter pendente em caso de falha recuperavel
```

## Estados recomendados

- `PENDENTE`: criado localmente, ainda nao enviado.
- `ENVIANDO`: tentativa em andamento.
- `CONCLUIDO`: processado com sucesso.
- `ERRO`: falha que precisa de acao ou nova tentativa.

## Evitar duplicidade

Confirmacoes devem continuar usando uma chave logica:

- medicamento
- data
- horario

Antes de enviar uma confirmacao, verificar se ja existe registro local equivalente.

## Retry

Use WorkManager para fluxos que precisam sobreviver ao fechamento do app, queda de rede ou reinicio do dispositivo.
