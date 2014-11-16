package com.hilats.server.test;

import com.hilats.server.Main;
import junit.framework.Assert;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public abstract class AbstractResourceTest {

    protected HttpServer server;
    protected WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = setupServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
    }

    public HttpServer setupServer() {
        return Main.startServer(URI.create(Main.BASE_URI));
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    public Response putWithSuccess(String path, Entity content) {
        Response putResponse = target.path(path).request().put(content);
        org.junit.Assert.assertEquals("Wrong HTTP status message : " + putResponse.getStatus() + "\n" + putResponse.getStatusInfo().getReasonPhrase(), Response.Status.Family.SUCCESSFUL, putResponse.getStatusInfo().getFamily());

        return putResponse;
    }

    public Response getWithSuccess(String path) {
        Response getResponse = target.path(path).request().get();
        org.junit.Assert.assertEquals("Wrong HTTP status message : " + getResponse.getStatus() + "\n" + getResponse.getStatusInfo().getReasonPhrase(), Response.Status.Family.SUCCESSFUL, getResponse.getStatusInfo().getFamily());

        return getResponse;
    }

    public Response deleteWithSuccess(String path) {
        Response deleteResponse = target.path(path).request().delete();
        org.junit.Assert.assertEquals("Wrong HTTP status message : " + deleteResponse.getStatus() + "\n" + deleteResponse.getStatusInfo().getReasonPhrase(), Response.Status.Family.SUCCESSFUL, deleteResponse.getStatusInfo().getFamily());

        return deleteResponse;
    }

    public Response getWithStatus(String path, int status) {
        Response getResponse = target.path(path).request().get();
        org.junit.Assert.assertEquals("Wrong HTTP status message : " + getResponse.getStatus(), status, getResponse.getStatus());

        return getResponse;
    }

}
