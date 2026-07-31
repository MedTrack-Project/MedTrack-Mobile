# Build e Release

## Debug

Para gerar build debug:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Release

Defina os endpoints HTTPS antes de gerar o artefato:

```bash
export MEDTRACK_API_BASE_URL="https://api.example.com/"
export MEDTRACK_SCAN_URL="https://ai.example.com/detect"
```

Para gerar build release:

```powershell
.\gradlew.bat :app:assembleRelease
```

O build falha se alguma variavel estiver ausente, nao for uma URL valida ou nao usar HTTPS.
`MEDTRACK_API_BASE_URL` deve terminar com `/`. Os valores sao configuracao publica compilada no APK;
tokens, senhas e credenciais nunca devem ser fornecidos por `BuildConfig`.

O build release atual esta com `isMinifyEnabled = false`.

## Pontos de atencao antes de release

- Configurar URLs HTTPS corretas para backend e scan.
- Confirmar que o logging HTTP esta em `NONE` no release.
- Validar permissoes no Manifest.
- Executar testes unitarios e teste manual de fluxos principais.
- Confirmar migrations Room.
- Testar notificacoes em dispositivo real.

## Versionamento

O versionamento fica em `app/build.gradle.kts`:

- `versionCode`
- `versionName`
