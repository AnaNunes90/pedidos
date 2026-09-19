# 02 — Caso de uso: Criar Pedido

## Contexto

O domínio (`Pedido`, `ItemPedido`, `StatusPedido`) já existe, coberto pela spec `01-dominio-pedido.md`. Esta spec adiciona a primeira fatia de aplicação: o caso de uso de criar um pedido, seguindo arquitetura hexagonal (`AGENTS.md`): `application` não pode depender de Spring, JPA, HTTP ou de nenhum adapter concreto.

**Emenda à spec 01**: ao definir o contrato deste caso de uso, ficou decidido que `Pedido` passa a ter um campo `clienteId`, obrigatório. A ambiguidade 1 da spec 01 ("não incluir clienteId ainda") fica superada por esta decisão — registrado aqui em vez de reescrever a spec 01.

## Tarefa

Criar, no pacote `br.com.pedidos.api.application`:

- Port de entrada `CriarPedido` — descreve o que se pode pedir à aplicação.
- Port de saída `Pedidos` — descreve o que a aplicação precisa para persistir um pedido.
- Caso de uso `CriarPedidoService`, implementando `CriarPedido`, orquestrando o domínio e o port de saída.

E, no domínio, adicionar `clienteId` a `Pedido` (com a validação de obrigatoriedade que os demais campos já têm).

Nenhum Controller, JPA, DTO ou configuração nesta etapa — só a aplicação e o ajuste mínimo no domínio para suportá-la.

## Regras

1. `CriarPedido` recebe um `clienteId` e uma lista de `ItemPedido`.
2. Lista de itens vazia (ou nula) é recusada antes de qualquer outra coisa — nem o domínio nem o port de saída são acionados.
3. O caso de uso cria o pedido só com o domínio: `Pedido.novo(clienteId)`, depois `adicionarItem(...)` para cada item da lista.
4. O caso de uso salva o pedido através do port de saída `Pedidos` e devolve o que o port devolver (não necessariamente a mesma instância que foi passada para salvar).
5. `application/` (ports e `CriarPedidoService`) não importa nada de `org.springframework.*`, `jakarta.persistence.*`, nada de HTTP, nem nenhuma classe de `adapter` (que ainda não existe).
6. `Pedido` passa a exigir `clienteId` não nulo, validado no construtor compacto, do mesmo jeito que `id`, `status` e `itens` já são hoje.

## Contrato dos tipos

```
br.com.pedidos.api.application.port.in.CriarPedido        (interface)
    Pedido criar(String clienteId, List<ItemPedido> itens);

br.com.pedidos.api.application.port.out.Pedidos            (interface)
    Pedido salvar(Pedido pedido);

br.com.pedidos.api.application.CriarPedidoService implements CriarPedido
    construtor: CriarPedidoService(Pedidos pedidos)
```

## Definição de pronto (casos de teste)

| # | Regra | Cenário | Resultado esperado |
|---|---|---|---|
| U1 | 2 | `criar(clienteId, List.of())` | lança `IllegalArgumentException`; `Pedidos.salvar` nunca é chamado |
| U2 | 2 | `criar(clienteId, null)` | lança `IllegalArgumentException`; `Pedidos.salvar` nunca é chamado |
| U3 | 1, 3, 6 | `criar(clienteId, [CAFE-500 2x18.90])` | pedido criado com `clienteId`, `status` `ABERTO`, item incluído, `total()` `37.80` |
| U4 | 1, 3 | `criar(clienteId, [item1, item2])` | pedido resultante contém os dois itens, na ordem em que foram passados |
| U5 | 4 | `criar(clienteId, itens)` com `Pedidos` em memória | o pedido devolvido por `criar` é o mesmo que o fake `Pedidos` guardou |
| U6 | 6 | `Pedido.novo(null)` (direto no domínio) | lança exceção (clienteId obrigatório) |
| U7 | 5 | Revisão de imports de todos os arquivos em `application/` | nenhum import de Spring, JPA, HTTP ou `adapter` |

Também é preciso ajustar os testes já existentes de `Pedido` (`PedidoTest.java`) para passar `clienteId` em toda chamada a `Pedido.novo(...)`, mantendo as regras da spec 01 intactas.

Pronto quando: todos os casos acima passam via `./mvnw test`, e nenhum arquivo em `application/` importa Spring/JPA/HTTP/adapter.

## Ambiguidades assumidas (baixo risco, sinalizadas para revisão)

- `clienteId` é `String` (mesmo tipo já cogitado na spec 01).
- Lista de itens `null` é tratada como "vazia" para efeito da regra 2 (mesma exceção).
- `Pedidos` tem só `salvar(Pedido)` nesta etapa — sem `buscarPorId` nem listagem, para não ampliar escopo além do pedido.
