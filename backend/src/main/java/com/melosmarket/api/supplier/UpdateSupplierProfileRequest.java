package com.melosmarket.api.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSupplierProfileRequest(
        @NotBlank @Size(max = 180) String businessName,
        @Email @Size(max = 255) String email,
        @Size(max = 50) String phone,
        @Size(max = 500) String website,
        @Size(max = 2000) String description,
        @Size(max = 255) String address,
        @Size(max = 120) String city,
        @Size(max = 80) String county,
        @Size(max = 500) String mapLocation,
        @Size(max = 2000) String openingHours,
        @Size(max = 2000) String productCategories,
        Boolean deliveryAvailable,
        Integer maxDeliveryDistanceKm,
        @Size(max = 2000) String deliveryArea,
        @Size(max = 2000) String deliveryInfo,
        @Size(max = 2000) String additionalServices) {
}
