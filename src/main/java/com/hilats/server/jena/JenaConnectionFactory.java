package com.hilats.server.jena;

import com.hilats.server.RepoConnection;
import com.hilats.server.RepoConnectionFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Created by pduchesne on 29/06/14.
 */
public class JenaConnectionFactory
    implements RepoConnectionFactory
{
    Dataset ds;
    Model currentModel;

    public JenaConnectionFactory(Dataset ds) {
        this.ds = ds;
    }

    public Model getCurrentModel() {
        return currentModel;
    }

    @Override
    public RepoConnection getCurrentConnection() {
        try {
            if (!isConnectionActive()) {
                ds.begin(ReadWrite.WRITE);
                currentModel = ds.getDefaultModel();
            }
            return new RepoConnection(currentModel);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open connection", e);
        }
    }

    @Override
    public void closeCurrentConnection() {
        try {
            if (isConnectionActive()) {
                ds.commit();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close connection", e);
        } finally {
            currentModel = null;
            ds.end();
        }
    }

    @Override
    public boolean isConnectionActive() {
        try {
            return ds.isInTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Failed connection", e);
        }
    }
}
