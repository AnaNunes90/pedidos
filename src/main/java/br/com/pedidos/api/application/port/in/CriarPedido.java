package br.com.pedidos.api.application.port.in;

import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;

import java.util.List;

public interface CriarPedido {

    Pedido criar(String clienteId, List<ItemPedido> itens);
}
