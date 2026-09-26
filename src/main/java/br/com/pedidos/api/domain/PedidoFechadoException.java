package br.com.pedidos.api.domain;

public class PedidoFechadoException extends RuntimeException {

    public PedidoFechadoException(String message) {
        super(message);
    }
}
