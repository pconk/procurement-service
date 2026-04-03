package com.pconk.procurement.api.mapper;

import com.pconk.procurement.api.dto.SupplierDTO;
import com.pconk.procurement.domain.entity.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface SupplierMapper {
    SupplierDTO toDTO(Supplier supplier);

    @Mapping(target = "id", ignore = true)
    Supplier toEntity(SupplierDTO supplierDTO);

    // Method untuk mengupdate entity yang sudah ada dari DTO
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(SupplierDTO dto, @MappingTarget Supplier entity);
}
