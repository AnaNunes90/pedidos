package br.com.pedidos.api.adapter.out.persistence;

import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;

import java.util.List;

final class PedidoMapper {

    private PedidoMapper() {
    }

    static PedidoJpaEntity paraEntidade(Pedido pedido) {
        PedidoJpaEntity entidade = new PedidoJpaEntity(pedido.id(), pedido.clienteId(), pedido.status());
        for (ItemPedido item : pedido.itens()) {
            entidade.adicionarItem(new ItemJpaEntity(item.codigoProduto(), item.quantidade(), item.precoUnitario()));
        }
        return entidade;
    }

    static Pedido paraDominio(PedidoJpaEntity entidade) {
        List<ItemPedido> itens = entidade.getItens().stream()
                .map(item -> new ItemPedido(item.getSku(), item.getQuantidade(), item.getPrecoUnitario()))
                .toList();
        return new Pedido(entidade.getId(), entidade.getClienteId(), entidade.getStatus(), itens);
    }
}
