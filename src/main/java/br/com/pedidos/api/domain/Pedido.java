package br.com.pedidos.api.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Pedido(UUID id, String clienteId, StatusPedido status, List<ItemPedido> itens) {

    public Pedido {
        Objects.requireNonNull(id, "id não pode ser nulo");
        Objects.requireNonNull(clienteId, "clienteId não pode ser nulo");
        Objects.requireNonNull(status, "status não pode ser nulo");
        Objects.requireNonNull(itens, "itens não pode ser nulo");
        itens = List.copyOf(itens);
    }

    public static Pedido novo(String clienteId) {
        return new Pedido(UUID.randomUUID(), clienteId, StatusPedido.ABERTO, List.of());
    }

    public Pedido adicionarItem(ItemPedido item) {
        Objects.requireNonNull(item, "item não pode ser nulo");
        if (status != StatusPedido.ABERTO) {
            throw new PedidoFechadoException("pedido precisa estar ABERTO para adicionar item");
        }
        List<ItemPedido> novosItens = new ArrayList<>(itens);
        novosItens.add(item);
        return new Pedido(id, clienteId, status, novosItens);
    }

    public Pedido pagar() {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("pedido precisa estar ABERTO para ser pago");
        }
        return new Pedido(id, clienteId, StatusPedido.PAGO, itens);
    }

    public Pedido cancelar() {
        if (status != StatusPedido.ABERTO) {
            throw new IllegalStateException("pedido precisa estar ABERTO para ser cancelado");
        }
        return new Pedido(id, clienteId, StatusPedido.CANCELADO, itens);
    }

    public BigDecimal total() {
        return itens.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
