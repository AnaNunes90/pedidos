# 01 — Domínio de Pedido

## Contexto

O projeto ainda não tem código de domínio (só o esqueleto padrão do Spring Boot). Conforme `AGENTS.md`, a camada de domínio deve ser modelada sem dependência de Spring/JPA, com dinheiro em `BigDecimal` e sem Lombok. Esta é a primeira spec em `.specs/`, cobrindo o núcleo do domínio de pedidos: `Pedido`, `ItemPedido` e `StatusPedido`.

Esta spec descreve **comportamento**, não implementação — os nomes de método/classe sugeridos são um mapeamento proposto, não uma exigência rígida além do que foi explicitamente pedido (`Pedido.novo`, `adicionarItem`).

## Tarefa

Modelar `Pedido`, `ItemPedido` e `StatusPedido` no pacote de domínio (sem Spring/JPA), como tipos imutáveis (records para os dados compostos), cobrindo as regras abaixo. Nenhum código deve ser escrito nesta etapa — apenas esta spec.

## Regras

Cada regra é descrita primeiro como comportamento observável, depois com o mapeamento de implementação sugerido.

1. **Quantidade e preço são sempre positivos.**
   Comportamento: um item de pedido não pode existir com quantidade ou preço unitário zero ou negativo.
   Implementação sugerida: validação no construtor compacto de `ItemPedido`, lançando exceção quando inválido.

2. **Total é derivado, nunca armazenado.**
   Comportamento: o total do pedido é sempre calculado a partir dos itens (quantidade × preço unitário de cada item, somados); não existe campo mutável de total.
   Implementação sugerida: método `total()` em `Pedido`, calculado sob demanda.

3. **Dinheiro usa `BigDecimal`.**
   Comportamento: nenhum valor monetário (preço unitário, total de item, total do pedido) usa `double`/`float`.
   Implementação sugerida: `BigDecimal` em todo campo e retorno monetário.

4. **Pedido novo começa ABERTO.**
   Comportamento: ao criar um pedido novo, ele nasce vazio (sem itens), no estado `ABERTO`, com identificador único.
   Implementação sugerida: `Pedido.novo()` cria um rascunho com `UUID` gerado internamente, status `ABERTO`, lista de itens vazia.

5. **Adicionar item devolve um novo pedido.**
   Comportamento: adicionar um item não modifica o pedido existente; produz um novo pedido com o item incluído. O pedido original permanece inalterado.
   Implementação sugerida: `Pedido.adicionarItem(ItemPedido)` retorna uma nova instância de `Pedido`.

6. **Pedido pago ou cancelado não aceita novo item.**
   Comportamento: tentar adicionar item a um pedido que não está `ABERTO` é recusado; o pedido original permanece inalterado.
   Implementação sugerida: `adicionarItem` lança exceção quando `status != ABERTO`.

7. **Listas não podem ser modificadas por fora.**
   Comportamento: quem obtém a lista de itens de um pedido não consegue alterar o estado interno do pedido através dela.
   Implementação sugerida: cópia defensiva na construção e retorno de lista não modificável em `itens()`.

8. **Nenhuma dependência de Spring/JPA.**
   Comportamento: `Pedido`, `ItemPedido` e `StatusPedido` não têm nenhum import de `org.springframework.*` nem `jakarta.persistence.*`.
   Implementação sugerida: classes vivem isoladas em um pacote de domínio, sem anotações de framework.

9. **Transições de estado a partir de ABERTO.**
   Comportamento: um pedido `ABERTO` pode ser pago (→ `PAGO`) ou cancelado (→ `CANCELADO`). Qualquer outra transição (pagar um já pago ou cancelado, cancelar um já pago ou cancelado) é recusada.
   Implementação sugerida: métodos `pagar()` e `cancelar()` em `Pedido`, cada um retornando um novo `Pedido` com status atualizado, ou lançando exceção se o status atual não for `ABERTO`.

10. **Records e cópia defensiva.**
    Comportamento: `Pedido` e `ItemPedido` são dados imutáveis.
    Implementação sugerida: `record` para `Pedido` e `ItemPedido`, com validação/cópia defensiva no construtor compacto; `StatusPedido` como `enum` (não é dado composto, então não se aplica "record" a ele).

### Exemplo de referência (da aula)

Cliente c-1 compra CAFE-500: 2 unidades × R$ 18,90 = R$ 37,80.

Vira caso de teste: `Pedido.novo()` + `adicionarItem(CAFE-500, 2, 18.90)` → `total()` deve ser `37.80`.

