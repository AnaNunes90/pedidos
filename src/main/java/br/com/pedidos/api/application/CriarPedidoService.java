package br.com.pedidos.api.application;

import br.com.pedidos.api.application.port.in.CriarPedido;
import br.com.pedidos.api.application.port.out.Pedidos;
import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;

import java.util.List;
import java.util.Objects;

public class CriarPedidoService implements CriarPedido {

    private final Pedidos pedidos;

    public CriarPedidoService(Pedidos pedidos) {
        this.pedidos = Objects.requireNonNull(pedidos, "pedidos não pode ser nulo");
    }

    @Override
    public Pedido criar(String clienteId, List<ItemPedido> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("itens não pode ser vazio");
        }

        Pedido pedido = Pedido.novo(clienteId);
        for (ItemPedido item : itens) {
            pedido = pedido.adicionarItem(item);
        }

        return pedidos.salvar(pedido);
    }
}
