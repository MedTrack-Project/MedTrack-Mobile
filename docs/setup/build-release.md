# Build e release

## Estado atual

O aplicativo está tecnicamente preparado para gerar APK release assinado, minificado e auditável.
Backend e API de scan ainda não possuem endpoints definitivos; portanto, APKs gerados com domínios
`.invalid` são artefatos locais de validação e **não podem ser publicados**. O upload para GitHub
Release está automatizado por tag e protegido pelo environment `production`, mas permanece bloqueado
até os endpoints definitivos e secrets serem cadastrados e aprovados.

## Contrato de configuração

`assembleRelease`, `bundleRelease`, `verifyReleaseApkSize` e `releaseReadiness` exigem:

| Variável | Regra |
|---|---|
| `MEDTRACK_RELEASE_TAG` | `vMAJOR.MINOR.PATCH`; pre-release opcional para validação |
| `MEDTRACK_API_BASE_URL` | HTTPS e terminada em `/` |
| `MEDTRACK_SCAN_URL` | URL HTTPS completa |
| `MEDTRACK_KEYSTORE_FILE` | caminho para keystore existente e fora do repositório |
| `MEDTRACK_KEYSTORE_PASSWORD` | secret; não usar `BuildConfig` |
| `MEDTRACK_KEY_ALIAS` | alias da chave |
| `MEDTRACK_KEY_PASSWORD` | secret; não usar `BuildConfig` |

A precedência é variável de ambiente, propriedade Gradle e `local.properties`. Em CI, usar somente
GitHub Environment/Secrets e um arquivo temporário com permissões restritas. Não passar senhas com
`-P`, pois argumentos podem aparecer na lista de processos e em logs.

O `versionName` é a tag sem `v`. O `versionCode` é determinístico:

```text
MAJOR * 1_000_000 + MINOR * 1_000 + PATCH
```

Major deve ser no máximo 2000 e minor/patch no máximo 999. Tags de produção não devem usar sufixo.

## Validação com APK fake

Crie uma chave temporária fora do projeto; o `keytool` solicitará as senhas interativamente:

```bash
mkdir -p /tmp/medtrack-release-validation
keytool -genkeypair \
  -keystore /tmp/medtrack-release-validation/validation.jks \
  -alias validation \
  -keyalg RSA \
  -keysize 4096 \
  -validity 30
```

Configure valores sintéticos. Use `read -s` para não registrar senhas no histórico:

```bash
export MEDTRACK_RELEASE_TAG="v0.0.0-validation.1"
export MEDTRACK_API_BASE_URL="https://backend-validation.invalid/"
export MEDTRACK_SCAN_URL="https://scan-validation.invalid/detect"
export MEDTRACK_KEYSTORE_FILE="/tmp/medtrack-release-validation/validation.jks"
export MEDTRACK_KEY_ALIAS="validation"
read -rs MEDTRACK_KEYSTORE_PASSWORD && export MEDTRACK_KEYSTORE_PASSWORD
read -rs MEDTRACK_KEY_PASSWORD && export MEDTRACK_KEY_PASSWORD

./gradlew releaseReadiness
```

Não crie nem envie uma tag Git para esse teste. O domínio reservado `.invalid` garante que o APK não
se comunique acidentalmente com uma API real.

## Saídas

- APK assinado: `app/build/outputs/apk/release/app-release.apk`;
- mapping R8: `app/build/outputs/mapping/release/mapping.txt`;
- relatório de dependências do APK: `app/build/reports/dependency-analysis/release/` quando gerado pelo AGP;
- SBOM CycloneDX: `app/build/reports/cyclonedx-direct/bom.json` e `bom.xml`;
- checksum: ao executar `scripts/release/verify-apk.sh`, ao lado do APK.

Mapping e SBOM pertencem à mesma versão do APK. O mapping deve ser preservado com acesso restrito
para desofuscar crashes, mas nunca anexado a um Release público.

## Release real por tag

Após os endpoints definitivos existirem:

1. validar o mesmo commit com `qualityCheck`, testes instrumentados e `releaseReadiness`;
2. criar tag anotada `vMAJOR.MINOR.PATCH` no commit aprovado;
3. o workflow materializará o keystore temporariamente e fornecerá endpoints/secrets;
4. o workflow verificará assinatura, budgets, SBOM e checksum antes de anexar o APK;
5. falha em qualquer gate deve impedir a publicação e remover credenciais temporárias.

Nunca reutilizar a chave temporária de validação em produção.

## R8 e shrinking

Release usa `isMinifyEnabled` e `isShrinkResources`. As regras locais preservam apenas metadados de
reflection e linhas para diagnóstico; Retrofit, Gson, Room, Hilt e outras bibliotecas fornecem
consumer rules. Não adicionar `-keep class com.medtrack.mobile.**` para contornar erros: reproduza o
fluxo, identifique a classe afetada e adicione a regra mínima.

## Rollback

Uma release não é sobrescrita. Para rollback, corrigir/reverter o commit, incrementar a versão e
publicar uma nova tag. Se houver risco de credencial, revogar a chave/secret antes da nova versão.
Consulte o [runbook de rollback e revogação](../release/rollback-runbook.md).
