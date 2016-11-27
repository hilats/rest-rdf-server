package com.hilats.server.sesame;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Created by pduchesne on 1/08/14.
 */
public class TurtleStatementsReaderWriter
    implements MessageBodyReader<TypedModel>, MessageBodyWriter<TypedModel> {

    MediaType TURTLE = MediaType.valueOf("text/turtle");

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type != null
            && TypedModel.class.isAssignableFrom(type)
            && TURTLE.equals(mediaType);
    }

    @Override
    public TypedModel readFrom(Class<TypedModel> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

        try {
            return new TypedModel(Rio.parse(entityStream, "http://localhost/test", RDFFormat.TURTLE));
        } catch (Exception e) {
            throw new WebApplicationException("Failed to parse Turtle", e);
        }
    }


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public long getSize(TypedModel statements, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(TypedModel typedModel, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            Rio.write(typedModel.getModel(), entityStream, RDFFormat.TURTLE);
        } catch (RDFHandlerException e) {
            throw new WebApplicationException("Failed to write Turtle", e);
        }
    }
}
