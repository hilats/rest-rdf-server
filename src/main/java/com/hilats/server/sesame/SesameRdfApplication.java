package com.hilats.server.sesame;

import com.hilats.server.RdfApplication;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pduchesne on 24/04/14.
 */

public class SesameRdfApplication
    extends RdfApplication

{
    private Logger log = LoggerFactory.getLogger(SesameRdfApplication.class);

    public static String DEFAULT_SPARQL_QUERY = "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o }";

    Repository repo;

    public SesameRdfApplication(Repository repo) throws RepositoryException {
        this.repo = repo;

        repo.initialize();
    }

    public void initWithData() throws RepositoryException, IOException, RDFParseException {
        RepositoryConnection con = repo.getConnection();
        try {
            con.add(this.getClass().getResourceAsStream("/annotations/example1.turtle"), null, RDFFormat.TURTLE);
        }
        finally {
            con.close();
        }
    }

    public Repository getRepo() {
        return repo;
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        try {
            // use "http://localhost/test" for turtle ?
            repo.getConnection().add(in, null, RDFFormat.forMIMEType(mimeType));
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert RDF stream", e);
        }
    }

    @Override
    public StreamingOutput getStatements(String sparql, String mimetype) {
        GraphQueryResult graph = getGraph(sparql!=null?sparql:DEFAULT_SPARQL_QUERY);
        return SesameStreamingOutput.createStreamer(graph, RDFFormat.forMIMEType(mimetype));
    }


    public GraphQueryResult getGraph(String queryString) {
        RepositoryConnection con = null;
        try {
            con = repo.getConnection();

            GraphQueryResult graphResult = con.prepareGraphQuery(
                    QueryLanguage.SPARQL, queryString).evaluate();

            return graphResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (con != null) try {con.close();} catch (RepositoryException e) {log.warn("Failed to close connection", e);}
        }
    }
}
