package com.hilats.social;

import com.hilats.server.spring.jwt.services.AuthProfile;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author pduchesne
 *         Created by pduchesne on 24/11/17.
 */
public class GoogleClient {

    final Client client = new JerseyClientBuilder().register(JacksonFeature.class).build();

    public static final String CLIENT_ID_KEY = "client_id",
            REDIRECT_URI_KEY = "redirect_uri",
            CLIENT_SECRET = "client_secret",
            CODE_KEY = "code",
            GRANT_TYPE_KEY = "grant_type",
            AUTH_CODE = "authorization_code",
            REFRESH_TOKEN = "refresh_token";

    final String ACCESS_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";
    final String PEOPLE_API_URL   = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";

    private String clientId;
    private String clientSecret;

    private Map<String, Object> credentials;
    private Consumer<Map<String, Object>> credentialsProcessor;

    public GoogleClient(String clientId,
                        String clientSecret,
                        Map<String, Object> credentials,
                        Consumer<Map<String, Object>> credentialsProcessor) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.credentials = credentials;
        this.credentialsProcessor = credentialsProcessor;
    }

    public Map<String, Object> authorize(String redirectUri, String authorizationCode) {
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<String, String>();
        accessData.add(CLIENT_ID_KEY, clientId);
        accessData.add(REDIRECT_URI_KEY, redirectUri);
        accessData.add(CLIENT_SECRET, clientSecret);
        accessData.add(CODE_KEY, authorizationCode);
        accessData.add(GRANT_TYPE_KEY, AUTH_CODE);
        Response response = client.target(ACCESS_TOKEN_URL).request().post(Entity.form(accessData));

        credentials = response.readEntity(Map.class);
        credentials.put(
                "expiration_date",
                System.currentTimeMillis() + ((Number)credentials.get("expires_in")).longValue() * 1000);


        if (credentialsProcessor != null)
            credentialsProcessor.accept(credentials);

        return credentials;
    }



    public Map<String, Object> refreshCredentials() {
        final MultivaluedMap<String, String> accessData = new MultivaluedHashMap<String, String>();
        accessData.add(CLIENT_ID_KEY, clientId);
        accessData.add(CLIENT_SECRET, clientSecret);
        accessData.add(GRANT_TYPE_KEY, REFRESH_TOKEN);
        accessData.add(REFRESH_TOKEN, (String)credentials.get("refresh_token"));

        Response response = client.target(ACCESS_TOKEN_URL).request().post(Entity.form(accessData));

        Map responseMap = response.readEntity(Map.class);
        credentials.putAll(responseMap);
        credentials.put(
                "expiration_date",
                System.currentTimeMillis() + (int)credentials.get("expires_in") * 1000);

        if (credentialsProcessor != null)
            credentialsProcessor.accept(credentials);

        return credentials;
    }

    public Response signSocialRequest(String requestString) {

        if (!isAccessTokenValid())
            refreshCredentials();

        final String accessToken = (String) credentials.get("access_token");
        Response response =
                client.target(requestString)
                        .request("text/plain")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .get();

        return response;
    }

    public boolean isAccessTokenValid() {
        return (long)credentials.getOrDefault("expiration_date", 0l) > System.currentTimeMillis() + 5000; // add 5s buffer
    }
}
