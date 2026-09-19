package br.com.pedidos.api.adapter.out.persistence;

import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;
import br.com.pedidos.api.domain.StatusPedido;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PedidosJpaAdapterIT {

    @Autowired
    private PedidosJpaAdapter adapter;

    @Test
    void salvaERecuperaPedidoEmUmaNovaTransacao() {
        Pedido pedido = Pedido.novo("c-1")
                .adicionarItem(new ItemPedido("CAFE-500", 2, new BigDecimal("18.90")));

        Pedido salvo = adapter.salvar(pedido);

        Optional<Pedido> recuperado = adapter.buscarPorId(salvo.id());

        assertTrue(recuperado.isPresent());
        Pedido pedidoRecuperado = recuperado.get();
        assertEquals("c-1", pedidoRecuperado.clienteId());
        assertEquals(StatusPedido.ABERTO, pedidoRecuperado.status());
        assertEquals(1, pedidoRecuperado.itens().size());
        assertEquals("CAFE-500", pedidoRecuperado.itens().get(0).codigoProduto());
        assertEquals(0, new BigDecimal("37.80").compareTo(pedidoRecuperado.total()));
    }
}
