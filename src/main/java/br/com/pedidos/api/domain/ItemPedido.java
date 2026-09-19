package br.com.pedidos.api.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record ItemPedido(String codigoProduto, int quantidade, BigDecimal precoUnitario) {

    public ItemPedido {
        Objects.requireNonNull(codigoProduto, "codigoProduto não pode ser nulo");
        Objects.requireNonNull(precoUnitario, "precoUnitario não pode ser nulo");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade deve ser positiva");
        }
        if (precoUnitario.signum() <= 0) {
            throw new IllegalArgumentException("precoUnitario deve ser positivo");
        }
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
