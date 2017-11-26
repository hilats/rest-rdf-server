package com.hilats.server.spring.jwt.services;

import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public abstract class AuthProfile {

    private String provider;

    private Map<String, Object> credentials;

    private Map<String, Object> userInfo;

    public AuthProfile(String provider, Map<String, Object> credentials, Map<String, Object> userInfo) {
        this.provider = provider;
        this.credentials = credentials;
        this.userInfo = userInfo;
    }

    public String getProvider() {
        return provider;
    }

    public Map<String, Object> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Object> credentials) {
        this.credentials = credentials;
    }

    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    public abstract String getId();

    public abstract String getEmail();

    public abstract String getDisplayName();

    public abstract String getPictureUrl();
}
