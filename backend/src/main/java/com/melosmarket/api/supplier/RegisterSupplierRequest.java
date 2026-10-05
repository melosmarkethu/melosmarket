package com.melosmarket.api.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterSupplierRequest(
        @NotBlank @Size(max = 180) String businessName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 120) String password,
        @Size(max = 50) String phone,
        @Size(max = 120) String city,
        @Size(max = 80) String county,
        @Size(max = 255) String address,
        @Size(max = 2000) String description) {
}
