package com.melosmarket.api.auth;

import com.melosmarket.api.generated.AuthApi;
import com.melosmarket.api.generated.model.AuthResponse;
import com.melosmarket.api.generated.model.CurrentUser;
import com.melosmarket.api.generated.model.LoginRequest;
import com.melosmarket.api.generated.model.RegisterCustomerRequest;
import com.melosmarket.api.generated.model.RegisterWorkerRequest;
import com.melosmarket.api.generated.model.VerifyEmailRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public ResponseEntity<CurrentUser> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @Override
    public ResponseEntity<AuthResponse> login(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<Long> getRegisteredUserCount() {
        return ResponseEntity.ok(authService.countRegisteredUsers());
    }

    @Override
    public ResponseEntity<AuthResponse> registerWorker(RegisterWorkerRequest registerWorkerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerWorker(registerWorkerRequest));
    }

    @Override
    public ResponseEntity<AuthResponse> registerCustomer(RegisterCustomerRequest registerCustomerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCustomer(registerCustomerRequest));
    }

    @Override
    public ResponseEntity<CurrentUser> verifyEmail(VerifyEmailRequest verifyEmailRequest) {
        return ResponseEntity.ok(authService.verifyEmail(verifyEmailRequest));
    }

    @Override
    public ResponseEntity<Void> resendVerificationEmail() {
        authService.resendVerificationEmail();
        return ResponseEntity.noContent().build();
    }
}
