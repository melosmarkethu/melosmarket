package com.melosmarket.api.auth;

import com.melosmarket.api.auth.persistence.AccountRole;

public record AuthenticatedUser(Long id, String email, AccountRole role) {
}
