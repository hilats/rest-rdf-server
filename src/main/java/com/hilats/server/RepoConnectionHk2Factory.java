package com.hilats.server;

import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by pduchesne on 29/06/14.
 */
public class RepoConnectionHk2Factory
    implements Factory<RepoConnection>
{
    Logger log = LoggerFactory.getLogger(RepoConnectionHk2Factory.class);

    @Inject
    RepoConnectionFactory connFac;

    @Override
    public RepoConnection provide() {
        log.info("HK2 - Opening connection");
        return connFac.getCurrentConnection();
    }

    @Override
    public void dispose(RepoConnection instance) {
        log.info("HK2 - Closing connection");
        //TODO wrong - must close given connection
        connFac.closeCurrentConnection();
    }
}
