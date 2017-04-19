package org.openrdf.rio.jsonld;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import com.hilats.server.rest.resources.Unique;
import com.hilats.server.sesame.TypedModel;
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by pduchesne on 1/08/14.
 */
public class JSONStatementsReaderWriter
    implements MessageBodyReader<TypedModel>, MessageBodyWriter<TypedModel> {

    MediaType JSONLD = MediaType.valueOf("application/ld+json");
    List<MediaType> supportedTypes = Arrays.asList(MediaType.APPLICATION_JSON_TYPE, JSONLD);

    public static Map PARSE_CONTEXT;
    public static Map<String, Map> JSONLD_FRAMES = new HashMap();

    private JsonLdOptions jsonldOptions = new JsonLdOptions();

    public JSONStatementsReaderWriter(InputStream context, Map<String, InputStream> frameResources) throws IOException {
        PARSE_CONTEXT = (Map)JsonUtils.fromInputStream(context, "UTF-8");

        for (Map.Entry<String, InputStream> frameDef: frameResources.entrySet())
            JSONLD_FRAMES.put(frameDef.getKey(), (Map)JsonUtils.fromInputStream(frameDef.getValue(), "UTF-8"));
    }

    public JsonLdOptions getJsonldOptions() {
        return jsonldOptions;
    }

    public void setJsonldOptions(JsonLdOptions jsonldOptions) {
        this.jsonldOptions = jsonldOptions;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (type != null
            && TypedModel.class.isAssignableFrom(type))
            for (MediaType st : supportedTypes) if (st.isCompatible(mediaType)) return true;

        return false;
    }

    @Override
    public TypedModel readFrom(Class<TypedModel> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        String typename =
                (genericType instanceof ParameterizedType) ?
                    ((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName():
                    genericType.getTypeName();

        try {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType)) {

                ContextStatementCollector collector = new ContextStatementCollector(null);
                final JSONLDInternalTripleCallback callback = new JSONLDInternalTripleCallback(collector);
                //final JsonLdOptions options = new JsonLdOptions("http://localhost/test");
                //options.useNamespaces = true;
                Object jsonObj = JsonUtils.fromInputStream(entityStream, "UTF-8");
                Map jsonld = new HashMap();
                jsonld.put("@graph", jsonObj);
                jsonld.put("@context", JSONLD_FRAMES.get(typename).get("@context"));
                Object flattened = JsonLdProcessor.flatten(jsonld, jsonldOptions);
                JsonLdProcessor.toRDF(flattened, callback, jsonldOptions);

                return new TypedModel(new LinkedHashModel(collector.getStatements()));
            } else if (JSONLD.isCompatible(mediaType)) {
                ContextStatementCollector collector = new ContextStatementCollector(null);
                final JSONLDInternalTripleCallback callback = new JSONLDInternalTripleCallback(collector);
                //final JsonLdOptions options = new JsonLdOptions("http://localhost/test");
                //options.useNamespaces = true;
                Object jsonObj = JsonUtils.fromInputStream(entityStream, "UTF-8");
                JsonLdProcessor.toRDF(jsonObj, callback, jsonldOptions);

                return new TypedModel(new LinkedHashModel(collector.getStatements()));
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
    public long getSize(TypedModel statements, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(TypedModel model, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        String typename =
                (genericType instanceof ParameterizedType) ?
                        ((ParameterizedType) genericType).getActualTypeArguments()[0].getTypeName():
                        genericType.getTypeName();

        try {
            Object statements = JsonLdProcessor.fromRDF(model.getModel(), new JSONLDInternalRDFParser());
            if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
                Map output = JsonLdProcessor.frame(statements, JSONLD_FRAMES.get(typename), jsonldOptions);

                List graph = (List)output.get("@graph");

                if (isUnique(annotations)) {
                    if (graph.size() > 1)
                        throw new IllegalStateException("Result should be unique, but graph contains multiple objects");

                    JsonUtils.writePrettyPrint(new OutputStreamWriter(entityStream, "UTF-8"), graph.get(0));
                }
                else
                    JsonUtils.writePrettyPrint(new OutputStreamWriter(entityStream, "UTF-8"), graph);
            } else if (JSONLD.equals(mediaType)) {
                Map output = JsonLdProcessor.compact(statements, PARSE_CONTEXT, jsonldOptions);
                JsonUtils.writePrettyPrint(new OutputStreamWriter(entityStream, "UTF-8"), output);
            } else
                throw new WebApplicationException("Unsupported media type: "+mediaType);

        } catch (JsonLdError jsonLdError) {
            throw new WebApplicationException("Failed to write RDF statements into JSON entity", jsonLdError);
        }
    }

    public boolean isUnique(Annotation[] annots) {
        for (Annotation a : annots)
            if (a instanceof Unique) return true;

        return false;
    }

}
