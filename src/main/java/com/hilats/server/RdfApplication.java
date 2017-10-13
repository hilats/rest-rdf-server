package com.hilats.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hilats.server.rest.resources.JacksonCustomMapperProvider;
import com.hilats.server.spring.jwt.HilatsUser;
import com.hilats.server.spring.jwt.HilatsUserService;
import com.hilats.server.spring.jwt.TokenAuthenticationService;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
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

    protected Resource[] initData;
    protected String initMimeType;

    protected boolean cleanOnInit = false;

    protected RdfApplication(TripleStore store, Object... components) {
        this.store = store;

        property(ServerProperties.MEDIA_TYPE_MAPPINGS, "rdf: application/rdf+xml, txt : text/plain, xml : application/xml, json : application/json, jsonld : application/ld+json, ttl : text/turtle");

        // make sure status>=400 is indeed processed by our exceptionMapper
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);

        for (Object component: components) register(component);
        this.packages(RdfApplication.class.getPackage().getName()+".rest.resources");
        this.register(ExceptionHandler.class);
        register(JacksonFeature.class);
        register(JacksonCustomMapperProvider.class);
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

        // Trigger initData only after servlet is loaded
        register(new ContainerLifecycleListener()
        {
            @Override
            public void onStartup(Container container)
            {
                try {
                    RdfApplication.this.initApplication();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onReload(Container container) {}

            @Override
            public void onShutdown(Container container) {}
        });
    }

    public void setCleanOnInit(boolean cleanOnInit) {
        this.cleanOnInit = cleanOnInit;
    }

    protected RdfApplication(TripleStore store, Resource[] initData, String mimeType, Object... components) throws FileNotFoundException {
        this (store, components);

        this.initData = initData;
        this.initMimeType = mimeType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);

        File configFile = new File(homedir.getRootDir(), "server.json");
        if (configFile.exists())
            config = mapper.readValue(configFile, ApplicationConfig.class);
        else
            mapper.writeValue(configFile, config = new ApplicationConfig());

    }

    public void initApplication() throws Exception {
        // check for users that should be admin
        if (getConfig().admin != null) {
            HilatsUser adminUser = getUserService().findUserByEmail(getConfig().admin);

            if (adminUser != null && !adminUser.getRoles().contains("admin")) {
                adminUser.getRoles().add("admin");

                getUserService().saveUser(adminUser);
            } else {
                // admin user not registered yet
            }
        }

        initData();
    }

    public void initData() throws Exception {
        if (cleanOnInit) {
            store.clean();
        }

        if (initData != null && store.isEmpty()) {
            try {
                for (Resource res : initData)
                    store.addStatements(res.getInputStream(), initMimeType);
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
