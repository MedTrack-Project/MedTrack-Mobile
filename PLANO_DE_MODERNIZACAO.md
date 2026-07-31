# Plano de Modernização — MedTrack Mobile

## 1. Escopo e premissas

Este plano foi elaborado a partir da inspeção estática do repositório e de uma tentativa de executar
`testDebugUnitTest`, `koverHtmlReport` e `lintDebug`.

A tentativa de validação Gradle foi interrompida pelo ambiente antes da conclusão. Portanto, este
documento não assume que o build, os testes, o lint ou o relatório de cobertura estejam aprovados.
Durante a execução foi confirmado ao menos um aviso: a permissão
`android.permission.USE_FULL_SCREEN_INTENT` está declarada duas vezes no Manifest.

O trabalho deve ser entregue em Pull Requests pequenos, sequenciais e reversíveis. Cada etapa abaixo
corresponde a um PR. Uma etapa só deve começar depois que a anterior estiver integrada à `main`.
Mudanças de dependências, arquitetura e comportamento não devem ser misturadas no mesmo PR.

As versões listadas neste documento são as encontradas no repositório em 29/07/2026. Antes de qualquer
upgrade, deve-se confirmar compatibilidade nas notas oficiais de AGP, Gradle, Kotlin, KSP, Compose e
demais bibliotecas; não se deve atualizar todas as dependências de uma vez.

## 2. Resumo do diagnóstico atual

### 2.1 Estrutura e arquitetura

O projeto possui um único módulo Android, `:app`, com aproximadamente 82 arquivos Kotlin de produção
e 5,4 mil linhas. A organização por pacotes implementa uma arquitetura em camadas próxima de
**MVVM com Clean Architecture pragmática**:

- `ui`: telas e componentes Jetpack Compose, navegação e ViewModels;
- `domain`: modelos, mappers, um use case e serviços;
- `data`: Room, Retrofit, DTOs, sessão e repositories;
- `di`: módulos Hilt;
- `utils`: conectividade, notificações, formatação e utilitários.

O fluxo predominante é `Compose -> ViewModel -> Repository -> Retrofit/Room`, com modelos de domínio
na volta. Há DTOs, entities e mappers separados, Hilt como contêiner de injeção e abstração
`MedicamentoRepositoryContract`.

Essa separação é útil, mas ainda não caracteriza Clean Architecture estrita:

- `domain/service` contém infraestrutura Android (`CameraService`, `DetectionService` e o
  `CoroutineWorker` `ScanUpload`), fazendo a camada de domínio depender de Android, CameraX,
  WorkManager, notificações e Hilt;
- repositories concretos dependem diretamente de `Context`, `AppDatabase`, WorkManager,
  `NotificationScheduler`, Retrofit e DAOs, concentrando rede, cache, regras, arquivos,
  agendamento e efeitos colaterais;
- somente o repository de medicamentos possui contrato; `AuthRepository`, `ScanRepository`,
  câmera, relógio e armazenamento de sessão não estão atrás de abstrações testáveis;
- ViewModels importam classes concretas da camada `data`, tratam exceções genéricas e expõem vários
  `LiveData` independentes, o que permite estados inconsistentes;
- `CameraViewModel` recebe `PreviewView`, `LifecycleOwner`, `Rect`, `Uri` e serviço CameraX,
  misturando coordenação de UI/framework com estado e regra de apresentação;
- `MainActivity`, navegação, notificações e workers compartilham payloads e ações por strings,
  aumentando acoplamento e risco em deep links;
- há apenas um use case explícito (`OrdenarMedicamentos`); regras relevantes permanecem em
  repositories e ViewModels;
- os documentos arquiteturais existentes descrevem a intenção, mas contêm divergências, como
  mencionar banco na versão 8 enquanto `AppDatabase` está na versão 9.

Recomendação de direção: manter MVVM, adotar fluxo unidirecional de dados em cada tela e tornar as
fronteiras `ui -> domain -> data` explícitas. A modularização física deve acontecer somente após
essas fronteiras estarem estabilizadas e justificadas por tempo de build, ownership ou reuso.

### 2.2 Ecossistema Kotlin/Android

Inventário encontrado:

