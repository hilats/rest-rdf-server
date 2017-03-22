package com.hilats.server.spring.jwt.services;

import java.util.Map;

/**
 * Created by pduchesne on 11/02/16.
 */
public class LinkedinProfile
    extends AuthProfile
{

    public LinkedinProfile(Map<String, Object> credentials, Map<String, Object> userInfo) {
        super("linkedin", credentials, userInfo);
    }

    @Override
    public String getId() {
        return (String)getUserInfo().get("id");
    }

    @Override
    public String getEmail() {
        return (String)getUserInfo().get("emailAddress");
    }

    @Override
    public String getDisplayName() {
        //TODO is there a display name in LinkedIn profile info ?
        return (String)getUserInfo().get("firstName") + " " + (String)getUserInfo().get("lastName");
    }


    @Override
    public String getPictureUrl() {
        return (String)getUserInfo().get("pictureUrl");
    }
}
