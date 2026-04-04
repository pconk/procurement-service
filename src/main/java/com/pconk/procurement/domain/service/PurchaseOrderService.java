package com.pconk.procurement.domain.service;

import com.pconk.procurement.api.dto.PurchaseOrderRequestDTO;
import com.pconk.procurement.api.dto.PurchaseOrderStatusDTO;
import com.pconk.procurement.api.dto.PurchaseOrderResponseDTO;
import com.pconk.procurement.api.mapper.PurchaseOrderMapper;
import com.pconk.procurement.domain.entity.OrderStatus;
import com.pconk.procurement.domain.entity.PurchaseOrder;
import com.pconk.procurement.infrastructure.repository.PurchaseOrderRepository;
import com.pconk.procurement.infrastructure.repository.SupplierRepository;
import com.pconk.procurement.infrastructure.config.Logged;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PurchaseOrderService {

    @Inject
    PurchaseOrderRepository poRepository;

    @Inject
    SupplierRepository supplierRepository;

    @Inject
    PurchaseOrderMapper poMapper;

    @Transactional
    @Logged
    public PurchaseOrderResponseDTO createPO(PurchaseOrderRequestDTO dto) {
        // Pengecekan supplier
        supplierRepository.findActiveById(dto.supplierId)
                .orElseThrow(() -> new NotFoundException("Supplier ID " + dto.supplierId + " not found"));

        // Menggunakan Mapper (Menggantikan loop manual)
        PurchaseOrder po = poMapper.toEntity(dto);

        po.poNumber = generatePONumber();
        po.status = OrderStatus.DRAFT;

        // Hitung total_amount
        BigDecimal total = po.items.stream()
                .map(item -> item.price.multiply(BigDecimal.valueOf(item.quantity)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        po.totalAmount = total;
        poRepository.persist(po);

        return poMapper.toResponse(po);
    }

    @Transactional
    public PurchaseOrderResponseDTO updateStatus(Long id, PurchaseOrderStatusDTO dto) {
        PurchaseOrder po = poRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order ID " + id + " not found"));

        // Validasi sesuai Phase 2 Roadmap: PO tidak bisa diedit jika sudah
        // APPROVED/RECEIVED
        if (po.status == OrderStatus.APPROVED || po.status == OrderStatus.RECEIVED
                || (po.status == OrderStatus.PENDING && dto.status == OrderStatus.DRAFT)) {
            throw new RuntimeException("Cannot update status. Purchase Order is already " + po.status);
        }

        po.status = dto.status;
        poRepository.persist(po);

        return poMapper.toResponse(po);
    }

    private String generatePONumber() {
        // Format sederhana: PO-YYYYMMDD-nanotime
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PO-" + date + "-" + System.nanoTime() % 10000;
    }

    public PurchaseOrderResponseDTO getById(Long id) {
        PurchaseOrder po = poRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order not found"));
        return poMapper.toResponse(po);
    }
}