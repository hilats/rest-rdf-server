package com.hilats.server.spring.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.springframework.security.core.userdetails.User;

public final class TokenHandler {

    private final String secret;
    private final TestUserService userService;

    public TokenHandler(String secret, TestUserService userService) {
        if (secret == null || secret.trim().length() == 0)
            throw new IllegalArgumentException("Secret not set");

        this.secret = secret;
        this.userService = Preconditions.checkNotNull(userService);
    }

    public User parseUserFromToken(String token) {
        String username = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return userService.loadUserByUsername(username);
    }

    public String createTokenForUser(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
