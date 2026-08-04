# Baseline de qualidade

## Toolchain

- JDK: 21
- Gradle Wrapper: 9.4.1
- Android Gradle Plugin: 9.2.1
- Kotlin: 2.3.21
- KSP: 2.3.2
- ktlint Gradle Plugin: 14.2.0
- detekt: 1.23.8
- Kover: 0.9.8

As versões são centralizadas em `gradle/libs.versions.toml`. Upgrades devem ser feitos em PRs
focados, após consulta à matriz oficial de compatibilidade.

## Gates locais

A tarefa agregadora é:

```bash
./gradlew qualityCheck
```

Ela executa:

- `:app:ktlintCheck`;
- `:app:detekt`;
- `:app:lintDebug`;
- `:app:testDebugUnitTest`;
- `:app:koverVerifyDebug`;
- `:app:koverXmlReportDebug`;
- `:app:koverHtmlReportDebug`.

O build do APK debug permanece explícito:

```bash
./gradlew assembleDebug
```

Para corrigir somente formatação:

```bash
./gradlew :app:ktlintFormat
```

Para revisar a dívida estática existente, consulte `config/detekt/baseline.xml`. O baseline não
deve crescer sem justificativa no Pull Request. A Etapa 6 elevou o gate global do Kover para
**20%** e adicionou variantes para pacotes críticos: casos de uso **75%**, repositories **65%** e
ViewModels **50%**. As metas devem subir gradualmente, sem ampliar exclusões para obter um
percentual artificial.

Os resultados ficam em `app/build/test-results/testDebugUnitTest/` (JUnit XML),
`app/build/reports/tests/testDebugUnitTest/` (JUnit HTML), `app/build/reports/kover/` (XML e HTML),
`app/build/reports/lint-results-debug.html` e `app/build/reports/detekt/`.

## Validação manual desta etapa

Por decisão operacional, os comandos Gradle finais serão executados manualmente pelo responsável do
repositório. Antes de abrir o Pull Request:

```bash
./gradlew qualityCheck
./gradlew assembleDebug
git diff --check
```

Também confirme:

- `./gradlew --version` usa JDK 21;
- o Manifest mesclado não contém permissões duplicadas;
- os relatórios existem em `app/build/reports`;
- os testes JUnit possuem XML em `app/build/test-results`;
- o APK debug existe em `app/build/outputs/apk/debug`;
- somente arquivos gerados ignorados aparecem após o build.

## Baseline comparável da Etapa 6 — 03/08/2026

Após a expansão para 78 testes JVM, o relatório sem exclusões funcionais registrou **761 de 3.606
linhas**, ou **21,1%**. As variantes Kover, que são a fonte dos gates, mediram:

| Escopo | Cobertura | Gate |
|---|---:|---:|
| Aplicação | 21,1% | 20% |
| Casos de uso | 75,9% | 75% |
| Repositories | 68,3% | 65% |
| ViewModels | 51,7% | 50% |

Reduzir qualquer gate exige justificativa e aprovação no PR. Repositories ou ViewModels não podem
ser excluídos integralmente para recuperar percentual.

## Observações do baseline

- O namespace/application ID foi renomeado para `com.medtrack.mobile`; a regra `package-name` do
  ktlint voltou a ser aplicada.
- Composables usam PascalCase e o singleton Room usa `INSTANCE`, seguindo convenções Android; as
  regras de nomenclatura conflitantes do ktlint estão desabilitadas.
- O detekt usa baseline apenas para problemas preexistentes. Código novo continua sujeito à
  configuração de `config/detekt/detekt.yml`.
- A propriedade `android.disallowKotlinSourceSets=false` é experimental e ainda produz warning.
  Sua remoção deve ser validada em PR específico para evitar mudança não intencional no build.
- O Gradle 9.4.1 reporta uso de APIs deprecadas antes do Gradle 10. Execute com
  `--warning-mode all` e trate cada origem em PR focado.

## Resultado registrado em 29/07/2026

O primeiro `qualityCheck` e `assembleDebug` concluíram com sucesso:

- testes unitários: **32 executados, 0 falhas, 0 erros e 0 ignorados**;
- detekt: **0 novos achados** após aplicação do baseline;
- ktlint: **0 violações**;
- Android lint: **0 erros e 46 warnings**;
- APK debug: gerado com **73.213.272 bytes**;
- Kover antes da correção das exclusões: 327 de 332 linhas, ou **98,49%**.

O percentual de 98,49% não deve ser usado como referência: naquele relatório, repositories,
ViewModels, UI, workers, navegação e outras áreas relevantes estavam excluídos. As exclusões foram
reduzidas para manter apenas código gerado/boilerplate de Android, Hilt e Dagger. Após essa correção,
execute novamente `./gradlew qualityCheck`; o novo percentual será o primeiro baseline comparável.

Após a remoção dos achados resolvidos por essa renomeação, o baseline do detekt contém **51
ocorrências preexistentes**:

| Regra | Ocorrências |
|---|---:|
| `FunctionNaming` | 25 |
| `LongMethod` | 11 |
| `TooGenericExceptionCaught` | 8 |
| `ReturnCount` | 2 |
| `ComplexCondition` | 1 |
| `ConstructorParameterNaming` | 1 |
| `MaxLineLength` | 1 |
| `PrintStackTrace` | 1 |
| `SwallowedException` | 1 |

As 93 ocorrências de `PackageNaming` associadas ao namespace legado foram removidas do baseline.
Nomenclatura de Composables explica parte relevante de `FunctionNaming`. Os achados restantes devem
ser reduzidos por refatorações focadas; regenerar o baseline sem revisar o diff não é permitido.

### Classificação dos warnings

| Origem | Quantidade/estado | Decisão |
|---|---:|---|
| Versões disponíveis | 15 ocorrências | Não atualizar em massa na Etapa 0; criar PRs focados com matriz de compatibilidade. |
| Recursos não usados | 18 | Auditar visualmente antes de remover; alguns podem ser assets reservados. |
| Bitmaps em `drawable` | 10 | Migrar para densidades adequadas ou `drawable-nodpi` em PR de assets. |
| Ícone monocromático ausente | 2 | Criar asset aprovado pelo design antes de alterar adaptive icons. |
| Qualificador `mipmap-anydpi-v26` redundante | 1 | Consolidar junto ao PR de launcher icons. |
| `android.disallowKotlinSourceSets=false` experimental | 1 | Mantido até teste específico sem a flag. |
| API Gradle deprecada | 1 | Originada pelo plugin detekt 1.23.8; atualizar quando uma versão compatível remover o uso. |
| Bibliotecas nativas sem strip | informativo | Esperado para binários prebuilt no debug; reavaliar no release hardening. |
| Múltiplos Kotlin daemons | informativo | Encerrar daemons órfãos se persistir; não afeta o artefato produzido. |

Os warnings de bibliotecas nativas (`libandroidx.graphics.path.so`,
`libimage_processing_util_jni.so`, `libmlkitcommonpipeline.so` e `libsurface_util_jni.so`) indicam
que o AGP empacotou binários prebuilt sem remover símbolos. Como o artefato validado é debug, isso
não bloqueia a Etapa 0. Tamanho e stripping do release pertencem à etapa de release hardening.

Na Etapa 7, ML Kit e os módulos CameraX sem uso foram removidos. Os nomes acima permanecem neste
documento somente como registro histórico do baseline; o APK release deve ser medido novamente.
