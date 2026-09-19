# 03 — Adapters REST e JPA (checkpoint 4)

## Contexto

O domínio (spec 01) e o caso de uso `CriarPedido` (spec 02) já existem e não dependem de framework. O banco Postgres local já está disponível (`infra/docker-compose.yml`, comprovado com `pg_isready` e `SELECT 1`).

Este checkpoint (4) prova, de ponta a ponta, que o sistema funciona com três evidências:

1. **Banco disponível** — já comprovado no checkpoint anterior.
2. **Persistência pelo adapter** (JPA) — objetivo desta etapa, **checkpoint 4B**.
3. **HTTP** (endpoint REST) — **checkpoint 4C**, fora do escopo desta spec. Nenhum Controller é criado aqui.

## Tarefa (escopo do checkpoint 4B)

Implementar o adapter de saída de persistência para `Pedidos` (port de saída da spec 02), usando Spring Data JPA contra o Postgres do `infra/docker-compose.yml`, sem alterar domínio ou aplicação.

## Regras

1. `pom.xml` ganha **somente** Data JPA e driver PostgreSQL — **já estão presentes**, nenhuma edição necessária.
2. `domain/` e `application/` continuam sem nenhum import de Spring/JPA — só o pacote `adapter` conhece framework.
3. Tabela `pedido` mapeada por `PedidoJpaEntity`. O `id` é o `UUID` do domínio — **sem `@GeneratedValue`**, o valor vem sempre de `Pedido.id()`.
4. Não existe coluna de total — o total continua derivado (`Pedido.total()`), calculado a partir dos itens após a leitura.
5. Cada item persistido guarda `sku`, `quantidade` e `precoUnitario` (mapeado de/para `ItemPedido.codigoProduto/quantidade/precoUnitario`).
6. O adapter usa transação (`@Transactional`) e faz o mapeamento de entidade → domínio **antes** de a sessão fechar (necessário porque `spring.jpa.open-in-view=false`, então não há sessão aberta fora da transação).
7. Testes unitários (domínio, aplicação) continuam passando sem banco, via `./mvnw test`.
8. Um teste de integração dedicado (`PedidosJpaAdapterIT`) é criado, com execução **explícita** (não roda no `./mvnw test` padrão).
9. O teste de contexto já existente (`ApiApplicationTests`) é **renomeado** para a mesma convenção `*IT` (`ApiApplicationIT`) — não é apagado nem desativado, só passa a fazer parte do grupo de testes de integração (faz sentido: ele sempre precisou de um contexto Spring completo, e agora precisa de datasource real).
10. Nada de H2, endpoint HTTP, Lombok ou MapStruct nesta etapa.

## Por que `*IT` e não rodar tudo junto

O Surefire (plugin usado por `./mvnw test`) só pega `*Tests`/`*Test` por padrão — não pega `*IT`. Ao renomear `ApiApplicationTests` → `ApiApplicationIT`, ele some do `./mvnw test` (que passa a rodar só unitários, sem precisar do Postgres de pé) e só roda quando chamado explicitamente:

```
./mvnw test -Dtest=ApiApplicationIT,PedidosJpaAdapterIT
```

Não estou adicionando o plugin Failsafe (isso seria uma dependência/configuração de build nova, fora do que foi pedido) — a convenção de nome já resolve a separação sem precisar de plugin novo.

## Contrato dos tipos (novos arquivos, todos em `adapter.out.persistence`)

```
PedidoJpaEntity      @Entity @Table(name = "pedido")
                      id: UUID (@Id, sem geração)
                      clienteId: String
                      status: StatusPedido (@Enumerated(STRING)) — reaproveita o enum do domínio
                      itens: List<ItemJpaEntity> (@OneToMany, mappedBy = "pedido", cascade ALL, orphanRemoval)

ItemJpaEntity         @Entity @Table(name = "item_pedido")
                      id: Long (@Id @GeneratedValue) — chave técnica só de persistência, não existe no domínio
                      sku: String
                      quantidade: int
                      precoUnitario: BigDecimal
                      pedido: PedidoJpaEntity (@ManyToOne @JoinColumn(name = "pedido_id"))

PedidoJpaRepository   interface extends JpaRepository<PedidoJpaEntity, UUID>

PedidoMapper          métodos estáticos:
                      PedidoJpaEntity paraEntidade(Pedido)
                      Pedido paraDominio(PedidoJpaEntity)

PedidosJpaAdapter     @Component, implements Pedidos (port de saída)
                      Pedido salvar(Pedido)                — @Transactional
                      Optional<Pedido> buscarPorId(UUID)   — @Transactional; método extra da classe,
                                                              NÃO faz parte do port `Pedidos` (ele só tem
                                                              `salvar` — ver spec 02). Existe só para o
                                                              IT comprovar a persistência. Evita repetir
                                                              o erro de "interface desnecessária" que já
                                                              discutimos: não há caso de uso de consulta
                                                              ainda, então o port não cresce à toa.
```

## Decisões assumidas nesta etapa (documentadas, não bloqueantes)

- **Schema**: `spring.jpa.hibernate.ddl-auto=update` no `application.yml` — cria as tabelas automaticamente a partir das entidades. Sem Flyway/Liquibase por enquanto (não pedido; considerar numa etapa futura).
- **Nome da tabela de itens**: `item_pedido` (não especificado no pedido).
- **Coluna de dinheiro**: `numeric(19,2)` para `preco_unitario` — padrão comum para valores monetários; compatível com os valores do exemplo (18.90).
- **`StatusPedido`** é reaproveitado diretamente como `@Enumerated(STRING)` na entidade, em vez de duplicar um enum só de persistência — reduz código, e o enum do domínio continua sem nenhuma anotação (a anotação fica na entidade, não no enum).

## Definição de pronto (prova 2 — persistência pelo adapter)

| # | Cenário | Resultado esperado |
|---|---|---|
| P1 | `./mvnw test` (sem banco de pé) | todos os testes unitários (domínio + aplicação) passam; nenhum tenta conectar no Postgres |
| P2 | `./mvnw test -Dtest=PedidosJpaAdapterIT` (com Postgres do `infra/docker-compose.yml` de pé) | salva pedido de `c-1` com `CAFE-500` (2 × 18.90), recupera pelo UUID **numa transação nova**, confere itens, `status` `ABERTO` e `total()` `37.80` |
| P3 | `./mvnw test -Dtest=ApiApplicationIT` (com Postgres de pé) | contexto Spring sobe com sucesso (datasource real configurado) |

Prova 1 (banco disponível) e prova 3/HTTP ficam como estão — prova 1 já satisfeita, prova HTTP é o checkpoint 4C, spec futura.

Pronto quando: P1, P2 e P3 passam, e nenhum arquivo de `domain/` ou `application/` importa Spring/JPA.
