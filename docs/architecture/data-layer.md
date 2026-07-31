# Camada de Dados

A camada de dados vive em `app/src/main/java/com/medtrack/mobile/data`.

Ela concentra persistencia local, integracao HTTP, sessao, repositories e configuracao de dependencias.

## Responsabilidades

- Buscar dados no backend via Retrofit.
- Persistir dados locais via Room.
- Guardar token de sessao via `SessionManager`.
- Coordenar sincronizacao, cache e fila offline.
- Converter DTOs e Entities em modelos de dominio.

## Estrutura

```text
data/
├── local/
│   ├── AppDatabase.kt
│   ├── Migrations.kt
│   ├── Converters.kt
│   ├── daos/
│   └── entity/
├── remote/
│   ├── ApiService.kt
│   ├── dto/
│   └── mapper/
├── repository/
└── session/
```

## Repositories

- `AuthRepository`: login, recuperacao e persistencia do token JWT.
- `MedicamentoRepository`: sincroniza usuario e medicamentos, agenda notificacoes e confirma uso de medicamento.
- `ScanRepository`: envia imagens para o servico de scan, salva scans offline e agenda processamento via WorkManager.

Repositories devem ser a unica porta de entrada da UI/ViewModel para dados persistidos ou remotos.

## Persistencia local

O projeto usa Room com `AppDatabase`, atualmente na versao `8`.

Entities registradas:

- `UsuarioEntity`
- `MedicamentoEntity`
- `NotificacaoEntity`
- `ConfirmacaoEntity`
- `ScanQueueItem`

DAOs principais:

- `UsuarioDao`
- `MedicamentoV2Dao`
- `NotificacaoDao`
- `ConfirmacaoDao`
- `ScanQueueDao`

## Integracao remota

`ApiService` define endpoints para:

- login mobile
- dados do usuario
- lista de medicamentos
- confirmacao de medicamento
- envio de imagem para scan

Retrofit e configurado em `NetworkModule` com Gson e OkHttp logging.

## Configuracao de endpoints

Os endpoints sao expostos por `BuildConfig`:

- `MEDTRACK_API_BASE_URL`
- `MEDTRACK_SCAN_URL`

Eles podem vir de variaveis de ambiente, propriedades Gradle ou `local.properties`. Apenas debug
possui fallback local. Release exige configuracao explicita e HTTPS.

## Convencao de modelos

- DTO: contrato com API.
- Entity: tabela Room.
- Domain: modelo usado pela aplicacao.
- Mapper: conversao explicita entre as camadas.
