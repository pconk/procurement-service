package com.pconk.procurement.api.dto;

import com.pconk.procurement.domain.entity.OrderStatus;
import java.math.BigDecimal;
import java.util.List;

public class PurchaseOrderResponseDTO {
    public Long id;
    public String poNumber;
    public Long supplierId;
    public OrderStatus status;
    public BigDecimal totalAmount;
    public List<PurchaseOrderItemResponseDTO> items;
}