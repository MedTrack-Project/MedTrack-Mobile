# Build e Release

## Debug

Para gerar build debug:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Release

Para gerar build release:

```powershell
.\gradlew.bat :app:assembleRelease
```

O build release atual esta com `isMinifyEnabled = false`.

## Pontos de atencao antes de release

- Configurar URLs corretas para backend e scan.
- Revisar logs HTTP em release.
- Validar permissoes no Manifest.
- Executar testes unitarios e teste manual de fluxos principais.
- Confirmar migrations Room.
- Testar notificacoes em dispositivo real.

## Versionamento

O versionamento fica em `app/build.gradle.kts`:

- `versionCode`
- `versionName`
