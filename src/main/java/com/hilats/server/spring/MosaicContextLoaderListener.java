package com.hilats.server.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * Created by pduchesne on 5/02/16.
 */
public class MosaicContextLoaderListener
    extends ContextLoaderListener
{
    @Override
    protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
        String[] contextFiles = sc.getInitParameter("contextConfigLocation").split(",");

        ApplicationContext xmlCtx = new ClassPathXmlApplicationContext(contextFiles);
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.setParent(xmlCtx);
        ctx.register(SpringSecurityConfig.class);
        ctx.refresh();

        return ctx;
    }
}
