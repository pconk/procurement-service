package com.pconk.procurement.api.dto;

import jakarta.validation.constraints.NotNull;
import com.pconk.procurement.domain.entity.OrderStatus;

public class PurchaseOrderStatusDTO {
    @NotNull(message = "Status tidak boleh kosong")
    public OrderStatus status;
}
