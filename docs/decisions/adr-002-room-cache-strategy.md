# ADR-002: Room como cache local e fonte operacional

## Status

Aceita.

## Contexto

O MedTrack Mobile precisa funcionar em contexto real de celular, onde a conexao pode cair. 
O app tambem precisa manter medicamentos, confirmacoes, notificacoes e scans pendentes.

## Decisao

Usar Room como armazenamento local principal para dados operacionais do app.

O banco local deve guardar:

- usuario
- medicamentos
- notificacoes
- confirmacoes
- fila de scans offline

## Consequencias

- Mudancas de schema exigem migration.
- Repositories devem persistir dados relevantes antes ou durante chamadas remotas.
- UI deve depender de dados de dominio vindos de repositories, nao de DTOs.
- Confirmacoes e filas offline podem sobreviver a fechamento do app.
