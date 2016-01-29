package com.hilats.server.sesame;

import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * Created by pduchesne on 22/01/16.
 */
public class AuthenticatedSparqlRepository
    extends SPARQLRepository
{
    public AuthenticatedSparqlRepository(String repositoryURL, String user, String password) {
        super(repositoryURL);

        setUsernameAndPassword(user, password);
    }
}
