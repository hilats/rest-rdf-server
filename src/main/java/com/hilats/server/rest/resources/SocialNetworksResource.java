package com.hilats.server.rest.resources;

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
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Taken from https://github.com/sahat/satellizer/blob/master/examples/server/java/src/main/java/com/example/helloworld/resources/AuthResource.java
 */

@Path("/social")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SocialNetworksResource
    extends AbstractResource
{

    final Client client = new JerseyClientBuilder().build();


    public SocialNetworksResource() {
        client.register(JacksonFeature.class);
    }

   /*
    @POST
    @Path("twitter/profile")
    public Response twitterProfile() {


        Response response;

        Map<String,String> twitterConf = getApplication().getConfig().authProviders.get("twitter");
        if (twitterConf == null)
            return Response.serverError().status(Status.BAD_REQUEST).entity("OAuth provider not found: twitter").build();

        final OAuth10aService service = new ServiceBuilder()
                .apiKey(twitterConf.get("client_id"))
                .apiSecret(twitterConf.get("client_secret"))
                .build(TwitterApi.instance());


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
    */



}