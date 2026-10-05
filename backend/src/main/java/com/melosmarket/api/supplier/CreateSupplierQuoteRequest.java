package com.melosmarket.api.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupplierQuoteRequest(
        @NotBlank @Email @Size(max = 255) String customerEmail,
        @Size(max = 50) String customerPhone,
        @NotBlank @Size(max = 4000) String materials,
        @Size(max = 4000) String message) {
}
