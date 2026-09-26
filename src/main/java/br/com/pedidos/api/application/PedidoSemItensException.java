package br.com.pedidos.api.application;

public class PedidoSemItensException extends RuntimeException {

    public PedidoSemItensException(String message) {
        super(message);
    }
}
