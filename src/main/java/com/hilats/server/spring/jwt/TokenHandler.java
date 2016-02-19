package com.hilats.server.spring.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jersey.repackaged.com.google.common.base.Preconditions;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public final class TokenHandler {

    private final String secret;
    private final HilatsUserDetailsService userService;

    public TokenHandler(String secret, HilatsUserDetailsService userService) {
        if (secret == null || secret.trim().length() == 0)
            throw new IllegalArgumentException("Secret not set");

        this.secret = secret;
        this.userService = Preconditions.checkNotNull(userService);
    }

    public HilatsUserDetails parseUserFromToken(String token) {
        String username = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return userService.loadUserByUsername(username);
    }

    public String createTokenForUser(UserDetails user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
