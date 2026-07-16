package com.melosmarket.api.auth;

import java.io.IOException;

import com.melosmarket.api.auth.persistence.UserEntity;
import com.melosmarket.api.auth.persistence.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                Long userId = jwtService.verifyAndReadUserId(authorization.substring("Bearer ".length()));
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                AuthContext.set(new AuthenticatedUser(user.getId(), user.getEmail(), user.getRole()));
            }
            filterChain.doFilter(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication token");
        } finally {
            AuthContext.clear();
        }
    }
}
