package com.pconk.procurement.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    public PurchaseOrder purchaseOrder;

    @Column(name = "item_id", nullable = false)
    public Long itemId; // ID dari warehouse-api (Go)

    @Column(nullable = false)
    public Integer quantity;

    @Column(nullable = false)
    public BigDecimal price;

    public BigDecimal getSubTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}