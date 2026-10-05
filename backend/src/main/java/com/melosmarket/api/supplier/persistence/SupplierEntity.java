package com.melosmarket.api.supplier.persistence;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.melosmarket.api.auth.persistence.UserEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "suppliers")
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "business_name", nullable = false, length = 180)
    private String businessName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(length = 500)
    private String website;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "profile_image_storage_path", length = 500)
    private String profileImageStoragePath;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "cover_image_storage_path", length = 500)
    private String coverImageStoragePath;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 255)
    private String address;

    @Column(length = 120)
    private String city;

    @Column(length = 80)
    private String county;

    @Column(name = "map_location", length = 500)
    private String mapLocation;

    @Column(name = "opening_hours", columnDefinition = "text")
    private String openingHours;

    @Column(name = "product_categories", columnDefinition = "text")
    private String productCategories;

    @Column(name = "delivery_available", nullable = false)
    private boolean deliveryAvailable;

    @Column(name = "max_delivery_distance_km")
    private Integer maxDeliveryDistanceKm;

    @Column(name = "delivery_area", columnDefinition = "text")
    private String deliveryArea;

    @Column(name = "delivery_info", columnDefinition = "text")
    private String deliveryInfo;

    @Column(name = "additional_services", columnDefinition = "text")
    private String additionalServices;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt desc")
    private List<SupplierPromotionEntity> promotions = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getProfileImageStoragePath() { return profileImageStoragePath; }
    public void setProfileImageStoragePath(String profileImageStoragePath) { this.profileImageStoragePath = profileImageStoragePath; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getCoverImageStoragePath() { return coverImageStoragePath; }
    public void setCoverImageStoragePath(String coverImageStoragePath) { this.coverImageStoragePath = coverImageStoragePath; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCounty() { return county; }
    public void setCounty(String county) { this.county = county; }
    public String getMapLocation() { return mapLocation; }
    public void setMapLocation(String mapLocation) { this.mapLocation = mapLocation; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public String getProductCategories() { return productCategories; }
    public void setProductCategories(String productCategories) { this.productCategories = productCategories; }
    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(boolean deliveryAvailable) { this.deliveryAvailable = deliveryAvailable; }
    public Integer getMaxDeliveryDistanceKm() { return maxDeliveryDistanceKm; }
    public void setMaxDeliveryDistanceKm(Integer maxDeliveryDistanceKm) { this.maxDeliveryDistanceKm = maxDeliveryDistanceKm; }
    public String getDeliveryArea() { return deliveryArea; }
    public void setDeliveryArea(String deliveryArea) { this.deliveryArea = deliveryArea; }
    public String getDeliveryInfo() { return deliveryInfo; }
    public void setDeliveryInfo(String deliveryInfo) { this.deliveryInfo = deliveryInfo; }
    public String getAdditionalServices() { return additionalServices; }
    public void setAdditionalServices(String additionalServices) { this.additionalServices = additionalServices; }
    public List<SupplierPromotionEntity> getPromotions() { return promotions; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