| Item | Estado atual |
|---|---|
| Build scripts | Kotlin DSL (`.gradle.kts`) |
| Módulos | Apenas `:app` |
| Version Catalog | `gradle/libs.versions.toml` em uso |
| Gradle Wrapper | 9.4.1 |
| Android Gradle Plugin | 9.2.1 |
| Kotlin | 2.3.21 |
| KSP | 2.3.2 |
| Java/JVM target | 21 |
| SDK | `compileSdk 37`, `targetSdk 36`, `minSdk 26` |
| UI | Jetpack Compose + Material 3; não foram encontradas telas XML |
| Compose BOM | 2026.05.01 |
| Estado assíncrono | Coroutines 1.11.0; predomínio de LiveData, uso pontual de StateFlow |
| Navegação | Navigation Compose 2.9.8 |
| DI | Hilt 2.59.2 + KSP |
| Rede | Retrofit 3.0.0, OkHttp 5.3.2, Gson 2.14.0 |
| Persistência | Room 2.8.4, schema version 9 e schemas 8/9 exportados |
| Background | WorkManager 2.11.2 e AlarmManager |
| Câmera/IA local | CameraX 1.6.1 e ML Kit |
| Cobertura | Kover 0.9.8 |

Pontos positivos:

- stack declarada centralmente no Version Catalog;
- Compose Compiler gerenciado pelo plugin Kotlin Compose;
- KSP no lugar de KAPT;
- Hilt, Room, WorkManager e coroutines já fazem parte do projeto;
- schemas Room recentes estão versionados;
- endpoints já são expostos via `BuildConfig`.

Problemas e oportunidades:

- não há verificação automatizada de compatibilidade entre Gradle, AGP, Kotlin, KSP e JDK;
- o `gradlew` está versionado sem bit executável, exigindo `bash gradlew` em ambientes Unix;
- há propriedades AGP experimentais e supressões na configuração, que devem ser justificadas ou
  removidas após validação;
- não existe política automatizada para atualização de dependências (Dependabot/Renovate) nem
  verificação de lockfile/dependency verification;
- há mistura de LiveData e Flow; Compose observa diversos LiveData em vez de consumir um
  `StateFlow<UiState>` lifecycle-aware;
- `runtime-livedata`, `lifecycle-livedata` e versões Compose individuais merecem revisão para evitar
  versões redundantes fora do BOM;
- `camera-camera2` e `camera-camera2-pipe` aparecem simultaneamente; é preciso comprovar a necessidade
  de ambos;
- o código usa Gson diretamente em repository/worker, dificultando troca e testes;
- `release` está com `isMinifyEnabled = false`, apesar de declarar regras ProGuard/R8;
- `versionCode` e `versionName` são fixos e não há estratégia de versionamento por tag;
- o namespace/application ID originalmente encontrado, `com.example.piec_1`, era provisório e
  inadequado para distribuição; ele foi substituído por `com.medtrack.mobile` em um PR próprio.

### 2.3 Configuração de APIs, segurança e privacidade

Existem duas configurações:

- `MEDTRACK_API_BASE_URL`;
- `MEDTRACK_SCAN_URL`.

Elas podem vir de propriedades Gradle ou `local.properties`, mas possuem fallbacks com IPs privados
hardcoded. O Manifest permite cleartext globalmente (`usesCleartextTraffic="true"`) e os exemplos da
documentação usam HTTP. Isso torna possível gerar por engano um APK apontando para infraestrutura
antiga/local e permite tráfego sem TLS.

Outros riscos observados:

- OkHttp registra `BODY` em todos os builds, podendo expor credenciais, JWTs, dados médicos e imagens;
- `LoginViewModel` registra o token e `TelaLogin` registra usuário e senha;
- JWT é armazenado em `SharedPreferences` comum;
- backup do aplicativo está habilitado, enquanto as regras ainda contêm comentários/TODOs padrão;
- URLs completas de scan são aceitas via `@Url`, exigindo validação para evitar hosts inesperados;
- permissões incluem `RECORD_AUDIO`, `FOREGROUND_SERVICE`, full-screen intent duplicada e acesso a
  storage legado; deve-se comprovar a necessidade de cada uma;
- `android:showWhenLocked`, `turnScreenOn` e full-screen notifications têm impacto de privacidade e
  políticas de plataforma;
