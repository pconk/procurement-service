package com.pconk.procurement.domain.service;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import com.pconk.procurement.api.dto.SupplierDTO;
import com.pconk.procurement.api.dto.PagedResponse;
import com.pconk.procurement.api.mapper.SupplierMapper;
import com.pconk.procurement.domain.entity.Supplier;
import com.pconk.procurement.infrastructure.repository.SupplierRepository;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Page;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SupplierService {    
    private static final Logger LOG = Logger.getLogger(SupplierService.class);

    @Inject
    SupplierMapper supplierMapper;

    @Inject
    SupplierRepository supplierRepository;

    public PagedResponse<SupplierDTO> getAllSuppliers(int page, int size) {
        List<SupplierDTO> dtos = supplierRepository.findAll().page(Page.of(page, size))
                .list().stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
        
        long total = supplierRepository.count();
        return new PagedResponse<>(dtos, total, page, size);
    }

    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id);
        if (supplier == null) {
            LOG.warnf("Supplier lookup failed. ID %d not found", id);
            throw new NotFoundException("Supplier with ID " + id + " not found");
        }
        return supplierMapper.toDTO(supplier);
    }

    @Transactional
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        Supplier supplier = supplierMapper.toEntity(supplierDTO);
        if (supplierRepository.findByCode(supplier.code) != null) {
            LOG.errorf("Failed to create supplier. Code %s already exists", supplier.code);
            throw new RuntimeException("Supplier code already exists!");
        }
        supplierRepository.persist(supplier);
        LOG.infof("Supplier created successfully with ID: %d", supplier.id);
        return supplierMapper.toDTO(supplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier entity = supplierRepository.findById(id);
        if (entity == null) {
            LOG.warnf("Update failed. Supplier ID %d not found", id);
            throw new NotFoundException("Supplier not found");
        }
        
        // Menggunakan MapStruct untuk update entity dari DTO. 
        // ID diabaikan di Mapper agar tetap menggunakan ID asli dari database.
        supplierMapper.updateEntityFromDTO(supplierDTO, entity);
        
        LOG.infof("Supplier ID %d updated successfully", id);
        return supplierMapper.toDTO(entity);
    }

    @Transactional
    public boolean deleteSupplier(Long id) {
        boolean deleted = supplierRepository.deleteById(id);
        if (!deleted) {
            LOG.warnf("Delete failed. Supplier ID %d not found", id);
            throw new NotFoundException("Supplier not found");
        }
        LOG.infof("Supplier ID %d deleted successfully", id);
        return true;
    }
}
