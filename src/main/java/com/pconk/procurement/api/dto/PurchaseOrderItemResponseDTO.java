package com.pconk.procurement.api.dto;

import java.math.BigDecimal;

public class PurchaseOrderItemResponseDTO {
    public Long id;
    public Long itemId;
    public Integer quantity;
    public BigDecimal price;
    public BigDecimal subTotal;
}