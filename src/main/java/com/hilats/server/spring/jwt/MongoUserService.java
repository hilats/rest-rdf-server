package com.hilats.server.spring.jwt;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;

import java.net.UnknownHostException;
import java.util.HashMap;

public class MongoUserService implements HilatsUserService {

    MongoClient client;
    MongoTemplate template;

    public MongoUserService(String uri) throws UnknownHostException {
        this (
                new MongoClient(new MongoClientURI(uri)),
                new MongoClientURI(uri).getDatabase()
        );
    }

    public MongoUserService(MongoClient client, String databaseName) {
        this.client = client;

        this.template = new MongoTemplate(client, databaseName);
    }

    public final HilatsUser findUser(String username) {
        Query searchUserQuery = new Query(Criteria.where("username").is(username));

        // find the saved user again.
        HilatsUser user = template.findOne(searchUserQuery, HilatsUser.class);
        return user;
    }

    @Override
    public void addUser(HilatsUser user) {
        template.save(user);
    }
}
