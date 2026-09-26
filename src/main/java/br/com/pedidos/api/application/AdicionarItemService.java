package br.com.pedidos.api.application;

import br.com.pedidos.api.application.port.in.AdicionarItem;
import br.com.pedidos.api.application.port.out.Pedidos;
import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;

import java.util.Objects;
import java.util.UUID;

public class AdicionarItemService implements AdicionarItem {

    private final Pedidos pedidos;

    public AdicionarItemService(Pedidos pedidos) {
        this.pedidos = Objects.requireNonNull(pedidos, "pedidos não pode ser nulo");
    }

    @Override
    public Pedido adicionarItem(UUID pedidoId, ItemPedido item) {
        Pedido pedido = pedidos.buscarPorId(pedidoId)
                .orElseThrow(() -> new PedidoNaoEncontradoException("pedido não encontrado: " + pedidoId));

        Pedido atualizado = pedido.adicionarItem(item);

        return pedidos.salvar(atualizado);
    }
}
