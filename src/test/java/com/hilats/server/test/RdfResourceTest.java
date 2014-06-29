package com.hilats.server.test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.hilats.server.Main;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class RdfResourceTest extends AbstractResourceTest
{

    @Test
    public void testGetJsonld() {
        String responseMsg = target.path("myresource.jsonld").request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testGetJsonldWithQuery() {
        String sparql = "test";
        String responseMsg = target.path("myresource.jsonld").queryParam("sparql", sparql).request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testGetXML() {
        String responseMsg = target.path("myresource.xml").request().get(String.class);
        System.out.print(responseMsg);
    }

    @Test
    public void testPutTurtle() {
        Entity content = Entity.entity(this.getClass().getResourceAsStream("/annotations/example1.ttl"), "text/turtle") ;
        //Entity content = Entity.entity("test", "text/turtle") ;
        Response putResponse = target.path("myresource.ttl").request().put(content);
        Assert.assertTrue("Wrong HTTP status message : "+putResponse.getStatus(), putResponse.getStatus()/100 == 2);
        String getResponse = target.path("myresource.jsonld").request().get(String.class);
        System.out.print(getResponse);
    }
}
