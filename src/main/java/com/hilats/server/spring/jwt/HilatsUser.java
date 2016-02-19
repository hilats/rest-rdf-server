package com.hilats.server.spring.jwt;

import com.hilats.server.spring.jwt.services.AuthProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public class HilatsUser {

    public String username;
    public String password;
    public String firstName;
    public String lastName;
    public String email;

    public String[] roles = new String[] {};

    public Map<String, AuthProfile> providerProfiles = new HashMap();

    public HilatsUser(String username, String password, String[] roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public Map<String, AuthProfile> getProviderProfiles() {
        return providerProfiles;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String[] getRoles() {
        return roles;
    }
}
