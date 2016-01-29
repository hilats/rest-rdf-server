package com.hilats.server.sesame;

import com.hilats.server.RepoConnection;
import com.hilats.server.RepoConnectionFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Created by pduchesne on 29/06/14.
 */
public class SesameConnectionFactory
    implements RepoConnectionFactory
{
    Repository repo;
    RepositoryConnection currentconnection;

    public SesameConnectionFactory(Repository repo) {
        this.repo = repo;
    }

    @Override
    public RepoConnection getCurrentConnection() {
        try {
            if (!isConnectionActive()) {
                currentconnection = repo.getConnection();
            }
            return new RepoConnection(currentconnection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open connection", e);
        }
    }

    @Override
    public void closeCurrentConnection() {
        try {
            if (isConnectionActive()) {
                currentconnection.commit();
                currentconnection.close();
                currentconnection = null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close connection", e);
        }
    }

    @Override
    public boolean isConnectionActive() {
        try {
            return currentconnection != null && currentconnection.isActive();
        } catch (Exception e) {
            throw new RuntimeException("Failed connection", e);
        }
    }

    public Repository getRepository() {
        return repo;
    }
}
