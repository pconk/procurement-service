package com.pconk.procurement.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

public class PurchaseOrderRequestDTO {
    @NotNull(message = "Supplier ID wajib diisi")
    @Min(value = 1, message = "Supplier ID harus berupa angka positif")
    public Long supplierId;

    @NotEmpty(message = "Item PO tidak boleh kosong")
    public List<PurchaseOrderItemRequestDTO> items;
}
