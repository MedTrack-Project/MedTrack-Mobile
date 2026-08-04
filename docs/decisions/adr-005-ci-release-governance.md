# ADR-005 — CI obrigatória e release somente por tag protegida

## Status

Aceita.

## Contexto

A `main` aceita mudanças somente por Pull Request. APKs de produção incorporam endpoints públicos,
dependem de assinatura privada e precisam ser reproduzíveis e auditáveis. Builds locais não devem
ter autoridade para publicar um artifact oficial.

## Decisão

- Pull Requests executam qualidade/build e instrumentação em jobs separados.
- Actions externas são fixadas por SHA e atualizadas pelo Dependabot com revisão.
- Os jobs têm permissões mínimas, timeout, concurrency e artifacts de diagnóstico com retenção.
- Release é acionada apenas por tag estável `vMAJOR.MINOR.PATCH`.
- O job usa o environment protegido `production`, com aprovação humana e secrets próprios.
- O workflow cria um draft, baixa e confere os assets, instala/inicializa o APK em emulador e somente
  então publica; APK e checksum são os únicos assets públicos.
- Mapping R8, SBOM e relatórios ficam em artifact privado com retenção limitada.
- Tags de validação e endpoints sintéticos não podem produzir GitHub Release.

## Consequências

Uma release exige configuração administrativa além dos arquivos versionados. A instrumentação torna
a CI mais lenta, mas isola falhas de dispositivo. A publicação fica consistente e auditável; perda ou
indisponibilidade do environment impede release em vez de produzir APK parcialmente configurado.

O processo de emergência publica uma nova versão e não move tags existentes, conforme o runbook de
rollback e revogação.
