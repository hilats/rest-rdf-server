package com.hilats.server.jena;

import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import com.hilats.server.TripleStore;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by pduchesne on 24/04/14.
 */
public class JenaTripleStore
    implements TripleStore
{
    Model model;

    public JenaTripleStore() {

        model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("/annotations/example1.ttl"), null, "TURTLE");

        //JenaJSONLD.init(); //TODO
    }

    public Model getModel() {
        return model;
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addStatements(Collection statements) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public StreamingOutput getStatementsStreamer(String sparql, final String mimeType, Map config) {
        final Model result = getStatements(sparql);

        return new StreamingOutput() {
            public void write(OutputStream output) {
                result.write(output, RDFLanguages.contentTypeToLang(mimeType).getName());
            }
        };
    }

    @Override
    public Model getStatements(String sparql) {
        return sparql != null ?
                getTupleSet(sparql) :
                model;
    }

    public Model getTupleSet(String queryString) {

        Query query = QueryFactory.create(queryString) ;
        QueryExecution qe = QueryExecutionFactory.create(query, model) ;

        return qe.execConstruct();
    }

    @Override
    public RepoConnectionFactory getRepoConnectionFactory() {
        //TODO
        return null;
    }
}
