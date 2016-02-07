package com.hilats.server.spring;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;

/**
 * Created by pduchesne on 5/02/16.
 */
public class SpringServletJerseyContainer
    extends ServletContainer
{


    @Override
    protected void init(WebConfig webConfig) throws ServletException {
        super.init(webConfig);
        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(webConfig.getServletContext());
        ResourceConfig app = ctx.getBean(ResourceConfig.class);

        reload(app);
    }

}
