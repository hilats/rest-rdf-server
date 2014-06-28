package com.hilats.server;

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
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        property("contextConfig", applicationContext); // this is the value of SpringComponentProvider.PARAM_SPRING_CONTEXT , but this is private
    }

    public abstract void addStatements(InputStream in, String mimeType);

    public abstract StreamingOutput getStatements(String sparql, String mimeType);
}
