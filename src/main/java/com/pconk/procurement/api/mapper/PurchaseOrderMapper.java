package com.pconk.procurement.api.mapper;

import com.pconk.procurement.api.dto.PurchaseOrderRequestDTO;
import com.pconk.procurement.api.dto.PurchaseOrderResponseDTO;
import com.pconk.procurement.api.dto.PurchaseOrderItemResponseDTO;
import com.pconk.procurement.domain.entity.PurchaseOrder;
import com.pconk.procurement.domain.entity.PurchaseOrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface PurchaseOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "poNumber", ignore = true)
    @Mapping(target = "status", ignore = true)
    PurchaseOrder toEntity(PurchaseOrderRequestDTO dto);

    PurchaseOrderResponseDTO toResponse(PurchaseOrder po);

    @Mapping(target = "subTotal", expression = "java(item.getSubTotal())")
    PurchaseOrderItemResponseDTO toItemResponse(PurchaseOrderItem item);

    @AfterMapping
    default void linkItems(@MappingTarget PurchaseOrder po) {
        po.items.forEach(item -> item.purchaseOrder = po);
    }
}