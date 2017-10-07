package com.hilats.server.spring.jwt;

import com.hilats.server.spring.jwt.services.AuthProfile;
import org.springframework.data.annotation.Id;

import java.util.*;

/**
 * Created by pduchesne on 11/02/16.
 */
public class HilatsUser {

    @Id
    public String username;

    public String password;
    public String displayName;
    public String email;
    public String pictureUrl;

    public List<String> roles = new ArrayList<String>();

    public Map<String, AuthProfile> providerProfiles = new HashMap();

    public HilatsUser(String username, String password, List<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = new ArrayList(roles);
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

    public String getDisplayName() {
        return displayName;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
