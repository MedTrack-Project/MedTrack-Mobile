# Injecao de Dependencias

O projeto usa Hilt para injecao de dependencias.

## Entrada

- `MedTrackApp` usa `@HiltAndroidApp`.
- `MainActivity` usa `@AndroidEntryPoint`.
- ViewModels usam `@HiltViewModel`.

## Modulos

Os modulos atuais ficam em `di/`:

- `DatabaseModule`
- `NetworkModule`
- `RepositoryModule`

### Estrutura atual

````text
di/
├── DatabaseModule.kt
├── NetworkModule.kt
└── RepositoryModule.kt
````

## DatabaseModule

Fornece:

- `AppDatabase` singleton;
- DAOs individuais consumidos pelos repositories e schedulers.

## NetworkModule

Fornece:

- `OkHttpClient`
- `Retrofit`
- `ApiService`
- `ApiEndpoints`

## Escopos

- Banco, Retrofit, ApiService e repositories sao singletons.
- ViewModels sao escopadas ao ciclo de vida da navegacao/tela.
- `SessionManager` e singleton e acessa `SharedPreferences`.

## Bindings de fronteira

`RepositoryModule` conecta contratos de dominio a implementacoes de autenticacao, medicamentos,
scan, fila offline, sessao, agendamento, relogio e dispatchers. Ele tambem liga o contrato de camera
da UI ao adaptador CameraX.

## Boas praticas

- Preferir injecao por construtor.
- Evitar singletons manuais fora de casos Android/Room ja existentes.
- Nao instanciar repositories diretamente em telas.
- Nao acessar `BuildConfig` fora de modulos de configuracao, exceto quando houver justificativa clara.
