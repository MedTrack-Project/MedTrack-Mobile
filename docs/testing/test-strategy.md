# Estratégia de testes

## Pirâmide e escopo

Testes JVM são o padrão para regras de domínio, mappers, repositories e ViewModels. Fakes pequenos,
relógios fixos e `TestDispatcher` têm preferência sobre mocks de implementação. Instrumentação fica
restrita a contratos que dependem do Android: Room migrations, WorkManager, notificações e semântica
Compose.

| Risco | Fluxo | Proteção principal | Owner |
|---|---|---|---|
| P0 | login e sessão | unitário + MockWebServer | data/auth |
| P0 | confirmação de dose | repository/use case/ViewModel | medication |
| P0 | migração Room | `MigrationTestHelper` no dispositivo | data/local |
| P1 | scan online/offline | repository/use case/Worker | scan |
| P1 | lembrete e deep link | contrato instrumentado + navegação JVM | notifications |
| P1 | estados críticos de UI | Compose semantics | ui |
| P2 | câmera | adapter CameraX + dispositivo | camera |

CameraX permanece isolado da UI por `CameraController` e `CameraService`. O app não faz detecção
local: a API de scan é responsável pelo reconhecimento. Testes devem usar imagens sintéticas pequenas
e sem informações pessoais; respostas reais de backend ou IA não são fixtures determinísticas.

## Execução

Gate JVM e relatórios:

```bash
./gradlew qualityCheck
```

Para reduzir tempo e isolar falhas de ADB, execute instrumentação por contexto:

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.medtrack.mobile.ui.screen.CriticalFlowsComposeTest
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.medtrack.mobile.utils.notifications.NotificationContractTest
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.medtrack.mobile.data.local.AppDatabaseMigrationTest
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.medtrack.mobile.data.worker.ScanUploadTest
```

Cada execução gera JUnit XML e HTML em `app/build/outputs/androidTest-results/connected/` e
`app/build/reports/androidTests/connected/`.

## Determinismo e testes instáveis

- Não use `Thread.sleep`, delays arbitrários, rede real ou data/hora do sistema em testes JVM.
- Compose deve aguardar pela sincronização da regra e consultar semântica visível.
- Retry automático não é gate; pode ser usado apenas para diagnóstico, preservando a primeira falha.
- O owner da tabela deve reproduzir e corrigir testes flaky. `@Ignore` ou aumento de timeout exige
  issue e evidência.
- Falha de ADB, dispositivo offline ou API level desconhecida é infraestrutura; repita somente o
  contexto afetado depois de recuperar o dispositivo.

## Política de cobertura

O Kover mede testes JVM; instrumentação Android não entra no percentual. Os gates e o baseline estão
em `docs/setup/quality-baseline.md`. Exclusões se limitam a código gerado e boilerplate
Android/Hilt/Dagger. Toda redução ou nova exclusão requer ADR ou justificativa explícita no PR.
