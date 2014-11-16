package com.hilats.server;

import com.hilats.server.sesame.SesameConnectionFactory;

/**
 * Created by pduchesne on 14/10/14.
 */
public abstract class AbstractTripleStore
    implements TripleStore
{
    RepoConnectionFactory connFactory;

    protected AbstractTripleStore(RepoConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    @Override
    public RepoConnectionFactory getRepoConnectionFactory() {
        return connFactory;
    }
}
