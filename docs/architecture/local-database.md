# Banco Local

O banco local usa Room e fica centralizado em `AppDatabase`.

## Configuracao

- Nome do banco: `app_database_db`
- Versao atual: `9`
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

O banco usa migrations explicitas:

- `MIGRATION_1_2`
- `MIGRATION_2_3`
- `MIGRATION_3_4`
- `MIGRATION_6_7`
- `MIGRATION_7_8`
- `MIGRATION_8_9`

`fallbackToDestructiveMigration(false)` indica que o app nao deve destruir dados automaticamente
quando faltar migration.

## Regras

- Toda mudanca de schema deve incrementar a versao do banco.
- Toda mudanca de schema deve criar migration.
- Atualizar schemas exportados apos alteracoes Room.
- Evitar consultas SQL fora de DAOs.
- Repositories devem orquestrar DAOs, nao telas ou ViewModels.
