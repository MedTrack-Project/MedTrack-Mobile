# Contratos HTTP consumidos pelo aplicativo

Este documento congela o contrato HTTP v1 esperado pelo aplicativo. Alteracoes incompatíveis no
backend ou no servico de IA exigem nova versao deste documento, fixtures atualizadas e contract tests.

| Operacao | Metodo e path | Autenticacao | Corpo/resposta | Compatibilidade |
|---|---|---|---|---|
| Login | `POST /auth/mobile/login` | Nao | JSON `username`/`password`; retorna `token` | backend mobile v1 |
| Usuario | `GET /usuario/mobile` | Bearer | `UsuarioDto` | backend mobile v1 |
| Medicamentos | `GET /medicamento/mobile/lista` | Bearer | lista de `MedicamentoDto` | backend mobile v1 |
| Confirmacao | `POST /api/confirmacao` | Bearer | multipart `dados` (JSON) e `imagem` opcional | backend mobile v1 |
| Scan | URL configurada por `MEDTRACK_SCAN_URL` | Bearer | multipart unico `file`; `ScanResponseDto` | IA detect v1 |

## Regras

- Datas usam ISO-8601 `yyyy-MM-dd`; horarios usam `HH:mm` ou `HH:mm:ss` e sao normalizados no dominio.
- HTTP `401`/`403` expira a sessao; `5xx`, falha de rede, resposta ausente e JSON invalido sao erros
  distintos. Mensagens de infraestrutura nao chegam diretamente a UI.
- O cliente adiciona `Authorization: Bearer <token>` centralmente, exceto no login.
- O host do scan continua validado e injetado por ambiente; nenhuma fixture executa chamadas externas.
- Gson é o serializer atual. Nomes divergentes, como `agente_ativo`, usam
  `@SerializedName` e nao contaminam modelos de dominio.

Fixtures aprovadas ficam em `app/src/test/resources/contracts/v1`.
