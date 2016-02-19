package com.hilats.server.rest.resources;

import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnection;
import com.hilats.server.spring.jwt.HilatsUser;
import com.hilats.server.spring.jwt.HilatsUserDetails;
import com.hilats.server.spring.jwt.UserAuthentication;
import org.springframework.security.core.Authentication;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by pduchesne on 28/06/14.
 */
public class AbstractResource {

    @Context
    protected SecurityContext securityContext;

    @Context
    RdfApplication app;

    private HilatsUser user;

    public RdfApplication getApplication() {
        return app;
    }

    @PostConstruct
    public void init() {
        if (securityContext.getUserPrincipal() != null &&
            securityContext.getUserPrincipal() instanceof Authentication &&
            ((Authentication)securityContext.getUserPrincipal()).getDetails() instanceof HilatsUserDetails) {
            user = ((HilatsUserDetails)((Authentication)securityContext.getUserPrincipal()).getDetails()).getUser();
        };
    }

    public HilatsUser getUser() {
        return user;
    }
}
