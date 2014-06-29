package com.hilats.server;

/**
 * Created by pduchesne on 29/06/14.
 */
public class RepoConnection {

    Object originalConnection;

    public RepoConnection(Object originalConnection) {
        this.originalConnection = originalConnection;
    }

    public Object getOriginalConnection() {
        return originalConnection;
    }
}