- arquivos de scans e payloads de medicamentos podem permanecer em armazenamento local ou em extras
  de Intent; retenção, descarte e exposição devem ser auditados;
- `file.delete()` no worker não verifica falha e não há política explícita de limpeza;
- não há Network Security Config por variante, redaction de logs ou validação de configuração de
  release.

Endpoints de backend e IA em modernização devem permanecer parametrizados. URLs reais não são
segredos criptográficos, mas devem ser tratadas como configuração de ambiente; tokens, keystores,
senhas e credenciais de assinatura são secrets e nunca devem entrar no Git ou em `BuildConfig`.

### 2.4 Concorrência, persistência e trabalho em background

- O uso de `viewModelScope` e `withContext(Dispatchers.IO)` está disseminado, mas dispatchers e relógio
  não são injetáveis, reduzindo determinismo dos testes.
- `NotificationReceiver` cria um `CoroutineScope(Dispatchers.IO)` manual após `goAsync`; isso requer
  timeout/cancelamento e garantia rigorosa de `finish()`.
- O worker `ScanUpload` usa EntryPoint manual de Hilt, estados de fila como strings e processa todos
  os itens em sequência; faltam política explícita de idempotência, backoff, limite de tentativas e
  distinção entre erro permanente e transitório.
- `ScanRepository` testa três nomes de multipart em sequência, sugerindo contrato instável com a API.
- A fila exige rede `UNMETERED`, o que pode postergar indefinidamente um envio que poderia aceitar
  qualquer rede.
- O banco está na versão 9, mas a lista de migrations salta de `3_4` para `6_7`; é necessário provar
  todos os caminhos suportados por testes de migração e definir a menor versão de origem suportada.
- `fallbackToDestructiveMigration(false)` protege contra destruição silenciosa, porém não substitui
  testes de migração.
- DAOs são obtidos de `AppDatabase` dentro dos repositories, em vez de serem injetados diretamente.
- Operações remotas e locais não possuem um modelo uniforme de resultado/erro nem estratégia
  transacional documentada.

### 2.5 Qualidade e testes

Foram encontrados 11 arquivos de teste local (`app/src/test`), aproximadamente 875 linhas:

- utilitários de data e comparação de texto;
- mappers de domínio e remotos;
- converters Room;
- `OrdenarMedicamentos`;
- `MedicamentoViewModel`;
- `DoseHorarioViewModel`;
- regra de dispatcher principal para coroutines.

Dependências declaradas: JUnit 4, AndroidX Arch Core Testing e Coroutines Test. Não há MockK nem
Mockito; os testes existentes usam fakes em alguns pontos.

Lacunas:

- não existe diretório/teste em `app/src/androidTest`, embora Espresso e Compose Test estejam
  declarados;
- não foram encontrados testes de UI Compose, navegação, acessibilidade, Room instrumentado,
  migrations, Retrofit/serialização, WorkManager, AlarmManager, notificações, autenticação,
  `LoginViewModel`, `CameraViewModel`, repositories ou fluxo offline;
- não há MockWebServer, Room Testing, WorkManager Testing nem Hilt Android Testing declarados;
- Kover está configurado, porém exclui quase toda a infraestrutura, repositories principais,
  duas ViewModels, UI, navegação, workers e serviços. Um percentual alto poderia ser enganoso;
- não há limiar mínimo de cobertura visível;
- não foram encontrados ktlint, detekt, Android lint customizado, baseline ou configuração de
  formatação;
- não há automação de CI visível em `.github/workflows`;
- a tentativa de `testDebugUnitTest koverHtmlReport lintDebug` não terminou, logo o baseline real
  ainda precisa ser estabelecido na Etapa 0.

### 2.6 Prioridades

| Prioridade | Tema | Motivo |
|---|---|---|
| P0 | Remover vazamento de credenciais/logs e impedir release com endpoints fallback | Segurança e risco de produção |
| P0 | Validar build, testes, lint e migrations em ambiente reproduzível | Não há baseline confiável |
| P0 | Parametrizar e validar endpoints de backend/IA por variante | APIs serão republicadas |
| P1 | Reduzir acoplamento e unificar estado de UI com StateFlow | Testabilidade e previsibilidade |
| P1 | Cobrir autenticação, confirmação, fila offline e migrations | Fluxos críticos de saúde/dados |
| P1 | Robustecer WorkManager, notificações e persistência | Confiabilidade offline |
| P2 | Ativar R8/resource shrinking e assinatura segura de release | Distribuição e segurança |
| P2 | Modularizar quando houver evidência | Escalabilidade sem refatoração prematura |

