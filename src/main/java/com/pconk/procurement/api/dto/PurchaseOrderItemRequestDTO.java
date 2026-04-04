package com.pconk.procurement.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PurchaseOrderItemRequestDTO {
    @NotNull(message = "Item ID wajib diisi")
    @Min(value = 1, message = "Item ID harus berupa angka positif")
    public Long itemId;
    @Min(value = 1, message = "Quantity minimal 1")
    public Integer quantity;
    @NotNull(message = "Price wajib diisi")
    public BigDecimal price;
}