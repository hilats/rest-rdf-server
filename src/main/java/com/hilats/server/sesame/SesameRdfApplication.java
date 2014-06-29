package com.hilats.server.sesame;

import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

    SesameConnectionFactory connFactory;

    public SesameRdfApplication(SesameConnectionFactory connFactory) throws RepositoryException {

        this.connFactory = connFactory;

        connFactory.getRepository().initialize();
    }

    public void initWithData() throws RepositoryException, IOException, RDFParseException {
        RepositoryConnection con = (RepositoryConnection)connFactory.getCurrentConnection().getOriginalConnection();
        con.add(this.getClass().getResourceAsStream("/annotations/example1.turtle"), null, RDFFormat.TURTLE);
    }


    @Override
    public void addStatements(InputStream in, String mimeType) {
        RepositoryConnection con = (RepositoryConnection)connFactory.getCurrentConnection().getOriginalConnection();
        try {
            // use "http://localhost/test" for turtle ?
            con.add(in, "http://localhost/test", RDFFormat.forMIMEType(mimeType));
            con.commit();
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
        RepositoryConnection con = (RepositoryConnection)connFactory.getCurrentConnection().getOriginalConnection();
        try {

            GraphQueryResult graphResult = con.prepareGraphQuery(
                    QueryLanguage.SPARQL, queryString).evaluate();

            return graphResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RepoConnectionFactory getRepoConnectionFactory() {
        return connFactory;
    }
}
