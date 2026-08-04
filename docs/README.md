# Documentacao do MedTrack Mobile

Esta pasta centraliza o contexto tecnico e arquitetural do aplicativo Android MedTrack Mobile.

## Mapa rapido

- `architecture/`: arquitetura do aplicativo, camadas, dados, UI, DI, navegacao, offline e integracao com APIs.
- `context/`: contexto de produto, stack, convencoes, glossario e premissas tecnicas.
- `decisions/`: ADRs, ou registros de decisoes arquiteturais.
- `setup/`: preparacao local, build, release e geracao de APK.
- `release/`: hardening, publicação, rollback e revogação.
- `governance/`: regras administrativas e operação do repositório.
- `security/`: ambientes, rede, segredos e privacidade.
- `testing/`: estratégia, pirâmide e execução dos testes.
- `tasks/`: tarefas em andamento, template e historico.
- `_assets/`: mídia histórica; não usar no README principal ou em nova documentação sem revisão.

## Como usar

Antes de implementar uma tarefa, leia:

1. `docs/context/product-context.md`
2. `docs/context/technical-context.md`
3. `docs/context/conventions.md`
4. O arquivo de arquitetura mais proximo da area alterada.
5. `docs/tasks/CURRENT_TASK.md`, quando existir uma tarefa ativa.

Quando uma decisao tecnica relevante for tomada, registre um novo ADR em `docs/decisions/`.
