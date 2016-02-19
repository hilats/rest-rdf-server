package com.hilats.server.spring.jwt;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;

public class TestUserService implements HilatsUserService {

    private final HashMap<String, HilatsUser> userMap = new HashMap();

    public TestUserService() {
        addUser(new HilatsUser("test", "test", new String[] {"user"}));
    }

    @Override
    public final HilatsUser findUser(String username) throws UsernameNotFoundException {
        return userMap.get(username);
    }

    public void addUser(HilatsUser user) {
        userMap.put(user.username, user);
    }
}
