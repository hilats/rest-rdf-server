package com.hilats.server.rest.resources;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.module.SimpleModule;

import javax.ws.rs.ext.ContextResolver;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pduchesne
 *         Created by pduchesne on 07/04/17.
 */
public class JacksonCustomMapperProvider implements ContextResolver<ObjectMapper> {

    final ObjectMapper objectMapper;

    public JacksonCustomMapperProvider() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new HilatsSerializerModule());
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}

class HilatsSerializerModule extends SimpleModule {

    public HilatsSerializerModule() {
        super("HilatsSerializerModule", new Version(0, 1, 0, "alpha"));
        this.addSerializer(Throwable.class, new ThrowableSerializer());
    }

    public class ThrowableSerializer extends JsonSerializer<Throwable> {


        @Override
        public void serialize(Throwable value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonProcessingException {

            Map throwableMap = getThrowableMap(value);
            jgen.writeObject(throwableMap);
        }

        public Map getThrowableMap(Throwable t) {
            Map error = new HashMap();

            error.put("message", t.getMessage());
            error.put("class", t.getClass().toString());
            if (t.getCause() != null)
                error.put("cause", getThrowableMap(t.getCause()));

            return error;
        }
    }
}