## 3. Estratégia de Pull Requests

### Etapa 0 — Diagnóstico executável e setup base

**Objetivo do PR**

Criar um baseline reproduzível de build/qualidade, padronizar o projeto e tornar falhas atuais
visíveis sem mudar comportamento funcional.

**Tarefas detalhadas**

- Corrigir o bit executável do Gradle Wrapper e validar seu checksum/distribuição.
- Documentar JDK 21, Android SDK e comandos oficiais de desenvolvimento.
- Executar separadamente `assembleDebug`, `testDebugUnitTest`, `lintDebug` e relatório Kover,
  registrando resultados iniciais.
- Adicionar ktlint (ou Spotless com ktlint) e detekt com versões no Version Catalog.
- Criar configurações explícitas, inicialmente com baseline apenas para dívida preexistente;
  arquivos novos/modificados não podem adicionar violações.
- Corrigir problemas mecânicos de baixo risco: permissão full-screen duplicada, imports/formatação,
  warnings de Manifest e divergência da documentação sobre schema Room 9.
- Revisar propriedades experimentais do `gradle.properties`; remover as desnecessárias e documentar
  as mantidas.
- Revisar aliases e dependências potencialmente redundantes, sem upgrade em massa.
- Configurar Kover para produzir XML e HTML, publicar o baseline real e reduzir exclusões injustificadas.
- Adicionar tarefas agregadoras, por exemplo `qualityCheck`, sem esconder tarefas oficiais.
- Criar uma matriz de compatibilidade JDK/Gradle/AGP/Kotlin/KSP e registrar a versão do Android Studio
  recomendada.
- Opcionalmente habilitar Gradle dependency verification depois de gerar e revisar metadados.

**Critérios de aceite/verificação**

- `./gradlew --version` funciona em Unix e usa JDK 21.
- `./gradlew clean assembleDebug testDebugUnitTest lintDebug koverXmlReport` termina com sucesso,
  ou falhas legadas justificadas estão registradas e isoladas em baseline aprovado.
- ktlint/Spotless e detekt executam localmente e falham com uma violação introduzida de propósito
  durante o teste do PR.
- Nenhuma tela ou fluxo funcional é alterado.
- `git diff --check` não reporta whitespace inválido.
- O relatório Kover é gerado e seu denominador/exclusões são revisados no PR.

### Etapa 1 — Configuração de ambientes e segurança imediata

**Objetivo do PR**

Impedir vazamento de dados sensíveis e garantir que cada APK use endpoints explícitos e válidos,
sem depender das URLs antigas.

**Tarefas detalhadas**

- Definir variantes/configurações claras para `debug` e `release` (e `staging`, se houver ambiente).
- Ler `MEDTRACK_API_BASE_URL` e `MEDTRACK_SCAN_URL` por Gradle properties ou variáveis de ambiente;
  aceitar `local.properties` apenas para desenvolvimento local.
- Remover IPs antigos como fallback. Debug pode usar um valor local documentado; release deve falhar
  no configuration phase se endpoints estiverem ausentes, forem HTTP, não tiverem host válido ou
  estiverem sem o formato exigido pelo Retrofit.
- Manter endpoints fora do código Kotlin. Não armazenar tokens ou credenciais em `BuildConfig`.
- Criar abstração tipada de configuração (`ApiEndpoints`) injetada por Hilt.
- Restringir cleartext por Network Security Config somente ao debug/local; release deve exigir HTTPS.
- Habilitar logging HTTP apenas em debug e aplicar redaction aos headers `Authorization` e cookies;
  usar nível `NONE` em release.
- Remover logs de token, usuário e senha e revisar logs que incluam dados médicos, URLs de imagem ou
  caminhos locais.
- Revisar backup/data extraction e excluir token, banco/arquivos sensíveis conforme decisão de produto.
- Revisar permissões e remover as não usadas/duplicadas; documentar justificativa para full-screen
  intent e comportamento na tela bloqueada.
