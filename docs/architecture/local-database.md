# Banco Local

O banco local usa Room e fica centralizado em `AppDatabase`.

## Configuracao

- Nome do banco: `app_database_db`
- Versao atual: `10`
- Schema exportado em `app/schemas`
- Migrations em `data/local/Migrations.kt`

## Entities

- `UsuarioEntity`
- `MedicamentoEntity`
- `NotificacaoEntity`
- `ConfirmacaoEntity`
- `ScanQueueItem`

## DAOs

- `UsuarioDao`
- `MedicamentoV2Dao`
- `NotificacaoDao`
- `ConfirmacaoDao`
- `ScanQueueDao`

## Conversores

`Converters.kt` concentra conversoes necessarias para tipos nao nativos do SQLite/Room.

## Migrations

O banco suporta migrations comprovadas por schemas versionados:

- `MIGRATION_8_9`
- `MIGRATION_9_10`

A menor origem suportada e a versao 8. Os schemas 4 e 5 nunca foram exportados e suas migrations
nunca foram implementadas; reconstruir SQL sem conhecer os schemas poderia corromper dados. Origens
1–7 falham de forma controlada e nunca usam migration destrutiva. A decisao esta no ADR 004.

`AppDatabaseMigrationTest` valida em dispositivo os caminhos `8 -> 10` e `9 -> 10`, incluindo
preservacao de dados e validacao estrutural pelo Room.

## Regras

- Toda mudanca de schema deve incrementar a versao do banco.
- Toda mudanca de schema deve criar migration.
- Atualizar schemas exportados apos alteracoes Room.
- Revisar o diff do JSON de schema no mesmo PR.
- Adicionar teste do caminho entre cada origem suportada e a versao atual.
- Evitar consultas SQL fora de DAOs.
- Repositories devem orquestrar DAOs, nao telas ou ViewModels.
