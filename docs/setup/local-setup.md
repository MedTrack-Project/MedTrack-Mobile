# Setup Local

## Pre-requisitos

- Android Studio recente.
- JDK 21.
- Android SDK com suporte ao `compileSdk` configurado no projeto.
- Dispositivo ou emulador Android.

## Configuracao de URLs

O debug usa por padrão o host do emulador Android (`10.0.2.2`). Para apontar para outro ambiente,
configure as propriedades abaixo em `local.properties`:

```properties
MEDTRACK_API_BASE_URL=http://10.0.2.2:8081/
MEDTRACK_SCAN_URL=http://10.0.2.2:8000/detect
```

`MEDTRACK_API_BASE_URL` deve terminar com `/`. As propriedades tambem podem ser fornecidas por
variaveis de ambiente ou `-P`, nessa ordem de precedencia:

1. variavel de ambiente;
2. Gradle property (`-P` ou `gradle.properties`);
3. `local.properties`;
4. fallback local, somente em debug.

Cleartext e permitido em debug apenas para `10.0.2.2` e `localhost`. Para dispositivo fisico ou
outro host, prefira HTTPS; ampliar a allowlist local exige alteracao explicita do Network Security
Config de debug.

## Abrir no Android Studio

1. Abra a pasta raiz do projeto.
2. Aguarde a sincronizacao Gradle.
3. Selecione um dispositivo/emulador.
4. Execute o modulo `app`.

## Migracao do identificador do aplicativo

O namespace e o application ID atuais sao `com.medtrack.mobile`. Versoes locais anteriores usavam
`com.example.piec_1`; por isso, o Android trata a versao renomeada como outro aplicativo. Antes de
validar esta migracao, desinstale a versao antiga do dispositivo ou emulador. Dados locais da
instalacao anterior nao sao migrados automaticamente.

## Comandos uteis

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

## Permissoes

O app solicita permissao de camera e, em Android 13 ou superior, notificacoes.
