package com.hilats.server.rest.resources;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.hilats.server.spring.jwt.TestUserService;
import com.hilats.server.spring.jwt.TokenAuthenticationService;
import com.hilats.server.spring.jwt.UserAuthentication;

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
        Map<String, Object> jsonResp = response.readEntity(Map.class);
        accessData.clear();


        // Step 2. Retrieve profile information about the current user.
        final String accessToken = (String) jsonResp.get("access_token");
        response =
                client.target(peopleApiUrl)
                      .request("text/plain")
                      .header("Authorization", String.format("Bearer %s", accessToken))
                      .get();

        final Map<String, Object> userInfo = response.readEntity(Map.class);


        // Step 3. Process the authenticated the user.

        return processUser(request, userInfo,
                userInfo.get("name").toString());

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


    private Response processUser(final HttpServletRequest request,
                                 Map<String,Object> userInfo, final String displayName) throws ParseException {

        TokenAuthenticationService tokenService = getApplication().getTokenService();
        TestUserService userService = getApplication().getUserService();


        String userId = userInfo.get("sub").toString();
        User user = userService.loadUserByUsername(userId);

        // Step 3a. If user is already signed in then link accounts.
        //final String authHeader = request.getHeader("Authorization");


        Authentication auth = tokenService.getAuthentication(request);

        if (auth != null) {

            final String subject = auth.getName();
            final User foundUser = userService.loadUserByUsername(subject);
            //TODO check null

        } else {
            // Step 3b. Create a new user account or return an existing one.


            user = new User(
                    (String)userInfo.get("email"),
                    UUID.randomUUID().toString(),
                    AuthorityUtils.createAuthorityList("user")
            );
            userService.addUser(user);

            /*
            if (user.isPresent()) {
                userToSave = user.get();
            } else {
                userToSave = new User();
                userToSave.setProviderId(provider, id);
                userToSave.setDisplayName(displayName);
                userToSave = dao.save(userToSave);
            }
            */
        }

        // Login that user
        UserAuthentication authentication = new UserAuthentication(user);

        String token = tokenService.createToken(authentication);
        //final Token token = AuthUtils.createToken(request.getRemoteHost(), userToSave.getId());
        Map result = new HashMap();
        result.put("token", token);
        return Response.ok().entity(result).build();
    }

}