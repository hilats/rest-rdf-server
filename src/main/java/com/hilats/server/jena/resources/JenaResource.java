package com.hilats.server.jena.resources;

import com.hilats.server.jena.JenaRdfApplication;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Application;
import java.io.*;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class JenaResource {

    @Context
    Application app;

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public StreamingOutput getJsonLd(@QueryParam("sparql") String sparql) {

        Model model = ((JenaRdfApplication)app).getModel();

        final Model result = sparql != null ?
                getTupleSet(sparql) :
                model;

        return new StreamingOutput() {
            public void write(OutputStream output) {
                result.write(output, "JSON-LD");
            }
        };
    }


    @GET
    @Produces({ "application/rdf+xml", MediaType.APPLICATION_XML })
    public StreamingOutput getData(@QueryParam("sparql") String sparql) {

        Model model = ((JenaRdfApplication)app).getModel();

        final Model result = sparql != null ?
                getTupleSet(sparql) :
                model;

        return new StreamingOutput() {
            public void write(OutputStream output) {
                result.write(output, "RDF/XML");
            }
        };
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

    public Model getTupleSet(String queryString) {
        final Model model = ((JenaRdfApplication)app).getModel();

        Query query = QueryFactory.create(queryString) ;
        QueryExecution qe = QueryExecutionFactory.create(query, model) ;

        return qe.execConstruct();
    }
}
