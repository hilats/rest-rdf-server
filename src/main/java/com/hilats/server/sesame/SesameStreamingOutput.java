package com.hilats.server.sesame;

import com.github.jsonldjava.sesame.SesameJSONLDSettings;
import com.github.jsonldjava.sesame.SesameJSONLDWriter;
import org.openrdf.model.Statement;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResults;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.JSONLDMode;
import org.openrdf.rio.helpers.JSONLDSettings;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by pduchesne on 2/05/14.
 */
public class SesameStreamingOutput
{

    public static StreamingOutput createStreamer(final Iterable<Statement> model, final RDFFormat format) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {

                    /*
                    RDFWriter rdfWriter = Rio.createWriter(RDFFormat.JSONLD, output); // new SesameJSONLDWriterFactory().getWriter(output);
                    rdfWriter.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, JSONLDMode.EXPAND);


                    rdfWriter.startRDF();
                    rdfWriter.handleStatement(st1);
                    rdfWriter.endRDF();
                    */

                    Rio.write(model, output, format);
                } catch (RDFHandlerException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    public static StreamingOutput createStreamer(final GraphQueryResult graphResult, final RDFFormat format, final Map config) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) {
                try {

                    RDFWriter rdfWriter = Rio.createWriter(format, output);
                    rdfWriter.getWriterConfig().set(JSONLDSettings.JSONLD_MODE, config != null && config.containsKey(JSONLDSettings.JSONLD_MODE) ? (JSONLDMode)config.get(JSONLDSettings.JSONLD_MODE) : JSONLDMode.COMPACT);
                    rdfWriter.getWriterConfig().set(SesameJSONLDSettings.CONTEXT, config != null && config.containsKey(SesameJSONLDSettings.CONTEXT) ? (Map)config.get(SesameJSONLDSettings.CONTEXT) : null);
                    rdfWriter.getWriterConfig().set(SesameJSONLDSettings.FRAME, config != null && config.containsKey(SesameJSONLDSettings.FRAME) ? (Map)config.get(SesameJSONLDSettings.FRAME) : null);

                    QueryResults.report(graphResult, rdfWriter);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

}
