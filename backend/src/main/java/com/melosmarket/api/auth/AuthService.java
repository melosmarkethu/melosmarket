package com.melosmarket.api.auth;

import com.melosmarket.api.auth.persistence.AccountRole;
import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.auth.persistence.UserRepository;
import com.melosmarket.api.customer.persistence.CustomerEntity;
import com.melosmarket.api.customer.persistence.CustomerRepository;
import com.melosmarket.api.generated.model.AuthResponse;
import com.melosmarket.api.generated.model.CurrentUser;
import com.melosmarket.api.generated.model.LoginRequest;
import com.melosmarket.api.generated.model.RegisterCustomerRequest;
import com.melosmarket.api.generated.model.RegisterWorkerRequest;
import com.melosmarket.api.generated.model.UserRole;
import com.melosmarket.api.generated.model.VerifyEmailRequest;
import com.melosmarket.api.generated.model.Worker;
import com.melosmarket.api.worker.WorkerService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final AuthContext authContext;
    private final WorkerService workerService;
    private final CustomerRepository customerRepository;
    private final EmailVerificationService emailVerificationService;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            JwtService jwtService,
            AuthContext authContext,
            WorkerService workerService,
            CustomerRepository customerRepository,
            EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.authContext = authContext;
        this.workerService = workerService;
        this.customerRepository = customerRepository;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public AuthResponse registerWorker(RegisterWorkerRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        user.setRole(AccountRole.WORKER);
        user.setEmailVerified(false);
        UserEntity savedUser = userRepository.save(user);
        Worker worker = workerService.createOwnedWorker(request, savedUser);
        emailVerificationService.sendVerificationEmail(savedUser);

        return authResponse(savedUser, worker);
    }

    @Transactional
    public AuthResponse registerCustomer(RegisterCustomerRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordHasher.hash(request.getPassword()));
        user.setRole(AccountRole.CUSTOMER);
        user.setEmailVerified(false);
        UserEntity savedUser = userRepository.save(user);
        ensureCustomerProfile(savedUser.getEmail());
        emailVerificationService.sendVerificationEmail(savedUser);
        return authResponse(savedUser, null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordHasher.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        Worker worker = user.getRole() == AccountRole.WORKER ? workerService.getWorkerForUser(user.getId()) : null;
        return authResponse(user, worker);
    }

    @Transactional(readOnly = true)
    public CurrentUser getCurrentUser() {
        AuthenticatedUser authenticatedUser = authContext.requireUser();
        UserEntity user = userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Long workerId = authenticatedUser.role() == AccountRole.WORKER
                ? workerService.getWorkerEntityForUser(authenticatedUser.id()).getId()
                : null;
        return currentUser(user.getId(), user.getEmail(), user.getRole(), workerId, user.isEmailVerified());
    }

    @Transactional(readOnly = true)
    public long countRegisteredUsers() {
        return userRepository.count();
    }

    @Transactional
    public CurrentUser verifyEmail(VerifyEmailRequest request) {
        UserEntity user = emailVerificationService.verifyEmail(request.getToken());
        Long workerId = user.getRole() == AccountRole.WORKER
                ? workerService.getWorkerEntityForUser(user.getId()).getId()
                : null;
        return currentUser(user.getId(), user.getEmail(), user.getRole(), workerId, user.isEmailVerified());
    }

    @Transactional
    public void resendVerificationEmail() {
        emailVerificationService.resendVerificationEmail(authContext.requireUser().id());
    }

    private AuthResponse authResponse(UserEntity user, Worker worker) {
        return new AuthResponse(jwtService.createToken(user.getId()), currentUser(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                worker == null ? null : worker.getId(),
                user.isEmailVerified()))
                .worker(worker);
    }

    private CurrentUser currentUser(Long id, String email, AccountRole role, Long workerId, boolean emailVerified) {
        return new CurrentUser(id, email, UserRole.valueOf(role.name()), emailVerified).workerId(workerId);
    }

    private CustomerEntity ensureCustomerProfile(String email) {
        return customerRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> {
                    CustomerEntity customer = new CustomerEntity();
                    customer.setEmail(email);
                    customer.setFullName(defaultCustomerName(email));
                    return customerRepository.save(customer);
                });
    }

    private String defaultCustomerName(String email) {
        int atSign = email.indexOf('@');
        String localPart = atSign > 0 ? email.substring(0, atSign) : email;
        return localPart.isBlank() ? "Ügyfél" : localPart;
    }
}
