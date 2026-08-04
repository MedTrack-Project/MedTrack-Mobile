# Como contribuir

## Pré-requisitos

- JDK 21;
- Android SDK com as plataformas definidas em `app/build.gradle.kts`;
- Android Studio compatível com as versões de Gradle, AGP e Kotlin do Version Catalog.

Consulte `docs/setup/local-setup.md` para configurar os endpoints de desenvolvimento. Nunca
adicione credenciais, tokens, keystores ou endpoints privados ao repositório.

## Fluxo Git

1. Atualize sua cópia da `main`.
2. Crie uma branch curta e focada. Prefixos recomendados: `feat/`, `fix/`, `refactor/`, `test/`,
   `docs/`, `chore/` ou `core/`.
3. Faça commits pequenos seguindo Conventional Commits.
4. Atualize testes e documentação no mesmo Pull Request quando aplicável.
5. Execute os gates locais.
6. Abra um Pull Request para `main` usando o template do repositório.

Alterações diretas na `main` não são permitidas. Todo trabalho deve passar por Pull Request,
revisão e checks obrigatórios. Prefira branches de curta duração e atualize-a com a `main` antes
da revisão final conforme a política adotada pelo time.

Os checks obrigatórios são `Dependency review`, `Quality and debug APK` e
`Instrumented tests (API 35)`. Não reinicie um job apenas para obter resultado verde sem registrar e
corrigir a causa da falha.

## Conventional Commits

Formato:

```text
tipo(escopo opcional): descrição curta no imperativo
```

Tipos usuais:

- `feat`: funcionalidade;
- `fix`: correção;
- `refactor`: refatoração sem mudança funcional;
- `test`: testes;
- `docs`: documentação;
- `build`: build ou dependências;
- `ci`: automação;
- `chore`: manutenção.

Exemplos:

```text
feat(auth): adiciona estado de sessão expirada
fix(database): preserva confirmações na migração
test(scan): cobre retry do processamento offline
```

Use `!` e a seção `BREAKING CHANGE:` no corpo quando houver quebra de compatibilidade.

## Verificações antes do Pull Request

Execute:

```bash
./gradlew qualityCheck
./gradlew assembleDebug
git diff --check
```

Se o PR alterar câmera, banco, notificações, background ou navegação, registre no PR os testes
manuais executados. Mudanças de schema devem incluir migration, snapshot atualizado e teste de
migração.

Não reduza cobertura ou amplie baselines/exclusões sem uma justificativa explícita no PR.

## Escopo e revisão

- Um PR deve resolver um problema coeso.
- Não misture upgrade amplo de dependências com refatoração funcional.
- Descreva risco, rollback e evidências de teste.
- Resolva todas as conversas antes do merge.
- Não faça force push depois do início da revisão sem avisar os revisores.

Falhas de segurança não devem ser abertas como issue pública. Siga `SECURITY.md`.

Configuração administrativa, owners e processo de release estão descritos em
`docs/governance/repository-settings.md`.
