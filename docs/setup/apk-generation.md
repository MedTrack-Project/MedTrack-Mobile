# Geração e verificação de APK

## Debug

```bash
./gradlew :app:assembleDebug
```

Saída: `app/build/outputs/apk/debug/app-debug.apk`.

## Release

Consulte [build-release.md](build-release.md). O comando recomendado gera APK assinado/minificado,
valida o budget e produz o SBOM:

```bash
./gradlew releaseReadiness
```

Saída: `app/build/outputs/apk/release/app-release.apk`.

Valide assinatura, versão, tamanho e checksum:

```bash
scripts/release/verify-apk.sh app/build/outputs/apk/release/app-release.apk
```

## Smoke test do APK minificado

Instale exatamente o artefato verificado, primeiro na API mínima (26) e depois no target (36):

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Para o APK fake, rede deve falhar de forma controlada. Verifique:

- inicialização sem crash e sem erro de classes removidas pelo R8;
- login exibe erro controlado com endpoint sintético;
- banco Room abre e migrations suportadas não falham;
- lista e estados locais já persistidos são renderizados;
- notificação abre a rota correta;
- câmera enquadra, captura e cria arquivo privado;
- scan online falha sem expor URL, token, imagem ou dado clínico;
- captura offline entra na fila do WorkManager e mantém política de retry;
- rotação, background/foreground e encerramento não causam crash.

O fluxo completo com sucesso em login/scan/confirmacão fica bloqueado até os serviços definitivos
estarem disponíveis. Registre modelo do dispositivo, API, ABI, tamanho, tempos e consumo de memória.
