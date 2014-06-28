package com.hilats.server.rest.resources;

import com.hilats.server.RdfApplication;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class RdfResource {

    @Context
    SecurityContext securityContext;

    @Context
    Application app;


    public RdfApplication getApplication() {
        return ((RdfApplication)app);
    }

    @PUT
    @Consumes({"application/ld+json", MediaType.APPLICATION_JSON})
    public void putJsonLd(InputStream jsonld) {

        getApplication().addStatements(jsonld, "application/ld+json");

    }

    @PUT
    @Consumes({"text/turtle"})
    public void putTurtle(InputStream turtle) {

        getApplication().addStatements(turtle, "text/turtle");

    }


    @GET
    @Produces({"application/ld+json", MediaType.APPLICATION_JSON})
    public StreamingOutput getJsonLd(@QueryParam("sparql") String sparql) {

        return getApplication().getStatements(sparql, "application/ld+json");

    }


    @GET
    @Produces({ "application/rdf+xml", MediaType.APPLICATION_XML })
    public StreamingOutput getData(@QueryParam("sparql") String sparql) {

        return getApplication().getStatements(sparql, "application/rdf+xml");
    }

}
