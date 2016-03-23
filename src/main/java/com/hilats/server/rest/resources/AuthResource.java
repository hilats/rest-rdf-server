package com.hilats.server.rest.resources;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.hilats.server.spring.jwt.*;

import com.hilats.server.spring.jwt.services.AuthProfile;
import com.hilats.server.spring.jwt.services.FacebookProfile;
import com.hilats.server.spring.jwt.services.GoogleProfile;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.hibernate.validator.constraints.NotBlank;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/**
 * Taken from https://github.com/sahat/satellizer/blob/master/examples/server/java/src/main/java/com/example/helloworld/resources/AuthResource.java
 */

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource
    extends AbstractResource
{

    final Client client = new JerseyClientBuilder().build();

    public static final String CLIENT_ID_KEY = "client_id", REDIRECT_URI_KEY = "redirect_uri",
            CLIENT_SECRET = "client_secret", CODE_KEY = "code", GRANT_TYPE_KEY = "grant_type",
            AUTH_CODE = "authorization_code";


    public AuthResource() {
        client.register(JacksonFeature.class);
    }

    @GET
    public Response getCurrentAuth(@Context final HttpServletRequest request)
    {
        TokenAuthenticationService tokenService = getApplication().getTokenService();

        // this takes the currently authenticated user, if any
        UserAuthentication auth = tokenService.getAuthentication(request);

        return Response.ok().entity(auth).build();
    }

    @POST
    @Path("login")
    public Response login(@Valid final User user, @Context final HttpServletRequest request)
    {
        //TODO
        return Response.status(Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("signup")
    public Response signup(@Valid final User user, @Context final HttpServletRequest request)
    {
        //TODO
        return Response.status(Status.UNAUTHORIZED).build();
    }




    @POST
    @Path("facebook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginFacebook(@Valid final Payload payload,
                                  @Context final HttpServletRequest request) throws ParseException {
        final String accessTokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token";
        final String graphApiUrl = "https://graph.facebook.com/v2.3/me";

        Response response;

        // Step 1. Exchange authorization code for access token.

        response =
                client.target(accessTokenUrl)
                        .queryParam("scope", "email,public_profile")
                        .queryParam(CLIENT_ID_KEY, payload.getClientId())
                        .queryParam(REDIRECT_URI_KEY, payload.getRedirectUri())
                        .queryParam(CLIENT_SECRET, "6609f0aec8aacf71ea5f7ffca1694dd1")
                        .queryParam(CODE_KEY, payload.getCode())
                        .request("text/plain")
                        .accept(MediaType.TEXT_PLAIN).get();

        Map<String, Object> credentials = response.readEntity(Map.class);

        response =
                client.target(graphApiUrl)
                        .queryParam("fields", "id,name,email,picture")
                        .queryParam("access_token", credentials.get("access_token"))
                        .queryParam("expires_in", credentials.get("expires_in"))
                        .request("application/json")
                        .get();

        final Map<String, Object> userInfo = response.readEntity(Map.class);

        FacebookProfile profile = new FacebookProfile(
                credentials,
                userInfo);

        // Step 3. Process the authenticated the user.
        return processUser(request, profile);
    }


    @POST
    @Path("google")
    public Response loginGoogle(@Valid final Payload payload,
                                @Context final HttpServletRequest request) throws ParseException, IOException {
        final String accessTokenUrl = "https://accounts.google.com/o/oauth2/token";
        final String peopleApiUrl = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";
        Response response;

        // Step 1. Exchange authorization code for access token.
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<String, String>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, "o8ZuNNP56rap-io6DrwgxJwf" /* secrets.getGoogle() */);
        accessData.add(CODE_KEY, payload.getCode());
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);
        response = client.target(accessTokenUrl).request().post(Entity.form(accessData));
        Map<String, Object> credentials = response.readEntity(Map.class);
        accessData.clear();


        // Step 2. Retrieve profile information about the current user.
        final String accessToken = (String) credentials.get("access_token");
        response =
                client.target(peopleApiUrl)
                      .request("text/plain")
                      .header("Authorization", String.format("Bearer %s", accessToken))
                      .get();

        final Map<String, Object> userInfo = response.readEntity(Map.class);

        GoogleProfile profile = new GoogleProfile(
                credentials,
                userInfo);

        // Step 3. Process the authenticated the user.

        return processUser(request, profile);

    }



    /*
     * Inner classes for entity wrappers
     */
    public static class Payload {
        @NotBlank
        String clientId;

        @NotBlank
        String redirectUri;

        @NotBlank
        String code;

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getCode() {
            return code;
        }
    }


    private Response processUser(final HttpServletRequest request, AuthProfile profile) throws ParseException {

        TokenAuthenticationService tokenService = getApplication().getTokenService();
        HilatsUserService userService = getApplication().getUserService();


        // this takes the currently authenticated user, if any
        Authentication auth = tokenService.getAuthentication(request);

        HilatsUser user;

        if (auth != null) {

            final String subject = auth.getName();
            user = userService.findUser(subject);

            if (user == null)
                //How can this happen?
                throw new IllegalStateException("user not found: "+subject);

        } else {
            // Step 3b. Create a new user account or return an existing one.

            user = userService.findUser(profile.getEmail());
            if (user != null) {
                // OK
            } else {
                user = new HilatsUser(
                        profile.getEmail(),
                        UUID.randomUUID().toString(),
                        new String[] {"user"});

                userService.addUser(user);
            }
        }

        if (! user.getProviderProfiles().containsKey(profile.getProvider())) {
            // This auth provider is not yet registered for this user --> add it
            //TODO ask confirmation ?
            user.getProviderProfiles().put(profile.getProvider(), profile);
            userService.saveUser(user);
        }

        // Login that user
        UserAuthentication authentication = new UserAuthentication(new HilatsUserDetails(user));

        String token = tokenService.createToken(authentication);
        //final Token token = AuthUtils.createToken(request.getRemoteHost(), userToSave.getId());
        Map result = new HashMap();
        result.put("token", token);
        return Response.ok().entity(result).build();
    }

}