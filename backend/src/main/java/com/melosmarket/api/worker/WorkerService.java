package com.melosmarket.api.worker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.melosmarket.api.auth.AuthContext;
import com.melosmarket.api.auth.AuthenticatedUser;
import com.melosmarket.api.auth.persistence.AccountRole;
import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.generated.model.CreateWorkerRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.CreateWorkerReviewRequest;
import com.melosmarket.api.generated.model.RegisterWorkerRequest;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.UpdateWorkerBadgesRequest;
import com.melosmarket.api.generated.model.UpdateWorkerProfileRequest;
import com.melosmarket.api.generated.model.Worker;
import com.melosmarket.api.generated.model.WorkerReferenceImage;
import com.melosmarket.api.generated.model.WorkerReview;
import com.melosmarket.api.worker.domain.WorkerTradeType;
import com.melosmarket.api.worker.domain.WorkerSubscriptionStatus;
import com.melosmarket.api.worker.persistence.WorkerEntity;
import com.melosmarket.api.worker.persistence.WorkerReferenceImageEntity;
import com.melosmarket.api.worker.persistence.WorkerReferenceImageRepository;
import com.melosmarket.api.worker.persistence.WorkerRepository;
import com.melosmarket.api.worker.persistence.WorkerReviewEntity;
import com.melosmarket.api.worker.persistence.WorkerReviewRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final WorkerReferenceImageRepository referenceImageRepository;
    private final WorkerReviewRepository reviewRepository;
    private final WorkerMapper workerMapper;
    private final AuthContext authContext;
    private final Path referenceUploadDir;
    private final Path profileImageUploadDir;

    public WorkerService(
            WorkerRepository workerRepository,
            WorkerReferenceImageRepository referenceImageRepository,
            WorkerReviewRepository reviewRepository,
            WorkerMapper workerMapper,
            AuthContext authContext,
            @Value("${melosmarket.uploads.worker-references-dir}") String referenceUploadDir,
            @Value("${melosmarket.uploads.worker-profile-images-dir}") String profileImageUploadDir) {
        this.workerRepository = workerRepository;
        this.referenceImageRepository = referenceImageRepository;
        this.reviewRepository = reviewRepository;
        this.workerMapper = workerMapper;
        this.authContext = authContext;
        this.referenceUploadDir = Path.of(referenceUploadDir);
        this.profileImageUploadDir = Path.of(profileImageUploadDir);
    }

    @Transactional
    public Worker createWorker(CreateWorkerRequest request) {
        if (workerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Worker email already exists");
        }

        WorkerEntity entity = workerMapper.toEntity(request);
        return workerMapper.toApi(workerRepository.save(entity));
    }

    @Transactional
    public Worker createOwnedWorker(RegisterWorkerRequest request, UserEntity user) {
        if (workerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Worker email already exists");
        }

        WorkerEntity entity = workerMapper.toEntity(request);
        entity.setUser(user);
        return workerMapper.toApi(workerRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Worker getWorker(long workerId) {
        return workerRepository.findById(workerId)
                .map(workerMapper::toApi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
    }

    @Transactional(readOnly = true)
    public List<Worker> searchWorkers(Trade trade, County county) {
        WorkerTradeType entityTrade = workerMapper.toEntityTrade(trade);
        String normalizedCounty = workerMapper.toEntityCounty(county);

        return workerRepository
                .findAll(workerSearch(entityTrade, normalizedCounty))
                .stream()
                .map(workerMapper::toApi)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Worker> listAdminWorkers() {
        requireAdmin();
        return workerRepository.findAll()
                .stream()
                .map(workerMapper::toApi)
                .toList();
    }

    @Transactional
    public void deleteAdminWorker(long workerId) {
        requireAdmin();
        if (!workerRepository.existsById(workerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found");
        }
        workerRepository.deleteById(workerId);
    }

    @Transactional
    public Worker updateAdminWorkerBadges(long workerId, UpdateWorkerBadgesRequest request) {
        requireAdmin();
        WorkerEntity worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
        worker.setVerified(request.getVerified());
        worker.setTopWorker(request.getTopWorker());
        worker.setManyReferences(request.getManyReferences());
        worker.setHundredJobs(request.getHundredJobs());
        worker.setFastResponder(request.getFastResponder());
        return workerMapper.toApi(worker);
    }

    @Transactional(readOnly = true)
    public Worker getMyWorkerProfile() {
        return workerMapper.toApi(getWorkerEntityForUser(authContext.requireUser().id()));
    }

    @Transactional(readOnly = true)
    public Worker getWorkerForUser(Long userId) {
        return workerMapper.toApi(getWorkerEntityForUser(userId));
    }

    @Transactional(readOnly = true)
    public WorkerEntity getWorkerEntityForUser(Long userId) {
        return workerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker profile not found"));
    }

    @Transactional
    public Worker updateMyWorkerProfile(UpdateWorkerProfileRequest request) {
        AuthenticatedUser user = authContext.requireUser();
        WorkerEntity entity = getWorkerEntityForUser(user.id());
        entity.setBusinessName(request.getBusinessName());
        entity.setContactName(request.getContactName());
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setTaxNumber(blankToNull(request.getTaxNumber()));
        entity.setTrade(workerMapper.toEntityTrade(request.getTrade()));
        entity.setServiceArea(blankToNull(request.getServiceArea()));
        entity.setCounty(workerMapper.toEntityCounty(request.getCounty()));
        entity.setDescription(blankToNull(request.getDescription()));
        entity.setAvailabilityStatus(workerMapper.toEntityAvailabilityStatus(request.getAvailabilityStatus()));
        entity.setUrgentWork(Boolean.TRUE.equals(request.getUrgentWork()));
        return workerMapper.toApi(entity);
    }

    @Transactional
    public Worker uploadMyWorkerProfileImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required");
        }

        String contentType = image.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG images are supported");
        }

        WorkerEntity worker = getWorkerEntityForUser(authContext.requireUser().id());
        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        String filename = worker.getId() + "-" + UUID.randomUUID() + extension;
        Path target = profileImageUploadDir.resolve(filename).normalize();
        String previousStoragePath = worker.getProfileImageStoragePath();

        try {
            Files.createDirectories(profileImageUploadDir);
            image.transferTo(target);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store profile image");
        }

        worker.setProfileImageStoragePath(target.toString());
        worker.setProfileImageUrl("/api/uploads/worker-profile-images/" + filename);
        deleteStoredFile(previousStoragePath);

        return workerMapper.toApi(worker);
    }

    @Transactional
    public WorkerReferenceImage uploadMyWorkerReference(String title, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required");
        }

        String contentType = image.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG images are supported");
        }

        WorkerEntity worker = getWorkerEntityForUser(authContext.requireUser().id());
        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        String filename = worker.getId() + "-" + UUID.randomUUID() + extension;
        Path target = referenceUploadDir.resolve(filename).normalize();

        try {
            Files.createDirectories(referenceUploadDir);
            image.transferTo(target);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store reference image");
        }

        WorkerReferenceImageEntity referenceImage = new WorkerReferenceImageEntity();
        referenceImage.setWorker(worker);
        referenceImage.setTitle(normalizedTitle(title, image.getOriginalFilename()));
        referenceImage.setStoragePath(target.toString());
        referenceImage.setImageUrl("/api/uploads/worker-references/" + filename);

        return workerMapper.toApiReferenceImage(referenceImageRepository.save(referenceImage));
    }

    @Transactional
    public void deleteMyWorkerReference(long referenceId) {
        WorkerEntity worker = getWorkerEntityForUser(authContext.requireUser().id());
        WorkerReferenceImageEntity referenceImage = referenceImageRepository.findById(referenceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reference image not found"));

        if (!referenceImage.getWorker().getId().equals(worker.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reference images");
        }

        Path storagePath = Path.of(referenceImage.getStoragePath()).normalize();
        referenceImageRepository.delete(referenceImage);
        deleteStoredFile(storagePath.toString());
    }

    @Transactional
    public WorkerReview createWorkerReview(long targetWorkerId, CreateWorkerReviewRequest request) {
        WorkerEntity reviewer = getWorkerEntityForUser(authContext.requireUser().id());
        WorkerEntity target = workerRepository.findById(targetWorkerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));

        if (reviewer.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot review your own profile");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        WorkerReviewEntity review = reviewRepository
                .findByReviewerIdAndTargetId(reviewer.getId(), target.getId())
                .orElseGet(WorkerReviewEntity::new);
        review.setReviewer(reviewer);
        review.setTarget(target);
        review.setRating(request.getRating());
        review.setText(normalizedReviewText(request.getText()));

        return workerMapper.toApiReview(reviewRepository.save(review));
    }

    private Specification<WorkerEntity> workerSearch(WorkerTradeType trade, String county) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("subscriptionStatus"), WorkerSubscriptionStatus.ACTIVE),
                    criteriaBuilder.greaterThan(root.get("trialEndsAt"), java.time.OffsetDateTime.now())));
            if (trade != null) {
                predicates.add(criteriaBuilder.equal(root.get("trade"), trade));
            }
            if (county != null) {
                predicates.add(criteriaBuilder.equal(root.get("county"), county));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizedTitle(String title, String filename) {
        if (title != null && !title.isBlank()) {
            return title.trim();
        }
        if (filename == null || filename.isBlank()) {
            return "Referencia munka";
        }
        return filename.replaceFirst("\\.[^.]+$", "");
    }

    private void deleteStoredFile(String storagePath) {
        if (storagePath == null || storagePath.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(Path.of(storagePath).normalize());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete image file");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizedReviewText(String value) {
        if (value == null || value.isBlank() || value.trim().length() < 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review text is too short");
        }
        return value.trim();
    }

    private void requireAdmin() {
        AuthenticatedUser user = authContext.requireUser();
        if (user.role() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin account is required");
        }
    }
}
