package com.melosmarket.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    static void set(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    static void clear() {
        CURRENT_USER.remove();
    }

    public AuthenticatedUser requireUser() {
        AuthenticatedUser user = CURRENT_USER.get();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        return user;
    }

    public AuthenticatedUser currentUserOrNull() {
        return CURRENT_USER.get();
    }
}
