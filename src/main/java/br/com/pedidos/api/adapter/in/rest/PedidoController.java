package br.com.pedidos.api.adapter.in.rest;

import br.com.pedidos.api.application.port.in.CriarPedido;
import br.com.pedidos.api.domain.ItemPedido;
import br.com.pedidos.api.domain.Pedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final CriarPedido criarPedido;

    public PedidoController(CriarPedido criarPedido) {
        this.criarPedido = criarPedido;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        List<ItemPedido> itens = request.itens().stream()
                .map(item -> new ItemPedido(item.codigoProduto(), item.quantidade(), item.precoUnitario()))
                .toList();

        Pedido pedido = criarPedido.criar(request.clienteId(), itens);

        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.de(pedido));
    }
}
