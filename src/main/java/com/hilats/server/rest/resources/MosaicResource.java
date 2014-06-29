package com.hilats.server.rest.resources;

import com.hilats.server.RdfApplication;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("mosaics")
public class MosaicResource
    extends AbstractResource
{

    @PUT
    @Consumes({"application/ld+json", MediaType.APPLICATION_JSON})
    public void putJsonLd(InputStream jsonld) {

        getApplication().addStatements(jsonld, "application/ld+json");

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
