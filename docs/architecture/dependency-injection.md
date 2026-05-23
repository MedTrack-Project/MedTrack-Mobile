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

### Estrutura atual

````text
di/
├── DatabaseModule.kt
└── NetworkModule.kt
````

## DatabaseModule

Fornece:

- `AppDatabase` singleton.

DAOs sao obtidos atualmente a partir do banco dentro dos repositories.

## NetworkModule

Fornece:

- `OkHttpClient`
- `Retrofit`
- `ApiService`
- `@Named("ScanUrl") String`

## Escopos

- Banco, Retrofit, ApiService e repositories sao singletons.
- ViewModels sao escopadas ao ciclo de vida da navegacao/tela.
- `SessionManager` e singleton e acessa `SharedPreferences`.

## Boas praticas

- Preferir injecao por construtor.
- Evitar singletons manuais fora de casos Android/Room ja existentes.
- Nao instanciar repositories diretamente em telas.
- Nao acessar `BuildConfig` fora de modulos de configuracao, exceto quando houver justificativa clara.
