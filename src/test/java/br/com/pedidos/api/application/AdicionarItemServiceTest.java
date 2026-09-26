package br.com.pedidos.api.application;

import br.com.pedidos.api.application.port.out.Pedidos;
import br.com.pedidos.api.domain.ItemInvalidoException;
import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;
import br.com.pedidos.api.domain.PedidoFechadoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdicionarItemServiceTest {

    private static final ItemPedido CAFE_500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));
    private static final ItemPedido MAIS_UM_CAFE_500 = new ItemPedido("CAFE-500", 1, new BigDecimal("18.90"));

    private static class PedidosEmMemoria implements Pedidos {

        private final Map<UUID, Pedido> armazenados = new HashMap<>();
        private int chamadasSalvar = 0;

        @Override
        public Pedido salvar(Pedido pedido) {
            chamadasSalvar++;
            armazenados.put(pedido.id(), pedido);
            return pedido;
        }

        @Override
        public Optional<Pedido> buscarPorId(UUID id) {
            return Optional.ofNullable(armazenados.get(id));
        }
    }

    @Test
    void adicionaItemEAtualizaTotal() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        Pedido existente = pedidos.salvar(Pedido.novo("c-1").adicionarItem(CAFE_500));
        AdicionarItemService service = new AdicionarItemService(pedidos);

        Pedido atualizado = service.adicionarItem(existente.id(), MAIS_UM_CAFE_500);

        assertEquals(existente.id(), atualizado.id());
        assertEquals(2, atualizado.itens().size());
        assertEquals(0, new BigDecimal("56.70").compareTo(atualizado.total()));
        assertEquals(atualizado, pedidos.armazenados.get(existente.id()));
    }

    @Test
    void pedidoInexistenteLancaExcecaoENaoSalva() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        AdicionarItemService service = new AdicionarItemService(pedidos);

        assertThrows(PedidoNaoEncontradoException.class,
                () -> service.adicionarItem(UUID.randomUUID(), CAFE_500));
        assertEquals(0, pedidos.chamadasSalvar);
    }

    @Test
    void pedidoPagoLancaExcecaoENaoSalvaNovamente() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        Pedido pago = pedidos.salvar(Pedido.novo("c-1").adicionarItem(CAFE_500).pagar());
        AdicionarItemService service = new AdicionarItemService(pedidos);

        assertThrows(PedidoFechadoException.class, () -> service.adicionarItem(pago.id(), CAFE_500));
        assertEquals(1, pedidos.chamadasSalvar);
        assertEquals(1, pedidos.armazenados.get(pago.id()).itens().size());
    }

    @Test
    void pedidoCanceladoLancaExcecaoENaoSalvaNovamente() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        Pedido cancelado = pedidos.salvar(Pedido.novo("c-1").adicionarItem(CAFE_500).cancelar());
        AdicionarItemService service = new AdicionarItemService(pedidos);

        assertThrows(PedidoFechadoException.class, () -> service.adicionarItem(cancelado.id(), CAFE_500));
        assertEquals(1, pedidos.chamadasSalvar);
        assertEquals(1, pedidos.armazenados.get(cancelado.id()).itens().size());
    }

    @Test
    void quantidadeInvalidaLancaExcecaoAntesDeBuscar() {
        assertThrows(ItemInvalidoException.class, () -> new ItemPedido("CAFE-500", 0, new BigDecimal("18.90")));
    }

    @Test
    void precoInvalidoLancaExcecao() {
        assertThrows(ItemInvalidoException.class, () -> new ItemPedido("CAFE-500", 1, BigDecimal.ZERO));
    }
}
