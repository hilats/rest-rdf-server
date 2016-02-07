package com.hilats.server.spring.jwt;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;

public class TestUserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final AccountStatusUserDetailsChecker detailsChecker = new AccountStatusUserDetailsChecker();
    private final HashMap<String, User> userMap = new HashMap();

    @Override
    public final User loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userMap.get(username);
        if (user != null)
            detailsChecker.check(user);
        return user;
    }

    public void addUser(User user) {
        userMap.put(user.getUsername(), user);
    }
}
