package com.hilats.server.sesame;

import org.openrdf.model.*;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by pduchesne on 30/07/14.
 */
public class Skolemizer extends RDFHandlerWrapper
{
    private String defaultNamespace;

    public Skolemizer(String defaultNamespace, RDFHandler... rdfHandlers) {
        super(rdfHandlers);
        this.defaultNamespace = defaultNamespace;
    }

    /**
     * mapping between bnode (local) identifiers and assigned
     (globally unique) URI.
     */
    private HashMap<String, URI> mapping = new HashMap<String, URI>();

    public HashMap<String, URI> getMapping() {
        return mapping;
    }

    public Model process(Model model) {
        Model newModel = new LinkedHashModel();
        for (Statement s: model) {
            Statement updated = processStatement(s);
            if (updated != null) newModel.add(updated);
            else newModel.add(s);
        }
        return newModel;
    }

    public Statement processStatement(Statement st) {
        final ValueFactory f = ValueFactoryImpl.getInstance();

        URI skolemSubject = null;
        URI skolemObject = null;
        Statement converted = null;
        if (st.getSubject() instanceof BNode) {
            String bnodeId = ((BNode)st.getSubject()).getID();
            if (mapping.containsKey(bnodeId)) {
                skolemSubject = mapping.get(bnodeId);
            } else {
                String id = UUID.randomUUID().toString();
                skolemSubject = f.createURI(defaultNamespace + id);
                mapping.put(bnodeId, skolemSubject);
            }
        }
        if (st.getObject() instanceof BNode) {
            String bnodeId = ((BNode)st.getObject()).getID();
            if (mapping.containsKey(bnodeId)) {
                skolemObject = mapping.get(bnodeId);
            } else {
                String id = UUID.randomUUID().toString();
                skolemObject = f.createURI(defaultNamespace + id);
                mapping.put(bnodeId, skolemObject);
            }
        }



        if (skolemSubject != null || skolemObject != null) {
            Resource newSubject = skolemSubject == null ? st.getSubject() : skolemSubject;
            Value newObject = skolemObject == null ? st.getObject() : skolemObject;

            converted = f.createStatement(newSubject,st.getPredicate(), newObject);
        }

        return converted;
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        Statement converted = processStatement(st);

        if (converted != null) {
            super.handleStatement(converted);
        }
        else {
            super.handleStatement(st);
        }
    }
}