- Restringir/validar o host do scan, evitando chamadas arbitrárias via `@Url`.
- Adicionar scanner de secrets ao processo de qualidade e conferir que `local.properties`,
  keystores e arquivos de credenciais estejam ignorados.

**Critérios de aceite/verificação**

- Build de release sem as duas URLs falha com mensagem clara antes da compilação.
- Build de release rejeita HTTP e não contém os IPs antigos ao inspecionar APK/string resources.
- Debug configurado acessa mocks/ambiente local; release usa somente valores injetados.
- OkHttp não registra body nem `Authorization` em release.
- Busca por tokens/senhas/IPs antigos não encontra hardcodes em fontes ou artefatos.
- Android lint e testes de validação da configuração passam.
- Manifest mesclado de release não permite cleartext.

### Etapa 2 — Fronteiras arquiteturais e domínio testável

**Objetivo do PR**

Corrigir dependências entre camadas sem alterar os fluxos visíveis ao usuário.

**Tarefas detalhadas**

- Registrar uma ADR com a arquitetura alvo: MVVM, UDF, camadas `ui`, `domain` e `data`, regras de
  dependência e estratégia de erros.
- Mover `CameraService`, `DetectionService` e `ScanUpload` para pacotes de infraestrutura adequados
  (`data/camera`, `data/worker` ou equivalente); o domínio não deve importar Android.
- Criar interfaces no domínio para autenticação, medicamentos, scan, sessão, câmera/arquivo,
  agendamento e conectividade quando forem consumidas por ViewModels/use cases.
- Injetar DAOs diretamente nos repositories em vez de expor `AppDatabase`.
- Extrair casos de uso para login/sincronização, confirmação de dose, scan e fila offline.
- Extrair relógio/gerador de datas e dispatchers para abstrações injetáveis.
- Substituir exceções genéricas e mensagens vindas de infraestrutura por erros tipados/resultados de
  domínio; mapear mensagens amigáveis apenas na camada de UI.
- Remover dependência direta de `Context`, `Uri`, WorkManager, Retrofit e entidades Room das regras de
  negócio.
- Manter o projeto em módulo único neste PR; preparar packages/fronteiras antes de decidir módulos.

**Critérios de aceite/verificação**

- Uma verificação de dependência (teste arquitetural ou detekt rule) impede imports Android/Compose/
  Retrofit/Room no domínio.
- ViewModels dependem de casos de uso/interfaces, não de repositories concretos de `data`.
- Testes existentes continuam passando e novos testes cobrem os casos de uso extraídos.
- Fluxos de login, listagem, confirmação, câmera e fila offline mantêm comportamento.
- Não há mudança de schema Room nem contrato HTTP neste PR.

### Etapa 3 — Estado de UI moderno e navegação previsível

**Objetivo do PR**

Adotar UDF com estado imutável e lifecycle-aware, reduzindo estados inválidos e eventos duplicados.

**Tarefas detalhadas**

- Migrar uma tela por vez de múltiplos LiveData para um único `StateFlow<UiState>` imutável.
- Modelar loading, conteúdo, vazio e erro como estados explícitos; modelar ações como eventos/intents.
- Coletar flows no Compose com `collectAsStateWithLifecycle`.
- Tratar navegação, snackbar e diálogo como eventos consumíveis, evitando booleans que reaparecem
  após recomposição/restauração.
- Remover `postValue` quando o estado já estiver no Main dispatcher.
- Evitar que `CameraViewModel` receba `PreviewView` ou `LifecycleOwner`; manter binding CameraX em
  adaptador/controlador lifecycle-aware na UI.
- Introduzir rotas tipadas/argumentos serializáveis de forma segura e centralizar chaves de Intent.
- Salvar apenas estado necessário com `SavedStateHandle`; não transportar payload médico grande via
  JSON em Intent quando uma chave/ID persistida for suficiente.
- Separar composables stateful/stateless para previews e testes.
- Revisar acessibilidade: content descriptions, touch targets, contraste, font scaling e semântica.

**Critérios de aceite/verificação**

