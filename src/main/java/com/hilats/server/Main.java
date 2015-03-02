package com.hilats.server;

import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.http.KeepAliveProbe;
import org.glassfish.grizzly.http.server.AddOn;
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
    public static final String BASE_URI = "http://localhost:8080/api/";


    public static HttpServer startServer(URI uri) {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml", "jersey-spring-applicationContext.xml");

        return startServer(uri, ctx);
    }

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(URI uri, ApplicationContext ctx) {

        //final JenaRdfApplication app = new JenaRdfApplication();
        //final SesameRdfApplication app = new SesameRdfApplication();

        ResourceConfig app = ctx.getBean(ResourceConfig.class);

        Map props = new HashMap<String, Object>();
        props.put(ServerProperties.MEDIA_TYPE_MAPPINGS, "rdf: application/rdf+xml, txt : text/plain, xml : application/xml, json : application/json, jsonld : application/ld+json, ttl : text/turtle");
        app.addProperties(props);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, app);

        WebappContext waCtx = new WebappContext("Proxy");
        waCtx.addFilter("cordFilter", new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {}

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

                String origin = ((HttpServletRequest)request).getHeader("Origin");

                chain.doFilter(request, response);

                if (origin == null || (origin.contains("highlatitud.es") || origin.contains("localhost"))) {
                    ((HttpServletResponse)response).setHeader("Access-Control-Allow-Origin", "*");
                    ((HttpServletResponse)response).setHeader("Access-Control-Allow-Headers", "Range");
                    ((HttpServletResponse)response).setHeader("Access-Control-Expose-Headers", "Accept-Ranges, Content-Encoding, Content-Length, Content-Range");
                }
            }

            @Override
            public void destroy() {}
        })
        .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

        ServletRegistration reg = waCtx.addServlet("ProxyServlet", new URITemplateProxyServlet());
        reg.setInitParameter("targetUri", "{_uri}");
        reg.addMapping("/proxy");

        waCtx.deploy(server);


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

