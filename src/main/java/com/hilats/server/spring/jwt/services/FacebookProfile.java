package com.hilats.server.spring.jwt.services;

import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public class FacebookProfile
    extends AuthProfile
{

    public FacebookProfile(Map<String, Object> credentials, Map<String, Object> userInfo) {
        super("facebook", credentials, userInfo);
    }

    @Override
    public String getId() {
        return (String)getUserInfo().get("id");
    }

    @Override
    public String getEmail() {
        return (String)getUserInfo().get("email");
    }
}
