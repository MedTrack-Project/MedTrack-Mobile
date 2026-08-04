# Runbook de rollback e revogação

## Objetivo

Conter uma release Android incorreta ou comprometida sem sobrescrever artifacts, esconder evidências
ou reutilizar uma versão já distribuída. GitHub Release não equivale a atualização forçada nos
dispositivos; a correção sempre requer uma nova versão.

## Classificação inicial

Interrompa novas publicações e registre um incidente privado. Classifique:

- falha funcional sem risco de dados;
- contrato ou endpoint incorreto;
- migration incompatível ou perda de dados;
- vazamento de token, keystore ou outro secret;
- dependência vulnerável;
- exposição de dados pessoais ou de saúde.

Não copie payloads, imagens ou credenciais para issues, logs ou canais públicos.

## Contenção

1. Desative o workflow/environment `production` ou remova temporariamente seus aprovadores.
2. Marque o GitHub Release afetado como draft ou remova apenas os assets públicos, preservando
   evidências privadas do job e o commit/tag para auditoria.
3. Se a API estiver envolvida, bloqueie a versão no backend apenas se houver mecanismo previamente
   testado e comunicação ao usuário.
4. Em caso de credencial, revogue primeiro; alterar o repositório ou apagar logs não revoga secrets.

## Credenciais e assinatura

- Endpoint ou token: rotacione no provedor, atualize o environment e audite acessos.
- Keystore exposto: restrinja distribuição imediatamente e siga o processo de troca de chave da
  plataforma de distribuição. Não gere silenciosamente outra chave esperando compatibilidade.
- Secret do GitHub: remova/rotacione e revise logs/artifacts de todas as execuções acessíveis.
- Mapping R8 exposto: remova o artifact público, revise alcance e preserve uma cópia privada.

## Correção

1. Crie branch de correção a partir do commit adequado da `main`.
2. Reproduza e adicione teste de regressão quando possível.
3. Reavalie migrations e compatibilidade de dados; nunca reduza a versão Room.
4. Execute CI completa e smoke test do APK minificado.
5. Incremente SemVer e crie uma nova tag. Nunca mova, apague ou recrie a tag afetada para substituir
   binários sob o mesmo nome.
6. Publique nova release e valide checksum, assinatura, endpoints e instalação/upgrade.

## Validação pós-publicação

- baixar APK e checksum do GitHub Release em ambiente limpo;
- executar `sha256sum --check`;
- verificar assinatura e versionName/versionCode;
- instalar sobre a versão anterior e em instalação limpa;
- executar login, banco, notificações, câmera, scan e fila offline;
- confirmar que endpoints e secrets revogados não funcionam mais;
- monitorar falhas sem registrar dados sensíveis.

## Comunicação e encerramento

Documente linha do tempo, versões afetadas, impacto, decisões, owners e ações preventivas. Se houver
dados pessoais ou de saúde, acione os responsáveis legais antes de comunicação pública. Encerre
somente depois de validar a nova release, confirmar revogações e criar tarefas de prevenção.
