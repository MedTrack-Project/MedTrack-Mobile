# Configuração de ambientes e segurança de rede

## Princípios

- Endpoints são configuração pública de build, nunca credenciais.
- Senhas, tokens, keystores e chaves privadas não pertencem ao `BuildConfig`.
- Release aceita somente HTTPS e não possui fallback funcional.
- Debug aceita cleartext somente para `10.0.2.2` e `localhost`.
- A URL completa do serviço de scan não vem de entrada do usuário: ela é encapsulada em
  `ApiEndpoints` e injetada pelo `NetworkModule`.

## Origem e precedência

As chaves são:

- `MEDTRACK_API_BASE_URL` — URL base do Retrofit e obrigatoriamente terminada em `/`;
- `MEDTRACK_SCAN_URL` — URL completa do endpoint de detecção.

Precedência:

1. variável de ambiente;
2. Gradle property, incluindo `-P` e `gradle.properties`;
3. `local.properties`;
4. fallback local exclusivo do debug.

Para desenvolvimento, copie `local.properties.example` para `local.properties`. O arquivo real é
ignorado pelo Git.

## Regras por variante

| Regra | Debug | Release |
|---|---|---|
| Configuração ausente | Usa `10.0.2.2` | Falha antes da compilação |
| HTTP | Somente `10.0.2.2`/`localhost` | Rejeitado |
| HTTPS | Permitido | Obrigatório |
| Logging OkHttp | `BASIC`, sem payload | `NONE` |
| Backup de dados | Desabilitado | Desabilitado |

O Network Security Config principal nega cleartext. O source set `debug` sobrepõe a configuração
somente para os hosts locais listados. Ampliar essa allowlist requer revisão explícita.

## Logs e dados sensíveis

O interceptor não registra bodies, pois login e confirmação podem transportar credenciais e dados
de saúde. Headers de autenticação e cookies também são marcados para redaction. Logs próprios devem
usar mensagens estáticas e não incluir:

- usuário ou senha;
- JWT ou header `Authorization`;
- nomes de medicamentos e payloads clínicos;
- URLs de imagens ou caminhos de arquivos;
- corpos ou mensagens de erro retornados pelo backend.

## Backup e sessão

`allowBackup` está desabilitado. As regras para APIs antigas e atuais também excluem banco,
SharedPreferences e arquivos como defesa em profundidade. O JWT é cifrado com AES-GCM e chave não
exportável do Android Keystore. Na primeira leitura, `SessionManager` migra o valor legado de
`MyAppPrefs`: grava e valida a representação protegida antes de remover o texto antigo. Dados
cifrados corrompidos são apagados e exigem nova autenticação. `401` também limpa a sessão.

## Permissões e notificações

As permissões de áudio, storage legado e foreground service foram removidas porque o código atual
não usa essas capacidades. Permanecem:

- `CAMERA`, necessária para captura do medicamento;
- `POST_NOTIFICATIONS`, solicitada em Android 13+;
- `VIBRATE`, usada pelos lembretes;
- `USE_FULL_SCREEN_INTENT`, usada pelo lembrete de dose categorizado como alarme.

O conteúdo das notificações foi marcado como privado na tela bloqueada. Full-screen intent,
`showWhenLocked` e `turnScreenOn` permanecem por fazerem parte do fluxo de lembrete crítico. Antes
de testes públicos e de cada release, esse comportamento deve ser validado contra as políticas da
plataforma e a decisão de produto.

## Verificação

Antes do PR:

```bash
./gradlew checkSecrets
./gradlew qualityCheck
./gradlew assembleDebug
```

Validações negativas recomendadas:

```bash
# Deve falhar: configuração ausente.
env -u MEDTRACK_API_BASE_URL -u MEDTRACK_SCAN_URL ./gradlew assembleRelease

# Deve falhar: cleartext em release.
MEDTRACK_API_BASE_URL=http://api.example.com/ \
MEDTRACK_SCAN_URL=http://ai.example.com/detect \
./gradlew assembleRelease
```

Para o teste positivo de release, use endpoints sintéticos HTTPS e confirme no Manifest mesclado
que `usesCleartextTraffic` permanece `false`.
