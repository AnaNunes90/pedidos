package br.com.pedidos.api.adapter.in.rest;

import br.com.pedidos.api.domain.Pedido;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(
        UUID id,
        String clienteId,
        List<ItemResponse> itens,
        String status,
        BigDecimal total
) {

    static PedidoResponse de(Pedido pedido) {
        List<ItemResponse> itens = pedido.itens().stream()
                .map(ItemResponse::de)
                .toList();
        return new PedidoResponse(pedido.id(), pedido.clienteId(), itens, pedido.status().name(), pedido.total());
    }
}
