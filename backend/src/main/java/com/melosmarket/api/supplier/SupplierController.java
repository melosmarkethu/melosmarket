package com.melosmarket.api.supplier;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierDto>> searchSuppliers(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String county,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(supplierService.searchSuppliers(city, county, area, name));
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierDto> getSupplier(@PathVariable long supplierId) {
        return ResponseEntity.ok(supplierService.getSupplier(supplierId));
    }

    @GetMapping("/me")
    public ResponseEntity<SupplierDto> getMySupplierProfile() {
        return ResponseEntity.ok(supplierService.getMySupplierProfile());
    }

    @PutMapping("/me")
    public ResponseEntity<SupplierDto> updateMySupplierProfile(@Valid @RequestBody UpdateSupplierProfileRequest request) {
        return ResponseEntity.ok(supplierService.updateMySupplierProfile(request));
    }

    @PostMapping("/me/images")
    public ResponseEntity<SupplierDto> uploadMySupplierImage(
            @RequestParam(defaultValue = "profile") String imageType,
            @RequestParam MultipartFile image) {
        return ResponseEntity.ok(supplierService.uploadMySupplierImage(imageType, image));
    }

    @PostMapping("/me/promotions")
    public ResponseEntity<SupplierPromotionDto> createMyPromotion(@Valid @RequestBody UpsertSupplierPromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createMyPromotion(request));
    }

    @PutMapping("/me/promotions/{promotionId}")
    public ResponseEntity<SupplierPromotionDto> updateMyPromotion(
            @PathVariable long promotionId,
            @Valid @RequestBody UpsertSupplierPromotionRequest request) {
        return ResponseEntity.ok(supplierService.updateMyPromotion(promotionId, request));
    }

    @DeleteMapping("/me/promotions/{promotionId}")
    public ResponseEntity<Void> deleteMyPromotion(@PathVariable long promotionId) {
        supplierService.deleteMyPromotion(promotionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/promotions/{promotionId}/image")
    public ResponseEntity<SupplierPromotionDto> uploadMyPromotionImage(
            @PathVariable long promotionId,
            @RequestParam MultipartFile image) {
        return ResponseEntity.ok(supplierService.uploadMyPromotionImage(promotionId, image));
    }

    @PostMapping("/{supplierId}/quote-requests")
    public ResponseEntity<SupplierQuoteRequestDto> createQuoteRequest(
            @PathVariable long supplierId,
            @Valid @RequestBody CreateSupplierQuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createQuoteRequest(supplierId, request));
    }
}
