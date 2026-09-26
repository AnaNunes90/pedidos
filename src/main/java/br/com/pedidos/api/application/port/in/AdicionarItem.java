package br.com.pedidos.api.application.port.in;

import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;

import java.util.UUID;

public interface AdicionarItem {

    Pedido adicionarItem(UUID pedidoId, ItemPedido item);
}
