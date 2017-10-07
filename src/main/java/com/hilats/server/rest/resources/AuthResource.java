package com.hilats.server.rest.resources;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.hilats.server.spring.jwt.*;

import com.hilats.server.spring.jwt.services.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.hibernate.validator.constraints.NotBlank;

import org.jvnet.hk2.annotations.Optional;
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

    public static final String CLIENT_ID_KEY = "client_id",
                               REDIRECT_URI_KEY = "redirect_uri",
                               CLIENT_SECRET = "client_secret",
                               CODE_KEY = "code",
                               GRANT_TYPE_KEY = "grant_type",
                               AUTH_CODE = "authorization_code";


    public AuthResource() {
        client.register(JacksonFeature.class);
    }

    @GET
    @Path("providers")
    @Produces(MediaType.APPLICATION_JSON)
    public Object getAuthProviders(@Context final HttpServletRequest request)
    {
        Map providers =
                getApplication().getConfig().authProviders
                .entrySet().stream()
                .collect (
                        Collectors.toMap(
                                Map.Entry::getKey,
                                providerConf -> providerConf.getValue().entrySet().stream()
                                        .filter(e -> e.getKey().equals("client_id"))
                                        .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        providerConfItem -> providerConfItem.getValue())
                                        ))
                );

        return providers;
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
    @Path("linkedin")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginLinkedIn(@Valid final Payload payload,
                                  @Context final HttpServletRequest request) throws ParseException {
        final String accessTokenUrl = "https://www.linkedin.com/uas/oauth2/accessToken";
        final String peopleApiUrl = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,picture-url)";

        Map<String,String> linkedinConf = getApplication().getConfig().authProviders.get("linkedin");
        if (linkedinConf == null)
            return Response.serverError().status(Status.BAD_REQUEST).entity("OAuth provider not found: linkedin").build();


        Response response;

        // Step 1. Exchange authorization code for access token.

        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<String, String>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, linkedinConf.get("client_secret"));
        accessData.add(CODE_KEY, payload.getCode());
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);
        response = client.target(accessTokenUrl).request().post(Entity.form(accessData));
        Map<String, Object> credentials = response.readEntity(Map.class);
        accessData.clear();


        final String accessToken = (String) credentials.get("access_token");
        response =
                client.target(peopleApiUrl)
                        .queryParam("oauth2_access_token", accessToken)
                        .queryParam("format", "json")
                        .request("text/plain")
                        .get();

        final Map<String, Object> userInfo = response.readEntity(Map.class);

        LinkedinProfile profile = new LinkedinProfile(
                credentials,
                userInfo);

        // Step 3. Process the authenticated the user.
        return processUser(request, profile);
    }



    @POST
    @Path("facebook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginFacebook(@Valid final Payload payload,
                                  @Context final HttpServletRequest request) throws ParseException {
        final String accessTokenUrl = "https://graph.facebook.com/v2.3/oauth/access_token";
        final String graphApiUrl = "https://graph.facebook.com/v2.3/me";

        Map<String,String> facebookConf = getApplication().getConfig().authProviders.get("facebook");
        if (facebookConf == null)
            return Response.serverError().status(Status.BAD_REQUEST).entity("OAuth provider not found: facebook").build();


        Response response;

        // Step 1. Exchange authorization code for access token.

        response =
                client.target(accessTokenUrl)
                        .queryParam("scope", "email,public_profile")
                        .queryParam(CLIENT_ID_KEY, payload.getClientId())
                        .queryParam(REDIRECT_URI_KEY, payload.getRedirectUri())
                        .queryParam(CLIENT_SECRET, facebookConf.get("client_secret"))
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

        Map<String,String> googleConf = getApplication().getConfig().authProviders.get("google");
        if (googleConf == null)
            return Response.serverError().status(Status.BAD_REQUEST).entity("OAuth provider not found: google").build();

        // Step 1. Exchange authorization code for access token.
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<String, String>();
        accessData.add(CLIENT_ID_KEY, payload.getClientId());
        accessData.add(REDIRECT_URI_KEY, payload.getRedirectUri());
        accessData.add(CLIENT_SECRET, googleConf.get("client_secret"));
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


    @POST
    @Path("twitter")
    public Response loginTwitter(@Valid final OAuth1Payload payload,
                                 @Context final HttpServletRequest request) throws ParseException, IOException, ExecutionException, InterruptedException {
        final String profileUrl = "https://api.twitter.com/1.1/account/verify_credentials.json?include_email=true";

        Response response;

        Map<String,String> twitterConf = getApplication().getConfig().authProviders.get("twitter");
        if (twitterConf == null)
            return Response.serverError().status(Status.BAD_REQUEST).entity("OAuth provider not found: google").build();

        final OAuth10aService service = new ServiceBuilder()
                .apiKey(twitterConf.get("client_id"))
                .apiSecret(twitterConf.get("client_secret"))
                .callback(payload.getRedirectUri()==null?"oob":payload.getRedirectUri())
                .build(TwitterApi.instance());


        // Part 1 of 2: Initial request from Satellizer.
        if (payload.getOauth_token() == null || payload.getOauth_verifier() == null) {
            final OAuth1RequestToken requestToken = service.getRequestToken();

            final MultivaluedMap<String, String> oauth_token_response = new MultivaluedHashMap<String, String>();
            oauth_token_response.add("oauth_token", requestToken.getToken());
            oauth_token_response.add("oauth_token_secret", requestToken.getTokenSecret());

            return Response.ok().entity(oauth_token_response).build();
        } else {
            // Part 2 of 2: Second request after Authorize app is clicked.

            final OAuth1AccessToken accessToken = service.getAccessToken(new OAuth1RequestToken(payload.getOauth_token(), payload.getOauth_verifier()), payload.getOauth_verifier());

            final OAuthRequest profileRequest = new OAuthRequest(Verb.GET, profileUrl);
            service.signRequest(accessToken, profileRequest);
            final com.github.scribejava.core.model.Response profileResponse = service.execute(profileRequest);


            Map<String, Object> accessTokens = new HashMap<String, Object>();
            accessTokens.put("oauth_token", payload.getOauth_token());
            accessTokens.put("oauth_token_secret", payload.getOauth_verifier());

            Map<String,Object> profileInfo = new ObjectMapper().readValue(profileResponse.getBody(), HashMap.class);

            TwitterProfile profile = new TwitterProfile(
                    accessTokens,
                    profileInfo);

            // Step 3. Process the authenticated the user.

            return processUser(request, profile);
        }

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

        @Optional
        String state;

        public String getClientId() {
            return clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public String getCode() {
            return code;
        }

        public String getState() {
            return state;
        }
    }

    public static class OAuth1Payload {
        @NotBlank
        String clientId;

        @NotBlank
        String redirectUri;

        @Optional
        String name;

        @Optional
        String oauthType;

        @Optional
        String url;

        @Optional
        String authorizationEndpoint;

        @Optional
        Map popupOptions;

        @Optional
        String oauth_token;

        @Optional
        String oauth_verifier;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOauthType() {
            return oauthType;
        }

        public void setOauthType(String oauthType) {
            this.oauthType = oauthType;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getAuthorizationEndpoint() {
            return authorizationEndpoint;
        }

        public void setAuthorizationEndpoint(String authorizationEndpoint) {
            this.authorizationEndpoint = authorizationEndpoint;
        }

        public Map getPopupOptions() {
            return popupOptions;
        }

        public void setPopupOptions(Map popupOptions) {
            this.popupOptions = popupOptions;
        }

        public String getOauth_token() {
            return oauth_token;
        }

        public void setOauth_token(String oauth_token) {
            this.oauth_token = oauth_token;
        }

        public String getOauth_verifier() {
            return oauth_verifier;
        }

        public void setOauth_verifier(String oauth_verifier) {
            this.oauth_verifier = oauth_verifier;
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

            user = userService.findUserByEmail(profile.getEmail());
            if (user != null) {
                // OK
            } else {
                byte[] emailHash = DigestUtils.md5(profile.getEmail());
                long userId = ByteBuffer.wrap(Arrays.copyOfRange(emailHash, 0, 8)).getLong();
                user = new HilatsUser(
                        String.valueOf(userId),
                        UUID.randomUUID().toString(),
                        Arrays.asList("user"));

                // if new user matches default admin in config, add admin role
                if (profile.getEmail().equals(getApplication().getConfig().admin)) {
                    user.getRoles().add("admin");
                }

                user.email = profile.getEmail();
                user.displayName = profile.getDisplayName();
                user.pictureUrl = profile.getPictureUrl();

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