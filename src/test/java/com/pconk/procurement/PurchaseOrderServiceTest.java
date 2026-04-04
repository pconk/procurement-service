package com.pconk.procurement;

import com.pconk.procurement.api.dto.*;
import com.pconk.procurement.api.mapper.PurchaseOrderMapper;
import com.pconk.procurement.domain.entity.OrderStatus;
import com.pconk.procurement.domain.entity.PurchaseOrder;
import com.pconk.procurement.domain.entity.PurchaseOrderItem;
import com.pconk.procurement.domain.entity.Supplier;
import com.pconk.procurement.domain.service.PurchaseOrderService;
import com.pconk.procurement.infrastructure.repository.PurchaseOrderRepository;
import com.pconk.procurement.infrastructure.repository.SupplierRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@QuarkusTest
public class PurchaseOrderServiceTest {

    @Inject
    PurchaseOrderService poService;

    @InjectMock
    PurchaseOrderRepository poRepository;

    @InjectMock
    SupplierRepository supplierRepository;

    @InjectMock
    PurchaseOrderMapper poMapper;

    @Test
    void testCreatePO_SuccessAndCalculateTotal() {
        PurchaseOrderRequestDTO dto = new PurchaseOrderRequestDTO();
        dto.supplierId = 1L;

        PurchaseOrder po = new PurchaseOrder();
        po.items = new ArrayList<>();
        
        PurchaseOrderItem item1 = new PurchaseOrderItem();
        item1.price = new BigDecimal("1000");
        item1.quantity = 2;
        po.addItem(item1); // Total 2000

        PurchaseOrderItem item2 = new PurchaseOrderItem();
        item2.price = new BigDecimal("5000");
        item2.quantity = 1;
        po.addItem(item2); // Total 5000

        Mockito.when(supplierRepository.findActiveById(1L)).thenReturn(Optional.of(new Supplier()));
        Mockito.when(poMapper.toEntity(dto)).thenReturn(po);
        Mockito.when(poMapper.toResponse(po)).thenReturn(new PurchaseOrderResponseDTO());

        poService.createPO(dto);

        Assertions.assertEquals(new BigDecimal("7000"), po.totalAmount);
        Mockito.verify(poRepository, Mockito.times(1)).persist(po);
    }

    @Test
    void testCreatePO_SupplierNotFound() {
        PurchaseOrderRequestDTO dto = new PurchaseOrderRequestDTO();
        dto.supplierId = 99L;

        Mockito.when(supplierRepository.findActiveById(99L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            poService.createPO(dto);
        });
    }

    @Test
    void testUpdateStatus_LockApprovedPO() {
        PurchaseOrder po = new PurchaseOrder();
        po.status = OrderStatus.APPROVED;

        PurchaseOrderStatusDTO statusDto = new PurchaseOrderStatusDTO();
        statusDto.status = OrderStatus.DRAFT;

        Mockito.when(poRepository.findByIdOptional(1L)).thenReturn(Optional.of(po));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            poService.updateStatus(1L, statusDto);
        });

        Assertions.assertTrue(exception.getMessage().contains("Cannot update status"));
    }

    @Test
    void testUpdateStatus_InvalidDraftTransition() {
        PurchaseOrder po = new PurchaseOrder();
        po.status = OrderStatus.PENDING;

        // Mencoba menurunkan status kembali ke DRAFT dari PENDING
        PurchaseOrderStatusDTO statusDto = new PurchaseOrderStatusDTO();
        statusDto.status = OrderStatus.DRAFT;

        Mockito.when(poRepository.findByIdOptional(1L)).thenReturn(Optional.of(po));

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            poService.updateStatus(1L, statusDto);
        });

        Assertions.assertTrue(exception.getMessage().contains("Cannot update status"));
    }
}
