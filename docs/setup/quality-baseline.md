# Estado de qualidade

Este documento registra os gates e a referência comparável do projeto. Ele deve refletir o estado
atual da `main`; resultados históricos que não representam mais o código devem permanecer no
histórico Git, não como instrução operacional.

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
focados, após consulta à matriz oficial de compatibilidade e com rollback definido.

## Gates locais

A tarefa agregadora é:

```bash
./gradlew qualityCheck
```

Ela executa:

- verificação de segredos;
- `:app:ktlintCheck`;
- `:app:detekt`;
- `:app:lintDebug`;
- `:app:testDebugUnitTest`;
- verificação e geração dos relatórios Kover.

O Detekt opera sem baseline de supressões: todo achado deve ser corrigido ou, quando a regra não se
aplicar legitimamente ao código, suprimido no menor escopo possível com justificativa revisável.
Para corrigir somente formatação, execute `./gradlew :app:ktlintFormat`.

O build e os testes instrumentados permanecem gates explícitos:

```bash
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

Os resultados ficam em `app/build/test-results/`, `app/build/reports/tests/`,
`app/build/reports/kover/`, `app/build/reports/lint-results-debug.html` e
`app/build/reports/detekt/`. APKs ficam em `app/build/outputs/apk/`.

## Referência comparável — 03/08/2026

Com 78 testes JVM, o relatório sem exclusões funcionais registrou **761 de 3.606 linhas**, ou
**21,1%**. As variantes Kover usadas pelos gates mediram:

| Escopo | Cobertura | Gate |
|---|---:|---:|
| Aplicação | 21,1% | 20% |
| Casos de uso | 75,9% | 75% |
| Repositories | 68,3% | 65% |
| ViewModels | 51,7% | 50% |

Reduzir qualquer gate, ampliar exclusões ou adicionar uma supressão exige justificativa e aprovação
no PR. As metas devem subir gradualmente conforme novos fluxos sejam cobertos.

## Pontos de atenção conhecidos

- `android.disallowKotlinSourceSets=false` é experimental. Sua remoção deve ser validada em PR
  específico para evitar mudança não intencional no build.
- O Gradle reporta APIs deprecadas antes da versão 10. Use `--warning-mode all` para identificar a
  origem e trate a compatibilidade em upgrades focados.
- Recursos não usados, densidades de bitmaps, ícone monocromático e qualificadores redundantes devem
  ser auditados visualmente antes da remoção.
- Mensagens sobre Kotlin daemons órfãos são operacionais; se persistirem, encerre os daemons e
  confirme que não alteram o artefato.

O ML Kit e dependências CameraX diretas sem uso já foram removidos. Warnings antigos referentes às
bibliotecas nativas dessas dependências não representam o APK atual e devem ser investigados de novo
somente se reaparecerem em um build limpo.

## Verificação antes do Pull Request

```bash
./gradlew qualityCheck
./gradlew assembleDebug
git diff --check
```

Quando o escopo tocar câmera, banco, notificações, background ou navegação, execute também os grupos
instrumentados relevantes, conforme `docs/testing/test-strategy.md`. Confirme que relatórios e APKs
foram gerados nos caminhos acima e que somente arquivos ignorados surgiram após o build.
