package com.hilats.server;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import java.io.IOException;

/**
 * @deprecated RepoConnectionHk2Factory is used instead
 */
public class DBConnectionFilter
    implements ContainerRequestFilter, ContainerResponseFilter
{
    @Context
    RdfApplication app;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        RepoConnectionFactory connFac = app.getRepoConnectionFactory();
        if (connFac != null && !connFac.isConnectionActive()) {
            connFac.getCurrentConnection();
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        RepoConnectionFactory connFac = app.getRepoConnectionFactory();
        if (connFac != null) {
            connFac.closeCurrentConnection();
        }
    }
}
