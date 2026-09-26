package br.com.pedidos.api.adapter.out.persistence;

import br.com.pedidos.api.application.port.out.Pedidos;
import br.com.pedidos.api.domain.Pedido;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedidosJpaAdapter implements Pedidos {

    private final PedidoJpaRepository repository;

    public PedidosJpaAdapter(PedidoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoJpaEntity entidade = PedidoMapper.paraEntidade(pedido);
        PedidoJpaEntity salva = repository.save(entidade);
        return PedidoMapper.paraDominio(salva);
    }

    @Override
    @Transactional
    public Optional<Pedido> buscarPorId(UUID id) {
        return repository.findById(id).map(PedidoMapper::paraDominio);
    }
}
