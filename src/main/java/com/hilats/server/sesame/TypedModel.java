package com.hilats.server.sesame;

import org.openrdf.model.Model;

/**
 * @author pduchesne
 *         Created by pduchesne on 27/11/16.
 */
public class TypedModel<T> {

    private Model model;

    public TypedModel(Model model) {
        this.model = model;
    }

    public Model getModel() {
        return model;
    }
}
