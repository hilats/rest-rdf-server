package com.hilats.server.jena;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.hilats.server.ExceptionHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by pduchesne on 24/04/14.
 */
public class JenaRdfApplication
    extends ResourceConfig
{
    Model model;

    public JenaRdfApplication() {
        super();

        this.packages(this.getClass().getPackage().getName()+".resources");
        this.register(ExceptionHandler.class);

        model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream("/annotations/example1.turtle"), null, "TURTLE");

        JenaJSONLD.init();
    }

    public Model getModel() {
        return model;
    }
}
