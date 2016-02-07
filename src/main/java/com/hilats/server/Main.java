package com.hilats.server;

import com.hilats.server.spring.MosaicContextLoaderListener;
import com.hilats.server.spring.SpringServletJerseyContainer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.http.KeepAliveProbe;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.servlet.ServletHandler;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.mitre.dsmiley.httpproxy.URITemplateProxyServlet;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080";


    public static HttpServer startServer(URI uri) throws IOException {

        return startServer(uri, null);
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(URI uri, String[] contextFiles) throws IOException {


        HttpServer server = HttpServer.createSimpleServer(".", uri.getHost(), uri.getPort());

        /* Add Mosaics REST api */

        WebappContext mosaicsApiCtx = new WebappContext("Mosaics");

        if (contextFiles == null) contextFiles = new String[] {"applicationContext.xml", "jersey-spring-applicationContext.xml"};
        mosaicsApiCtx.addContextInitParameter("contextConfigLocation", StringUtils.join(contextFiles,","));
        /*
        mosaicsApiCtx.addContextInitParameter("contextClass", "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        */
        mosaicsApiCtx.addListener(MosaicContextLoaderListener.class);


        ServletRegistration servletRegistration = mosaicsApiCtx.addServlet("Jersey", new SpringServletJerseyContainer());
        //servletRegistration.setInitParameter("javax.ws.rs.Application", "resource config class goes here");
        servletRegistration.addMapping("/api/*");

        mosaicsApiCtx.addFilter("cordFilter", new Filter() {
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
        FilterRegistration springSecurityFilter = mosaicsApiCtx.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class);
        springSecurityFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");

        mosaicsApiCtx.deploy(server);


        /* Add proxy servlet */
        WebappContext proxyCtx = new WebappContext("Proxy");

        ServletRegistration reg = proxyCtx.addServlet("ProxyServlet", new URITemplateProxyServlet());
        reg.setInitParameter("targetUri", "{_uri}");
        reg.addMapping("/proxy");

        proxyCtx.deploy(server);



        // Static content handler
        server.getServerConfiguration().addHttpHandler(
                new CLStaticHttpHandler(Main.class.getClassLoader(), "/web/"), "/");

        server.start();



        return server;

    }

    /**
     * Main method.
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //System.setProperty("java.util.logging.config.file","logging.properties");

        //SLF4JBridgeHandler.removeHandlersForRootLogger();
        //SLF4JBridgeHandler.install();

        Logger.getLogger("org.glassfish.jersey.server.ServerRuntime$Responder").setLevel(Level.FINER);

        String port = System.getenv().get("PORT");
        if (port == null || port.length()==0) port = "8080";

        String host = System.getenv().get("HOST");
        if (host == null || host.length()==0) host = "localhost";

        final HttpServer server = startServer(URI.create("http://"+host+":"+port+"/api"));

        synchronized (Main.class) { Main.class.wait(); }
        server.stop();
    }
}

