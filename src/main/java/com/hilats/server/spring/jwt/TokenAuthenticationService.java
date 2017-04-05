package com.hilats.server.spring.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TokenAuthenticationService {

    private static final String AUTH_HEADER_NAME = "Authorization";

    private final TokenHandler tokenHandler;

    public TokenAuthenticationService(String secret, HilatsUserDetailsService userService) {
        tokenHandler = new TokenHandler(secret, userService);
    }

    public String createToken(UserAuthentication authentication) {
        final UserDetails user = authentication.getPrincipal();
        String token = tokenHandler.createTokenForUser(user);
        return token;
    }

    public String addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
        String token = createToken(authentication);
        response.addHeader(AUTH_HEADER_NAME, token); // TODO Bearer ?
        return token;
    }

    public UserAuthentication getAuthentication(HttpServletRequest request) {
        final String token = request.getHeader(AUTH_HEADER_NAME);
        if (token != null) {
            String[] parts = token.split(" ");
            if ("Bearer".equals(parts[0])) {
                final HilatsUserDetails user = tokenHandler.parseUserFromToken(token.split(" ")[1]);
                if (user != null) {
                    return new UserAuthentication(user);
                }
            }
        }
        return null;
    }
}
