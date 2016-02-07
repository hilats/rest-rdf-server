package com.hilats.server.spring.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenAuthenticationService {

    private static final String AUTH_HEADER_NAME = "Authorization";

    private final TokenHandler tokenHandler;

    public TokenAuthenticationService(String secret, TestUserService userService) {
        tokenHandler = new TokenHandler(secret, userService);
    }

    public String createToken(UserAuthentication authentication) {
        final User user = authentication.getDetails();
        String token = tokenHandler.createTokenForUser(user);
        return token;
    }

    public String addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
        String token = createToken(authentication);
        response.addHeader(AUTH_HEADER_NAME, token); // TODO Bearer ?
        return token;
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null) {
            String[] parts = token.split(" ");
            if ("Bearer".equals(parts[0])) {
                final User user = tokenHandler.parseUserFromToken(token.split(" ")[1]);
                if (user != null) {
                    return new UserAuthentication(user);
                }
            }
        }
        return null;
    }
}
