package com.pconk.procurement.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import com.pconk.procurement.domain.entity.Supplier;
import io.quarkus.hibernate.orm.panache.PanacheRepository; // <-- Tambahkan ini

@ApplicationScoped
public class SupplierRepository implements PanacheRepository<Supplier> {
    // Di sini tempat kamu buat custom query
    public Supplier findByCode(String code) {
        return find("code", code).firstResult();
    }
}
