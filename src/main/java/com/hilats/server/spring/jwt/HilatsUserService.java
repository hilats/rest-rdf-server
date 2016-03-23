package com.hilats.server.spring.jwt;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by pduchesne on 11/02/16.
 */
public interface HilatsUserService
{
    public HilatsUser findUser(String userid);

    void addUser(HilatsUser user);

    void saveUser(HilatsUser user);
}
