package com.melosmarket.api.auth;

import com.melosmarket.api.generated.model.AuthResponse;
import com.melosmarket.api.supplier.RegisterSupplierRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SupplierAuthController {

    private final AuthService authService;

    public SupplierAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/supplier")
    public ResponseEntity<AuthResponse> registerSupplier(@Valid @RequestBody RegisterSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerSupplier(request));
    }
}
