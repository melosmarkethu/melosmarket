package com.melosmarket.api.problem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.melosmarket.api.auth.AuthContext;
import com.melosmarket.api.auth.AuthenticatedUser;
import com.melosmarket.api.auth.persistence.AccountRole;
import com.melosmarket.api.customer.persistence.CustomerEntity;
import com.melosmarket.api.customer.persistence.CustomerRepository;
import com.melosmarket.api.generated.model.CreateProblemRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.Problem;
import com.melosmarket.api.generated.model.ProblemImage;
import com.melosmarket.api.generated.model.ProblemStatus;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.UpdateProblemRequest;
import com.melosmarket.api.problem.domain.ProblemStatusEntity;
import com.melosmarket.api.problem.domain.TradeType;
import com.melosmarket.api.problem.persistence.ProblemEntity;
import com.melosmarket.api.problem.persistence.ProblemImageEntity;
import com.melosmarket.api.problem.persistence.ProblemImageRepository;
import com.melosmarket.api.problem.persistence.ProblemRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemImageRepository problemImageRepository;
    private final ProblemMapper problemMapper;
    private final AuthContext authContext;
    private final CustomerRepository customerRepository;
    private final Path problemImagesDir;

    public ProblemService(
            ProblemRepository problemRepository,
            ProblemImageRepository problemImageRepository,
            ProblemMapper problemMapper,
            AuthContext authContext,
            CustomerRepository customerRepository,
            @Value("${melosmarket.uploads.problem-images-dir}") String problemImagesDir) {
        this.problemRepository = problemRepository;
        this.problemImageRepository = problemImageRepository;
        this.problemMapper = problemMapper;
        this.authContext = authContext;
        this.customerRepository = customerRepository;
        this.problemImagesDir = Path.of(problemImagesDir);
    }

    @Transactional
    public Problem createProblem(CreateProblemRequest request) {
        CustomerEntity customer = requireCustomerProfile();
        ProblemEntity entity = problemMapper.toEntity(request);
        entity.setCustomerId(customer.getId());
        return problemMapper.toApi(problemRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Problem getProblem(long problemId) {
        return problemRepository.findById(problemId)
                .map(problemMapper::toApi)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
    }

    @Transactional
    public Problem updateMyProblem(long problemId, UpdateProblemRequest request) {
        CustomerEntity customer = requireCustomerProfile();
        ProblemEntity problem = getOwnedProblem(problemId, customer);
        problemMapper.updateEntity(problem, request);
        return problemMapper.toApi(problem);
    }

    @Transactional
    public void deleteMyProblem(long problemId) {
        CustomerEntity customer = requireCustomerProfile();
        ProblemEntity problem = getOwnedProblem(problemId, customer);
        problemRepository.delete(problem);
    }

    @Transactional
    public ProblemImage uploadProblemPhoto(long problemId, String title, MultipartFile image) {
        CustomerEntity customer = requireCustomerProfile();
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image is required");
        }

        String contentType = image.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG and PNG images are supported");
        }

        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        if (!customer.getId().equals(problem.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can upload photos");
        }
        String extension = "image/png".equals(contentType) ? ".png" : ".jpg";
        String filename = problem.getId() + "-" + UUID.randomUUID() + extension;
        Path target = problemImagesDir.resolve(filename).normalize();

        try {
            Files.createDirectories(problemImagesDir);
            image.transferTo(target);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store problem image");
        }

        ProblemImageEntity problemImage = new ProblemImageEntity();
        problemImage.setProblem(problem);
        problemImage.setTitle(normalizedTitle(title, image.getOriginalFilename()));
        problemImage.setStoragePath(target.toString());
        problemImage.setImageUrl("/api/uploads/problem-images/" + filename);

        return problemMapper.toApiProblemImage(problemImageRepository.save(problemImage));
    }

    @Transactional
    public void deleteProblemPhoto(long problemId, long imageId) {
        CustomerEntity customer = requireCustomerProfile();
        getOwnedProblem(problemId, customer);

        ProblemImageEntity image = problemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem image not found"));
        if (!problemImageRepository.existsByProblemIdAndId(problemId, imageId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem image not found");
        }

        Path storagePath = Path.of(image.getStoragePath()).normalize();
        problemImageRepository.delete(image);
        try {
            Files.deleteIfExists(storagePath);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not delete problem image file");
        }
    }

    @Transactional(readOnly = true)
    public List<Problem> searchProblems(Trade trade, ProblemStatus status, County county) {
        TradeType entityTrade = problemMapper.toEntityTrade(trade);
        ProblemStatusEntity entityStatus = problemMapper.toEntityStatus(status);
        String normalizedCounty = problemMapper.toEntityCounty(county);

        return problemRepository
                .findAll(problemSearch(entityTrade, entityStatus, normalizedCounty))
                .stream()
                .map(problemMapper::toApi)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Problem> listAdminProblems() {
        requireAdmin();
        return problemRepository.findAll()
                .stream()
                .map(problemMapper::toApi)
                .toList();
    }

    @Transactional
    public void deleteAdminProblem(long problemId) {
        requireAdmin();
        if (!problemRepository.existsById(problemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found");
        }
        problemRepository.deleteById(problemId);
    }

    @Transactional
    public List<Problem> getMyProblems() {
        CustomerEntity customer = requireCustomerProfile();
        return problemRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(problemMapper::toApi)
                .toList();
    }

    private CustomerEntity requireCustomerProfile() {
        AuthenticatedUser user = authContext.requireUser();
        if (user.role() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer account is required");
        }
        return customerRepository.findByEmailIgnoreCase(user.email())
                .orElseGet(() -> {
                    CustomerEntity customer = new CustomerEntity();
                    customer.setEmail(user.email());
                    customer.setFullName(defaultCustomerName(user.email()));
                    return customerRepository.save(customer);
                });
    }

    private ProblemEntity getOwnedProblem(long problemId, CustomerEntity customer) {
        ProblemEntity problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem not found"));
        if (!customer.getId().equals(problem.getCustomerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can edit this problem");
        }
        return problem;
    }

    private void requireAdmin() {
        AuthenticatedUser user = authContext.requireUser();
        if (user.role() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin account is required");
        }
    }

    private String defaultCustomerName(String email) {
        int atSign = email.indexOf('@');
        String localPart = atSign > 0 ? email.substring(0, atSign) : email;
        return localPart.isBlank() ? "Ügyfél" : localPart;
    }

    private Specification<ProblemEntity> problemSearch(
            TradeType trade,
            ProblemStatusEntity status,
            String county) {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            List<Predicate> predicates = new ArrayList<>();
            if (trade != null) {
                predicates.add(criteriaBuilder.equal(root.get("trade"), trade));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
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
            return "Probléma fotó";
        }
        return filename.replaceFirst("\\.[^.]+$", "");
    }
}
