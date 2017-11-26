package com.hilats.server.rest.resources;

import org.springframework.security.core.userdetails.User;

import java.text.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
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
    @RolesAllowed("admin")
    public Response getAllUsers(@Context HttpServletRequest request) throws ParseException {

        return Response.ok().entity(this.getApplication().getUserService().getUsers()).build();
    }

    @GET
    @Path("/current")
    @RolesAllowed("user")
    public Response getCurrentUser(@Context HttpServletRequest request) throws ParseException {

        return Response.ok().entity(this.securityContext.getUserPrincipal()).build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("admin")
    public Response getUser(@PathParam("id") String id, @Context HttpServletRequest request) throws ParseException {

        return Response.ok().entity(this.getApplication().getUserService().findUser(id)).build();
    }


    @PUT
    @RolesAllowed("user")
    public Response updateUser(@Valid User user, @Context HttpServletRequest request) throws ParseException {

        if (this.securityContext.isUserInRole("admin") ||
                this.securityContext.getUserPrincipal().getName().equals(user.getUsername())) {
            //TODO
            return Response.ok().build();
        } else {
            return Response.status(Status.FORBIDDEN).build();
        }

    }

    @Path("/{id}/social/{provider}")
    public Class<? extends SocialAccountResource> getSocialAccount(@PathParam("provider") String provider) {
        switch (provider) {
            case "twitter":
                return SocialAccountResource.TwitterAccountResource.class;
            case "google":
                return SocialAccountResource.GoogleAccountResource.class;
            case "facebook":
                return SocialAccountResource.FacebookAccountResource.class;
            default:
                return SocialAccountResource.class;
        }
    }


}