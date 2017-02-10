package com.hilats.server.jena;

import com.hilats.server.AbstractTripleStore;
import com.hilats.server.RdfApplication;
import com.hilats.server.RepoConnectionFactory;
import com.hilats.server.TripleStore;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.springframework.core.io.Resource;

import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Created by pduchesne on 24/04/14.
 */
public class JenaTripleStore
    extends AbstractTripleStore
{
    Dataset ds;

    public static Dataset initDataset(Resource dataResource) throws IOException {
        return TDBFactory.createDataset(dataResource.getFile().getAbsolutePath());
    }

    public JenaTripleStore(Dataset ds, JenaConnectionFactory connFac) {
        super(connFac);
        this.ds = ds;
    }

    public Model getModel() {
        return getRepoConnectionFactory().getCurrentModel();
    }

    @Override
    public void addStatements(InputStream in, String mimeType) {
        RDFDataMgr.read(getModel(), in, RDFLanguages.nameToLang(mimeType));
    }

    @Override
    public StreamingOutput removeStatements(String sparql, String mimetype, Map config) {
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
                getModel();
    }

    public Model getTupleSet(String queryString) {

        Query query = QueryFactory.create(queryString) ;
        QueryExecution qe = QueryExecutionFactory.create(query, getModel()) ;

        return qe.execConstruct();
    }

    @Override
    public JenaConnectionFactory getRepoConnectionFactory() {
        return (JenaConnectionFactory)super.getRepoConnectionFactory();
    }


    public boolean isEmpty() {
        return getModel().isEmpty();
    }
}
