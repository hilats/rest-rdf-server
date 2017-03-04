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

    public void addStatements(Collection statements);

    public void addStatements(InputStream in, String mimeType);

    public StreamingOutput removeStatements(String sparql, String mimetype, Map config);

    public Object getStatements(String sparql);

    public StreamingOutput getStatementsStreamer(String sparql, String mimeType, Map config);

    public RepoConnectionFactory getRepoConnectionFactory();

    void clean();

    boolean isEmpty();
}
