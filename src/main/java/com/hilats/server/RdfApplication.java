package com.hilats.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by pduchesne on 2/05/14.
 */
public class RdfApplication
    extends ResourceConfig
    implements ApplicationContextAware
{
    TripleStore store;

    protected RdfApplication(TripleStore store, Object... components) {
        this.store = store;

        for (Object component: components) register(component);
        this.packages(RdfApplication.class.getPackage().getName()+".rest.resources");
        this.register(ExceptionHandler.class);
        register(JacksonFeature.class);
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

    protected RdfApplication(TripleStore store, File initData, String mimeType, Object... components) throws FileNotFoundException {
        this (store, components);
        store.addStatements(new FileInputStream(initData), mimeType);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        property("contextConfig", applicationContext); // this is the value of SpringComponentProvider.PARAM_SPRING_CONTEXT , but this is private
    }

    public TripleStore getStore() {
        return store;
    }
}
