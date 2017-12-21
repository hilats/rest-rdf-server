package com.hilats.server;

import com.hilats.server.spring.MosaicContextLoaderListener;
import com.hilats.server.spring.SpringServletJerseyContainer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Level;

/**
 * Main class.
 *
 */
public class RestRDFServer {

    public static Logger log = LoggerFactory.getLogger(RestRDFServer.class);

    private URI serverURI;

    private HttpServer restServer;
    private HttpServer proxyServer;
    private ApplicationContext restSpringContext;

    public RestRDFServer(URI serverURI) {
        this.serverURI = serverURI;
    }

    public void startServers() throws IOException {
        this.restServer.start();
        if (this.proxyServer != null && this.proxyServer != this.restServer)
            this.proxyServer.start();
    }

    public void startServer() throws IOException {
        configureServer();
        this.restServer.start();
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public void startServer(URI uri, String[] contextFiles) throws IOException {
        this.configureServer(uri, contextFiles);
        this.restServer.start();
    }

    public void configureServer() throws IOException {
        this.configureServer(this.serverURI, null);
    }

    public void configureServer(URI uri, String[] contextFiles) throws IOException {


        HttpServer server = HttpServer.createSimpleServer(".", uri.getHost(), uri.getPort());

        /* Add REST api */

        WebappContext restApiCtx = new WebappContext("RestRDF");

        if (contextFiles == null) contextFiles = new String[] {"applicationContext.xml", "jersey-spring-applicationContext.xml"};
        restApiCtx.addContextInitParameter("contextConfigLocation", StringUtils.join(contextFiles, ","));
        /*
        restApiCtx.addContextInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        */
        restApiCtx.addListener(MosaicContextLoaderListener.class);


        ServletRegistration servletRegistration = restApiCtx.addServlet("Jersey", new SpringServletJerseyContainer());
        //servletRegistration.setInitParameter("javax.ws.rs.Application", "resource config class goes here");
        servletRegistration.addMapping("/api/*");

        restApiCtx.addFilter("cordFilter", new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

                String origin = ((HttpServletRequest) request).getHeader("Origin");

                if (origin == null || (origin.contains("highlatitud.es") || origin.contains("localhost"))) {
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Range, X-Requested-With");
                    ((HttpServletResponse) response).setHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
                }

                chain.doFilter(request, response);

                if (origin == null || (origin.contains("highlatitud.es") || origin.contains("localhost"))) {
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Range, X-Requested-With");
                    ((HttpServletResponse) response).setHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
                }

            }

            @Override
            public void destroy() {
            }
        })
        .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

        // Add Spring security filter chain
        FilterRegistration springSecurityFilter = restApiCtx.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
        springSecurityFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

        restApiCtx.deploy(server);

        this.restSpringContext = WebApplicationContextUtils.getWebApplicationContext(restApiCtx);
        this.restServer = server;

        // this is required to allow url-encoded slashes in IDs
        server.getHttpHandler().setAllowEncodedSlash(true);

    }

    public void configureStaticServer() {
        if (this.restSpringContext.containsBean("staticFolders")) {
            Map<String, String> staticFolders = this.restSpringContext.getBean("staticFolders", Map.class);
            staticFolders.forEach((key,value) -> {
                this.restServer.getServerConfiguration().addHttpHandler(
                        new StaticHttpHandler(value), key);
            });
        }

        // Static content handler
        this.restServer.getServerConfiguration().addHttpHandler(
                new CLStaticHttpHandler(RestRDFServer.class.getClassLoader(), "/web/"), "/");
    }


    public void configureProxyServer() throws IOException, URISyntaxException {
        ApplicationConfig config = this.restSpringContext.getBean(RdfApplication.class).getConfig();

        /* Add proxy servlet */
        WebappContext proxyCtx = new WebappContext("Proxy");

        FilterRegistration filterReg = proxyCtx.addFilter("cordFilter", new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


                String origin = ((HttpServletRequest) request).getHeader("Origin");

                if (origin == null || origin.contains("localhost")) {
                    // for test & debug purposes
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
                    //((HttpServletResponse) response).setHeader("X-Frame-Options", "ALLOW-FROM "+((HttpServletRequest) request).getHeader("referer"));
                    //((HttpServletResponse) response).setHeader("Content-Security-Policy", "frame-ancestors "+((HttpServletRequest) request).getHeader("referer"));
                }
                else if (config.proxyAllowedOrigin != null) {
                    ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", config.proxyAllowedOrigin);
                    //((HttpServletResponse) response).setHeader("X-Frame-Options", "ALLOW-FROM "+((HttpServletRequest) request).getHeader("referer"));
                    //((HttpServletResponse) response).setHeader("Content-Security-Policy", "frame-ancestors "+((HttpServletRequest) request).getHeader("referer"));
                }

                ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", ((HttpServletRequest) request).getHeader("Access-Control-Request-Headers"));
                ((HttpServletResponse) response).setHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");

                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {
            }
        });
        filterReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        ServletRegistration reg = proxyCtx.addServlet("ProxyServlet", new HilatsProxyServlet(true));
        reg.setInitParameter("targetUri", "{_uri}");
        // set agressive timeouts to prevent proxy threads jams
        reg.setInitParameter("httpClient.socketTimeout", "15000");
        reg.setInitParameter("httpClient.connectionTimeout", "4000");
        reg.setInitParameter("httpClient.connectionRequestTimeout", "1000");
        reg.addMapping("/proxy");


        // deploy to server

        int port = serverURI.getPort()+1;
        if (config.proxyPort != -1)
            port = config.proxyPort;
        else
            config.proxyPort = port;

        if (serverURI.getPort() != port)  {
            URI uri = new URI(serverURI.getScheme(), serverURI.getUserInfo(), serverURI.getHost(), port, serverURI.getPath(), serverURI.getQuery(), serverURI.getFragment());

            this.proxyServer = new HttpServer();
            final NetworkListener listener =
                    new NetworkListener("Proxy",uri.getHost(),uri.getPort());
            this.proxyServer.addListener(listener);
        } else {
            this.proxyServer = this.restServer;
        }

        proxyCtx.deploy(this.proxyServer);

        // this is required to allow url-encoded slashes in IDs
        this.proxyServer.getHttpHandler().setAllowEncodedSlash(true);

    }

    public void stop() {
        if (this.restServer != null)
            this.restServer.stop();
        if (this.proxyServer != null)
            this.proxyServer.stop();
    }
}

