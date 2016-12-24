package com.hilats.server;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /** Copy proxied response headers back to the servlet client. */
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (servletResponse.getHeader(header.getName()) == null)
                // copy header only if not already set
                // this can be a problem f.i. for CKAN that sets Allow-Origin header, causing duplicate values
                copyResponseHeader(servletRequest, servletResponse, header);
        }
    }
}
