package com.pconk.procurement.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "po_number", unique = true, nullable = false)
    public String poNumber;

    @Column(name = "supplier_id", nullable = false)
    public Long supplierId;

    @Enumerated(EnumType.STRING)
    public OrderStatus status;

    @Column(name = "total_amount")
    public BigDecimal totalAmount;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<PurchaseOrderItem> items = new ArrayList<>();

    // Helper method untuk menjaga sinkronisasi bi-directional
    public void addItem(PurchaseOrderItem item) {
        items.add(item);
        item.purchaseOrder = this;
    }
}