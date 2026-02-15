package com.workbuddy.matrix.security;

import java.util.List;

public record JwtUserPrinciple(
        String id,
        String name,
        String email,
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> role
) {
}
