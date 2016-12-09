package com.hilats.server;

import org.apache.http.client.HttpClient;
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
        return super.createHttpClient(hcParams);
    }
}
