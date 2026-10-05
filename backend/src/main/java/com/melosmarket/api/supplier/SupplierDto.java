package com.melosmarket.api.supplier;

import java.time.OffsetDateTime;
import java.util.List;

public record SupplierDto(
        Long id,
        String businessName,
        String email,
        String phone,
        String website,
        String profileImageUrl,
        String coverImageUrl,
        String description,
        String address,
        String city,
        String county,
        String mapLocation,
        String openingHours,
        String productCategories,
        boolean deliveryAvailable,
        Integer maxDeliveryDistanceKm,
        String deliveryArea,
        String deliveryInfo,
        String additionalServices,
        List<SupplierPromotionDto> promotions,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
