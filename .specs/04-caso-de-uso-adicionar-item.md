# 04 — Caso de uso: Adicionar Item (ponta a ponta)

## Contexto

`CriarPedido` (spec 02) e o adapter JPA (spec 03) já existem e funcionam ponta a ponta (`POST /pedidos` → 201). `PedidosJpaAdapter` já tem um método `buscarPorId(UUID)`, hoje usado só pelo `PedidosJpaAdapterIT` — não fazia parte do port `Pedidos` porque, na spec 03, nenhum caso de uso real precisava buscar (ver nota de YAGNI lá). Este caso de uso agora precisa buscar, então essa busca sobe para o port.

`Pedido.adicionarItem()` (spec 01) já recusa item fora do estado `ABERTO`, lançando `IllegalStateException` — genérico demais para distinguir por tipo no handler REST, então essa etapa introduz uma exceção dedicada para esse caso específico.

## Tarefa

Implementar, de ponta a ponta, `AdicionarItem`: busca o pedido pelo port `Pedidos`, adiciona o item via domínio, salva pelo mesmo port, e expõe via `POST /pedidos/{id}/itens`. **Sem mudar `CriarPedido`** (nem o port, nem o service, nem o que ele já faz).

## Regras

1. O caso de uso busca o pedido pelo port de saída `Pedidos` (método novo, `buscarPorId`).
2. Pedido inexistente → `PedidoNaoEncontradoException` (aplicação). HTTP: **404**.
3. Pedido não `ABERTO` (pago ou cancelado) → nova exceção de domínio dedicada (substitui o `IllegalStateException` genérico só nesse ponto). HTTP: **409**.
4. Quantidade ou preço inválidos → `ItemInvalidoException` (já existe, spec 03). HTTP: **422** (mesmo handler já existente).
5. Sucesso: o item é adicionado, o pedido resultante (com total recalculado) é salvo pelo port `Pedidos.salvar`, e devolvido.
6. Endpoint `POST /pedidos/{id}/itens` devolve **200** (não 201 — é uma atualização de recurso existente, não criação) com o `PedidoResponse` do pedido atualizado.
7. `PedidoExceptionHandler` (já existente, spec 03) é **ampliado** com os dois novos mapeamentos (404, 409) — não é criado um handler novo.
8. `CriarPedido`/`CriarPedidoService` não são alterados.
9. Erros (404, 409, 422, 400) não persistem nada — nem pedido, nem item.

## Emendas às specs anteriores

- **Spec 02/03 — port `Pedidos`**: ganha `Optional<Pedido> buscarPorId(UUID id)`. `PedidosJpaAdapter.buscarPorId` (já implementado) passa a ser `@Override` desse método do port, em vez de método solto da classe.
- **Spec 01 — `Pedido.adicionarItem`**: quando `status != ABERTO`, passa a lançar `PedidoFechadoException` (nova, pacote `domain`) em vez de `IllegalStateException`. `pagar()`/`cancelar()` **não mudam** — continuam com `IllegalStateException`, pois não são expostos por nenhum endpoint ainda. `PedidoTest` (`naoAceitaItemEmPedidoPago`, `naoAceitaItemEmPedidoCancelado`) passa a esperar `PedidoFechadoException`.
- **Ripple obrigatório**: como `Pedidos` ganha um método novo, o `PedidosEmMemoria` (fake usado dentro de `CriarPedidoServiceTest`) precisa implementar `buscarPorId` para continuar compilando — mesmo sem usá-lo nos testes de `CriarPedido`.

## Contrato dos tipos

```
application.port.out.Pedidos                    (modificado)
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(UUID id);        [novo método no port]

application.port.in.AdicionarItem                (novo)
    Pedido adicionarItem(UUID pedidoId, ItemPedido item);

application.AdicionarItemService implements AdicionarItem   (novo)
    construtor: AdicionarItemService(Pedidos pedidos)

application.PedidoNaoEncontradoException         (novo, RuntimeException, sem framework)

domain.PedidoFechadoException                    (novo, RuntimeException, sem framework)
```

HTTP: `POST /pedidos/{id}/itens`, corpo = `ItemRequest` (já existe, reaproveitado), resposta = `PedidoResponse` (já existe, reaproveitado).

