package com.hilats.server.rest.resources;

import com.hilats.server.spring.jwt.*;
import com.hilats.server.spring.jwt.services.AuthProfile;
import com.hilats.server.spring.jwt.services.FacebookProfile;
import com.hilats.server.spring.jwt.services.GoogleProfile;
import com.hilats.server.spring.jwt.services.LinkedinProfile;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.hibernate.validator.constraints.NotBlank;
import org.jvnet.hk2.annotations.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Taken from https://github.com/sahat/satellizer/blob/master/examples/server/java/src/main/java/com/example/helloworld/resources/AuthResource.java
 */

@Path("/server")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServerResource
    extends AbstractResource
{
    @GET
    @Path("config")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getConfig()
    {
        return getApplication().getConfig().webappConfig;
    }

    @POST
    @Path("exec")
    @RolesAllowed("admin")
    public Object execute(@QueryParam("action") String action) throws Exception {
        if ("clean".equals(action)) {
            getApplication().getStore().clean();
        } else if ("initdata".equals(action)) {
            getApplication().initData();
        } else {
            throw new WebApplicationException("Unknown action: "+action, 500);
        }

        return null;
    }
}