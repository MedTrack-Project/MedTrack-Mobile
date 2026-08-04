# Política de segurança

## Reporte de vulnerabilidades

Não divulgue vulnerabilidades, credenciais ou dados sensíveis em issues, Pull Requests, commits ou
canais públicos.

Reporte a vulnerabilidade por um canal privado mantido pelos responsáveis do repositório,
preferencialmente **GitHub Security Advisories > Report a vulnerability**. Se o private vulnerability
reporting ainda não estiver habilitado, contate privadamente os maintainers listados no repositório
e solicite um canal seguro antes de enviar detalhes técnicos.

Inclua, quando possível:

- componente e versão afetados;
- impacto observado;
- passos mínimos para reprodução;
- evidência sem dados reais de usuários;
- mitigação sugerida;
- forma segura de contato.

Os maintainers devem confirmar o recebimento, avaliar severidade e impacto, preparar a correção em
ambiente privado e coordenar a divulgação. Não há SLA público definido nesta fase; o responsável
deve manter o pesquisador informado sobre o andamento.

## Segredos e dados sensíveis

- Nunca versione tokens, senhas, keystores, chaves privadas, arquivos de credenciais ou dados reais
  de pacientes.
- Use `local.properties` ou variáveis de ambiente para configuração local.
- Use secrets e environments protegidos da plataforma de CI para assinatura e credenciais.
- Materialize o keystore de release apenas em arquivo temporário com acesso restrito e remova-o ao
  final do job. Preserve o keystore de produção em cofre seguro, com backup e acesso auditado.
- Nunca reutilize keystore de validação em produção nem publique mapping R8 junto ao APK.
- Endpoints devem ser injetados no build; URLs antigas ou privadas não devem ser hardcoded.
- Não registre senha, JWT, header `Authorization`, imagens ou dados de saúde em logs.
- Não envie secrets em `BuildConfig`: valores compilados no APK podem ser extraídos.
- Use dados sintéticos em testes, screenshots, fixtures e relatórios.
- Execute `./gradlew checkSecrets` antes de abrir o Pull Request.

Se um segredo for exposto, remova seu uso imediatamente, revogue/rotacione a credencial na origem e
trate a limpeza do histórico como ação complementar — apagar apenas o arquivo não invalida o segredo.

## Dependências e correções

Atualizações de segurança devem ser entregues em PR focado, com testes e avaliação de
compatibilidade. Relatórios de scanners são indícios: confirme impacto e alcance antes de aceitar,
mitigar ou classificar um alerta como falso positivo.
