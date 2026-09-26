package br.com.pedidos.api.config;

import br.com.pedidos.api.application.CriarPedidoService;
import br.com.pedidos.api.application.port.in.CriarPedido;
import br.com.pedidos.api.application.port.out.Pedidos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public CriarPedido criarPedido(Pedidos pedidos) {
        return new CriarPedidoService(pedidos);
    }
}
