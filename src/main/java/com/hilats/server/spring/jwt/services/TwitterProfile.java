package com.hilats.server.spring.jwt.services;

import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public class TwitterProfile
    extends AuthProfile
{

    public TwitterProfile(Map<String, Object> credentials, Map<String, Object> userInfo) {
        super("twitter", credentials, userInfo);
    }

    @Override
    public String getId() {
        return String.valueOf(getUserInfo().get("id"));
    }

    @Override
    public String getEmail() {
        return String.valueOf(getUserInfo().get("email"));
    }
}
