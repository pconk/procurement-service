package com.pconk.procurement.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import com.pconk.procurement.domain.entity.Supplier;
import io.quarkus.hibernate.orm.panache.PanacheRepository; // <-- Tambahkan ini
import java.util.Optional;

@ApplicationScoped
public class SupplierRepository implements PanacheRepository<Supplier> {
    // Di sini tempat kamu buat custom query
    public Supplier findByCode(String code) {
        return find("code", code).firstResult();
    }

    public Optional<Supplier> findActiveById(Long id) {
        return find("id = ?1 and deletedAt is null", id).singleResultOptional();
    }
}
