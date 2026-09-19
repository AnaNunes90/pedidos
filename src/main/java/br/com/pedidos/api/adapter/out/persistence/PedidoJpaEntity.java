package br.com.pedidos.api.adapter.out.persistence;

import br.com.pedidos.api.domain.StatusPedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedido")
public class PedidoJpaEntity {

    @Id
    private UUID id;

    private String clienteId;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemJpaEntity> itens = new ArrayList<>();

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(UUID id, String clienteId, StatusPedido status) {
        this.id = id;
        this.clienteId = clienteId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public List<ItemJpaEntity> getItens() {
        return itens;
    }

    public void adicionarItem(ItemJpaEntity item) {
        item.setPedido(this);
        itens.add(item);
    }
}
