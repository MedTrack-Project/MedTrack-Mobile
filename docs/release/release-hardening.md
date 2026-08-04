# Release hardening, desempenho e observabilidade

## Controles implementados

- R8 e resource shrinking obrigatórios em release;
- assinatura v1/v2/v3/v4 exclusivamente por configuração externa;
- tag SemVer e `versionCode` determinístico;
- endpoints HTTPS obrigatórios, sem fallback funcional em release;
- budget automatizado de tamanho do APK;
- SBOM CycloneDX JSON/XML com dependências transitivas;
- remoção do ML Kit e da análise contínua de frames;
- remoção das declarações diretas de CameraX Video, Extensions e Camera2 Pipe sem uso;
- logs HTTP desabilitados em release e mensagens próprias sem payload clínico.

## Budgets

Os limites versionados ficam em `config/release/budgets.properties`:

| Métrica | Limite inicial | Verificação |
|---|---:|---|
| APK | 30 MiB | automática por `verifyReleaseApkSize` |
| cold startup | 2500 ms | Macrobenchmark/manual em dispositivo de referência |
| warm startup | 1200 ms | Macrobenchmark/manual em dispositivo de referência |
| memória residente | 256 MiB | Android Studio Profiler/`dumpsys meminfo` |

Alterar um limite exige evidência comparativa e justificativa no PR. Medições devem registrar versão,
modelo, API, ABI, modo térmico e ao menos cinco amostras; usar mediana, não o melhor resultado.

O SBOM ainda pode listar `camera-video` e `camera-camera2-pipe` como transitivos de CameraX. Eles não
são APIs usadas diretamente pelo app; R8/shrinking e o budget determinam o impacto efetivo no APK.

## Baseline Profile e Macrobenchmark

O módulo de benchmark foi adiado até existir um backend/scan de homologação e uma assinatura próxima
da produção. Um profile coletado sobre endpoints inoperantes otimiza um fluxo não representativo e
adiciona custo de manutenção sem baseline confiável. Após os serviços estarem disponíveis, medir
startup e login/listagem/scan; adicionar o módulo somente se o ganho ou uma regressão justificar.

## Observabilidade e privacidade

Nenhum SDK externo de crash/analytics foi ativado nesta etapa. O aplicativo trata dados de saúde e
ainda não há decisão de fornecedor, consentimento, retenção, residência dos dados nem configuração de
produção. Ativar coleta antes dessas definições contrariaria minimização e privacy by default.

Até essa decisão:

- release não registra tráfego HTTP;
- logs próprios usam textos estáticos, sem exceção/mensagem remota, token, imagem ou medicamento;
- mapping R8 é preservado de forma privada;
- smoke tests usam `adb logcat` apenas em dispositivos de teste sem dados reais.

Para adotar um fornecedor, criar ADR e implementar coleta desabilitada por padrão, consentimento
revogável, redaction testada, ambientes separados, retenção mínima e teste de payload. São proibidos:
JWT, credenciais, request/response bodies, URL de imagem, nome/posologia, identificadores de usuário e
arquivos capturados. A Etapa 8 só pode fazer upload de mapping ao fornecedor após essa aprovação.

## Supply chain

`./gradlew :app:cyclonedxDirectBom` gera SBOM CycloneDX 1.6. Na Etapa 8, os arquivos serão artifacts privados e
entrada de um scanner de vulnerabilidades/dependency review e da auditoria de licenças. A resolução
remota de metadados fica desabilitada no Gradle para tornar a geração determinística; o scanner da CI
deve enriquecer o inventário. Vulnerabilidade alta/crítica explorável deve bloquear release; exceções
precisam de owner, justificativa e prazo. Licenças desconhecidas ou incompatíveis exigem revisão antes
da distribuição.

## Pendências externas

- endpoints e contratos definitivos de backend e scan;
- smoke test de sucesso ponta a ponta;
- escolha jurídica/técnica do fornecedor de observabilidade;
- métricas representativas para decidir Baseline Profile;
- workflow e proteção do ambiente `production` (Etapa 8).
