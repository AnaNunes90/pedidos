package br.com.pedidos.api.application;

import br.com.pedidos.api.application.port.out.Pedidos;
import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;
import br.com.pedidos.api.domain.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CriarPedidoServiceTest {

    private static final ItemPedido CAFE_500 = new ItemPedido("CAFE-500", 2, new BigDecimal("18.90"));

    private static class PedidosEmMemoria implements Pedidos {

        private final Map<UUID, Pedido> armazenados = new HashMap<>();

        @Override
        public Pedido salvar(Pedido pedido) {
            armazenados.put(pedido.id(), pedido);
            return pedido;
        }
    }

    @Test
    void recusaListaDeItensVazia() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        assertThrows(PedidoSemItensException.class, () -> service.criar("c-1", List.of()));
        assertTrue(pedidos.armazenados.isEmpty());
    }

    @Test
    void recusaListaDeItensNula() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        assertThrows(PedidoSemItensException.class, () -> service.criar("c-1", null));
        assertTrue(pedidos.armazenados.isEmpty());
    }

    @Test
    void criaPedidoComClienteEItem() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        Pedido pedido = service.criar("c-1", List.of(CAFE_500));

        assertEquals("c-1", pedido.clienteId());
        assertEquals(StatusPedido.ABERTO, pedido.status());
        assertEquals(1, pedido.itens().size());
        assertEquals(0, new BigDecimal("37.80").compareTo(pedido.total()));
    }

    @Test
    void criaPedidoComVariosItens() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);
        ItemPedido outro = new ItemPedido("PAO-100", 3, new BigDecimal("5.00"));

        Pedido pedido = service.criar("c-1", List.of(CAFE_500, outro));

        assertEquals(List.of(CAFE_500, outro), pedido.itens());
    }

    @Test
    void devolveOMesmoPedidoQueOPortDeSaidaGuardou() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        Pedido devolvido = service.criar("c-1", List.of(CAFE_500));

        assertSame(pedidos.armazenados.get(devolvido.id()), devolvido);
    }

    @Test
    void naoAceitaClienteIdNulo() {
        PedidosEmMemoria pedidos = new PedidosEmMemoria();
        CriarPedidoService service = new CriarPedidoService(pedidos);

        assertThrows(NullPointerException.class, () -> service.criar(null, List.of(CAFE_500)));
    }
}
