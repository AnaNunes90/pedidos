package br.com.pedidos.api.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemRequest(

        @NotBlank
        String codigoProduto,

        @NotNull
        Integer quantidade,

        @NotNull
        BigDecimal precoUnitario
) {
}
