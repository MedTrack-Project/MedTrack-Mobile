# CURRENT_TASK.md — Adicionar tela de dose com imagem do medicamento

## Objetivo

Adaptar o MedTrack Mobile para consumir, persistir e exibir a imagem do medicamento no fluxo de horário/dose e nas notificações.

## Contexto

A API passou a retornar o campo `imagemUrl` na resposta de `medicamento/mobile/lista`.

Hoje, a listagem de horários aparece em `TelaPrincipal.kt`, usando o componente `ListaHorarios.kt`, 
com um botão de câmera que leva diretamente para `TelaCamera.kt` e depois para `TelaConfirmacao.kt`.

Com a nova necessidade, a confirmação deve passar por uma tela intermediária de dose/horário. Ao 
clicar em um item da lista de horários, o usuário deve navegar para uma nova tela específica daquele
medicamento naquele horário. Essa tela deve exibir os dados da dose, a imagem do medicamento quando 
disponível e o botão para abrir a câmera.

## Escopo

- Atualizar o DTO mobile de medicamento para receber `imagemUrl`.
- Atualizar a Entity do Room para persistir a URL da imagem.
- Criar migration do Room para adicionar o campo de imagem sem quebrar dados existentes.
- Atualizar o Domain Model usado por ViewModels e UI.
- Ajustar mappers entre DTO, Entity e Domain.
- Criar tela de detalhe de dose/horário.
- Tornar os itens de `ListaHorarios.kt` clicáveis.
- Navegar da lista de horários para a nova tela de dose/horário.
- Mover o botão de câmera da tela principal para a nova tela de dose/horário.
- Manter o fluxo existente de câmera e confirmação após o clique no botão.
- Exibir a imagem do medicamento na nova tela quando `imagemUrl` estiver disponível.
- Exibir fallback visual quando o medicamento não possuir imagem.
- Adicionar miniatura da imagem do medicamento nas notificações quando disponível.
- Garantir que notificações e telas continuem funcionando para medicamentos sem imagem.

## Fora do escopo

- Alterar o endpoint da API.
- Fazer upload de imagem pelo mobile.
- Implementar cache local de arquivo de imagem.
- Alterar o modelo de IA/OCR.
- Refatorar completamente a navegação do app.
- Reescrever o fluxo de confirmação já existente.
- Mudar regras de negócio de confirmação de dose.

## Arquivos prováveis

- `app/src/main/java/com/example/piec_1/data/remote/dto/...`
- `app/src/main/java/com/example/piec_1/data/local/entity/...`
- `app/src/main/java/com/example/piec_1/data/local/AppDatabase.kt`
- `app/src/main/java/com/example/piec_1/data/mapper/...`
- `app/src/main/java/com/example/piec_1/domain/model/...`
- `app/src/main/java/com/example/piec_1/data/repository/MedTrackRepository.kt`
- `app/src/main/java/com/example/piec_1/ui/screen/TelaPrincipal.kt`
- `app/src/main/java/com/example/piec_1/ui/screen/TelaDoseHorario.kt`
- `app/src/main/java/com/example/piec_1/ui/components/ListaHorarios.kt`
- `app/src/main/java/com/example/piec_1/ui/navigation/AppNavigation.kt`
- `app/src/main/java/com/example/piec_1/utils/notification/...`

## Critérios de aceite

- [ ] O DTO mobile recebe corretamente o campo `imagemUrl`.
- [ ] A Entity do Room possui campo para armazenar a URL da imagem.
- [ ] Existe migration do Room para preservar medicamentos já cadastrados.
- [ ] O Domain Model expõe a imagem para ViewModels e UI.
- [ ] Os mappers preservam `imagemUrl` no fluxo DTO -> Entity -> Domain.
- [ ] A lista de horários permite clique em um medicamento/horário específico.
- [ ] O clique em um horário navega para a nova tela de dose/horário.
- [ ] A nova tela exibe nome, horário/dose e imagem do medicamento quando disponível.
- [ ] Medicamentos sem imagem exibem fallback visual adequado.
- [ ] O botão de câmera fica na nova tela de dose/horário.
- [ ] O fluxo `TelaDoseHorario.kt -> TelaCamera.kt -> TelaConfirmacao.kt` funciona sem regressão.
- [ ] As notificações exibem miniatura do medicamento quando houver imagem.
- [ ] Notificações de medicamentos sem imagem continuam funcionando normalmente.
- [ ] A UI não acessa Retrofit, Room ou SharedPreferences diretamente.
- [ ] Operações de IO continuam fora da Main Thread.
- [ ] O app compila sem erros.

## Validação

Executar:

```bash
./gradlew clean build
````

Quando aplicável, executar também:

```bash
./gradlew test
```

Validar manualmente:

* Fazer login e carregar medicamentos pela API.
* Confirmar que medicamentos com `imagemUrl` são persistidos no Room.
* Abrir a tela principal.
* Clicar em um horário da lista.
* Confirmar navegação para a nova tela de dose/horário.
* Confirmar exibição da imagem do medicamento.
* Testar medicamento sem imagem.
* Clicar no botão de câmera.
* Confirmar continuidade do fluxo até `TelaConfirmacao.kt`.
* Receber notificação de medicamento com imagem.
* Receber notificação de medicamento sem imagem.

## Observações

* Usar `imagemUrl: String?` para manter compatibilidade com medicamentos sem imagem.
* Preferir bibliotecas já existentes no projeto para carregamento de imagem.
* Caso nenhuma biblioteca esteja configurada, avaliar uso de Coil para Compose.
* Não armazenar a imagem binária no Room nesta tarefa; persistir apenas a URL.
* A nova tela deve receber identificadores suficientes para localizar o medicamento e o 
* horário correto.
* Evitar passar objetos grandes por rota de navegação.
* A lógica de busca da dose deve permanecer em ViewModel/Repository, não diretamente na UI.
* Preservar a arquitetura em camadas: DTO em `data/remote`, Entity em `data/local`, Domain em 
* `domain/model`, UI em `ui`.
