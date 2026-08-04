# Governança do repositório

Arquivos versionados definem owners, templates, workflows e atualização de dependências. Regras de
branch e environments são estado administrativo do GitHub e devem ser configuradas após o merge.

## Owners

- [Ellen Rocha](https://github.com/EllenRocha1)
- [Clara Ferreira](https://github.com/MClaraFerreira5)
- [Yann Leão](https://github.com/YannLeao)

O `CODEOWNERS` atribui os três owners ao projeto e reforça caminhos de CI, build e segurança. A regra
de branch deve exigir revisão de code owner; isso não significa exigir aprovação dos três em todo PR.

## Ruleset da `main`

Em `Settings > Rules > Rulesets`, criar uma regra ativa para a branch padrão:

- bloquear criação, atualização e exclusão direta, permitindo atualização somente via Pull Request;
- exigir pelo menos uma aprovação;
- exigir revisão de CODEOWNERS;
- dispensar aprovações antigas quando novos commits forem enviados;
- exigir resolução de todas as conversas;
- exigir branch atualizada antes do merge;
- exigir histórico linear;
- bloquear force push e exclusão;
- não permitir bypass, inclusive para administradores, salvo procedimento emergencial auditado;
- exigir os checks `Dependency review`, `Quality and debug APK` e `Instrumented tests (API 35)`;
- exigir que o resultado venha da GitHub Action `CI` e não aceitar checks antigos ou de outra origem.

Teste a regra com um PR propositalmente quebrado antes de considerá-la ativa.

## Environment `production`

Criar `Settings > Environments > production`:

- adicionar os três owners como required reviewers e impedir self-review;
- restringir deployment às tags protegidas que correspondam a `v*.*.*`;
- não permitir bypass administrativo;
- cadastrar como Environment Secrets:
  - `MEDTRACK_API_BASE_URL`;
  - `MEDTRACK_SCAN_URL`;
  - `MEDTRACK_KEYSTORE_BASE64`;
  - `MEDTRACK_KEYSTORE_PASSWORD`;
  - `MEDTRACK_KEY_ALIAS`;
  - `MEDTRACK_KEY_PASSWORD`.

Endpoints são configuração compilada, mas ficam em secrets para mascaramento consistente dos logs.
Nunca cadastrar valores fake, localhost ou `10.0.2.2` no environment de produção.

## Tags e release

Criar ruleset para tags `v*.*.*` que bloqueie atualização e exclusão. A publicação deve ocorrer
somente pelo workflow `Release APK`. Tags de validação com sufixo não são publicáveis.

## Dependências

O Dependabot abre PRs semanais agrupados. Cada PR deve passar por toda a CI e revisão humana. Não
habilitar auto-merge para AGP/Kotlin/KSP/Gradle, bibliotecas de segurança, Room ou CameraX.
Habilite Dependency Graph, Dependabot alerts e Dependabot security updates. O workflow
`Dependency submission` atualiza o grafo Gradle após mudanças na `main`; o PR falha para nova
vulnerabilidade de severidade alta ou crítica.

## Auditoria periódica

Trimestralmente ou após mudança de owners:

1. revisar acessos, bypasses, secrets e required reviewers;
2. confirmar que os checks exigidos ainda correspondem aos nomes dos jobs;
3. validar um PR bloqueado e uma release de homologação;
4. rotacionar credenciais conforme política e remover artifacts expirados;
5. conferir alertas do Dependabot, dependency review e Security Advisories.
