package com.hilats.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    public Response toResponse(Throwable t) {

        if (t instanceof WebApplicationException) {
            Response originalResponse = ((WebApplicationException)t).getResponse();
            return Response
                    .status(originalResponse.getStatus())
                    .entity(t)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } else {
            log.error("REST: uncaught exception", t);
            return Response
                    .serverError()
                    .entity(t)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        }
    }
}