package com.hilats.server.spring.jwt;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by pduchesne on 11/02/16.
 */
public interface HilatsUserService
{
    public Iterable<HilatsUser> getUsers();

    public HilatsUser findUser(String userid);

    public HilatsUser findUserByEmail(String email);

    void addUser(HilatsUser user);

    void saveUser(HilatsUser user);
}
