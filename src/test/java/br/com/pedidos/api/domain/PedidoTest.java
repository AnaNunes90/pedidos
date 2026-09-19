package br.com.pedidos.api.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PedidoTest {

    private static final ItemPedido CAFE_500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

    @Test
    void totalDeUmItem() {
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500);
        assertEquals(0, new BigDecimal("37.80").compareTo(pedido.total()));
    }

    @Test
    void totalDeDoisItens() {
        ItemPedido outro = new ItemPedido("PAO-100", 3, new BigDecimal("5.00"));
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500).adicionarItem(outro);
        assertEquals(0, new BigDecimal("52.80").compareTo(pedido.total()));
    }

    @Test
    void novoPedidoComecaAbertoVazio() {
        Pedido pedido = Pedido.novo();
        assertEquals(StatusPedido.ABERTO, pedido.status());
        assertTrue(pedido.itens().isEmpty());
        assertNotNull(pedido.id());
    }

    @Test
    void doisPedidosNovosTemIdsDiferentes() {
        assertNotEquals(Pedido.novo().id(), Pedido.novo().id());
    }

    @Test
    void adicionarItemDevolveNovaInstanciaSemAlterarOriginal() {
        Pedido original = Pedido.novo();
        Pedido comItem = original.adicionarItem(CAFE_500);

        assertNotSame(original, comItem);
        assertTrue(original.itens().isEmpty());
        assertEquals(1, comItem.itens().size());
    }

    @Test
    void naoAceitaItemEmPedidoPago() {
        Pedido pago = Pedido.novo().adicionarItem(CAFE_500).pagar();
        assertThrows(IllegalStateException.class, () -> pago.adicionarItem(CAFE_500));
        assertEquals(1, pago.itens().size());
    }

    @Test
    void naoAceitaItemEmPedidoCancelado() {
        Pedido cancelado = Pedido.novo().adicionarItem(CAFE_500).cancelar();
        assertThrows(IllegalStateException.class, () -> cancelado.adicionarItem(CAFE_500));
        assertEquals(1, cancelado.itens().size());
    }

    @Test
    void listaDeItensNaoPodeSerModificadaPorFora() {
        Pedido pedido = Pedido.novo().adicionarItem(CAFE_500);
        List<ItemPedido> itens = pedido.itens();
        assertThrows(UnsupportedOperationException.class, () -> itens.add(CAFE_500));
        assertEquals(1, pedido.itens().size());
    }

    @Test
    void pedidoAbertoPodeSerPago() {
        Pedido pago = Pedido.novo().pagar();
        assertEquals(StatusPedido.PAGO, pago.status());
    }

    @Test
    void pedidoAbertoPodeSerCancelado() {
        Pedido cancelado = Pedido.novo().cancelar();
        assertEquals(StatusPedido.CANCELADO, cancelado.status());
    }

    @Test
    void naoPodePagarPedidoJaPago() {
        Pedido pago = Pedido.novo().pagar();
        assertThrows(IllegalStateException.class, pago::pagar);
    }

    @Test
    void naoPodePagarPedidoCancelado() {
        Pedido cancelado = Pedido.novo().cancelar();
        assertThrows(IllegalStateException.class, cancelado::pagar);
    }

    @Test
    void naoPodeCancelarPedidoJaCancelado() {
        Pedido cancelado = Pedido.novo().cancelar();
        assertThrows(IllegalStateException.class, cancelado::cancelar);
    }

    @Test
    void naoPodeCancelarPedidoPago() {
        Pedido pago = Pedido.novo().pagar();
        assertThrows(IllegalStateException.class, pago::cancelar);
    }

    @Test
    void exemploDaAula() {
        Pedido pedido = Pedido.novo().adicionarItem(new ItemPedido("CAFE-500", 2, new BigDecimal("18.90")));
        assertEquals(0, new BigDecimal("37.80").compareTo(pedido.total()));
    }
}
