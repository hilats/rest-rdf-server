package com.hilats.server.test;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

public class RdfResourceTest extends AbstractResourceTest
{

    @Test
    public void testGetJsonld() {
        String responseMsg = target.path("query.jsonld").request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testGetJsonldWithQuery() {
        String sparql = "CONSTRUCT { ?o1 ?s1 ?o2} WHERE { ?o1 ?s1 ?o2}";
        String responseMsg = target.path("query.jsonld").queryParam("sparql", "{sparql}").resolveTemplate("sparql", sparql).request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testGetXML() {
        String responseMsg = target.path("query.xml").request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testPutTurtle() {
        Entity content = Entity.entity(this.getClass().getResourceAsStream("/annotations/example1.ttl"), "text/turtle") ;
        //Entity content = Entity.entity("test", "text/turtle") ;
        Response putResponse = target.path("query.ttl").request().put(content);
        Assert.assertTrue("Wrong HTTP status message : "+putResponse.getStatus(), putResponse.getStatus()/100 == 2);
        String getResponse = target.path("query.jsonld").request().get(String.class);
        System.out.print(getResponse);
    }
}
