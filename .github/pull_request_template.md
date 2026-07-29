## Objetivo

<!-- Explique o problema e o resultado deste PR. -->

## Escopo

<!-- Liste as mudanças incluídas e o que ficou explicitamente fora. -->

## Evidências

<!-- Inclua comandos, resultados, screenshots ou vídeos relevantes. Não inclua dados sensíveis. -->

## Riscos e rollback

<!-- Descreva impacto, compatibilidade, migration e como reverter, quando aplicável. -->

## Checklist

- [ ] O PR tem escopo único e a branch está atualizada com a `main`.
- [ ] Os commits seguem Conventional Commits.
- [ ] Executei `./gradlew qualityCheck`.
- [ ] Executei `./gradlew assembleDebug`.
- [ ] Executei `git diff --check`.
- [ ] Adicionei ou atualizei testes para o comportamento alterado.
- [ ] A cobertura dos pacotes tocados não foi reduzida sem justificativa.
- [ ] Registrei testes manuais para câmera, notificação, migração, background ou navegação.
- [ ] Não incluí secrets, endpoints antigos, credenciais ou dados reais de pacientes.
- [ ] Atualizei documentação/ADR quando alterei uma decisão técnica.
- [ ] Documentei rollback para migration, autenticação ou release.
- [ ] Não misturei upgrade amplo de dependências com refatoração funcional.

## Testes executados

```text
./gradlew qualityCheck
./gradlew assembleDebug
git diff --check
```
