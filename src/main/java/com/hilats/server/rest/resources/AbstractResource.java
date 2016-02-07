package com.hilats.server.rest.resources;

import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnection;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

/**
 * Created by pduchesne on 28/06/14.
 */
public class AbstractResource {

    @Context
    SecurityContext securityContext;

    @Context
    RdfApplication app;

    public RdfApplication getApplication() {
        return app;
    }

}