## Caso verificável

Pedido existente com total `37.80` (`CAFE-500` 2×18.90) recebe mais um item `CAFE-500` 1×18.90 → total passa a `56.70`, **mesmo UUID** do pedido (não cria um pedido novo).

## Casos de teste

**Unitários — `AdicionarItemServiceTest` (com `Pedidos` em memória, sem banco)**

| # | Cenário | Resultado esperado |
|---|---|---|
| A1 | Pedido existente `ABERTO` + item válido | pedido retornado tem o item novo, total atualizado; `salvar` foi chamado |
| A2 | Pedido inexistente | `PedidoNaoEncontradoException`; `salvar` nunca chamado |
| A3 | Pedido `PAGO` | `PedidoFechadoException`; `salvar` nunca chamado; pedido no fake continua sem o item |
| A4 | Pedido `CANCELADO` | `PedidoFechadoException`; `salvar` nunca chamado |
| A5 | Quantidade inválida (0) | `ItemInvalidoException`; `salvar` nunca chamado |
| A6 | Preço inválido (0 ou negativo) | `ItemInvalidoException`; `salvar` nunca chamado |

**Domínio — `PedidoTest` (ajuste de tipo de exceção)**

| # | Cenário | Resultado esperado |
|---|---|---|
| D1 | `adicionarItem` em pedido `PAGO` | `PedidoFechadoException` (era `IllegalStateException`) |
| D2 | `adicionarItem` em pedido `CANCELADO` | `PedidoFechadoException` (era `IllegalStateException`) |

**HTTP — manual, aplicação rodando contra Postgres real**

| # | Cenário | Resultado esperado |
|---|---|---|
| H7 | `POST /pedidos/{id}/itens` em pedido existente `ABERTO`, item válido | `200`; total `37.80 + 18.90 = 56.70`; mesmo UUID no corpo |
| H8 | `POST /pedidos/{id-inexistente}/itens` | `404` |
| H9 | `POST /pedidos/{id}/itens` em pedido `PAGO` ou `CANCELADO` | `409` |
| H10 | `POST /pedidos/{id}/itens` com quantidade/preço inválidos | `422` (handler já existente) |
| H11 | Consulta ao banco após H8/H9/H10 | nenhuma linha nova em `item_pedido`; pedido (quando existir) sem alteração |

## Limites de arquivos

**Pode criar/tocar:**
- `application/port/in/AdicionarItem.java` (novo)
- `application/port/out/Pedidos.java` (modificado — novo método)
- `application/AdicionarItemService.java` (novo)
- `application/PedidoNaoEncontradoException.java` (novo)
- `application/CriarPedidoServiceTest.java` (modificado — só o fake `PedidosEmMemoria`, para compilar)
- `domain/PedidoFechadoException.java` (novo)
- `domain/Pedido.java` (modificado — 1 linha, tipo da exceção em `adicionarItem`)
- `domain/PedidoTest.java` (modificado — tipo esperado em 2 testes)
- `adapter/out/persistence/PedidosJpaAdapter.java` (modificado — `buscarPorId` vira `@Override`)
- `adapter/in/rest/PedidoController.java` (modificado — novo método/endpoint)
- `adapter/in/rest/PedidoExceptionHandler.java` (modificado — 2 novos `@ExceptionHandler`)
- `config/CasosDeUsoConfig.java` (modificado — novo `@Bean` para `AdicionarItem`)
- `test/application/AdicionarItemServiceTest.java` (novo)

**Não deve ser tocado:**
- `application/port/in/CriarPedido.java`, `application/CriarPedidoService.java` (lógica inalterada)
- `domain/ItemPedido.java`, `domain/StatusPedido.java`
- `adapter/in/rest/PedidoRequest.java`, `ItemRequest.java`, `PedidoResponse.java`, `ItemResponse.java` (reaproveitados sem mudança)
- `pom.xml`, `application.yml`, `infra/docker-compose.yml`

Pronto quando: A1–A6, D1–D2 passam via `./mvnw test` (sem banco), e H7–H11 são confirmados manualmente contra a aplicação rodando; nenhum import de Spring/JPA em `domain/` ou `application/`.
