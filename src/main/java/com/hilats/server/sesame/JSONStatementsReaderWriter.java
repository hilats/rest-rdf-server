package com.hilats.server.sesame;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.sesame.SesameRDFParser;
import com.github.jsonldjava.sesame.SesameTripleCallback;
import com.github.jsonldjava.utils.JsonUtils;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.util.RDFInserter;
import org.openrdf.rio.helpers.ContextStatementCollector;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by pduchesne on 1/08/14.
 */
public class JSONStatementsReaderWriter
    implements MessageBodyReader<Model>, MessageBodyWriter<Model> {

    MediaType JSONLD = MediaType.valueOf("application/ld+json");
    List<MediaType> supportedTypes = Arrays.asList(MediaType.APPLICATION_JSON_TYPE, JSONLD);

    public static Map PARSE_CONTEXT;
    public static Map JSONLD_FRAME;


    public JSONStatementsReaderWriter(InputStream context, InputStream frame) throws IOException {
        PARSE_CONTEXT = (Map)JsonUtils.fromInputStream(context);
        JSONLD_FRAME = (Map)JsonUtils.fromInputStream(frame);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type != null
            && Model.class.isAssignableFrom(type))
            for (MediaType st : supportedTypes) if (st.isCompatible(mediaType)) return true;

        return false;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        try {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {

                ContextStatementCollector collector = new ContextStatementCollector(null);
                final SesameTripleCallback callback = new SesameTripleCallback(collector);
                final JsonLdOptions options = new JsonLdOptions("http://localhost/test");
                options.useNamespaces = true;
                Object jsonObj = JsonUtils.fromInputStream(entityStream);
                Map jsonld = new HashMap();
                jsonld.put("@graph", jsonObj);
                jsonld.put("@context", JSONLD_FRAME.get("@context"));
                Object flattened = JsonLdProcessor.flatten(jsonld, options);
                JsonLdProcessor.toRDF(flattened, callback, options);

                return new LinkedHashModel(collector.getStatements());
            } else if (JSONLD.isCompatible(mediaType)) {
                ContextStatementCollector collector = new ContextStatementCollector(null);
                final SesameTripleCallback callback = new SesameTripleCallback(collector);
                final JsonLdOptions options = new JsonLdOptions("http://localhost/test");
                options.useNamespaces = true;
                Object jsonObj = JsonUtils.fromInputStream(entityStream);
                JsonLdProcessor.toRDF(jsonObj, callback, options);

                return new LinkedHashModel(collector.getStatements());
            } else
                throw new WebApplicationException("Unsupported media type: "+mediaType);
        } catch (JsonLdError jsonLdError) {
            throw new WebApplicationException("Failed to parse JSON entity into RDF statements", jsonLdError);
        }
    }


    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public long getSize(Model statements, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Model model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            Object statements = JsonLdProcessor.fromRDF(model, new SesameRDFParser());
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                Map output = JsonLdProcessor.frame(statements, JSONLD_FRAME, new JsonLdOptions());
                JsonUtils.writePrettyPrint(new OutputStreamWriter(entityStream), output.get("@graph"));
            } else if (JSONLD.equals(mediaType)) {
                Map output = JsonLdProcessor.compact(statements, PARSE_CONTEXT, new JsonLdOptions());
                JsonUtils.writePrettyPrint(new OutputStreamWriter(entityStream), output);
            } else
                throw new WebApplicationException("Unsupported media type: "+mediaType);

        } catch (JsonLdError jsonLdError) {
            throw new WebApplicationException("Failed to write RDF statements into JSON entity", jsonLdError);
        }
    }
}
