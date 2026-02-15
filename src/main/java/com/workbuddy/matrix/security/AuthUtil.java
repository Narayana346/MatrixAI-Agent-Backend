package com.workbuddy.matrix.security;

import com.workbuddy.matrix.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class AuthUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private int expiration;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {

//        List<String> roles = user.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList();

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("user_name", user.getName())
                .claim("user_id", user.getId().toString())
//                .claim("role", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSecretKey())
                .compact();

    }

    public JwtUserPrinciple verifyAccessToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String userId = claims.get("user_id", String.class);
        String userName = claims.get("user_name", String.class);
//        List<String> roles = claims.get("roles", List.class);
//        List<SimpleGrantedAuthority> authorities = roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .toList();
        String email = claims.getSubject();
        return new JwtUserPrinciple(userId, userName, email, Collections.emptyList());
    }

    public Long getCurrentUserId(){
       Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
       if(authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrinciple)){
           throw new AuthenticationCredentialsNotFoundException("No jwt found");
       }
       JwtUserPrinciple jwtUserPrinciple = (JwtUserPrinciple) authentication.getPrincipal();
       return Long.parseLong(jwtUserPrinciple.id());

    }

}
