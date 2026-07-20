package com.melosmarket.api.worker.persistence;

import java.util.ArrayList;
import java.util.List;
import java.time.OffsetDateTime;

import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.worker.domain.WorkerAvailabilityState;
import com.melosmarket.api.worker.domain.WorkerSubscriptionStatus;
import com.melosmarket.api.worker.domain.WorkerTradeType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "workers")
public class WorkerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 180)
    private String businessName;

    @Column(name = "contact_name", nullable = false, length = 160)
    private String contactName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 50)
    private String phone;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "profile_image_storage_path", length = 500)
    private String profileImageStoragePath;

    @Column(nullable = false)
    private boolean verified;

    @Column(name = "top_worker", nullable = false)
    private boolean topWorker;

    @Column(name = "many_references", nullable = false)
    private boolean manyReferences;

    @Column(name = "hundred_jobs", nullable = false)
    private boolean hundredJobs;

    @Column(name = "fast_responder", nullable = false)
    private boolean fastResponder;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 40)
    private WorkerAvailabilityState availabilityStatus = WorkerAvailabilityState.AVAILABLE;

    @Column(name = "urgent_work", nullable = false)
    private boolean urgentWork;

    @Column(name = "trial_started_at", nullable = false)
    private OffsetDateTime trialStartedAt;

    @Column(name = "trial_ends_at", nullable = false)
    private OffsetDateTime trialEndsAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 40)
    private WorkerSubscriptionStatus subscriptionStatus = WorkerSubscriptionStatus.TRIALING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 80)
    private WorkerTradeType trade;

    @Column(name = "service_area", length = 160)
    private String serviceArea;

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "worker", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt desc")
    private List<WorkerReferenceImageEntity> referenceImages = new ArrayList<>();

    @OneToMany(mappedBy = "target", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("updatedAt desc")
    private List<WorkerReviewEntity> reviews = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
        if (trialStartedAt == null) {
            trialStartedAt = createdAt;
        }
        if (trialEndsAt == null) {
            trialEndsAt = trialStartedAt.plusDays(30);
        }
    }

    public Long getId() {
        return id;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getProfileImageStoragePath() {
        return profileImageStoragePath;
    }

    public void setProfileImageStoragePath(String profileImageStoragePath) {
        this.profileImageStoragePath = profileImageStoragePath;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isTopWorker() {
        return topWorker;
    }

    public void setTopWorker(boolean topWorker) {
        this.topWorker = topWorker;
    }

    public boolean isManyReferences() {
        return manyReferences;
    }

    public void setManyReferences(boolean manyReferences) {
        this.manyReferences = manyReferences;
    }

    public boolean isHundredJobs() {
        return hundredJobs;
    }

    public void setHundredJobs(boolean hundredJobs) {
        this.hundredJobs = hundredJobs;
    }

    public boolean isFastResponder() {
        return fastResponder;
    }

    public void setFastResponder(boolean fastResponder) {
        this.fastResponder = fastResponder;
    }

    public WorkerAvailabilityState getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(WorkerAvailabilityState availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public boolean isUrgentWork() {
        return urgentWork;
    }

    public void setUrgentWork(boolean urgentWork) {
        this.urgentWork = urgentWork;
    }

    public OffsetDateTime getTrialStartedAt() {
        return trialStartedAt;
    }

    public void setTrialStartedAt(OffsetDateTime trialStartedAt) {
        this.trialStartedAt = trialStartedAt;
    }

    public OffsetDateTime getTrialEndsAt() {
        return trialEndsAt;
    }

    public void setTrialEndsAt(OffsetDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public WorkerSubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(WorkerSubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public WorkerTradeType getTrade() {
        return trade;
    }

    public void setTrade(WorkerTradeType trade) {
        this.trade = trade;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<WorkerReferenceImageEntity> getReferenceImages() {
        return referenceImages;
    }

    public List<WorkerReviewEntity> getReviews() {
        return reviews;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