- Cada ViewModel migrada expõe apenas estado público imutável.
- Rotação/recriação não dispara novamente confirmação, navegação ou mensagens.
- Testes de ViewModel cobrem transições de estado, concorrência e erros.
- Testes Compose validam pelo menos login, lista vazia/erro e confirmação.
- Navegação por notificação/deep link possui teste de happy path e argumento inválido.

### Etapa 4 — Camada de dados, contrato de APIs e sessão

**Objetivo do PR**

Adaptar com segurança o aplicativo aos novos backends, tornar rede/cache testáveis e proteger sessão.

**Tarefas detalhadas**

- Congelar os novos contratos de backend e IA com exemplos versionados e matriz de compatibilidade.
- Revisar paths, multipart, nomes de campos, autenticação, timeouts e códigos de erro.
- Eliminar a tentativa sequencial de multipart `"file"`, `"image"` e `"photo"` depois que o contrato
  oficial estiver definido.
- Criar interceptador de autenticação e política uniforme de erro/expiração de sessão.
- Avaliar Moshi ou Kotlin Serialization em PR separado; se Gson permanecer, centralizar adapters e
  testes de compatibilidade. Não combinar troca de serializer com mudança de API.
- Implementar repositories usando remote/local data sources explícitos.
- Definir source of truth local e estratégia de cache/sincronização por agregado.
- Proteger token usando armazenamento apoiado por Android Keystore ou solução oficialmente suportada,
  com migração segura do valor existente e política de logout/expiração.
- Aplicar transações Room onde persistências relacionadas precisem ser atômicas.
- Padronizar DTO -> domain -> entity, nullability, datas/horários e timezone.
- Criar testes com MockWebServer para sucesso, 4xx, 5xx, timeout, payload inválido e expiração de token.
- Não usar APIs reais em testes automatizados.

**Critérios de aceite/verificação**

- Contract tests passam contra fixtures aprovadas dos novos backends.
- Nenhum teste depende de rede externa.
- Token antigo é migrado sem logout inesperado; token não aparece em logs, backup ou texto do APK.
- Erros HTTP são convertidos em erros de domínio previsíveis.
- Cache permanece consistente após falha parcial de rede.
- Configurações de backend e IA continuam injetáveis por ambiente.

### Etapa 5 — Room, offline-first, WorkManager e notificações

**Objetivo do PR**

Garantir confiabilidade de dados, processamento offline idempotente e notificações compatíveis com a
plataforma.

**Tarefas detalhadas**

- Adicionar `room-testing` e testes instrumentados para migrations suportadas até a versão 9.
- Verificar por que faltam migrations `4_5` e `5_6`; adicionar caminhos válidos ou declarar
  formalmente a menor versão atualizável.
- Revisar schemas versionados e exigir diff/revisão a cada alteração.
- Substituir status string da fila por tipo fechado persistido com conversor e estados explícitos.
- Definir idempotency key para scan/confirmacão, unique work, backoff exponencial, máximo de tentativas
  e política para erro permanente.
- Usar Hilt Worker (`@HiltWorker`) em vez de EntryPoint manual, se compatível com a stack validada.
- Rever `UNMETERED` versus `CONNECTED` conforme custo/tamanho e requisito de produto.
- Definir retenção e limpeza de imagens temporárias; verificar o resultado de exclusão.
- Proteger o receiver assíncrono com timeout e mover trabalho durável para WorkManager.
- Revisar agendamentos após reboot, mudança de timezone/horário e atualização do app.
- Adequar full-screen intents às regras atuais da plataforma e fornecer fallback para notificação
  comum quando a permissão/capacidade não estiver disponível.
- Testar canais, permissão de notificação e deep links sem expor conteúdo sensível na lock screen.

**Critérios de aceite/verificação**

- Testes de migration abrem snapshots das versões suportadas sem perda de dados.
- O mesmo job executado mais de uma vez não duplica confirmação, upload ou notificação.
- Erros 4xx permanentes não entram em retry infinito; falhas transitórias respeitam backoff.
- Arquivos temporários são removidos no sucesso e mantidos/limpos conforme política em falha.
- Testes WorkManager cobrem sucesso, retry, failure e ausência de sessão.
- Cenários offline/online, reboot e mudança de timezone passam em dispositivo/emulador.

### Etapa 6 — Expansão da pirâmide de testes e quality gates

