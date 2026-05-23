# Setup Local

## Pre-requisitos

- Android Studio recente.
- JDK 21.
- Android SDK com suporte ao `compileSdk` configurado no projeto.
- Dispositivo ou emulador Android.

## Configuracao de URLs

Configure as propriedades abaixo em `local.properties` ou `gradle.properties`:

```properties
MEDTRACK_API_BASE_URL=http://seu-host:8081/
MEDTRACK_SCAN_URL=http://seu-host:8000/detect
```

Se nao forem definidas, o app usa os fallbacks de `app/build.gradle.kts`.

## Abrir no Android Studio

1. Abra a pasta raiz do projeto.
2. Aguarde a sincronizacao Gradle.
3. Selecione um dispositivo/emulador.
4. Execute o modulo `app`.

## Comandos uteis

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

## Permissoes

O app solicita permissao de camera e, em Android 13 ou superior, notificacoes.