## Definição de pronto

Cada regra acima vira ao menos um caso de teste. Lista mínima de casos (incluindo caminhos de erro):

| # | Regra | Cenário | Resultado esperado |
|---|---|---|---|
| T1 | 1 | `ItemPedido` com quantidade 0 | lança exceção |
| T2 | 1 | `ItemPedido` com quantidade negativa | lança exceção |
| T3 | 1 | `ItemPedido` com preço unitário 0 | lança exceção |
| T4 | 1 | `ItemPedido` com preço unitário negativo | lança exceção |
| T5 | 2, 3 | Pedido com item CAFE-500 (2 × 18.90) | `total()` == `37.80` (BigDecimal) |
| T6 | 2 | Pedido com dois itens diferentes | `total()` == soma dos subtotais |
| T7 | 4 | `Pedido.novo()` | status `ABERTO`, itens vazio, id não nulo |
| T8 | 4 | Duas chamadas a `Pedido.novo()` | ids diferentes entre si |
| T9 | 5 | `pedido.adicionarItem(item)` | retorna instância diferente da original; pedido original continua sem o item |
| T10 | 6 | `pedido.pagar().adicionarItem(item)` | lança exceção; pedido pago permanece sem o item |
| T11 | 6 | `pedido.cancelar().adicionarItem(item)` | lança exceção; pedido cancelado permanece sem o item |
| T12 | 7 | Obter `pedido.itens()` e tentar modificar a lista retornada | lança `UnsupportedOperationException` (ou equivalente) e não afeta o pedido |
| T13 | 9 | `pedido.pagar()` (pedido `ABERTO`) | novo pedido com status `PAGO` |
| T14 | 9 | `pedido.cancelar()` (pedido `ABERTO`) | novo pedido com status `CANCELADO` |
| T15 | 9 | `pedido.pagar()` em pedido já `PAGO` | lança exceção |
| T16 | 9 | `pedido.pagar()` em pedido `CANCELADO` | lança exceção |
| T17 | 9 | `pedido.cancelar()` em pedido já `CANCELADO` | lança exceção |
| T18 | 9 | `pedido.cancelar()` em pedido `PAGO` | lança exceção |
| T19 | 8 | Revisão de imports de `Pedido`/`ItemPedido`/`StatusPedido` | nenhum import de `org.springframework.*` ou `jakarta.persistence.*` |
| T20 | exemplo da aula | `Pedido.novo()` + `adicionarItem(CAFE-500, 2, 18.90)` | `total()` == `37.80` |

Pronto quando: todos os casos acima existem como testes automatizados, passam via `./mvnw test` (Maven Wrapper), e o domínio não tem nenhuma dependência de Spring/JPA.

## Ambiguidades a decidir

1. **"c-1" no exemplo**: o domínio descrito (`Pedido`, `ItemPedido`, `StatusPedido`) não inclui um `Cliente`. O `c-1` do exemplo da aula deve virar um campo no `Pedido` (ex. `clienteId`) já nesta spec, ou fica só como contexto do exemplo sem representação no código por enquanto?
2. **Tipo de exceção**: para "recusa" (adicionar item fora de ABERTO, transição de estado inválida, quantidade/preço inválidos), devo usar exceções padrão do Java (`IllegalArgumentException`/`IllegalStateException`) ou criar exceções de domínio dedicadas (ex. `PedidoNaoAbertoException`)?
3. **Campos de `ItemPedido`**: assumi `codigoProduto` (String, ex. `"CAFE-500"`), `quantidade` e `precoUnitario` (`BigDecimal`). Precisa também de um nome/descrição do produto, ou o código já é suficiente para este domínio inicial?
4. **Item repetido**: se `adicionarItem` for chamado duas vezes com o mesmo `codigoProduto`, o resultado deve somar as quantidades numa única linha, ou manter duas linhas separadas no pedido?
5. **Escala/arredondamento do `BigDecimal`**: preço unitário e total devem ter escala fixa (ex. sempre 2 casas decimais, com `RoundingMode` definido), ou usar a escala natural resultante da multiplicação/soma sem arredondamento explícito?
6. **Tipo de `quantidade`**: assumi um inteiro (`int` ou `long`). O domínio precisa suportar quantidade fracionária (ex. peso em kg) em algum momento, o que mudaria para `BigDecimal`?
7. **Geração do UUID em `Pedido.novo()`**: assumi que o UUID é gerado internamente (`UUID.randomUUID()`). Confirma que não deve ser passado como parâmetro externo (ex. vindo de um adapter)?
