package com.melosmarket.api.supplier;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertSupplierPromotionRequest(
        @NotBlank @Size(max = 180) String title,
        @Size(max = 1500) String description,
        @NotBlank @Size(max = 80) String currentPrice,
        @Size(max = 80) String originalPrice,
        LocalDate startDate,
        LocalDate expirationDate,
        Boolean active) {
}
