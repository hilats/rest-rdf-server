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

    @Override
    public Iterable<HilatsUser> getUsers() {
        return template.findAll(HilatsUser.class);
    }

    public final HilatsUser findUser(String username) {
        Query searchUserQuery = new Query(Criteria.where("username").is(username));

        // find the saved user again.
        HilatsUser user = template.findOne(searchUserQuery, HilatsUser.class);
        return user;
    }

    public final HilatsUser findUserByEmail(String email) {
        Query searchUserQuery = new Query(Criteria.where("email").is(email));

        // find the saved user again.
        HilatsUser user = template.findOne(searchUserQuery, HilatsUser.class);
        return user;
    }

    @Override
    public void addUser(HilatsUser user) {
        Query searchUserQuery = new Query(Criteria.where("username").is(user.getUsername()));
        if (template.exists(searchUserQuery, HilatsUser.class))
            throw new IllegalArgumentException("User already exists : "+user.getUsername());

        saveUser(user);
    }

    @Override
    public void saveUser(HilatsUser user) {
        //TODO check existing ?
        template.save(user);
    }
}
