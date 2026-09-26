package br.com.pedidos.api.domain;

public class ItemInvalidoException extends RuntimeException {

    public ItemInvalidoException(String message) {
        super(message);
    }
}
