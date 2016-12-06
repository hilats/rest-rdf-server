package com.hilats.server;

import com.hilats.server.spring.MosaicContextLoaderListener;
import com.hilats.server.spring.SpringServletJerseyContainer;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.server.*;
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
import java.util.logging.Level;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080";

    public static Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * Main method.
     * @param args
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        //System.setProperty("java.util.logging.config.file","logging.properties");

        // Mongo logging - redirect JDK logging to log4j - this must be done prior to any potential invocation of JDK logging
        //System.getProperties().put("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

        //SLF4JBridgeHandler.removeHandlersForRootLogger();
        //SLF4JBridgeHandler.install();

        java.util.logging.Logger.getLogger("org.glassfish.jersey.server.ServerRuntime$Responder").setLevel(Level.FINER);

        String baseUrl;
        if (System.getProperties().containsKey("baseurl")) {
            baseUrl = System.getProperties().getProperty("baseurl");
        } else {
            // needed for heroku

            String port = System.getenv().get("PORT");
            if (port == null || port.length()==0) port = "8080";

            String host = System.getenv().get("HOST");
            if (host == null || host.length()==0) host = "localhost";

            baseUrl = "http://"+host+":"+port+"/api";
        }

        RestRDFServer serverInstance = startServer(URI.create(baseUrl));

        // register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("Stopping server..");
            serverInstance.stop();
        }, "shutdownHook"));

        Thread.currentThread().join();
    }

    public static RestRDFServer startServer(URI uri) throws IOException, URISyntaxException {
        RestRDFServer serverInstance = new RestRDFServer(uri);

        serverInstance.startServer();

        serverInstance.startProxyServer();

        return serverInstance;
    }
}

