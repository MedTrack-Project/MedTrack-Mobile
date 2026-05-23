# Camada de Dominio

A camada de dominio vive em `app/src/main/java/com/example/piec_1/domain`.

Ela representa o vocabulário central do aplicativo: medicamentos, usuario, frequencia de uso, dados capturados por scan e regras auxiliares.

## Estrutura

```text
domain/
├── model/
│   ├── mappers/
│   ├── FrequenciaUsoDomain.kt
│   ├── FrequenciaUsoTipo.kt
│   ├── MedicamentoCapturadoDomain.kt
│   ├── MedicamentoDomain.kt
│   ├── MedicamentoItem.kt
│   └── Usuario.kt
├── service/
└── usecase/
```

## Modelos principais

- `MedicamentoDomain`: medicamento cadastrado e sincronizado com o usuario.
- `MedicamentoCapturadoDomain`: medicamento identificado por camera/scan.
- `FrequenciaUsoDomain`: regra de horarios, periodo de uso e continuidade.
- `Usuario`: usuario autenticado.

## Use cases

O pacote `domain/usecase` concentra funcoes de regra reaproveitavel, como ordenacao de medicamentos
e calculo de horarios/datas.

Novas regras de negocio que nao pertencem a ViewModel, Repository, DAO ou DTO devem ser candidatas
a esse pacote.

## Services

O pacote `domain/service` contem servicos ligados a capacidades do app:

- `CameraService`: operacoes de camera e captura.
- `DetectionService`: suporte a deteccao.
- `ScanUpload`: Worker para processar scans offline pendentes.

Embora `ScanUpload` dependa de infraestrutura Android, ele representa um fluxo de dominio: 
transformar uma captura offline pendente em medicamento capturado e notificar o usuario.

## Regras de separacao

- Domain nao deve depender de Compose.
- Domain nao deve conhecer DTOs diretamente fora dos mappers.
- Domain nao deve executar chamadas HTTP.
- Validacoes e calculos reaproveitaveis devem ser movidos para use cases quando crescerem.
