# Integracao com API

A integracao remota usa Retrofit, OkHttp e Gson.

## Configuracao

`NetworkModule` cria:

- `OkHttpClient` com logging `BASIC` em debug e `NONE` em release
- `Retrofit` com `GsonConverterFactory`
- `ApiService`

## Endpoints atuais

```text
POST auth/mobile/login
GET  usuario/mobile
GET  medicamento/mobile/lista
POST /api/confirmacao
POST <MEDTRACK_SCAN_URL>
```

## Autenticacao

O login retorna um token JWT, salvo por `SessionManager` em `SharedPreferences`.

Repositories montam o header:

```text
Authorization: Bearer <token>
```

## DTOs

DTOs ficam em `data/remote/dto` e representam exclusivamente contratos de rede.

## Mappers remotos

`data/remote/mapper/RemoteMappers.kt` converte DTOs para modelos de dominio.

## Configuracao local

As URLs podem ser configuradas por propriedades:

- `MEDTRACK_API_BASE_URL`
- `MEDTRACK_SCAN_URL`

Essas propriedades podem vir de variaveis de ambiente, Gradle properties ou `local.properties`.
Release exige ambas em HTTPS. Debug permite HTTP somente para os hosts locais autorizados pelo
Network Security Config.

## Cuidados

- Nao expor segredos reais no repositorio.
- Nao registrar senhas, tokens, payloads, imagens ou headers de autenticacao.
- Validar respostas nulas e erros HTTP antes de acessar `body()`.
- Manter DTOs desacoplados dos modelos de dominio.
