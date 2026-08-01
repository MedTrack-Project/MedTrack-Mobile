# Camada de Dominio

A camada de dominio vive em `app/src/main/java/com/medtrack/mobile/domain`.

Ela representa o vocabulário central do aplicativo: medicamentos, usuario, frequencia de uso, dados
capturados por scan, contratos e regras de negocio testaveis sem Android.

## Estrutura

```text
domain/
├── coroutines/
├── error/
├── model/
│   ├── FrequenciaUsoDomain.kt
│   ├── FrequenciaUsoTipo.kt
│   ├── MedicamentoCapturadoDomain.kt
│   ├── MedicamentoDomain.kt
│   ├── MedicamentoItem.kt
│   └── Usuario.kt
├── repository/
├── service/
├── time/
└── usecase/
```

## Modelos principais

- `MedicamentoDomain`: medicamento cadastrado e sincronizado com o usuario.
- `MedicamentoCapturadoDomain`: medicamento identificado por camera/scan.
- `FrequenciaUsoDomain`: regra de horarios, periodo de uso e continuidade.
- `Usuario`: usuario autenticado.

## Use cases

O pacote `domain/usecase` concentra login/sincronizacao, consulta e confirmacao de dose, scan/fila
offline, ordenacao de medicamentos e calculo de horarios/datas.

Novas regras de negocio que nao pertencem a ViewModel, Repository, DAO ou DTO devem ser candidatas
a esse pacote.

## Contratos

Interfaces de autenticacao, medicamentos, scan, fila offline, sessao e agendamento ficam no dominio.
Relogio e dispatchers tambem sao abstraidos para tornar tempo e concorrencia deterministicos em
testes. Implementacoes Android ficam em `data` ou em adaptadores da UI.

## Regras de separacao

- Domain nao deve depender de Android, Compose, Room, Retrofit, OkHttp ou `data`.
- Domain nao conhece DTOs, entities ou mappers de infraestrutura.
- Domain nao deve executar chamadas HTTP.
- Validacoes e calculos reaproveitaveis devem ser movidos para use cases quando crescerem.
- O teste `DomainBoundaryTest` aplica essa fronteira a cada PR.
