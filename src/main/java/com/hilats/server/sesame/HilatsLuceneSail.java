package com.hilats.server.sesame;

import org.openrdf.sail.Sail;
import org.openrdf.sail.lucene.LuceneSail;

import java.io.File;

/**
 * @author pduchesne
 *         Created by pduchesne on 08/04/17.
 */
public class HilatsLuceneSail
        extends LuceneSail
{
    public HilatsLuceneSail(Sail baseSail, File dataDir) {
        super();

        setBaseSail(baseSail);
        setDataDir(dataDir);
    }

    public void setIndexClass(String indexClassName) {
        setParameter(INDEX_CLASS_KEY, indexClassName);
        setParameter(INDEXEDFIELDS,
                "index.1=http://purl.org/dc/elements/1.1/title\n"
              + "index.2=http://www.opengis.net/ont/geosparql#asWKT"
        );
    }


}
