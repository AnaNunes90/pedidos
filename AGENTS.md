# AGENTS.md

Fonte principal de regras para qualquer agente (humano ou IA) trabalhando neste repositório.

## Objetivo do serviço

API de pedidos (`br.com.pedidos`), construída como serviço backend em Java.

## Stack

- **Java 21**
- **Spring Boot** (ver `pom.xml` para a versão exata)

## Comandos

Sempre usar o Maven Wrapper, nunca um `mvn` instalado globalmente:

- Rodar testes: `./mvnw test` (Linux/macOS) ou `mvnw.cmd test` (Windows)
- Build: `./mvnw clean verify`
- Rodar a aplicação: `./mvnw spring-boot:run`

## Arquitetura

Arquitetura hexagonal (ports & adapters):

- **Domínio** (`domain`): regras de negócio puras. **Sem Spring, sem JPA, sem anotações de framework.**
- **Aplicação** (`application`): casos de uso, orquestra o domínio. **Sem Spring, sem JPA.**
- **Adapters** (`adapter`/`infrastructure`): onde Spring, JPA, controllers REST, persistência e demais integrações vivem. É a única camada que pode depender de framework.

Dependências apontam sempre de fora para dentro: adapters dependem de aplicação/domínio, nunca o contrário.

## Convenções de código

- **Dinheiro é sempre `BigDecimal`** — nunca `double`/`float` para valores monetários.
- **Sem Lombok.** Escrever construtores, getters e `equals`/`hashCode`/`toString` explicitamente.
- **Nenhuma dependência nova** (no `pom.xml` ou qualquer outra) sem pedir e obter aprovação explícita antes.

## Fluxo de trabalho obrigatório

1. Ler a spec indicada (ver `.specs/`).
2. Planejar a mudança e apresentar o plano.
3. Aguardar OK explícito antes de editar qualquer arquivo.
4. Implementar a mudança.
5. Rodar os testes via Maven Wrapper.
6. Mostrar o diff das mudanças antes de considerar a tarefa concluída.
