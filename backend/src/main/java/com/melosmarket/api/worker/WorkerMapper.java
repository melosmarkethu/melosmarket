package com.melosmarket.api.worker;

import java.time.Duration;
import java.time.OffsetDateTime;

import com.melosmarket.api.generated.model.CreateWorkerRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.RegisterWorkerRequest;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.Worker;
import com.melosmarket.api.generated.model.WorkerAccessStatus;
import com.melosmarket.api.generated.model.WorkerAvailabilityStatus;
import com.melosmarket.api.generated.model.WorkerReferenceImage;
import com.melosmarket.api.generated.model.WorkerReview;
import com.melosmarket.api.worker.domain.WorkerAvailabilityState;
import com.melosmarket.api.worker.domain.WorkerSubscriptionStatus;
import com.melosmarket.api.worker.domain.WorkerTradeType;
import com.melosmarket.api.worker.persistence.WorkerEntity;
import com.melosmarket.api.worker.persistence.WorkerReferenceImageEntity;
import com.melosmarket.api.worker.persistence.WorkerReviewEntity;

import org.springframework.stereotype.Component;

@Component
class WorkerMapper {

    WorkerEntity toEntity(CreateWorkerRequest request) {
        WorkerEntity entity = new WorkerEntity();
        entity.setBusinessName(request.getBusinessName());
        entity.setContactName(request.getContactName());
        entity.setEmail(request.getEmail());
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setTaxNumber(blankToNull(request.getTaxNumber()));
        entity.setTrade(toEntityTrade(request.getTrade()));
        entity.setServiceArea(blankToNull(request.getServiceArea()));
        entity.setCounty(toEntityCounty(request.getCounty()));
        entity.setDescription(blankToNull(request.getDescription()));
        return entity;
    }

    WorkerEntity toEntity(RegisterWorkerRequest request) {
        WorkerEntity entity = new WorkerEntity();
        entity.setBusinessName(request.getBusinessName());
        entity.setContactName(request.getContactName());
        entity.setEmail(request.getEmail());
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setTaxNumber(blankToNull(request.getTaxNumber()));
        entity.setTrade(toEntityTrade(request.getTrade()));
        entity.setServiceArea(blankToNull(request.getServiceArea()));
        entity.setCounty(toEntityCounty(request.getCounty()));
        entity.setDescription(blankToNull(request.getDescription()));
        return entity;
    }

    Worker toApi(WorkerEntity entity) {
        return new Worker(
                entity.getId(),
                entity.getBusinessName(),
                entity.getContactName(),
                entity.getEmail(),
                toApiTrade(entity.getTrade()),
                entity.getCreatedAt())
                .phone(entity.getPhone())
                .taxNumber(entity.getTaxNumber())
                .profileImageUrl(entity.getProfileImageUrl())
                .facebookUrl(entity.getFacebookUrl())
                .instagramUrl(entity.getInstagramUrl())
                .verified(entity.isVerified())
                .topWorker(entity.isTopWorker())
                .manyReferences(entity.isManyReferences())
                .hundredJobs(entity.isHundredJobs())
                .fastResponder(entity.isFastResponder())
                .availabilityStatus(toApiAvailabilityStatus(entity.getAvailabilityStatus()))
                .urgentWork(entity.isUrgentWork())
                .trialStartedAt(entity.getTrialStartedAt())
                .trialEndsAt(entity.getTrialEndsAt())
                .accessStatus(toApiAccessStatus(entity))
                .trialDaysRemaining(trialDaysRemaining(entity.getTrialEndsAt()))
                .serviceArea(entity.getServiceArea())
                .county(toApiCounty(entity.getCounty()))
                .description(entity.getDescription())
                .referenceImages(entity.getReferenceImages().stream()
                        .map(this::toApiReferenceImage)
                        .toList())
                .reviews(entity.getReviews().stream()
                        .map(this::toApiReview)
                        .toList());
    }

    WorkerTradeType toEntityTrade(Trade trade) {
        return trade == null ? null : WorkerTradeType.valueOf(trade.name());
    }

    String toEntityCounty(County county) {
        return county == null ? null : county.getValue();
    }

    WorkerAvailabilityState toEntityAvailabilityStatus(WorkerAvailabilityStatus availabilityStatus) {
        return availabilityStatus == null
                ? WorkerAvailabilityState.AVAILABLE
                : WorkerAvailabilityState.valueOf(availabilityStatus.name());
    }

    private Trade toApiTrade(WorkerTradeType trade) {
        return Trade.valueOf(trade.name());
    }

    private County toApiCounty(String county) {
        return county == null ? null : County.fromValue(county);
    }

    private WorkerAvailabilityStatus toApiAvailabilityStatus(WorkerAvailabilityState availabilityStatus) {
        WorkerAvailabilityState safeAvailabilityStatus = availabilityStatus == null
                ? WorkerAvailabilityState.AVAILABLE
                : availabilityStatus;
        return WorkerAvailabilityStatus.valueOf(safeAvailabilityStatus.name());
    }

    private WorkerAccessStatus toApiAccessStatus(WorkerEntity entity) {
        WorkerSubscriptionStatus status = entity.getSubscriptionStatus();
        if (status == WorkerSubscriptionStatus.ACTIVE) {
            return WorkerAccessStatus.ACTIVE;
        }
        if (status == WorkerSubscriptionStatus.PAST_DUE) {
            return WorkerAccessStatus.PAST_DUE;
        }
        if (status == WorkerSubscriptionStatus.CANCELED) {
            return WorkerAccessStatus.CANCELED;
        }
        return entity.getTrialEndsAt() != null && entity.getTrialEndsAt().isAfter(OffsetDateTime.now())
                ? WorkerAccessStatus.TRIALING
                : WorkerAccessStatus.TRIAL_EXPIRED;
    }

    private int trialDaysRemaining(OffsetDateTime trialEndsAt) {
        if (trialEndsAt == null || !trialEndsAt.isAfter(OffsetDateTime.now())) {
            return 0;
        }
        long secondsRemaining = Duration.between(OffsetDateTime.now(), trialEndsAt).getSeconds();
        return (int) ((secondsRemaining + 86_399) / 86_400);
    }

    WorkerReferenceImage toApiReferenceImage(WorkerReferenceImageEntity entity) {
        return new WorkerReferenceImage(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getCreatedAt());
    }

    WorkerReview toApiReview(WorkerReviewEntity entity) {
        return new WorkerReview(
                entity.getId(),
                entity.getReviewer().getId(),
                entity.getReviewer().getBusinessName(),
                entity.getRating(),
                entity.getText(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
