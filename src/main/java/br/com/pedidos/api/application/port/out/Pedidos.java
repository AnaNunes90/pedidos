package br.com.pedidos.api.application.port.out;

import br.com.pedidos.api.domain.Pedido;

public interface Pedidos {

    Pedido salvar(Pedido pedido);
}
