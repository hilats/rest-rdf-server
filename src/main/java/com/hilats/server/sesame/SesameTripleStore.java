package com.hilats.server.sesame;

import com.hilats.server.AbstractTripleStore;
import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import com.hilats.server.TripleStore;
import org.openrdf.model.Model;
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
    extends AbstractTripleStore

{
    protected SesameTripleStore(SesameConnectionFactory connFactory) throws RepositoryException {
        super(connFactory);
        connFactory.getRepository().initialize();
    }

    public void addModelStatements(Model statements) {
        addStatements(statements);
    }

    @Override
    public void addStatements(Iterable<Statement> statements) {
        RepositoryConnection con = getSesameConnection();
        try {
            con.begin();
            //RDFHandlerWrapper rdfInserter = new Skolemizer(new RDFInserter(con)); //TODO still needed here ?
            //for (Statement s: (Collection<Statement>)statements) rdfInserter.handleStatement(s);
            con.add(statements);
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        }  finally {
            try {
                if (con.isActive())
                    con.rollback();
            } catch (Exception e) {
                throw new RuntimeException("Failed to roll back transaction", e);
            }
        }
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        RepositoryConnection con = getSesameConnection();
        try {
            con.begin();

            // use "http://localhost/test" for turtle ?
            con.add(in, "http://localhost/test", RDFFormat.forMIMEType(mimeType));
            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        } finally {
            try {
                if (con.isActive())
                    con.rollback();
            } catch (Exception e) {
                throw new RuntimeException("Failed to roll back transaction", e);
            }
        }
    }

    @Override
    public StreamingOutput removeStatements(String sparql, String mimetype, Map config) {
        RepositoryConnection con = getSesameConnection();
        try {
            con.begin();
            GraphQueryResult graphResult = con.prepareGraphQuery(
                    QueryLanguage.SPARQL, (sparql!=null && sparql.length()>0)?sparql:DEFAULT_SPARQL_QUERY).evaluate();

            con.remove(graphResult);
            con.commit();

            return SesameStreamingOutput.createStreamer(graphResult, RDFFormat.forMIMEType(mimetype), config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        } finally {
            try {
                if (con.isActive())
                    con.rollback();
            } catch (Exception e) {
                throw new RuntimeException("Failed to roll back transaction", e);
            }
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
        return (RepositoryConnection)getRepoConnectionFactory().getCurrentConnection().getOriginalConnection();
    }

    @Override
    public boolean isEmpty() {
        try {
            return getSesameConnection().isEmpty();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clean() {
        try {
            getSesameConnection().clear();
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
}
