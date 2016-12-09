package com.hilats.server;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;

/**
 * @author pduchesne
 *         Created by pduchesne on 09/12/16.
 */
public class HilatsProxyServlet
    extends URITemplateProxyServlet
{
    public HilatsProxyServlet() {
    }

    @Override
    protected HttpClient createHttpClient(HttpParams hcParams) {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(Integer.parseInt(getConfigParam("httpClient.socketTimeout")))
                .setConnectTimeout(Integer.parseInt(getConfigParam("httpClient.connectionTimeout")))
                .setConnectionRequestTimeout(Integer.parseInt(getConfigParam("httpClient.connectionRequestTimeout")))
                .build();

        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        return httpclient;
    }
}
