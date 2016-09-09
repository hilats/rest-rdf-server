package com.hilats.server.rest.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * SPARQL query resource
 */
@Path("query")
public class RdfResource
    extends AbstractResource
{

    @Context
    Request request;

    @PUT
    @Consumes({"application/ld+json", MediaType.APPLICATION_JSON})
    public void putJsonLd(InputStream jsonld) {

        getApplication().getStore().addStatements(jsonld, "application/ld+json");

    }

    @PUT
    @Consumes({"application/rdf+xml", MediaType.APPLICATION_XML})
    public void putXml(InputStream xml) {

        getApplication().getStore().addStatements(xml, "application/rdf+xml");

    }

    @PUT
    @Consumes({"text/turtle"})
    public void putTurtle(InputStream turtle) {

        getApplication().getStore().addStatements(turtle, "text/turtle");

    }


    @DELETE
    public StreamingOutput delete(@QueryParam("sparql") String sparql) {
        List<Variant> variants = Variant.mediaTypes(
                MediaType.valueOf("application/ld+json"),
                MediaType.valueOf("application/rdf+xml"),
                MediaType.valueOf("text/turtle")).build();
        Variant selected = request.selectVariant(variants);
        if (selected == null) {
            throw new WebApplicationException(Response.notAcceptable(variants).build());
        }
        return getApplication().getStore().removeStatements(sparql, selected.getMediaType().toString(), null);

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

    @GET
    @Produces({ "text/turtle" })
    public StreamingOutput getTurtle(@QueryParam("sparql") String sparql) {

        return getApplication().getStore().getStatementsStreamer(sparql, "text/turtle", null);
    }

}
