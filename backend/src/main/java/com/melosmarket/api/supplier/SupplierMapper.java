package com.melosmarket.api.supplier;

import java.time.LocalDate;

import com.melosmarket.api.supplier.persistence.SupplierEntity;
import com.melosmarket.api.supplier.persistence.SupplierPromotionEntity;

import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierDto toDto(SupplierEntity entity) {
        return new SupplierDto(
                entity.getId(),
                entity.getBusinessName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getWebsite(),
                entity.getProfileImageUrl(),
                entity.getCoverImageUrl(),
                entity.getDescription(),
                entity.getAddress(),
                entity.getCity(),
                entity.getCounty(),
                entity.getMapLocation(),
                entity.getOpeningHours(),
                entity.getProductCategories(),
                entity.isDeliveryAvailable(),
                entity.getMaxDeliveryDistanceKm(),
                entity.getDeliveryArea(),
                entity.getDeliveryInfo(),
                entity.getAdditionalServices(),
                entity.getPromotions().stream().map(this::toPromotionDto).toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    public SupplierPromotionDto toPromotionDto(SupplierPromotionEntity entity) {
        return new SupplierPromotionDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCurrentPrice(),
                entity.getOriginalPrice(),
                entity.getImageUrl(),
                entity.getStartDate(),
                entity.getExpirationDate(),
                entity.isActive(),
                isCurrent(entity),
                entity.getCreatedAt());
    }

    boolean isCurrent(SupplierPromotionEntity promotion) {
        LocalDate today = LocalDate.now();
        boolean started = promotion.getStartDate() == null || !promotion.getStartDate().isAfter(today);
        boolean notExpired = promotion.getExpirationDate() == null || !promotion.getExpirationDate().isBefore(today);
        return promotion.isActive() && started && notExpired;
    }
}
