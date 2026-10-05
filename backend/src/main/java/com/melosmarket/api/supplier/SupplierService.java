package com.melosmarket.api.supplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.melosmarket.api.auth.AuthContext;
import com.melosmarket.api.auth.AuthenticatedUser;
import com.melosmarket.api.auth.persistence.AccountRole;
import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.supplier.persistence.SupplierEntity;
import com.melosmarket.api.supplier.persistence.SupplierPromotionEntity;
import com.melosmarket.api.supplier.persistence.SupplierPromotionRepository;
import com.melosmarket.api.supplier.persistence.SupplierQuoteRequestEntity;
import com.melosmarket.api.supplier.persistence.SupplierQuoteRequestRepository;
import com.melosmarket.api.supplier.persistence.SupplierRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierPromotionRepository promotionRepository;
    private final SupplierQuoteRequestRepository quoteRequestRepository;
    private final SupplierMapper supplierMapper;
    private final AuthContext authContext;
    private final JavaMailSender mailSender;
    private final Path supplierImageUploadDir;
    private final Path promotionImageUploadDir;
    private final String fromEmail;
    private final String publicBaseUrl;

    public SupplierService(
            SupplierRepository supplierRepository,
            SupplierPromotionRepository promotionRepository,
            SupplierQuoteRequestRepository quoteRequestRepository,
            SupplierMapper supplierMapper,
            AuthContext authContext,
            JavaMailSender mailSender,
            @Value("${melosmarket.uploads.supplier-images-dir}") String supplierImageUploadDir,
            @Value("${melosmarket.uploads.supplier-promotion-images-dir}") String promotionImageUploadDir,
            @Value("${melosmarket.email.from}") String fromEmail,
            @Value("${melosmarket.public-base-url}") String publicBaseUrl) {
        this.supplierRepository = supplierRepository;
        this.promotionRepository = promotionRepository;
        this.quoteRequestRepository = quoteRequestRepository;
        this.supplierMapper = supplierMapper;
        this.authContext = authContext;
        this.mailSender = mailSender;
        this.supplierImageUploadDir = Path.of(supplierImageUploadDir);
        this.promotionImageUploadDir = Path.of(promotionImageUploadDir);
        this.fromEmail = fromEmail;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Transactional
    public SupplierDto createOwnedSupplier(RegisterSupplierRequest request, UserEntity user) {
        if (supplierRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Supplier email already exists");
        }

        SupplierEntity supplier = new SupplierEntity();
        supplier.setUser(user);
        supplier.setBusinessName(request.businessName().trim());
        supplier.setEmail(request.email().trim().toLowerCase());
        supplier.setPhone(blankToNull(request.phone()));
        supplier.setCity(blankToNull(request.city()));
        supplier.setCounty(blankToNull(request.county()));
        supplier.setAddress(blankToNull(request.address()));
        supplier.setDescription(blankToNull(request.description()));
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierDto> searchSuppliers(String city, String county, String area, String name) {
        return supplierRepository.findAll(supplierSearch(city, county, area, name))
                .stream()
                .map(supplierMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplier(long supplierId) {
        return supplierRepository.findById(supplierId)
                .map(supplierMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    }

    @Transactional(readOnly = true)
    public SupplierDto getMySupplierProfile() {
        return supplierMapper.toDto(getSupplierEntityForUser(authContext.requireUser().id()));
    }

    @Transactional(readOnly = true)
    public SupplierDto getSupplierForUser(Long userId) {
        return supplierMapper.toDto(getSupplierEntityForUser(userId));
    }

    @Transactional(readOnly = true)
    public SupplierEntity getSupplierEntityForUser(Long userId) {
        return supplierRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier profile not found"));
    }

    @Transactional
    public SupplierDto updateMySupplierProfile(UpdateSupplierProfileRequest request) {
        SupplierEntity supplier = getSupplierEntityForUser(authContext.requireUser().id());
        supplier.setBusinessName(request.businessName().trim());
        if (request.email() != null && !request.email().isBlank()) {
            supplier.setEmail(request.email().trim().toLowerCase());
        }
        supplier.setPhone(blankToNull(request.phone()));
        supplier.setWebsite(blankToNull(request.website()));
        supplier.setDescription(blankToNull(request.description()));
        supplier.setAddress(blankToNull(request.address()));
        supplier.setCity(blankToNull(request.city()));
        supplier.setCounty(blankToNull(request.county()));
        supplier.setMapLocation(blankToNull(request.mapLocation()));
        supplier.setOpeningHours(blankToNull(request.openingHours()));
        supplier.setProductCategories(blankToNull(request.productCategories()));
        supplier.setDeliveryAvailable(Boolean.TRUE.equals(request.deliveryAvailable()));
        supplier.setMaxDeliveryDistanceKm(request.maxDeliveryDistanceKm());
        supplier.setDeliveryArea(blankToNull(request.deliveryArea()));
        supplier.setDeliveryInfo(blankToNull(request.deliveryInfo()));
        supplier.setAdditionalServices(blankToNull(request.additionalServices()));
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public SupplierDto uploadMySupplierImage(String imageType, MultipartFile image) {
        SupplierEntity supplier = getSupplierEntityForUser(authContext.requireUser().id());
        StoredImage storedImage = storeImage(image, supplierImageUploadDir, "supplier-images", supplier.getId());
        if ("cover".equalsIgnoreCase(imageType)) {
            String previous = supplier.getCoverImageStoragePath();
            supplier.setCoverImageStoragePath(storedImage.storagePath());
            supplier.setCoverImageUrl(storedImage.imageUrl());
            deleteStoredFile(previous);
        } else {
            String previous = supplier.getProfileImageStoragePath();
            supplier.setProfileImageStoragePath(storedImage.storagePath());
            supplier.setProfileImageUrl(storedImage.imageUrl());
            deleteStoredFile(previous);
        }
        return supplierMapper.toDto(supplier);
    }

    @Transactional
    public SupplierPromotionDto createMyPromotion(UpsertSupplierPromotionRequest request) {
        SupplierEntity supplier = getSupplierEntityForUser(authContext.requireUser().id());
        SupplierPromotionEntity promotion = new SupplierPromotionEntity();
        promotion.setSupplier(supplier);
        applyPromotionRequest(promotion, request);
        return supplierMapper.toPromotionDto(promotionRepository.save(promotion));
    }

    @Transactional
    public SupplierPromotionDto updateMyPromotion(long promotionId, UpsertSupplierPromotionRequest request) {
        SupplierPromotionEntity promotion = getOwnedPromotion(promotionId);
        applyPromotionRequest(promotion, request);
        return supplierMapper.toPromotionDto(promotion);
    }

    @Transactional
    public void deleteMyPromotion(long promotionId) {
        SupplierPromotionEntity promotion = getOwnedPromotion(promotionId);
        String storagePath = promotion.getImageStoragePath();
        promotionRepository.delete(promotion);
        deleteStoredFile(storagePath);
    }

    @Transactional
    public SupplierPromotionDto uploadMyPromotionImage(long promotionId, MultipartFile image) {
        SupplierPromotionEntity promotion = getOwnedPromotion(promotionId);
        StoredImage storedImage = storeImage(image, promotionImageUploadDir, "supplier-promotions", promotion.getSupplier().getId());
        String previous = promotion.getImageStoragePath();
        promotion.setImageStoragePath(storedImage.storagePath());
        promotion.setImageUrl(storedImage.imageUrl());
        deleteStoredFile(previous);
        return supplierMapper.toPromotionDto(promotion);
    }

    @Transactional
    public SupplierQuoteRequestDto createQuoteRequest(long supplierId, CreateSupplierQuoteRequest request) {
        SupplierEntity supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
        SupplierQuoteRequestEntity quoteRequest = new SupplierQuoteRequestEntity();
        quoteRequest.setSupplier(supplier);
        quoteRequest.setCustomerEmail(request.customerEmail().trim().toLowerCase());
        quoteRequest.setCustomerPhone(blankToNull(request.customerPhone()));
        quoteRequest.setMaterials(request.materials().trim());
        quoteRequest.setMessage(blankToNull(request.message()));
        SupplierQuoteRequestEntity saved = quoteRequestRepository.save(quoteRequest);

        try {
            sendQuoteRequestEmail(supplier, saved);
        } catch (MessagingException | MailException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not send quote request email");
        }

        return new SupplierQuoteRequestDto(saved.getId(), supplier.getId(), saved.getStatus(), saved.getCreatedAt());
    }

    private Specification<SupplierEntity> supplierSearch(String city, String county, String area, String name) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));
            List<Predicate> predicates = new ArrayList<>();
            if (hasText(city)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), like(city)));
            }
            if (hasText(county)) {
                predicates.add(criteriaBuilder.equal(root.get("county"), county.trim()));
            }
            if (hasText(area)) {
                String value = like(area);
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), value),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), value),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("deliveryArea")), value)));
            }
            if (hasText(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("businessName")), like(name)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private SupplierPromotionEntity getOwnedPromotion(long promotionId) {
        SupplierEntity supplier = getSupplierEntityForUser(authContext.requireUser().id());
        SupplierPromotionEntity promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
        if (!promotion.getSupplier().getId().equals(supplier.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own promotions");
        }
        return promotion;
    }

    private void applyPromotionRequest(SupplierPromotionEntity promotion, UpsertSupplierPromotionRequest request) {
        promotion.setTitle(request.title().trim());
        promotion.setDescription(blankToNull(request.description()));
        promotion.setCurrentPrice(request.currentPrice().trim());
        promotion.setOriginalPrice(blankToNull(request.originalPrice()));
        promotion.setStartDate(request.startDate());
        promotion.setExpirationDate(request.expirationDate());
        promotion.setActive(!Boolean.FALSE.equals(request.active()));
    }

    private StoredImage storeImage(MultipartFile image, Path uploadDir, String publicFolder, Long ownerId) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required");
        }
        String contentType = image.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG images are supported");
        }
        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        String filename = ownerId + "-" + UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename).normalize();

        try {
            Files.createDirectories(uploadDir);
            image.transferTo(target);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store image");
        }

        return new StoredImage(target.toString(), "/api/uploads/" + publicFolder + "/" + filename);
    }

    private void sendQuoteRequestEmail(SupplierEntity supplier, SupplierQuoteRequestEntity request)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(supplier.getEmail());
        helper.setFrom(fromEmail);
        helper.setReplyTo(request.getCustomerEmail());
        helper.setSubject("New Quote Request - MelosMarket");
        helper.setText(quoteRequestText(supplier, request), false);
        mailSender.send(message);
    }

    private String quoteRequestText(SupplierEntity supplier, SupplierQuoteRequestEntity request) {
        return """
                New quote request received from MelosMarket

                Customer email:
                %s

                Phone:
                %s

                Requested materials:
                %s

                Additional information:
                %s

                Supplier:
                %s

                MelosMarket profile:
                %s/tuzep/%s
                """.formatted(
                request.getCustomerEmail(),
                request.getCustomerPhone() == null ? "-" : request.getCustomerPhone(),
                request.getMaterials(),
                request.getMessage() == null ? "-" : request.getMessage(),
                supplier.getBusinessName(),
                publicBaseUrl.replaceAll("/+$", ""),
                supplier.getId());
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record StoredImage(String storagePath, String imageUrl) {
    }
}
