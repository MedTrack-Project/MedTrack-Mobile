# Geracao de APK

## APK debug

```powershell
.\gradlew.bat :app:assembleDebug
```

Saida esperada:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## APK release

O build de release exige `MEDTRACK_API_BASE_URL` e `MEDTRACK_SCAN_URL` em HTTPS. Consulte
`docs/setup/build-release.md` antes de executar:

```powershell
.\gradlew.bat :app:assembleRelease
```

Saida esperada:

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## Instalacao via ADB

Com um dispositivo conectado:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Checklist manual

- Login.
- Lista de medicamentos e horarios.
- Notificacao.
- Camera.
- Scan online.
- Scan offline.
- Confirmacao de medicamento.
