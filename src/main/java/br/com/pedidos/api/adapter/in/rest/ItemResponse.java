package br.com.pedidos.api.adapter.in.rest;

import br.com.pedidos.api.domain.ItemPedido;

import java.math.BigDecimal;

public record ItemResponse(String codigoProduto, int quantidade, BigDecimal precoUnitario) {

    static ItemResponse de(ItemPedido item) {
        return new ItemResponse(item.codigoProduto(), item.quantidade(), item.precoUnitario());
    }
}
