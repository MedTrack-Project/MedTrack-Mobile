# Convencoes

## Camadas

- UI chama ViewModel.
- ViewModel chama Repository ou service injetado.
- Repository coordena Room, Retrofit, sessao, mappers e sincronizacao.
- DAO acessa Room.
- ApiService acessa backend.

## Modelos

- `Dto`: usado apenas para rede.
- `Entity`: usado apenas para Room.
- `Domain`: usado pela aplicacao.
- `Mapper`: converte entre DTO, Entity e Domain.

## Nomes

- Telas Compose usam prefixo `Tela`.
- ViewModels terminam com `ViewModel`.
- Repositories terminam com `Repository`.
- DAOs terminam com `Dao`.
- Entidades Room terminam com `Entity`.
- DTOs terminam com `Dto`.

## Estado e erro

- Estados de UI devem representar loading, sucesso e erro de forma explicita.
- Excecoes especificas devem ficar em `utils/exceptions`.
- Mensagens de erro para usuario devem ser traduzidas na ViewModel ou camada de apresentacao.

## Persistencia

- Toda alteracao Room precisa de migration.
- Confirmacoes devem evitar duplicidade por medicamento, data e horario.
- Dados pendentes de sincronizacao devem ser persistidos antes da tentativa remota.

## Rede

- Endpoints e URLs base devem vir de configuracao, nao de strings espalhadas.
- DTOs nao devem vazar para UI.
- Token deve ser obtido via `AuthRepository`/`SessionManager`.

## Testes

- Toda nova implementacao mobile deve incluir testes relacionados quando houver regra, mapper, ViewModel, Repository ou persistencia alterada.
- O comando minimo antes de abrir PR e `./gradlew test`.
- A metrica de cobertura dos testes unitarios deve ser gerada com Kover usando `./gradlew :app:koverHtmlReportDebug :app:koverXmlReportDebug` quando solicitada.
- O relatorio Kover atual mede o escopo unitario: dominio, mappers, utilitarios puros e ViewModels testaveis com fakes. UI Compose, Room/DAO, Camera, notificacoes, Hilt e repositories concretos ficam fora dessa metrica ate a etapa de testes instrumentados/integracao.
- Testes unitarios ficam em `app/src/test/` e nao devem depender de backend, internet ou Android Framework.
- Testes instrumentados ficam em `app/src/androidTest/` e devem ser usados para Room, DAO, migrations e comportamentos que dependem do Android.

## UI

- Componentes reutilizaveis nao devem conhecer repositories.
- Rotas devem ficar centralizadas em `AppRoutes`.
- Telas recebem callbacks de navegacao.
