package com.hilats.server;

import com.hilats.server.jena.JenaRdfApplication;
import com.hilats.server.sesame.SesameRdfApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.SpringComponentProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/myapp/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {

        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml", "jersey-spring-applicationContext.xml");

        //final JenaRdfApplication app = new JenaRdfApplication();
        //final SesameRdfApplication app = new SesameRdfApplication();

        ResourceConfig app = ctx.getBean(ResourceConfig.class);

        Map props = new HashMap<String, Object>();
        props.put(ServerProperties.MEDIA_TYPE_MAPPINGS, "txt : text/plain, xml : application/xml, json : application/json, jsonld : application/ld+json, ttl : text/turtle");
        app.addProperties(props);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), app);


        /*
        WebappContext webappContext = new WebappContext("grizzly web context", "");

        FilterRegistration testFilterReg = webappContext.addFilter("ExtensionFilter", TestFilter.class);
        testFilterReg.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), "/*");

        ServletRegistration servletRegistration = webappContext.addServlet("Jersey", org.glassfish.jersey.servlet.ServletContainer.class);
        servletRegistration.addMapping("/myapp/*");
        servletRegistration.setInitParameter("jersey.config.server.provider.packages", "com.example");


        HttpServer server = HttpServer.createSimpleServer();
        webappContext.deploy(server);
        */

        return server;

    }

    /**
     * Main method.
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.stop();
    }
}

