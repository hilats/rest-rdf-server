package com.hilats.server.test;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.github.jsonldjava.jena.JenaRDFParser;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;

public class MosaicJsonldTest
{

    @Before
    public void init() {
        JenaJSONLD.init();
    }

    @Test
    public void test1() {
        Model model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResource("/mosaics/sample.turtle").toString(), null, "TURTLE");

        model.write(System.out, "JSON-LD");

    }

}
