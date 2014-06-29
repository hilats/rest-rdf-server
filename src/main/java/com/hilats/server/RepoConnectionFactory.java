package com.hilats.server;

/**
 * ConnectionFactories implement a strategy to create, initialize, cleanup and destroy Sesame's RepositoryConnections.
 */
public interface RepoConnectionFactory {

    /**
     * @return The connections that should be used in the caller's context.
     *
     * @throws
     */
    RepoConnection getCurrentConnection();

    /**
     * Close and dispose of a connection if one is open. Effectively a call to getCurrentConnection()
     * after closeCurrentConnection() will return a new RepositoryConnection. Uncommited changes to
     * the underlying store will be lost.
     */
    void closeCurrentConnection();

    boolean isConnectionActive();

}

