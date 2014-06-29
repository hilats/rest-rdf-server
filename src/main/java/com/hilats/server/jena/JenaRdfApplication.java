package com.hilats.server.jena;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hilats.server.ExceptionHandler;
import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by pduchesne on 24/04/14.
 */
public class JenaRdfApplication
    extends RdfApplication
{
    Model model;

    public JenaRdfApplication() {
        super();

        model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("/annotations/example1.ttl"), null, "TURTLE");

        JenaJSONLD.init();
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public StreamingOutput getStatements(String sparql, final String mimeType) {
        final Model result = sparql != null ?
                getTupleSet(sparql) :
                model;

        return new StreamingOutput() {
            public void write(OutputStream output) {
                result.write(output, RDFLanguages.contentTypeToLang(mimeType).getName());
            }
        };
    }

    public Model getTupleSet(String queryString) {

        Query query = QueryFactory.create(queryString) ;
        QueryExecution qe = QueryExecutionFactory.create(query, model) ;

        return qe.execConstruct();
    }

    @Override
    public RepoConnectionFactory getRepoConnectionFactory() {
        //TODO
        return null;
    }

    /*
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJsonLd() throws RDFHandlerException {

        Repository repo = new SailRepository( new MemoryStore(dataDir) );
        repo.initialize();

        Iterable<Statement> statements = ;

        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) {
                Rio.write(statements, os, RDFFormat.JSONLD);
            }
        };
        return Response.ok(stream).build();

        return  Response.ok(stream, RDFFormat.JSONLD.getDefaultMIMEType()).build();
    }
    */
}
