package com.hilats.server.test;

import com.hilats.server.Main;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import static org.junit.Assert.assertEquals;

public class MosaicResourceTest
    extends AbstractResourceTest
{


    @Test
    public void testGetJsonld() {
        String responseMsg = target.path("mosaics.jsonld").request().get(String.class);
        System.out.print(responseMsg);
    }

}
