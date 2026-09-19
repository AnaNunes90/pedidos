package br.com.pedidos.api.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
public class ItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sku;

    private int quantidade;

    @Column(precision = 19, scale = 2)
    private BigDecimal precoUnitario;

    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private PedidoJpaEntity pedido;

    protected ItemJpaEntity() {
    }

    public ItemJpaEntity(String sku, int quantidade, BigDecimal precoUnitario) {
        this.sku = sku;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public PedidoJpaEntity getPedido() {
        return pedido;
    }

    void setPedido(PedidoJpaEntity pedido) {
        this.pedido = pedido;
    }
}
