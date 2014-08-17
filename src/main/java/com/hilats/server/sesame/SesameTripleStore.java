package com.hilats.server.sesame;

import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import com.hilats.server.TripleStore;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.RDFHandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by pduchesne on 24/04/14.
 */

public class SesameTripleStore
    implements TripleStore

{
    SesameConnectionFactory connFactory;

    protected SesameTripleStore(SesameConnectionFactory connFactory) throws RepositoryException {

        this.connFactory = connFactory;
        connFactory.getRepository().initialize();
    }


    @Override
    public void addStatements(Collection statements) {
        RepositoryConnection con = getSesameConnection();
        try {
            RDFHandlerWrapper rdfInserter = new Skolemizer(new RDFInserter(con)); //TODO still needed here ?
            for (Statement s: (Collection<Statement>)statements) rdfInserter.handleStatement(s);
            //con.add((Collection<Statement>) statements);
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        }
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        RepositoryConnection con = getSesameConnection();
        try {
            // use "http://localhost/test" for turtle ?
            con.add(in, "http://localhost/test", RDFFormat.forMIMEType(mimeType));
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        }
    }

    @Override
    public StreamingOutput getStatementsStreamer(String sparql, String mimetype, Map config) {
        GraphQueryResult graph = getGraph(sparql);
        return SesameStreamingOutput.createStreamer(graph, RDFFormat.forMIMEType(mimetype), config);
    }

    @Override
    public Object getStatements(String sparql) {
        try {
            return QueryResults.asModel(getGraph(sparql));
        } catch (QueryEvaluationException e) {
            throw new RuntimeException("Failed to query repo", e);
        }
    }

    public GraphQueryResult getGraph(String queryString) {
        RepositoryConnection con = getSesameConnection();
        try {

            GraphQueryResult graphResult = con.prepareGraphQuery(
                    QueryLanguage.SPARQL, (queryString!=null && queryString.length()>0)?queryString:DEFAULT_SPARQL_QUERY).evaluate();

            return graphResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RepositoryConnection getSesameConnection() {
        return (RepositoryConnection)connFactory.getCurrentConnection().getOriginalConnection();
    }

    @Override
    public RepoConnectionFactory getRepoConnectionFactory() {
        return connFactory;
    }
}
