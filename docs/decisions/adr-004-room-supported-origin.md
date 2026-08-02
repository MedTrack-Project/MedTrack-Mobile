# ADR 004 — Origem mínima suportada do Room

## Status

Aceito em 02/08/2026.

## Contexto

As versões 4 e 5 do banco foram publicadas sem migrations e sem schemas exportados. Não existe fonte
confiável para reconstruir sua estrutura ou provar preservação de dados. O primeiro snapshot
recuperável é o schema 8.

## Decisão

- Declarar a versão 8 como menor origem atualizável.
- Suportar e testar `8 -> 10` e `9 -> 10`.
- Não criar migrations especulativas para 4/5 nem encadear migrations antigas sem fixtures.
- Manter `fallbackToDestructiveMigration(false)`; origens não suportadas falham sem apagar dados.
- Exigir schema exportado, migration e teste de preservação em toda mudança futura.

## Consequências

Instalações nas versões 1–7 precisam de recuperação orientada pelo produto ou reinstalação explícita;
o aplicativo não toma essa decisão silenciosamente. Em troca, nenhum caminho anunciado como
suportado depende de SQL não verificável.
