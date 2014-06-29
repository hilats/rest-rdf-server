package com.hilats.server;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

/**
 * Created by pduchesne on 2/05/14.
 */
public abstract class RdfApplication
    extends ResourceConfig
    implements ApplicationContextAware
{
    protected RdfApplication() {
        this.packages(RdfApplication.class.getPackage().getName()+".rest.resources");
        this.register(ExceptionHandler.class);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        property("contextConfig", applicationContext); // this is the value of SpringComponentProvider.PARAM_SPRING_CONTEXT , but this is private
    }

    public abstract void addStatements(InputStream in, String mimeType);

    public abstract StreamingOutput getStatements(String sparql, String mimeType);

    public abstract RepoConnectionFactory getRepoConnectionFactory();
}
