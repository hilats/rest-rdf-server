package com.hilats.server;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pduchesne
 *         Created by pduchesne on 23/10/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationConfig {

    public int publicProxyPort = -1;
    public int proxyPort = -1;

    public int securePort = -1;
    public int secureProxyPort = -1;

    public String keystorePath = null;
    public String keystorePass = null;

    public String proxyAllowedOrigin = null;

    public Map<String, Map<String, String>> authProviders = new HashMap<String, Map<String, String>>();

    public Map<String, Object> webappConfig = new HashMap();

    public String admin;

}
