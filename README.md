# MedTrack Mobile

Aplicativo Android nativo para acompanhamento de medicamentos, lembretes de dose e validação por
foto. CameraX captura a imagem e a API de scan realiza o reconhecimento; quando não há conexão, o
WorkManager mantém o envio na fila.

O projeto está em modernização e os endpoints definitivos do backend e do scan ainda não estão
disponíveis. Builds debug podem usar serviços locais. Um APK release somente pode ser publicado após
configuração e aprovação do environment `production`.

## Stack

- Kotlin, Coroutines e Flow;
- Jetpack Compose e Material 3;
- MVVM/UDF com casos de uso e domínio independente de Android;
- Hilt e KSP;
- Room;
- Retrofit, OkHttp e Gson;
- CameraX;
- WorkManager e AlarmManager;
- Gradle Kotlin DSL com Version Catalog.

Consulte [stack](docs/context/stack.md) e [visão geral da arquitetura](docs/architecture/overview.md)
para mais contexto.

## Pré-requisitos

- Git;
- JDK 21;
- Android SDK com `compileSdk` 37 e platform tools;
- Android Studio compatível com AGP 9.2.1 e Kotlin 2.3.21;
- dispositivo ou emulador com Android 8.0/API 26 ou superior.

Use sempre o Gradle Wrapper versionado; não é necessário instalar Gradle globalmente.

## Configuração local

Clone o repositório e crie a configuração local:

```bash
git clone https://github.com/MedTrack-Project/MedTrack-Mobile.git
cd MedTrack-Mobile
cp local.properties.example local.properties
```

As propriedades esperadas são:

```properties
MEDTRACK_API_BASE_URL=<url-base-do-backend>
MEDTRACK_SCAN_URL=<url-completa-da-api-de-scan>
```

`MEDTRACK_API_BASE_URL` deve terminar com `/`. Debug aceita HTTP somente para `localhost` e
`10.0.2.2`; os releases aceitam apenas HTTPS. Credenciais e tokens nunca pertencem a esses campos.

As regras completas de precedência, rede e configuração estão em:

- [setup local](docs/setup/local-setup.md);
- [ambientes e segurança de rede](docs/security/environment-and-network.md);
- [contrato das APIs](docs/contracts/api-v1.md).

## Build e execução

No Linux/macOS:

```bash
./gradlew assembleDebug
```

No Windows:

```powershell
.\gradlew.bat assembleDebug
```

O APK debug fica em `app/build/outputs/apk/debug/app-debug.apk`. Abra o projeto no Android Studio,
selecione o módulo `app` e execute em um dispositivo ou emulador.

## Qualidade e testes

O gate local principal executa verificação de segredos, KtLint, Detekt, Android Lint, testes
unitários e limites de cobertura:

```bash
./gradlew qualityCheck
```

Para a suíte instrumentada:

```bash
./gradlew connectedDebugAndroidTest
```

Antes de abrir um Pull Request, execute também:

```bash
./gradlew assembleDebug
git diff --check
```

Consulte a [estratégia de testes](docs/testing/test-strategy.md) para execução por contexto,
relatórios, determinismo e gates Kover.

## Arquitetura

O código é organizado em `ui`, `domain`, `data` e `di`:

```text
Compose -> ViewModel -> Use case -> Repository -> Room/Retrofit/WorkManager
```

As principais referências são:

- [arquitetura](docs/architecture/overview.md);
- [camada de UI](docs/architecture/ui-layer.md);
- [camada de domínio](docs/architecture/domain-layer.md);
- [camada de dados](docs/architecture/data-layer.md);
- [integração com APIs](docs/architecture/api-integration.md);
- [estratégia offline](docs/architecture/offline-first.md);
- [decisões arquiteturais](docs/decisions/).

## CI/CD e release

Todo Pull Request para `main` executa três checks obrigatórios:

- `Dependency review`;
- `Quality and debug APK`;
- `Instrumented tests (API 35)`.

Tags estáveis `vMAJOR.MINOR.PATCH` acionam o workflow de release. O job aguarda aprovação do
environment `production`, valida endpoints/secrets, gera APK assinado e minificado, confere tamanho e
assinatura, produz SBOM/checksum, valida os assets baixados e instala o APK em emulador antes de
publicar apenas o APK e o SHA-256 no GitHub Release.

Enquanto as APIs definitivas não estiverem publicadas, não crie tags estáveis. Para validação local,
use o APK fake sem publicar descrito em [build e release](docs/setup/build-release.md).

Documentação operacional:

- [geração e verificação de APK](docs/setup/apk-generation.md);
- [release hardening](docs/release/release-hardening.md);
- [rollback e revogação](docs/release/rollback-runbook.md);
- [configuração de governança](docs/governance/repository-settings.md).

## Troubleshooting

### Endpoint rejeitado

Confira HTTPS, barra final da URL base e a precedência descrita no setup. Release rejeita valores
ausentes, `.invalid`, `localhost`, `10.0.2.2` e HTTP.

### Dispositivo físico não acessa `10.0.2.2`

Esse endereço pertence ao emulador. Use um endpoint HTTPS acessível pelo dispositivo; não amplie
cleartext sem revisão do Network Security Config.

### Falha de ADB ou instrumentação

Execute `adb devices`, confirme que o dispositivo está autorizado e repita apenas o grupo afetado.
Veja a [estratégia de testes](docs/testing/test-strategy.md).

### Falha de migration

Não use destructive migration. Verifique os schemas versionados e siga a documentação do
[banco local](docs/architecture/local-database.md).

### Release sem assinatura ou versão

Use `releaseReadiness` e configure todas as variáveis listadas em
[build e release](docs/setup/build-release.md). Nunca versione o keystore.

## Contribuição e segurança

Pull Requests são obrigatórios. Leia [CONTRIBUTING.md](CONTRIBUTING.md), use Conventional Commits e
preencha o template do PR. Vulnerabilidades não devem ser abertas como issue pública; siga
[SECURITY.md](SECURITY.md).

CODEOWNERS:

- [Yann Leão](https://github.com/YannLeao);
- [Ellen Rocha](https://github.com/EllenRocha1);
- [Clara Ferreira](https://github.com/MClaraFerreira5).

## Licença

Projeto acadêmico desenvolvido para a disciplina Projeto Interdisciplinar de Engenharia da
Computação 1 da Universidade Federal Rural de Pernambuco, Unidade Acadêmica de Belo Jardim.
