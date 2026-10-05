package com.melosmarket.api.supplier;

import java.time.OffsetDateTime;

public record SupplierQuoteRequestDto(
        Long id,
        Long supplierId,
        String status,
        OffsetDateTime createdAt) {
}
