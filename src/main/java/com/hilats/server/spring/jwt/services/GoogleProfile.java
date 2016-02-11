package com.hilats.server.spring.jwt.services;

import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public class GoogleProfile
    extends AuthProfile
{

    public GoogleProfile(Map<String, Object> credentials, Map<String, Object> userInfo) {
        super("google", credentials, userInfo);
    }

    @Override
    public String getId() {
        return (String)getUserInfo().get("sub");
    }

    @Override
    public String getEmail() {
        return (String)getUserInfo().get("email");
    }
}
