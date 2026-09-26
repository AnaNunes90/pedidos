package br.com.pedidos.api.adapter.in.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(

        @NotBlank
        String clienteId,

        @NotNull
        List<@Valid ItemRequest> itens
) {
}
