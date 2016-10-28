package com.hilats.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilats.server.sesame.JSONStatementsReaderWriter;
import com.hilats.server.spring.jwt.HilatsUserService;
import com.hilats.server.spring.jwt.TokenAuthenticationService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.openrdf.model.Model;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pduchesne on 2/05/14.
 */
public class RdfApplication
    extends ResourceConfig
    implements ApplicationContextAware, InitializingBean
{
    TripleStore store;

    @Autowired
    ServerHomeDir homedir;

    ApplicationConfig config;

    @Autowired
    RepoConnectionFactory connFactory;

    @Autowired
    TokenAuthenticationService tokenService;

    @Autowired
    HilatsUserService userService;

    protected Resource initData;
    protected String initMimeType;

    protected RdfApplication(TripleStore store, Object... components) {
        this.store = store;

        Map props = new HashMap<String, Object>();
        props.put(ServerProperties.MEDIA_TYPE_MAPPINGS, "rdf: application/rdf+xml, txt : text/plain, xml : application/xml, json : application/json, jsonld : application/ld+json, ttl : text/turtle");
        this.addProperties(props);

        for (Object component: components) register(component);
        this.packages(RdfApplication.class.getPackage().getName()+".rest.resources");
        this.register(ExceptionHandler.class);
        register(JacksonFeature.class);
        register(CORSResponseFilter.class);
        register(RolesAllowedDynamicFeature.class);
        //this.register(DBConnectionFilter.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(RepoConnectionHk2Factory.class)
                        .to(RepoConnection.class)
                        .in(RequestScoped.class); // necessary to have the .dispose() method called
            }
        });
    }

    protected RdfApplication(TripleStore store, Resource initData, String mimeType, Object... components) throws FileNotFoundException {
        this (store, components);

        this.initData = initData;
        this.initMimeType = mimeType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        File configFile = new File(homedir.getRootDir(), "server.json");
        if (configFile.exists())
            config = mapper.readValue(configFile, ApplicationConfig.class);
        else
            mapper.writeValue(configFile, config = new ApplicationConfig());

        initData();
    }

    public void initData() throws Exception {
        if (initData != null) {
            RepoConnection conn = connFactory.getCurrentConnection();
            try {
                store.addStatements(initData.getInputStream(), initMimeType);
            } finally {
                connFactory.closeCurrentConnection();
            }
        }
    }

    protected <C> List<? extends C> findRegisteredComponents(Class<C> c) {
        List result = new ArrayList();
        for (Object o: getInstances())
            if (c.isAssignableFrom(o.getClass()))
                result.add(o);

        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        property("contextConfig", applicationContext); // this is the value of SpringComponentProvider.PARAM_SPRING_CONTEXT , but this is private

        //TODO why? these should be autowired
        connFactory = applicationContext.getBean(RepoConnectionFactory.class);

        tokenService = applicationContext.getBean(TokenAuthenticationService.class);

        userService = applicationContext.getBean(HilatsUserService.class);

        homedir = applicationContext.getBean(ServerHomeDir.class);
    }

    public TripleStore getStore() {
        return store;
    }

    public TokenAuthenticationService getTokenService() {
        return tokenService;
    }

    public HilatsUserService getUserService() {
        return userService;
    }

    protected RepoConnectionFactory getConnFactory() {
        return connFactory;
    }

    public ServerHomeDir getHomedir() {
        return homedir;
    }

    public ApplicationConfig getConfig() {
        return config;
    }
}
