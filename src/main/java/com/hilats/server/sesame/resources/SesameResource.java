package com.hilats.server.sesame.resources;

import com.hilats.server.sesame.SesameRdfApplication;
import com.hilats.server.sesame.SesameStreamingOutput;
import org.openrdf.query.*;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class SesameResource {

    public static String DEFAULT_SPARQL_QUERY = "CONSTRUCT {?s ?p ?o } WHERE {?s ?p ?o }";

    @Context
    SecurityContext securityContext;

    @Context
    Application app;


    @PUT
    @Consumes({"application/ld+json", MediaType.APPLICATION_JSON})
    public void putJsonLd(InputStream jsonld) throws RepositoryException, IOException, RDFParseException {

        ((SesameRdfApplication)app).getRepo().getConnection().add(jsonld, null, RDFFormat.JSONLD);

    }

    @PUT
    @Consumes({"text/turtle"})
    public void putTurtle(InputStream turtle) throws RepositoryException, IOException, RDFParseException {

        ((SesameRdfApplication)app).getRepo().getConnection().add(turtle, "http://localhost/test", RDFFormat.TURTLE);

    }


    @GET
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public StreamingOutput getJsonLd(@QueryParam("sparql") String sparql) throws RepositoryException {

        GraphQueryResult graph = getGraph(sparql!=null?sparql:DEFAULT_SPARQL_QUERY);
        return SesameStreamingOutput.createStreamer(graph, RDFFormat.JSONLD);

    }


    @GET
    @Produces({ "application/rdf+xml", MediaType.APPLICATION_XML })
    public StreamingOutput getData(@QueryParam("sparql") String sparql) {

        GraphQueryResult graph = getGraph(sparql!=null?sparql:DEFAULT_SPARQL_QUERY);
        return SesameStreamingOutput.createStreamer(graph, RDFFormat.RDFXML);
    }



    public GraphQueryResult getGraph(String queryString) {
        Repository repo = ((SesameRdfApplication)app).getRepo();
        try {
            RepositoryConnection con = repo.getConnection();

            GraphQueryResult graphResult = con.prepareGraphQuery(
                    QueryLanguage.SPARQL, queryString).evaluate();

            return graphResult;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
