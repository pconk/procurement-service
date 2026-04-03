package com.pconk.procurement.domain.entity;

import jakarta.persistence.*;
// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    // @NotBlank(message = "Kode supplier wajib diisi")
    @Column(unique = true, nullable = false)
    public String code;

    // @NotBlank(message = "Nama supplier wajib diisi")
    @Column(nullable = false)
    public String name;
    
    @Email(message="Format email salah")
    public String email;

    public String address;
}
