# ADR 0001: Fronteiras de camadas e domínio testável

- Status: aceita
- Data: 2026-08-01

## Contexto

O aplicativo já usava ViewModels, repositories, Room, Retrofit e Hilt, mas as fronteiras eram
informais. ViewModels dependiam de implementações em `data`, repositories recebiam o banco inteiro,
e o pacote `domain` continha CameraX, ML Kit, WorkManager, Android e mappers de entidades Room. Isso
dificultava testes isolados e permitia que detalhes de infraestrutura vazassem para regras de
negócio e mensagens de UI.

## Decisão

O projeto permanece em módulo único e adota MVVM com fluxo unidirecional de dados (UDF).

As dependências seguem esta direção:

```text
ui -> domain <- data
       ^          |
       +---- di --+
```

- `domain` contém modelos, erros tipados, contratos, relógio, dispatchers e casos de uso puros.
- `data` implementa contratos e concentra Android, CameraX, Room, Retrofit, OkHttp,
  WorkManager, persistência de sessão e conversões DTO/entity.
- `ui` depende de casos de uso e modelos do domínio. Integrações estritamente visuais ou de
  lifecycle podem usar adaptadores Android próprios da UI até serem isoladas completamente.
- `di` é a composition root e pode conhecer implementações e contratos.
- Repositories recebem apenas os DAOs que utilizam; `AppDatabase` fica restrito à criação dos DAOs.
- Mappers de DTO/entity pertencem a `data`; o domínio não conhece representações de transporte ou
  persistência.

## Estratégia de erros

Infraestrutura traduz falhas técnicas em subclasses de `DomainException`, sem propagar bodies HTTP,
mensagens de servidor ou detalhes de armazenamento. Casos de uso propagam esses erros tipados. A UI
é o único lugar que os converte em texto amigável. Erros inesperados recebem mensagem genérica e
não devem revelar dados técnicos ou sensíveis.

## Testabilidade e gates

- Relógio e dispatchers são contratos injetáveis.
- ViewModels recebem casos de uso, não classes concretas de repository.
- Um teste arquitetural varre `domain` e falha ao encontrar imports Android, Compose, Room,
  Retrofit, OkHttp ou do pacote `data`.
- Mudanças de schema Room e contrato HTTP ficam fora desta decisão e exigem PR próprio.

## Consequências

O domínio pode ser testado no JVM sem Android e as integrações ficam substituíveis por fakes. Há
mais tipos e bindings no Hilt, mas o acoplamento passa a ser explícito. `PreviewView` e
`LifecycleOwner` permanecem contidos no adaptador de câmera da UI, sem atravessar a fronteira do
domínio. O ML Kit, presente no contexto original desta decisão, foi removido quando o scan passou a
ser responsabilidade do serviço remoto.

## Rollback

Não há migration de dados, autenticação ou release. Em caso de regressão, este PR pode ser revertido
integralmente sem alterar schema Room, conteúdo persistido ou contrato HTTP.
