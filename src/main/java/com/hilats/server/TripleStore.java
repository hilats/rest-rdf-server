package com.hilats.server;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by pduchesne on 1/08/14.
 */
public interface TripleStore {

    public static String DEFAULT_SPARQL_QUERY = "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o }";

    public abstract void addStatements(Collection statements);

    public abstract void addStatements(InputStream in, String mimeType);

    public abstract StreamingOutput removeStatements(String sparql, String mimetype, Map config);

    public abstract Object getStatements(String sparql);

    public abstract StreamingOutput getStatementsStreamer(String sparql, String mimeType, Map config);

    public abstract RepoConnectionFactory getRepoConnectionFactory();
}
