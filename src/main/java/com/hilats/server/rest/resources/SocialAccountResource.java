package com.hilats.server.rest.resources;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.hilats.server.spring.jwt.HilatsUser;
import com.hilats.server.spring.jwt.services.AuthProfile;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author pduchesne
 *         Created by pduchesne on 23/10/17.
 */
public class SocialAccountResource
        extends AbstractResource
{
    final Client client = new JerseyClientBuilder().build();

    protected String targetUserId;
    protected String provider;

    protected HilatsUser user;

    public SocialAccountResource(@PathParam("id") String userId, @PathParam("provider") String provider) {
        super();

        this.targetUserId = userId;
        this.provider = provider;
    }

    @PostConstruct
    public void fetchUserProvider() {
        this.user = this.getApplication().getUserService().findUser(targetUserId);

        if (! (this.securityContext.isUserInRole("admin") ||
                this.securityContext.getUserPrincipal().getName().equals(user.getUsername()))) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        if (!user.getProviderProfiles().containsKey(provider)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @DELETE
    @RolesAllowed("user")
    public Response deleteSocialAccount(@PathParam("id") String id, @PathParam("provider") String provider, @Context HttpServletRequest request) throws ParseException {

        if (user.getProviderProfiles().containsKey(provider)) {
            //TODO de-register from oauth provider
            user.getProviderProfiles().remove(provider);
            getApplication().getUserService().saveUser(user);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/sign")
    @RolesAllowed("user")
    public Response signSocialRequest(@QueryParam("request") String requestString)
            throws ParseException, InterruptedException, ExecutionException, IOException
    {
        throw new WebApplicationException(Response.Status.NOT_IMPLEMENTED);
    }



    public static class GoogleAccountResource
            extends SocialAccountResource {

        public GoogleAccountResource(@PathParam("id") String userId, @PathParam("provider") String provider) {
            super(userId, provider);
        }

        @Override
        public Response signSocialRequest(String requestString) throws ParseException, InterruptedException, ExecutionException, IOException {

            AuthProfile authProfile = user.getProviderProfiles().get(provider);

            final String accessToken = (String) authProfile.getCredentials().get("access_token");
            Response response =
                    client.target(requestString)
                            .request("text/plain")
                            .header("Authorization", String.format("Bearer %s", accessToken))
                            .get();

            return response;
        }
    }

    public static class TwitterAccountResource
        extends SocialAccountResource {
        public TwitterAccountResource(@PathParam("id") String userId, @PathParam("provider") String provider) {
            super(userId, provider);
        }

        @Override
        public Response signSocialRequest(String requestString) throws ParseException, InterruptedException, ExecutionException, IOException {

            AuthProfile authProfile = user.getProviderProfiles().get(provider);

            Map<String,String> twitterConf = getApplication().getConfig().authProviders.get("twitter");
            final OAuth10aService service = new ServiceBuilder()
                    .apiKey(twitterConf.get("client_id"))
                    .apiSecret(twitterConf.get("client_secret"))
                    .callback("http://localhost:9000")
                    .build(TwitterApi.instance());

            final OAuth1AccessToken accessToken = new OAuth1AccessToken(
                    (String)authProfile.getCredentials().get("oauth_token"),
                    (String)authProfile.getCredentials().get("oauth_token_secret"));

            final OAuthRequest signedRequest = new OAuthRequest(Verb.GET, requestString);
            service.signRequest(accessToken, signedRequest);
            final com.github.scribejava.core.model.Response response = service.execute(signedRequest);

            Response.ResponseBuilder builder = Response
                    .status(response.getCode())
                    .entity(response.getStream());

            response.getHeaders().forEach( (k,v) -> builder.header(k,v));

            return builder.build();
        }
    }

}
