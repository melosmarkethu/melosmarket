package com.melosmarket.api.supplier.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierPromotionRepository extends JpaRepository<SupplierPromotionEntity, Long> {
    List<SupplierPromotionEntity> findBySupplierIdOrderByCreatedAtDesc(Long supplierId);
}
