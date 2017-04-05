package com.hilats.server.spring.jwt;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashMap;

public class TestUserService implements HilatsUserService {

    private final HashMap<String, HilatsUser> userMap = new HashMap();
    private final HashMap<String, HilatsUser> userMapByEmail = new HashMap();

    public TestUserService() {
        addUser(new HilatsUser("test", "test", new String[] {"user", "admin"}));
    }

    @Override
    public Iterable<HilatsUser> getUsers() {
        return userMap.values();
    }

    @Override
    public final HilatsUser findUser(String username) throws UsernameNotFoundException {
        return userMap.get(username);
    }

    @Override
    public HilatsUser findUserByEmail(String email) {
        return userMapByEmail.get(email);
    }

    public void addUser(HilatsUser user) {
        userMap.put(user.getUsername(), user);
        userMapByEmail.put(user.email, user);
    }

    @Override
    public void saveUser(HilatsUser user) {
        // in memory -- nothing to do
    }
}
