package com.pconk.procurement.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SupplierDTO {
    public Long id;

    @NotBlank(message = "Kode supplier wajib diisi")
    public String code;

    @NotBlank(message = "Nama supplier wajib diisi")
    public String name;
    
    @Email(message="Email harus valid")
    public String email;
    public String address;
}
