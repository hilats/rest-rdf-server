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

    public int proxyPort = -1;

    public Map<String, Map<String, String>> authProviders = new HashMap<String, Map<String, String>>();

}
