package com.pconk.procurement;

import com.pconk.procurement.api.dto.SupplierDTO;
import com.pconk.procurement.api.mapper.SupplierMapper;
import com.pconk.procurement.domain.entity.Supplier;
import com.pconk.procurement.domain.service.SupplierService;
import com.pconk.procurement.infrastructure.repository.SupplierRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

@QuarkusTest
public class SupplierServiceTest {

    @Inject
    SupplierService supplierService;

    @InjectMock
    SupplierRepository supplierRepository;

    @InjectMock
    SupplierMapper supplierMapper;

    @Test
    void testCreateSupplier_Success() {
        SupplierDTO dto = new SupplierDTO();
        dto.code = "SUP-001";
        dto.name = "Test Supplier";

        Supplier entity = new Supplier();
        entity.code = "SUP-001";

        Mockito.when(supplierMapper.toEntity(dto)).thenReturn(entity);
        Mockito.when(supplierRepository.findByCode("SUP-001")).thenReturn(null);
        Mockito.when(supplierMapper.toDTO(entity)).thenReturn(dto);

        SupplierDTO result = supplierService.createSupplier(dto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("SUP-001", result.code);
        Mockito.verify(supplierRepository, Mockito.times(1)).persist(entity);
    }

    @Test
    void testCreateSupplier_AlreadyExists() {
        SupplierDTO dto = new SupplierDTO();
        dto.code = "SUP-001";

        Supplier existing = new Supplier();
        existing.code = "SUP-001";

        Mockito.when(supplierMapper.toEntity(dto)).thenReturn(existing);
        Mockito.when(supplierRepository.findByCode("SUP-001")).thenReturn(existing);

        Assertions.assertThrows(RuntimeException.class, () -> {
            supplierService.createSupplier(dto);
        });
    }

    @Test
    void testGetSupplierById_NotFound() {
        Mockito.when(supplierRepository.findActiveById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            supplierService.getSupplierById(1L);
        });
    }

    @Test
    void testDeleteSupplier_SoftDelete() {
        Supplier entity = new Supplier();
        entity.id = 1L;
        entity.deletedAt = null;

        Mockito.when(supplierRepository.findActiveById(1L)).thenReturn(Optional.of(entity));

        boolean result = supplierService.deleteSupplier(1L);

        Assertions.assertTrue(result);
        Assertions.assertNotNull(entity.deletedAt);
    }

    @Test
    void testDeleteSupplier_NotFound() {
        Mockito.when(supplierRepository.findActiveById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> {
            supplierService.deleteSupplier(1L);
        });
    }
}