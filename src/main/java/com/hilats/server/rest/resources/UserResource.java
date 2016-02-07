package com.hilats.server.rest.resources;

import com.hilats.server.spring.jwt.TokenHandler;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.User;

import java.text.ParseException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource
    extends AbstractResource
{

    @GET
    public Response getUser(@Context HttpServletRequest request) throws ParseException {

        return Response.ok().entity(this.securityContext.getUserPrincipal()).build();
    }


    @PUT
    public Response updateUser(@Valid User user, @Context HttpServletRequest request) throws ParseException {

        return Response.ok().build();
    }


}