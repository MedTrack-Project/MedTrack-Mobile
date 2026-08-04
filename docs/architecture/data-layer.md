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
├── camera/
├── local/
│   ├── AppDatabase.kt
│   ├── Migrations.kt
│   ├── Converters.kt
│   ├── daos/
│   ├── entity/
│   └── source/
├── remote/
│   ├── ApiService.kt
│   ├── dto/
│   └── source/
├── mapper/local/
├── repository/
├── session/
├── system/
└── worker/
```

## Repositories

- `AuthRepository`: login, recuperacao e persistencia do token JWT.
- `MedicamentoRepository`: sincroniza usuario e medicamentos, agenda notificacoes e confirma uso de medicamento.
- `ScanRepository`: envia imagens para o servico de scan, salva scans offline e agenda processamento via WorkManager.

Repositories implementam contratos do dominio e coordenam data sources locais/remotos. ViewModels
os acessam somente por casos de uso. A substituicao do snapshot de usuario e medicamentos ocorre
em uma transacao Room e o cache local e a source of truth apresentada pela aplicacao.

## Persistencia local

O projeto usa Room com `AppDatabase`, atualmente na versao `10`. O snapshot correspondente fica em
`app/schemas` para permitir revisão de schema e testes de migration contra versões preservadas.

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

Retrofit e configurado em `NetworkModule` com uma unica instancia Gson, autenticacao OkHttp
centralizada e logging sem bodies. `RemoteCallExecutor` converte rede, payload invalido, sessao,
4xx e 5xx em erros de dominio previsiveis.

O contrato congelado e sua matriz de compatibilidade estao em `docs/contracts/api-v1.md`.

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
