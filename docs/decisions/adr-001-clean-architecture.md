# ADR-001: Organizacao em camadas

## Status

Aceita.

## Contexto

O aplicativo possui fluxos mobile sensiveis a estado, persistencia local, camera, notificacoes, 
sincronizacao e navegacao. Sem separacao clara, ViewModels e telas tenderiam a concentrar 
responsabilidades demais.

## Decisao

Organizar o projeto em camadas:

- `ui` para telas, componentes, navegacao e ViewModels.
- `domain` para modelos, regras e use cases.
- `data` para Room, Retrofit, repositories, DTOs, entities e sessao.
- `utils` para recursos transversais.

## Consequencias

- UI nao acessa banco ou API diretamente.
- Repositories viram a fronteira principal entre aplicacao e dados.
- DTOs e Entities nao devem vazar para telas.
- Novas features precisam respeitar a direcao das dependencias.