**Objetivo do PR**

Criar proteção automatizada proporcional ao risco dos fluxos e tornar a cobertura um sinal confiável.

**Tarefas detalhadas**

- Adotar fakes como padrão; adicionar MockK/Mockito somente onde mocks reduzam custo sem acoplar testes
  à implementação.
- Cobrir casos de uso, ViewModels, repositories, mappers, matching, datas e validações de configuração.
- Adicionar MockWebServer, Room Testing, WorkManager Testing e Hilt Testing conforme necessário.
- Criar testes Compose para fluxos críticos, estados e acessibilidade.
- Criar poucos testes end-to-end instrumentados: login simulado, listagem, scan simulado,
  confirmação e fila offline.
- Isolar câmera/ML Kit atrás de adapters; testar contrato com imagens fixture pequenas e testes de
  dispositivo separados.
- Reconfigurar Kover para medir código relevante; excluir apenas gerados/boilerplate com justificativa.
- Definir meta incremental baseada no baseline da Etapa 0, com mínimo por PR e aumento gradual nos
  pacotes críticos, evitando uma meta global cosmética.
- Publicar JUnit XML, lint, detekt e cobertura como artifacts de CI.
- Tratar testes flaky: seed/clock/dispatcher determinísticos, retries somente para diagnóstico e
  ownership documentado.

**Critérios de aceite/verificação**

- Unit tests e instrumented tests passam em ambiente limpo.
- Fluxos P0/P1 têm cobertura de sucesso, erro e cancelamento.
- Kover falha quando a meta acordada é reduzida.
- Exclusões de cobertura não incluem repositories/ViewModels inteiros sem ADR.
- Testes Compose usam semântica, não delays arbitrários.
- Relatórios permitem localizar facilmente teste/linha que falhou.

### Etapa 7 — Release hardening, desempenho e observabilidade

**Objetivo do PR**

Produzir um APK de release seguro, enxuto, diagnosticável e próximo das condições reais de produção.

**Tarefas detalhadas**

- Ativar R8/minificação e resource shrinking em release; adicionar keep rules mínimas e testadas.
- Configurar assinatura somente via variáveis/arquivos temporários no CI; nunca versionar keystore,
  alias ou senhas.
- Definir versionamento semântico por tag e derivação determinística de `versionCode`/`versionName`.
- Adicionar Baseline Profiles/Macrobenchmark para startup e fluxos críticos, se métricas justificarem.
- Medir startup, tamanho do APK, memória e impacto de CameraX/ML Kit; criar budgets.
- Revisar dependências e recursos não usados, incluindo módulos CameraX potencialmente redundantes.
- Adicionar observabilidade com redaction e consentimento: crashes e métricas técnicas, sem tokens,
  imagens, nomes de medicamentos ou outros dados de saúde.
- Gerar SBOM/lista de dependências e executar análise de vulnerabilidades/licenças.
- Realizar smoke test do APK minificado em API mínima e target, incluindo login simulado, banco,
  notificações, câmera e worker.

**Critérios de aceite/verificação**

- `assembleRelease` gera APK assinado quando secrets estão presentes e falha claramente quando faltam.
- APK minificado instala e completa smoke tests nas APIs definidas.
- Nenhum secret aparece em Gradle logs, artifacts ou APK.
- Tamanho/startup respeitam os budgets registrados.
- Crash reporting de teste funciona e payload foi inspecionado quanto a dados sensíveis.
- SBOM e relatório de dependências são artifacts do pipeline.

### Etapa 8 — CI/CD, governança e documentação final

**Objetivo do PR**

Fechar a modernização com gates obrigatórios em PR e entrega automatizada de APK em GitHub Release.

**Tarefas detalhadas**

- Criar workflow de CI para `pull_request` com:
  - checkout e validação do Gradle Wrapper;
  - JDK 21 e cache Gradle seguro;
  - ktlint/format check e detekt;
  - Android lint;
  - testes unitários e gate Kover;
  - `assembleDebug`;
  - testes instrumentados em emulator runner, em job separado;
  - upload de relatórios mesmo em falha.
