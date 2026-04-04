package com.pconk.procurement.infrastructure.repository;

import com.pconk.procurement.domain.entity.PurchaseOrder;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PurchaseOrderRepository implements PanacheRepository<PurchaseOrder> {}