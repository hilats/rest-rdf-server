package com.hilats.server;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @Override
    protected void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest, String headerName) {
        // ignore referer header ; some remote services choke on it
        if (headerName.equals("referer") || headerName.equals("origin"))
            return;

        super.copyRequestHeader(servletRequest, proxyRequest, headerName);
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
        super.service(servletRequest, servletResponse);

        if (servletResponse.getStatus() == 400 && "OPTIONS".equals(servletRequest.getMethod())) {
            // let's assume this is a preflight request and the proxied server can't handle it - vouch for him
            servletResponse.setStatus(200);
        }
    }
}
