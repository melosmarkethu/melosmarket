package com.melosmarket.api.supplier;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record SupplierPromotionDto(
        Long id,
        String title,
        String description,
        String currentPrice,
        String originalPrice,
        String imageUrl,
        LocalDate startDate,
        LocalDate expirationDate,
        boolean active,
        boolean current,
        OffsetDateTime createdAt) {
}
