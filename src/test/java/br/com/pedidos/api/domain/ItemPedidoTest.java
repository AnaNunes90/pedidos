package br.com.pedidos.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemPedidoTest {

    @Test
    void naoAceitaQuantidadeZero() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 0, new BigDecimal("18.90")));
    }

    @Test
    void naoAceitaQuantidadeNegativa() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", -1, new BigDecimal("18.90")));
    }

    @Test
    void naoAceitaPrecoZero() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 1, BigDecimal.ZERO));
    }

    @Test
    void naoAceitaPrecoNegativo() {
        assertThrows(ItemInvalidoException.class,
                () -> new ItemPedido("CAFE-500", 1, new BigDecimal("-1.00")));
    }
}