- Aplicar `concurrency` para cancelar execuções antigas da mesma branch, timeouts e permissões mínimas.
- Fixar actions por SHA ou política equivalente e habilitar atualização automatizada revisada.
- Criar workflow de release acionado por tag validada (por exemplo `vX.Y.Z`) ou publicação de Release:
  - validar correspondência entre tag e versão;
  - receber `MEDTRACK_API_BASE_URL` e `MEDTRACK_SCAN_URL` via GitHub Environment variables/secrets;
  - materializar keystore temporariamente a partir de secret base64;
  - fornecer alias e senhas por secrets mascarados;
  - executar quality gates e `assembleRelease`;
  - localizar e renomear deterministicamente o APK;
  - gerar checksum SHA-256 e, se adotado, SBOM;
  - anexar APK e checksum diretamente aos assets do GitHub Release;
  - nunca publicar `local.properties`, keystore ou outputs intermediários sensíveis;
  - limpar credenciais temporárias ao final.
- Usar GitHub Environment `production` com aprovação manual e secrets próprios. Endpoints não devem
  ser reutilizados de ambientes de PR.
- Opcionalmente gerar AAB em paralelo para futura Play Store, sem substituir o requisito de anexar APK.
- Criar template de PR com escopo, evidências, riscos, screenshots, testes e checklist de privacidade.
- Criar templates de bug/feature/security e `CODEOWNERS`.
- Configurar proteção da `main`: PR obrigatório, aprovações, conversas resolvidas, branch atualizada,
  status checks obrigatórios, bloqueio de force push/delete e revisão de CODEOWNERS.
- Habilitar Dependabot/Renovate com PRs agrupados por ecossistema e execução de toda a CI.
- Atualizar `README.md` com pré-requisitos, setup, variáveis sem valores reais, build, testes,
  arquitetura, troubleshooting e processo de release.
- Atualizar documentação/ADRs para refletir a implementação final e adicionar runbook de rollback/
  revogação de release.

**Critérios de aceite/verificação**

- Um PR de teste executa todos os checks e não pode ser mesclado quando um deles falha.
- Um commit sem formatação, teste quebrado ou queda de cobertura é bloqueado.
- Testes instrumentados executam em emulador reproduzível.
- Uma tag de homologação produz APK assinado, checksum e artifacts no GitHub Release.
- O APK publicado contém exatamente os endpoints fornecidos ao job e rejeita os IPs antigos.
- Logs do workflow não exibem endpoints classificados como secrets, senhas, keystore ou token.
- Download, checksum, instalação e smoke test do APK anexado são validados.
- Branch protection, templates e CODEOWNERS estão ativos e documentados.
- Uma pessoa nova consegue clonar, configurar, executar testes e gerar debug seguindo apenas o README.

## 4. Gates obrigatórios entre etapas

Todo PR deve:

- ter escopo único e referência à etapa;
- estar atualizado com a `main`;
- incluir testes para comportamento novo ou alterado;
- executar format check, detekt, lint, testes e build aplicáveis;
- não reduzir cobertura dos pacotes tocados sem justificativa aprovada;
- incluir evidência de teste manual quando envolver câmera, notificação, migração ou background;
- não conter secrets, endpoints antigos, credenciais ou dados reais de pacientes;
- atualizar ADR/documentação quando mudar decisão arquitetural ou operacional;
- explicitar plano de rollback quando houver migration, autenticação ou mudança de release.

Não combinar no mesmo PR:

- upgrade amplo de dependências e refatoração funcional;
- mudança de serializer e mudança de contrato HTTP;
- migration Room e refatoração de repositories;
- ativação de R8 e grandes mudanças de navegação/UI;
- criação do pipeline e correções extensas feitas apenas para fazê-lo passar.

## 5. Resultado esperado

Ao final da Etapa 8, o MedTrack Mobile deverá possuir:

- arquitetura MVVM/UDF com domínio independente de Android e fronteiras testáveis;
- estado Compose lifecycle-aware com StateFlow;
- integração configurável com os novos serviços de backend e IA;
- release exclusivamente HTTPS, sem logs/armazenamento indevido de credenciais;
- cache, migrations, fila offline e notificações cobertos por testes;
- quality gates locais e em CI;
- APK de produção assinado, versionado e anexado automaticamente ao GitHub Release em cada tag;
- governança da `main`, templates, ownership e documentação suficientes para manutenção contínua.
