package com.hilats.server;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpParams;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author pduchesne
 *         Created by pduchesne on 09/12/16.
 */
public class HilatsProxyServlet
    extends URITemplateProxyServlet
{
    private boolean acceptUnverifiedCertificates = false;

    public HilatsProxyServlet() {
    }

    public HilatsProxyServlet(boolean acceptUnverifiedCertificates) {
        this.acceptUnverifiedCertificates = acceptUnverifiedCertificates;
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    @Override
    protected HttpClient createHttpClient(HttpParams hcParams) {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(Integer.parseInt(getConfigParam("httpClient.socketTimeout")))
                .setConnectTimeout(Integer.parseInt(getConfigParam("httpClient.connectionTimeout")))
                .setConnectionRequestTimeout(Integer.parseInt(getConfigParam("httpClient.connectionRequestTimeout")))
                .build();

        HttpClientBuilder builder = HttpClients.custom()
                   .setDefaultRequestConfig(defaultRequestConfig);

        if (this.acceptUnverifiedCertificates) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
                SSLContext.setDefault(ctx);

                builder.setSSLContext(ctx);

            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (KeyManagementException e) {
                throw new RuntimeException(e);
            }
        }

        return builder.build();
    }

    /** Copy proxied response headers back to the servlet client. */
    protected void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (servletResponse.getHeader(header.getName()) == null &&
                // remove X-Frame-Options
                // TODO should be configurable just like allowedProxyOrigin
                !"X-Frame-Options".equals(header.getName()))
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
