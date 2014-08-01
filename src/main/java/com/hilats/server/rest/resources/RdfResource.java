package com.hilats.server.rest.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class RdfResource
    extends AbstractResource
{

    @PUT
    @Consumes({"application/ld+json", MediaType.APPLICATION_JSON})
    public void putJsonLd(InputStream jsonld) {

        getApplication().getStore().addStatements(jsonld, "application/ld+json");

    }

    @PUT
    @Consumes({"text/turtle"})
    public void putTurtle(InputStream turtle) {

        getApplication().getStore().addStatements(turtle, "text/turtle");

    }


    @GET
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public StreamingOutput getJsonLd(@QueryParam("sparql") String sparql) {

        return getApplication().getStore().getStatementsStreamer(sparql, "application/ld+json", null);

    }


    @GET
    @Produces({ "application/rdf+xml", MediaType.APPLICATION_XML })
    public StreamingOutput getData(@QueryParam("sparql") String sparql) {

        return getApplication().getStore().getStatementsStreamer(sparql, "application/rdf+xml", null);
    }

}
